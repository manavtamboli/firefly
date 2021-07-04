package com.manavtamboli.firefly.firestore.realtime

import com.google.firebase.firestore.DocumentChange.Type
import com.google.firebase.firestore.Query
import com.manavtamboli.firefly.firestore.Transformer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transform

@ExperimentalCoroutinesApi
fun <T> Query.realtimeChanges(transformer: Transformer<T>) = realtimeChanges().map { it.map { change -> transformer.transform(change.document) to change.type } }

fun <T> Flow<List<Pair<T, Type>>>.onAdded(action : suspend (T) -> Unit) = transform {
    val groups = it.groupBy { pair -> pair.second }
    groups[Type.ADDED]?.forEach { pair ->
        action(pair.first)
    }
    emit((groups[Type.MODIFIED] ?: emptyList()) + (groups[Type.REMOVED] ?: emptyList()))
}

fun <T> Flow<List<Pair<T, Type>>>.onModified(action : suspend (T) -> Unit) = transform {
    val groups = it.groupBy { pair -> pair.second }
    groups[Type.MODIFIED]?.forEach { pair ->
        action(pair.first)
    }
    emit((groups[Type.ADDED] ?: emptyList()) + (groups[Type.REMOVED] ?: emptyList()))
}

fun <T> Flow<List<Pair<T, Type>>>.onRemoved(action : suspend (T) -> Unit) = transform {
    val groups = it.groupBy { pair -> pair.second }
    groups[Type.REMOVED]?.forEach { pair ->
        action(pair.first)
    }
    emit((groups[Type.ADDED] ?: emptyList()) + (groups[Type.MODIFIED] ?: emptyList()))
}