package com.manavtamboli.firefly.firestore.realtime

import com.google.firebase.firestore.DocumentChange.Type
import com.google.firebase.firestore.DocumentChange.Type.*
import com.google.firebase.firestore.Query
import com.manavtamboli.firefly.firestore.Transformer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.transform

@ExperimentalCoroutinesApi
fun <T> Query.realtimeChanges(transformer: Transformer<T>) = realtimeChanges().map { it.map { change -> transformer.transform(change.document) to change.type } }

fun <T> Flow<List<Pair<T, Type>>>.added() = map { it.filter { (_, type) -> type == ADDED }.map { (value, _) -> value } }

fun <T> Flow<List<Pair<T, Type>>>.onAdded(action : suspend (T) -> Unit) = onEach {
    it.forEach { (value, type) ->
        if (type == ADDED) action(value)
    }
}

fun <T> Flow<List<Pair<T, Type>>>.onModified(action : suspend (T) -> Unit) = onEach {
    it.forEach { (value, type) ->
        if (type == MODIFIED) action(value)
    }
}


fun <T> Flow<List<Pair<T, Type>>>.onRemoved(action : suspend (T) -> Unit) = onEach {
    it.forEach { (value, type) ->
        if (type == REMOVED) action(value)
    }
}
