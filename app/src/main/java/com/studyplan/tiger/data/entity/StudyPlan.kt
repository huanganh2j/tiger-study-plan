package com.studyplan.tiger.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * 学习计划实体类
 */
@Entity(tableName = "study_plans")
data class StudyPlan(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // 事项名称
    val taskName: String,
    
    // 开始时间
    val startTime: Date,
    
    // 结束时间
    val endTime: Date,
    
    // 创建日期
    val createDate: Date = Date(),
    
    // 重复类型：0-当天，1-学习日（周一到周五），2-每天
    val repeatType: Int = 0,
    
    // 是否已完成
    val isCompleted: Boolean = false,
    
    // 完成时间
    val completedTime: Date? = null,
    
    // 是否已开始提醒
    val hasStartReminder: Boolean = false,
    
    // 是否已结束提醒
    val hasEndReminder: Boolean = false,
    
    // 是否启用（用于重复计划的开关）
    val isEnabled: Boolean = true
) {
    
    /**
     * 获取重复类型描述
     */
    fun getRepeatTypeText(): String {
        return when (repeatType) {
            0 -> "当天"
            1 -> "学习日"
            2 -> "每天"
            else -> "未知"
        }
    }
    
    /**
     * 判断是否为学习日（周一到周五）
     */
    fun isWeekday(date: Date = Date()): Boolean {
        val calendar = java.util.Calendar.getInstance()
        calendar.time = date
        val dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK)
        return dayOfWeek in 2..6 // 周一到周五
    }
    
    /**
     * 判断今天是否应该执行此计划
     */
    fun shouldExecuteToday(today: Date = Date()): Boolean {
        return when (repeatType) {
            0 -> { // 当天
                val todayCalendar = java.util.Calendar.getInstance().apply { time = today }
                val createCalendar = java.util.Calendar.getInstance().apply { time = createDate }
                
                todayCalendar.get(java.util.Calendar.YEAR) == createCalendar.get(java.util.Calendar.YEAR) &&
                todayCalendar.get(java.util.Calendar.DAY_OF_YEAR) == createCalendar.get(java.util.Calendar.DAY_OF_YEAR)
            }
            1 -> isWeekday(today) // 学习日
            2 -> true // 每天
            else -> false
        }
    }
    
    /**
     * 获取今日的计划时间（结合当前日期和计划时间）
     */
    fun getTodayPlanTime(today: Date = Date()): Pair<Date, Date> {
        val todayCalendar = java.util.Calendar.getInstance().apply { time = today }
        val startCalendar = java.util.Calendar.getInstance().apply { time = startTime }
        val endCalendar = java.util.Calendar.getInstance().apply { time = endTime }
        
        // 设置为今天的日期，但保持原有的时分秒
        val todayStart = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.YEAR, todayCalendar.get(java.util.Calendar.YEAR))
            set(java.util.Calendar.MONTH, todayCalendar.get(java.util.Calendar.MONTH))
            set(java.util.Calendar.DAY_OF_MONTH, todayCalendar.get(java.util.Calendar.DAY_OF_MONTH))
            set(java.util.Calendar.HOUR_OF_DAY, startCalendar.get(java.util.Calendar.HOUR_OF_DAY))
            set(java.util.Calendar.MINUTE, startCalendar.get(java.util.Calendar.MINUTE))
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        
        val todayEnd = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.YEAR, todayCalendar.get(java.util.Calendar.YEAR))
            set(java.util.Calendar.MONTH, todayCalendar.get(java.util.Calendar.MONTH))
            set(java.util.Calendar.DAY_OF_MONTH, todayCalendar.get(java.util.Calendar.DAY_OF_MONTH))
            set(java.util.Calendar.HOUR_OF_DAY, endCalendar.get(java.util.Calendar.HOUR_OF_DAY))
            set(java.util.Calendar.MINUTE, endCalendar.get(java.util.Calendar.MINUTE))
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        
        return Pair(todayStart.time, todayEnd.time)
    }
}