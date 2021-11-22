package com.manavtamboli.firefly.storage

import android.net.Uri
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.FileDownloadTask.TaskSnapshot
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.manavtamboli.firefly.await
import java.io.File

suspend fun StorageReference.downloadUrl() : Uri = downloadUrl.await()

suspend fun StorageReference.download(file : File) = download { getFile(file) }

suspend fun StorageReference.download(uri: Uri) = download { getFile(uri) }

private suspend fun download(getTask : () -> StorageTask<TaskSnapshot>): TaskSnapshot = getTask().await()