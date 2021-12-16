package com.manavtamboli.firefly

import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

/**
 * Converts this [LocalDate] to a [Timestamp].
 * */
fun LocalDate.toFirebaseTimestamp() = Timestamp(Date.from(atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()))

/**
 * Converts this [LocalDateTime] to a [Timestamp].
 * */
fun LocalDateTime.toFirebaseTimestamp() = Timestamp(Date.from(atZone(ZoneId.systemDefault()).toInstant()))

/**
 * Converts this [Timestamp] to a [LocalDate].
 * */
fun Timestamp.toLocalDate() : LocalDate = toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

/**
 * Converts this [Timestamp] to a [LocalDateTime].
 * */
fun Timestamp.toLocalDatetime() : LocalDateTime = toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()