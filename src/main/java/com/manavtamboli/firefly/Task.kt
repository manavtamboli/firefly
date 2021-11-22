package com.manavtamboli.firefly

import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun <T> Task<T>.await() = suspendCancellableCoroutine<T> { continuation ->
    addOnSuccessListener {
        continuation.resume(it)
    }
    addOnFailureListener {
        continuation.resumeWithException(it)
    }
}

suspend fun <T> Task<T>.awaitCatching() = kotlin.runCatching { await() }