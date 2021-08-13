package com.manavtamboli.firefly.storage

import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.InputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun StorageReference.upload(stream : InputStream) = suspendCancellableCoroutine<UploadTask.TaskSnapshot> { continuation ->
    putStream(stream)
        .addOnSuccessListener { continuation.resume(it) }
        .addOnFailureListener { continuation.resumeWithException(it) }
}

suspend fun StorageReference.upload(stream : InputStream, metaData : StorageMetadata) = suspendCancellableCoroutine<UploadTask.TaskSnapshot> { continuation ->
    putStream(stream, metaData)
        .addOnSuccessListener { continuation.resume(it) }
        .addOnFailureListener { continuation.resumeWithException(it) }
}
