package com.manavtamboli.firefly.firestore.realtime

import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentChange.Type
import com.google.firebase.firestore.DocumentChange.Type.*
import com.manavtamboli.firefly.firestore.Transformer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

private fun <T> Flow<Pair<T, Type>>.onType(type: Type, action : suspend (T) -> Unit) = onEach { (data, _type) ->
    if (_type == type) action(data)
}

fun <T> Flow<Pair<T, Type>>.onAdded(action: suspend (T) -> Unit) = onType(ADDED, action)

fun <T> Flow<Pair<T, Type>>.onModified(action: suspend (T) -> Unit) = onType(MODIFIED, action)

fun <T> Flow<Pair<T, Type>>.onRemoved(action: suspend (T) -> Unit) = onType(REMOVED, action)


fun <T> Flow<DocumentChange>.applyTransformer(transformer: Transformer<T>) = map { transformer.transform(it.document) to it.type }