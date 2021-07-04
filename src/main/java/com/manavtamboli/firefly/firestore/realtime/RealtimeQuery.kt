package com.manavtamboli.firefly.firestore.realtime

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.manavtamboli.firefly.firestore.Transformer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlin.coroutines.cancellation.CancellationException

/**
 * Listen to realtime updates of this query.
 *
 * The returned cold flow will
 * - emit updates of [List] of [DocumentSnapshot].
 * - get cancelled if Firestore API result in a failure
 * - get cancelled if [callbackFlow] API cannot send result downstream.
 *
 * The list emitted in the flow is cumulative and not individual document changes.
 * To get only document changes, see [Query.realtimeChanges].
 *
 * Sample Usage :
 * ```
 * query.realtime()
 *      .onEach { snaps -> /* Success */ }
 *      .onCompletion { throwable ->
 *          if (throwable != null) {
 *              val ex = throwable.cause
 *              // Firebase or Flow exception
 *          }
 *      }.launchIn(coroutineScope)
 * ```
 *
 * @return A cold flow which emits realtime updates of the query.
 * */
@ExperimentalCoroutinesApi
fun Query.realtime() = callbackFlow<List<DocumentSnapshot>> {
    val registration = addSnapshotListener { snap, ex ->
        when {
            snap != null -> trySendBlocking(snap.documents).onFailure { cancel(CancellationException("Channel Exception", it)) }
            ex != null -> cancel(CancellationException("Firestore Exception", ex))
        }
    }

    awaitClose { registration.remove() }
}

/**
 * Listen to realtime updates of this query, and applies the given [transformer] on each document.
 *
 * The returned cold flow will
 * - emit updates of [List] of [T].
 * - get cancelled if Firestore API result in a failure
 * - get cancelled if [callbackFlow] API cannot send result downstream.
 * - throws any Exception that occurred in the [transformer]
 *
 * Sample Usage :
 * ```
 * query.realtime()
 *      .onEach { snaps -> /* Success */ }
 *      .catch { throwable -> /* Transformer Exception */ }
 *      .onCompletion { throwable ->
 *          if (throwable != null) {
 *              val ex = throwable.cause
 *              // Firebase or Flow exception
 *          }
 *      }.launchIn(coroutineScope)
 * ```
 *
 * @param transformer The transformer to apply on document snapshots.
 *
 * @return A cold flow which emits realtime updates of the query mapped to [T].
 * */
@ExperimentalCoroutinesApi
fun <T> Query.realtime(transformer: Transformer<T>) = realtime().map { items -> items.map(transformer::transform) }