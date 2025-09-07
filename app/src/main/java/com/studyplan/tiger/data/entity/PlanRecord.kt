package com.studyplan.tiger.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * 计划完成记录实体类
 */
@Entity(tableName = "plan_records")
data class PlanRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // 关联的计划ID
    val planId: Long,
    
    // 事项名称（冗余存储，便于查询历史记录）
    val taskName: String,
    
    // 计划日期
    val planDate: Date,
    
    // 开始时间
    val startTime: Date,
    
    // 结束时间
    val endTime: Date,
    
    // 是否完成
    val isCompleted: Boolean,
    
    // 实际完成时间
    val actualCompletedTime: Date? = null,
    
    // 记录创建时间
    val recordTime: Date = Date()
) {
    
    /**
     * 获取计划时长（分钟）
     */
    fun getDurationMinutes(): Int {
        return ((endTime.time - startTime.time) / (1000 * 60)).toInt()
    }
    
    /**
     * 是否按时完成（在计划结束时间前完成）
     */
    fun isCompletedOnTime(): Boolean {
        return isCompleted && actualCompletedTime != null && actualCompletedTime!! <= endTime
    }
}