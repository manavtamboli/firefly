package com.manavtamboli.firefly.firestore.pagination

import com.google.firebase.firestore.Query
import com.manavtamboli.firefly.firestore.Transformer
import com.manavtamboli.firefly.firestore.pagination.RealtimePagination.Companion.paginateRealtimeIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn

@FlowPreview
@ExperimentalCoroutinesApi
class TypedRealtimePagination<T> private constructor(externalScope: CoroutineScope, baseQuery : Query, transformer: Transformer<T>){

    private val source = baseQuery.paginateRealtimeIn(externalScope)

    /**
     * Throws any exception occurred in the transformer
     * */
    val documents = source.documents
        .map { snaps -> snaps.map { transformer.transform(it) } }
        .conflate()
        .shareIn(externalScope, SharingStarted.Eagerly, replay = 1)

    /**
     * Note : Start collecting before calling fetch for first time, or it will result in data loss.
     * */
    fun changes() = source.changes()

    fun fetch(count : Long) = source.fetch(count)

    companion object {
        fun <T> Query.paginateRealtimeIn(externalScope: CoroutineScope, transformer: Transformer<T>) = TypedRealtimePagination(externalScope, this, transformer)
    }
}