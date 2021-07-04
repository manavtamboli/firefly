package com.manavtamboli.firefly.firestore

import com.google.firebase.firestore.DocumentSnapshot

/**
 * Interface to convert a [DocumentSnapshot] into the given type [T].
 * */
fun interface Transformer<T> {

    /**
     * Transforms [snap] to type [T].
     * Any failures to transform [snap] must be thrown in this method.
     * Errors must be handled by APIs using this method.
     * */
    fun transform(snap: DocumentSnapshot) : T
}