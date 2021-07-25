package com.manavtamboli.firefly

import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

/**
 * Converts this [LocalDate] to a [Timestamp] (at start of the day) .
 * */
fun LocalDate.toFirebaseTimestamp() = Timestamp(Date.from(atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()))

fun Timestamp.toLocalDate() : LocalDate = toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()