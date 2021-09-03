package com.manavtamboli.firefly.storage

import android.net.Uri
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun StorageReference.downloadUrl() = suspendCancellableCoroutine<Uri> { continuation ->
    downloadUrl
        .addOnSuccessListener { continuation.resume(it) }
        .addOnFailureListener { continuation.resumeWithException(it) }
}

suspend fun StorageReference.download(file : File) = download { getFile(file) }

suspend fun StorageReference.download(uri: Uri) = download { getFile(uri) }

private suspend fun download(taskProducer : () -> StorageTask<FileDownloadTask.TaskSnapshot>) =
    suspendCancellableCoroutine<FileDownloadTask.TaskSnapshot> { continuation ->
        taskProducer()
            .addOnSuccessListener { continuation.resume(it) }
            .addOnFailureListener { continuation.resumeWithException(it) }
    }