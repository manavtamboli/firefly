package com.manavtamboli.firefly.firestore.realtime

import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentChange.Type.*
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.*
import kotlin.coroutines.cancellation.CancellationException


/**
 * Listen to realtime updates of this query, but emits document changes instead of cumulative
 * list of [DocumentSnapshot].
 *
 * The returned cold flow will
 * - emit updates of [List] of [DocumentChange].
 * - get cancelled if Firestore API result in a failure
 * - get cancelled if [callbackFlow] API cannot send result downstream.
 *
 * The list emitted in the flow is of document changes and not cumulative.
 * To get updates as cumulative list, see [Query.realtime].
 *
 * ###
 *
 * Sample Usage :
 * ```
 * query.realtimeChanges()
 *      .onEach { changes ->
 *          changes.forEach {
 *              when (it.type) {
 *                  ADDED -> {}
 *                  MODIFIED -> {}
 *                  REMOVED -> {}
 *              }
 *          }
 *      }
 *      .onCompletion { throwable ->
 *          if (throwable != null) {
 *              val ex = throwable.cause
 *              // Firebase or Flow exception
 *          }
 *      }.launchIn(coroutineScope)
 * ```
 *
 * ###
 * This flow can also be used with [onAdded], [onModified], and [onRemoved] operators as below.
 * ```
 * query.realtimeChanges()
 *      .onAdded { ... }
 *      .onModified { ... }
 *      .onRemoved { ... }
 *      .onCompletion { ... }
 *      .launchIn(coroutineScope)
 * ```
 *
 * @return A cold flow which emits realtime changes of the query.
 * */
@ExperimentalCoroutinesApi
fun Query.realtimeChanges() = callbackFlow<List<DocumentChange>> {
    val registration = addSnapshotListener { snap, ex ->
        when {
            snap != null -> trySendBlocking(snap.documentChanges).onFailure { cancel(CancellationException("Channel Exception", it)) }
            ex != null -> cancel(CancellationException("Firestore Exception", ex))
        }
    }

    awaitClose { registration.remove() }
}

// TODO : Remove if not necessary
fun Flow<List<DocumentChange>>.onEmpty(action : suspend () -> Unit) = transform { changes ->
    if (changes.isEmpty()) action()
    else emit(changes)
}


fun Flow<List<DocumentChange>>.added() = map { it.filter { change -> change.type == ADDED } }

/**
 * A flow operator that invokes [action] on each document change with type [ADDED].
 *
 *
 * Usage
 * ```
 * query.realtimeChanges()
 *      .onAdded { snap ->
 *          Log.i(TAG, "A document was added. Added Document: $snap")
 *      }.launchIn(coroutineScope)
 * ```
 *
 * @param action the action to invoke when a document is added.
 * */
fun Flow<List<DocumentChange>>.onAdded(action : suspend (DocumentSnapshot) -> Unit) = onEach {
    it.forEach { doc ->
        if (doc.type == ADDED) action(doc.document)
    }
}


/**
 * A flow operator that invokes [action] on each document change with type [MODIFIED].
 *
 * Usage
 * ```
 * query.realtimeChanges()
 *      .onModified { snap ->
 *          Log.i(TAG, "A document was modified. Modified Document: $snap")
 *      }.launchIn(coroutineScope)
 * ```
 *
 * @param action the action to invoke when a document is modified.
 * */
fun Flow<List<DocumentChange>>.onModified(action : suspend (DocumentSnapshot) -> Unit) = onEach {
    it.forEach { doc ->
        if (doc.type == MODIFIED) action(doc.document)
    }
}

/**
 * A flow operator that invokes [action] on each document change with type [REMOVED].
 *
 *
 * Usage
 * ```
 * query.realtimeChanges()
 *      .onRemoved { snap ->
 *          Log.i(TAG, "A document was removed. Removed Document: $snap")
 *      }.launchIn(coroutineScope)
 * ```
 *
 * @param action the action to invoke when a document is removed.
 * */
fun Flow<List<DocumentChange>>.onRemoved(action : suspend (DocumentSnapshot) -> Unit) =onEach {
    it.forEach { doc ->
        if (doc.type == REMOVED) action(doc.document)
    }
}