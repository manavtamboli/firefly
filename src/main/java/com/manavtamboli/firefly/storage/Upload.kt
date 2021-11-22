@file:Suppress("unused")

package com.manavtamboli.firefly.storage

import android.net.Uri
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.manavtamboli.firefly.await
import java.io.InputStream

suspend fun StorageReference.upload(stream: InputStream) = upload { putStream(stream) }

suspend fun StorageReference.upload(fileUri : Uri) = upload { putFile(fileUri) }

suspend fun StorageReference.upload(bytes : ByteArray) = upload { putBytes(bytes) }

suspend fun StorageReference.upload(stream: InputStream, metadata: StorageMetadata) = upload { putStream(stream, metadata) }

suspend fun StorageReference.upload(fileUri: Uri, metadata: StorageMetadata) = upload { putFile(fileUri, metadata) }

suspend fun StorageReference.upload(bytes: ByteArray, metadata: StorageMetadata) = upload { putBytes(bytes, metadata) }

private suspend fun upload(getTask : () -> StorageTask<UploadTask.TaskSnapshot>): UploadTask.TaskSnapshot = getTask().await()