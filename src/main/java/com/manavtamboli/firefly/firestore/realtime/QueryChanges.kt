@file:Suppress("unused")

package com.manavtamboli.firefly.firestore.realtime

import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentChange.Type
import com.google.firebase.firestore.DocumentChange.Type.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.transform

private fun Flow<DocumentChange>.byType(type : Type) = transform { if (it.type == type) emit(it) }

private fun Flow<DocumentChange>.onType(type: Type, action : suspend (DocumentChange) -> Unit) = onEach {
    if (it.type == type) action(it)
}

fun Flow<DocumentChange>.added() = byType(ADDED)

fun Flow<DocumentChange>.modified() = byType(MODIFIED)

fun Flow<DocumentChange>.removed() = byType(REMOVED)

fun Flow<DocumentChange>.onAdded(action: suspend (DocumentChange) -> Unit) = onType(ADDED, action)

fun Flow<DocumentChange>.onModified(action: suspend (DocumentChange) -> Unit) = onType(MODIFIED, action)

fun Flow<DocumentChange>.onRemoved(action: suspend (DocumentChange) -> Unit) = onType(REMOVED, action)