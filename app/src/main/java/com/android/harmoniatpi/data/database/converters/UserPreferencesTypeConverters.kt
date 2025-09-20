package com.android.harmoniatpi.data.database.converters

import androidx.room.TypeConverter
import com.android.harmoniatpi.domain.model.userPreferences.AppTheme

class UserPreferencesTypeConverters {

    @TypeConverter
    fun fromAppTheme(theme: AppTheme): Boolean = theme.value

    @TypeConverter
    fun toAppTheme(value: Boolean): AppTheme =
        AppTheme.entries.first { it.value == value }

}