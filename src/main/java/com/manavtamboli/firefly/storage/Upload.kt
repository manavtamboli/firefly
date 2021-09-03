@file:Suppress("unused")

package com.manavtamboli.firefly.storage

import android.net.Uri
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.InputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun StorageReference.upload(stream: InputStream) = upload { putStream(stream) }

suspend fun StorageReference.upload(fileUri : Uri) = upload { putFile(fileUri) }

suspend fun StorageReference.upload(bytes : ByteArray) = upload { putBytes(bytes) }

suspend fun StorageReference.upload(stream: InputStream, metadata: StorageMetadata) = upload { putStream(stream, metadata) }

suspend fun StorageReference.upload(fileUri: Uri, metadata: StorageMetadata) = upload { putFile(fileUri, metadata) }

suspend fun StorageReference.upload(bytes: ByteArray, metadata: StorageMetadata) = upload { putBytes(bytes, metadata) }

private suspend fun upload(taskProducer : () -> StorageTask<UploadTask.TaskSnapshot>) = suspendCancellableCoroutine<UploadTask.TaskSnapshot> { continuation ->
    taskProducer()
        .addOnSuccessListener { continuation.resume(it) }
        .addOnFailureListener { continuation.resumeWithException(it) }
}