package com.manavtamboli.firefly.firestore.pagination

import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentChange.Type.*
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.manavtamboli.firefly.firestore.realtime.onAdded
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.*
import kotlin.coroutines.cancellation.CancellationException

@FlowPreview
@ExperimentalCoroutinesApi
class RealtimePagination private constructor(externalScope: CoroutineScope, baseQuery: Query){

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
    private fun updateInfo(query : Query, querySnapshot: QuerySnapshot, count : Long){
        if (query != this.query) return
        if (querySnapshot.size() < count) lastItemReached = true
        querySnapshot.documents.lastOrNull()?.let { lastVisible = it }
    }

    private val _flows = MutableSharedFlow<Flow<List<DocumentChange>>>(extraBufferCapacity = 64)

    val documents = changes()
        .runningFold(emptyList<DocumentSnapshot>()){ snaps, newChanges ->
            snaps.toMutableList().apply {
                newChanges.forEach {
                    when (it.type){
                        ADDED -> add(it.document)
                        MODIFIED -> {
                            val index = indexOfFirst { d -> d.id == it.document.id }
                            if (index > -1) this[index] = it.document
                        }
                        REMOVED -> removeIf { d -> d.id == it.document.id }
                    }
                }
            }
        }
        .drop(1)
        .conflate()
        .shareIn(externalScope, SharingStarted.Eagerly, replay = 1)

    /**
     * Note : Start collecting before calling fetch for first time, or it will result in data loss.
     * */
    fun changes() : Flow<List<DocumentChange>>  = _flows.flattenMerge(Int.MAX_VALUE)

    fun fetch(count : Long) : Boolean {
        if (lastItemReached) return false
        val finalQuery = getQuery(count)
        val flow = callbackFlow<List<DocumentChange>> {
            val registration = finalQuery.addSnapshotListener { snap, ex ->
                when {
                    snap != null -> {
                        kotlin.runCatching { trySendBlocking(snap.documentChanges) }
                        updateInfo(finalQuery, snap, count)
                    }
                    ex != null -> cancel(CancellationException("Firestore Exception", ex))
                }
            }

            awaitClose { registration.remove() }
        }
        _flows.tryEmit(flow)
        return true
    }

    companion object {
        fun Query.paginateRealtimeIn(externalScope: CoroutineScope) = RealtimePagination(externalScope, this)
    }
}