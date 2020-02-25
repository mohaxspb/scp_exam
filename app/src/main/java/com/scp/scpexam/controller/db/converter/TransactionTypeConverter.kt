package com.scp.scpexam.controller.db.converter

import androidx.room.TypeConverter
import com.scp.scpexam.model.db.TransactionType

class TransactionTypeConverter {
    @TypeConverter
    fun restoreEnum(enumName: String): TransactionType = TransactionType.valueOf(enumName)

    @TypeConverter
    fun saveEnumToString(enumType: TransactionType) = enumType.name
}