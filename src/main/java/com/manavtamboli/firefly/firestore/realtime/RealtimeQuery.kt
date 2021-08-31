package com.manavtamboli.firefly.firestore.realtime

import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.manavtamboli.firefly.firestore.Transformer
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.coroutines.cancellation.CancellationException

fun Query.realtime(): Flow<QuerySnapshot> = callbackFlow {
    val registration = addSnapshotListener { snap, ex ->
        when {
            snap != null -> trySendBlocking(snap).onFailure { cancel(CancellationException("Channel Exception", it)) }
            ex != null -> cancel(CancellationException("Firestore Exception", ex))
        }
    }

    awaitClose { registration.remove() }
}

fun Query.realtimeChanges() = realtime().changes()

fun Query.realtimeDocuments() = realtime().documents()

fun <T> Query.realtimeChanges(transformer: Transformer<T>) = realtimeChanges().applyTransformer(transformer)

fun <T> Query.realtimeDocuments(transformer: Transformer<T>) = realtimeDocuments().applyTransformer(transformer)