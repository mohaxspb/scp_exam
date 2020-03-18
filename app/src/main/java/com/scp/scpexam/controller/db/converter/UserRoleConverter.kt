package com.scp.scpexam.controller.db.converter

import androidx.room.TypeConverter
import com.scp.scpexam.model.db.UserRole

class UserRoleConverter {
    @TypeConverter
    fun restoreEnum(enumName: String): UserRole = UserRole.valueOf(enumName)

    @TypeConverter
    fun saveEnumToString(enumType: UserRole) = enumType.name
}