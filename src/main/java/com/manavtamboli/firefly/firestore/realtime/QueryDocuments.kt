package com.manavtamboli.firefly.firestore.realtime

import com.google.firebase.firestore.DocumentSnapshot
import com.manavtamboli.firefly.firestore.Transformer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

fun <T> Flow<List<DocumentSnapshot>>.applyTransformer(transformer: Transformer<T>) = map { it.map(transformer::transform) }