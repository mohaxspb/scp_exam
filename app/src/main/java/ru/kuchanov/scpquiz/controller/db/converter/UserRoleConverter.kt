package ru.kuchanov.scpquiz.controller.db.converter

import androidx.room.TypeConverter
import ru.kuchanov.scpquiz.model.db.UserRole

class UserRoleConverter {
    @TypeConverter
    fun restoreEnum(enumName: String): UserRole = UserRole.valueOf(enumName)

    @TypeConverter
    fun saveEnumToString(enumType: UserRole) = enumType.name
}