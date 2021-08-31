package com.manavtamboli.firefly.firestore.realtime

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
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
 * Listen to realtime updates of this document.
 *
 * The returned cold flow will
 * - emit updates of [DocumentSnapshot].
 * - get cancelled if Firestore API result in a failure
 * - get cancelled if [callbackFlow] API cannot send result downstream.
 *
 * Sample Usage :
 * ```
 * docRef.realtime()
 *      .onEach { snap -> /* new update */ }
 *      .onCompletion { throwable ->
 *          if (throwable != null) {
 *              val ex = throwable.cause
 *              // Firestore or Flow exception
 *          }
 *      }.launchIn(coroutineScope)
 * ```
 *
 * @return A cold flow which emits realtime updates of the document.
 * */
@ExperimentalCoroutinesApi
fun DocumentReference.realtime() = callbackFlow {
    val registration = addSnapshotListener { snap, ex ->
        when {
            snap != null -> trySendBlocking(snap).onFailure { cancel(CancellationException("Channel Exception", it)) }
            ex != null -> cancel(CancellationException("Firestore Exception", ex))
        }
    }
    awaitClose { registration.remove() }
}

/**
 * Listen to realtime updates of this document, and applies the given [transformer] on each update.
 *
 * The returned cold flow will
 * - emit updates of [T] or null if the document does not exists.
 * - get cancelled if Firestore API result in a failure
 * - get cancelled if [callbackFlow] API cannot send result downstream.
 * - throws any Exception that occurred in the [transformer]
 *
 * Sample Usage :
 * ```
 * docRef.realtime(myTransformer)
 *      .onEach { obj ->
 *          if (obj == null) { /* Document does not exists. */ }
 *          else { /* Process results */ }
 *      }
 *      .catch { throwable -> /* Transformer Exception */ }
 *      .onCompletion { throwable ->
 *          if (throwable != null) {
 *              val ex = throwable.cause
 *              // Handle Firestore or Flow exception
 *          }
 *      }.launchIn(coroutineScope)
 * ```
 *
 * @param transformer The transformer to apply on document snapshots.
 *
 * @return A cold flow which emits realtime updates of the document mapped to [T].
 * */
fun <T> DocumentReference.realtime(transformer: Transformer<T>) = realtime().map { if (it.exists()) transformer.transform(it) else null }

