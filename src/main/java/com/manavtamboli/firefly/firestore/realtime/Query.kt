package com.manavtamboli.firefly.firestore.realtime

import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.transform

fun Flow<QuerySnapshot>.changes(): Flow<DocumentChange> = transform { snap ->
    snap.documentChanges.forEach {
        emit(it)
    }
}

fun Flow<QuerySnapshot>.documents(): Flow<List<DocumentSnapshot>> = map { it.documents }

fun Flow<QuerySnapshot>.onFirst(action : suspend (QuerySnapshot) -> Unit): Flow<QuerySnapshot> {
    var isFirst = true
    return onEach {
        if (isFirst) {
            action(it)
            isFirst = false
        }
    }
}