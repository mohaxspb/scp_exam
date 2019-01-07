package ru.kuchanov.scpquiz.controller.db.converter

import android.arch.persistence.room.TypeConverter
import ru.kuchanov.scpquiz.model.db.TransactionType

class TransactionTypeConverter {
    @TypeConverter
    fun restoreEnum(enumName: String): TransactionType = TransactionType.valueOf(enumName)

    @TypeConverter
    fun saveEnumToString(enumType: TransactionType) = enumType.name
}