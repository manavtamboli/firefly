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

fun Flow<List<DocumentChange>>.onAdded(action : suspend (DocumentSnapshot) -> Unit) = transform { changes ->
    changes.groupBy { change -> change.type }.let {
        it[ADDED]?.forEach { change -> action(change.document) }
        emit((it[MODIFIED] ?: emptyList()) + (it[REMOVED] ?: emptyList()))
    }
}

fun Flow<List<DocumentChange>>.onModified(action : suspend (DocumentSnapshot) -> Unit) = transform { changes ->
    changes.groupBy { change -> change.type }.let {
        it[MODIFIED]?.forEach { change -> action(change.document) }
        emit((it[ADDED] ?: emptyList()) + (it[REMOVED] ?: emptyList()))
    }
}

fun Flow<List<DocumentChange>>.onRemoved(action : suspend (DocumentSnapshot) -> Unit) = transform { changes ->
    changes.groupBy { change -> change.type }.let {
        it[REMOVED]?.forEach { change -> action(change.document) }
        emit((it[ADDED] ?: emptyList()) + (it[MODIFIED] ?: emptyList()))
    }
}