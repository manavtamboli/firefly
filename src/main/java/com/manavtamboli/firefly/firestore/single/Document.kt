package com.manavtamboli.firefly.firestore.single

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Source
import com.manavtamboli.firefly.await
import com.manavtamboli.firefly.firestore.Transformer

/**
 * Fetches this document once.
 *
 * Sample Usage :
 *
 * ```
 * coroutineScope.launch(Dispatchers.IO) {
 *      runCatching { docRef.fetch() }
 *          .onSuccess { snap -> /* Success */ }
 *          .onFailure { ex -> /* Firestore Exception */ }
 * }
 * ```
 *
 * @return The document's [DocumentSnapshot].
 *
 * @throws FirebaseFirestoreException Any exception thrown by the official Firestore API.
 * */
suspend fun DocumentReference.fetch() : DocumentSnapshot = get().await()


/**
 * Fetches this document once with the given [source].
 *
 * Sample Usage :
 *
 * ```
 * coroutineScope.launch(Dispatchers.IO) {
 *      runCatching { docRef.fetch(Source.SERVER) }
 *          .onSuccess { snap -> /* Success */ }
 *          .onFailure { ex -> /* Firestore Exception */ }
 * }
 * ```
 *
 * @param source The source to fetch the document from.
 *
 * @return The document's [DocumentSnapshot].
 *
 * @throws FirebaseFirestoreException Any exception thrown by the official Firestore API.
 * */
suspend fun DocumentReference.fetchWith(source: Source) : DocumentSnapshot = get(source).await()


/**
 * Fetches this document once and applies the given [transformer] on it.
 *
 * Sample Usage :
 *
 * ```
 * coroutineScope.launch(Dispatchers.IO) {
 *      runCatching { docRef.fetch(myTransformer) }
 *          .onSuccess { obj -> /* Success */ }
 *          .onFailure { ex -> /* Firestore or Transformer Exception */ }
 * }
 * ```
 *
 * @param transformer The transformer to transform the document snapshot.
 *
 * @return Object of type [T] or null if the document does not exists.
 *
 * @throws FirebaseFirestoreException Any exception thrown by the official Firestore API.
 * @throws Exception Any exception thrown by the given [transformer].
 * */
suspend fun <T> DocumentReference.fetch(transformer: Transformer<T>) = fetch().let {
    if (it.exists()) transformer.transform(it)
    else null
}

/**
 * Fetches this document once with the given [source] and applies the given [transformer] on it.
 *
 * Sample Usage :
 *
 * ```
 * coroutineScope.launch(Dispatchers.IO) {
 *      runCatching { docRef.fetchWith(Source.SERVER, myTransformer) }
 *          .onSuccess { obj -> /* Success */ }
 *          .onFailure { ex -> /* Firestore or Transformer Exception */ }
 * }
 * ```
 *
 * @param source The source to fetch the document from.
 * @param transformer The transformer to transform the document snapshot.
 *
 * @return Object of type [T] or null if the document does not exists.
 *
 * @throws FirebaseFirestoreException Any exception thrown by the official Firestore API.
 * @throws Exception Any exception thrown by the given [transformer].
 * */
suspend fun <T> DocumentReference.fetchWith(source: Source, transformer: Transformer<T>) = fetchWith(source).let {
    if (it.exists()) transformer.transform(it)
    else null
}