package com.manavtamboli.firefly.firestore.pagination

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Source
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
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

    private suspend fun fetchNextBatch(count : Long) : List<DocumentSnapshot> {
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

    /**
     * A [SharedFlow] which emits cumulative list of all documents fetched.
     **/
    @ExperimentalCoroutinesApi
    val documents =
        batched.runningReduce { accumulator, value -> accumulator + value }
            .shareIn(externalScope, SharingStarted.Eagerly)

    /**
     * Starts fetching the next batch of size [count].
     * If the [externalScope] is cancelled, then this function will have no effect.
     *
     * @param count Number of documents to fetch.
     * */
    fun fetch(count: Long){
        externalScope.launch(Dispatchers.IO) {
            val nextBatch = fetchNextBatch(count)
            batched.tryEmit(nextBatch)
        }
    }

    companion object {
        /**
         * Returns an instance of [Pagination].
         *
         * @param externalScope the scope to which the instance of [Pagination] is tied to.
         * */
        fun Query.paginateIn(externalScope: CoroutineScope, source: Source = Source.DEFAULT) = Pagination(externalScope, this, source)
    }
}