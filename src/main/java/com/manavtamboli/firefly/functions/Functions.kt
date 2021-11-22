package com.manavtamboli.firefly.functions

import com.google.firebase.functions.HttpsCallableReference
import com.google.firebase.functions.HttpsCallableResult
import com.manavtamboli.firefly.await
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Suspends function call.
 * */
suspend fun HttpsCallableReference.suspendCall() : HttpsCallableResult = call().await()

/**
 * Suspends function call.
 * */
suspend fun HttpsCallableReference.suspendCall(data : Any) : HttpsCallableResult = call(data).await()