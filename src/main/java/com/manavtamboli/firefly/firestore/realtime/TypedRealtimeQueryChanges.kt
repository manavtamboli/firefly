package com.manavtamboli.firefly.firestore.realtime

import com.google.firebase.firestore.DocumentChange.Type
import com.google.firebase.firestore.DocumentChange.Type.*
import com.google.firebase.firestore.Query
import com.manavtamboli.firefly.firestore.Transformer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transform

@ExperimentalCoroutinesApi
fun <T> Query.realtimeChanges(transformer: Transformer<T>) = realtimeChanges().map { it.map { change -> transformer.transform(change.document) to change.type } }

fun <T> Flow<List<Pair<T, Type>>>.onAdded(action : suspend (T) -> Unit) = transform { changes ->
    changes.groupBy { it.second }.let {
        it[ADDED]?.forEach { pair -> action.invoke(pair.first) }
        emit((it[MODIFIED] ?: emptyList()) + (it[REMOVED] ?: emptyList()))
    }
}

fun <T> Flow<List<Pair<T, Type>>>.onModified(action : suspend (T) -> Unit) = transform { changes ->
    changes.groupBy { it.second }.let {
        it[MODIFIED]?.forEach { pair -> action.invoke(pair.first) }
        emit((it[ADDED] ?: emptyList()) + (it[REMOVED] ?: emptyList()))
    }
}

fun <T> Flow<List<Pair<T, Type>>>.onRemoved(action : suspend (T) -> Unit) = transform { changes ->
    changes.groupBy { it.second }.let {
        it[REMOVED]?.forEach { pair -> action.invoke(pair.first) }
        emit((it[ADDED] ?: emptyList()) + (it[MODIFIED] ?: emptyList()))
    }
}