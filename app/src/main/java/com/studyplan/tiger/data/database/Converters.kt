package com.studyplan.tiger.data.database

import androidx.room.TypeConverter
import java.util.Date

/**
 * Room数据库类型转换器
 */
class Converters {
    
    /**
     * Date转Long
     */
    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }
    
    /**
     * Long转Date
     */
    @TypeConverter
    fun toDate(timestamp: Long?): Date? {
        return timestamp?.let { Date(it) }
    }
}