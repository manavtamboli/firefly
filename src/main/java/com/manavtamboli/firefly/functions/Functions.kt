package com.manavtamboli.firefly.functions

import com.google.firebase.functions.HttpsCallableReference
import com.google.firebase.functions.HttpsCallableResult
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun HttpsCallableReference.suspendCall() = suspendCancellableCoroutine<HttpsCallableResult?> { continuation ->
    call()
        .addOnSuccessListener { continuation.resume(it) }
        .addOnFailureListener { continuation.resumeWithException(it) }
}

suspend fun HttpsCallableReference.suspendCall(data : Any?) = suspendCancellableCoroutine<HttpsCallableResult?> { continuation ->
    call(data)
        .addOnSuccessListener { continuation.resume(it) }
        .addOnFailureListener { continuation.resumeWithException(it) }
}