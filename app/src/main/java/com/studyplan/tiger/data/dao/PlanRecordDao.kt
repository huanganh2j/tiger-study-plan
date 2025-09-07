package com.studyplan.tiger.data.dao

import androidx.room.*
import androidx.lifecycle.LiveData
import com.studyplan.tiger.data.entity.PlanRecord
import java.util.Date

/**
 * 计划记录数据访问对象
 */
@Dao
interface PlanRecordDao {
    
    /**
     * 插入记录
     */
    @Insert
    suspend fun insertRecord(record: PlanRecord): Long
    
    /**
     * 更新记录
     */
    @Update
    suspend fun updateRecord(record: PlanRecord)
    
    /**
     * 删除记录
     */
    @Delete
    suspend fun deleteRecord(record: PlanRecord)
    
    /**
     * 根据计划ID和日期获取记录
     */
    @Query("""
        SELECT * FROM plan_records 
        WHERE planId = :planId 
        AND date(planDate/1000, 'unixepoch') = date(:date/1000, 'unixepoch')
    """)
    suspend fun getRecordByPlanAndDate(planId: Long, date: Long): PlanRecord?
    
    /**
     * 获取最近7天的记录
     */
    @Query("""
        SELECT * FROM plan_records 
        WHERE planDate >= date('now', '-7 days') 
        ORDER BY planDate DESC, startTime ASC
    """)
    fun getRecentRecords(): LiveData<List<PlanRecord>>
    
    /**
     * 获取最近7天的记录（非LiveData）
     */
    @Query("""
        SELECT * FROM plan_records 
        WHERE planDate >= date('now', '-7 days') 
        ORDER BY planDate DESC, startTime ASC
    """)
    suspend fun getRecentRecordsList(): List<PlanRecord>
    
    /**
     * 获取指定日期的记录
     */
    @Query("""
        SELECT * FROM plan_records 
        WHERE date(planDate/1000, 'unixepoch') = date(:date/1000, 'unixepoch')
        ORDER BY startTime ASC
    """)
    suspend fun getRecordsByDate(date: Long): List<PlanRecord>
    
    /**
     * 获取指定日期范围的记录
     */
    @Query("""
        SELECT * FROM plan_records 
        WHERE planDate BETWEEN :startDate AND :endDate
        ORDER BY planDate DESC, startTime ASC
    """)
    suspend fun getRecordsByDateRange(startDate: Long, endDate: Long): List<PlanRecord>
    
    /**
     * 获取完成率统计
     */
    @Query("""
        SELECT 
            COUNT(*) as total,
            SUM(CASE WHEN isCompleted = 1 THEN 1 ELSE 0 END) as completed
        FROM plan_records 
        WHERE planDate >= date('now', '-7 days')
    """)
    suspend fun getCompletionStats(): CompletionStats
    
    /**
     * 删除7天前的记录
     */
    @Query("DELETE FROM plan_records WHERE planDate < date('now', '-7 days')")
    suspend fun deleteOldRecords()
    
    /**
     * 按日期分组获取记录
     */
    @Query("""
        SELECT 
            date(planDate/1000, 'unixepoch') as date,
            COUNT(*) as totalCount,
            SUM(CASE WHEN isCompleted = 1 THEN 1 ELSE 0 END) as completedCount
        FROM plan_records 
        WHERE planDate >= date('now', '-7 days')
        GROUP BY date(planDate/1000, 'unixepoch')
        ORDER BY date DESC
    """)
    suspend fun getRecordsByDateGroup(): List<DateGroupRecord>
}

/**
 * 完成统计数据类
 */
data class CompletionStats(
    val total: Int,
    val completed: Int
) {
    val completionRate: Float
        get() = if (total == 0) 0f else completed.toFloat() / total.toFloat()
}

/**
 * 按日期分组的记录
 */
data class DateGroupRecord(
    val date: String,
    val totalCount: Int,
    val completedCount: Int
) {
    val completionRate: Float
        get() = if (totalCount == 0) 0f else completedCount.toFloat() / totalCount.toFloat()
}