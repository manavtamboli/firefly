package com.manavtamboli.firefly.firestore.pagination

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Source
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.runningReduce
import kotlinx.coroutines.flow.shareIn
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class Pagination private constructor(private val externalScope: CoroutineScope, baseQuery : Query, private val source: Source) {

    @Volatile private var query = baseQuery
    @Volatile private var lastVisible : DocumentSnapshot? = null
    @Volatile private var lastItemReached = false

    @Synchronized
    private fun getQuery(limit : Long) : Query {
        query = query.limit(limit)
        lastVisible?.let { query = query.startAfter(it) }
        return query
    }

    @Synchronized
    private fun updateInfo(querySnapshot: QuerySnapshot, count : Long){
        if (querySnapshot.size() < count) lastItemReached = true
        querySnapshot.documents.lastOrNull()?.let { lastVisible = it }
    }

    private suspend fun suspendFetch(count : Long) : List<DocumentSnapshot> {
        if (lastItemReached) return emptyList()
        val finalQuery = getQuery(count)
        return suspendCancellableCoroutine { continuation ->
            finalQuery.get(source)
                .addOnFailureListener { continuation.resumeWithException(it) }
                .addOnSuccessListener {
                    updateInfo(it, count)
                    continuation.resume(it.documents)
                }
        }
    }

    private val batched = MutableSharedFlow<List<DocumentSnapshot>>(extraBufferCapacity = 64)

    @ExperimentalCoroutinesApi
    val documents =
        batched.runningReduce { accumulator, value -> accumulator + value }
            .shareIn(externalScope, SharingStarted.Eagerly)

    /**
     * Starts fetching the next batch of size [count].
     * */
    fun fetch(count: Long){
        externalScope.launch(Dispatchers.IO) {
            val nextBatch = suspendFetch(count)
            batched.tryEmit(nextBatch)
        }
    }

    companion object {
        /**
         * Paginates the query.
         *
         * @return An instance of [Pagination].
         * */
        fun Query.paginateIn(externalScope: CoroutineScope, source: Source = Source.DEFAULT) = Pagination(externalScope, this, source)
    }
}