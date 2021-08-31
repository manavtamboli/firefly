package com.manavtamboli.firefly.firestore.pagination

import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import com.manavtamboli.firefly.firestore.Transformer
import com.manavtamboli.firefly.firestore.pagination.Pagination.Companion.paginateIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.map

class TypedPagination<T> private constructor(externalScope: CoroutineScope, baseQuery: Query, private val transformer: Transformer<T>, source: Source) {

    private val pagination = baseQuery.paginateIn(externalScope, source)

    val allDocuments = pagination.documents.map { it.map(transformer::transform) }

    fun fetch(count : Long) = pagination.fetch(count)

    companion object {
        /**
         * Returns an instance of [TypedPagination] of type [T].
         *
         * @param externalScope the scope which the instance of [TypedPagination] is tied to.
         * */
        fun <T> Query.paginateIn(externalScope: CoroutineScope, transformer: Transformer<T>, source: Source = Source.DEFAULT) =
            TypedPagination(externalScope, this, transformer, source)
    }
}