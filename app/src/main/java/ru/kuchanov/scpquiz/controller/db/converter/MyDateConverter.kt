package ru.kuchanov.scpquiz.controller.db.converter

import android.arch.persistence.room.TypeConverter
import java.text.SimpleDateFormat
import java.util.*

class MyDateConverter {

    @TypeConverter
    fun fromTimestamp(value: Long) = Date(value)

    @TypeConverter
    fun dateToTimestamp(date: Date) = date.time

    @TypeConverter
    fun dateToString(date: Date) = DATE_FORMAT.format(date)

    @TypeConverter
    fun fromTimestamp(value: String) = DATE_FORMAT.parse(value)

    companion object {

        /**
         * i.e. 2018-05-20T14:57:40.698+0000
         */
        val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSSZ", Locale.getDefault())
    }
}