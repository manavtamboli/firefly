package com.manavtamboli.firefly.firestore.single

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import com.manavtamboli.firefly.firestore.Transformer
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Fetches this query once.
 *
 * Sample Usage :
 *
 * ```
 * coroutineScope.launch(Dispatchers.IO) {
 *      runCatching { query.fetch() }
 *          .onSuccess { snaps -> /* Success */ }
 *          .onFailure { ex -> /* Firestore Exception */ }
 * }
 * ```
 *
 * @return [List] of [DocumentSnapshot].
 *
 * @throws FirebaseFirestoreException Any exception thrown by the official Firestore API.
 * */
suspend fun Query.fetch() : List<DocumentSnapshot> = suspendCancellableCoroutine { continuation ->
    get()
        .addOnSuccessListener { continuation.resume(it.documents) }
        .addOnFailureListener { continuation.resumeWithException(it) }
}

/**
 * Fetches this query once with the given [source].
 *
 * Sample Usage :
 *
 * ```
 * coroutineScope.launch(Dispatchers.IO) {
 *      runCatching { query.fetch(Source.SERVER) }
 *          .onSuccess { snaps -> /* Success */ }
 *          .onFailure { ex -> /* Firestore Exception */ }
 * }
 * ```
 * @param source The source to fetch the query from.
 *
 * @return [List] of [DocumentSnapshot].
 *
 * @throws FirebaseFirestoreException Any exception thrown by the official Firestore API.
 * */
suspend fun Query.fetchWith(source: Source) : List<DocumentSnapshot> = suspendCancellableCoroutine { continuation ->
    get(source)
        .addOnSuccessListener { continuation.resume(it.documents) }
        .addOnFailureListener { continuation.resumeWithException(it) }
}

/**
 * Fetches this query once and applies the given [transformer] on each item.
 *
 * Sample Usage :
 *
 * ```
 * coroutineScope.launch(Dispatchers.IO) {
 *      runCatching { query.fetch(myTransformer) }
 *          .onSuccess { objs -> /* Success */ }
 *          .onFailure { ex -> /* Firestore or Transformer Exception */ }
 * }
 * ```
 *
 * @param transformer The transformer to transform the document snapshots.
 *
 * @return [List] of [T].
 *
 * @throws FirebaseFirestoreException Any exception thrown by the official Firestore API.
 * @throws Exception Any exception thrown by the [transformer].
 * */
suspend fun <T> Query.fetch(transformer: Transformer<T>) : List<T> = fetch().map(transformer::transform)


/**
 * Fetches this query once with the given [source] and applies the given [transformer] on it.
 *
 * Sample Usage :
 *
 * ```
 * coroutineScope.launch(Dispatchers.IO) {
 *      runCatching { query.fetch(Source.SERVER, myTransformer) }
 *          .onSuccess { objs -> /* Success */ }
 *          .onFailure { ex -> /* Firestore or Transformer Exception */ }
 * }
 * ```
 *
 * @param source The source to fetch the query from.
 * @param transformer The transformer to transform the document snapshot.
 *
 * @return [List] of [T].
 *
 * @throws FirebaseFirestoreException Any exception thrown by the official Firestore API.
 * @throws Exception Any exception thrown by the given [transformer].
 * */
suspend fun <T> Query.fetchWith(source: Source, transformer: Transformer<T>) : List<T> = fetchWith(source).map(transformer::transform)