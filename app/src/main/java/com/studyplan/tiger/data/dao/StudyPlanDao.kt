package com.studyplan.tiger.data.dao

import androidx.room.*
import androidx.lifecycle.LiveData
import com.studyplan.tiger.data.entity.StudyPlan
import java.util.Date

/**
 * 学习计划数据访问对象
 */
@Dao
interface StudyPlanDao {
    
    /**
     * 插入新计划
     */
    @Insert
    suspend fun insertPlan(plan: StudyPlan): Long
    
    /**
     * 更新计划
     */
    @Update
    suspend fun updatePlan(plan: StudyPlan)
    
    /**
     * 删除计划
     */
    @Delete
    suspend fun deletePlan(plan: StudyPlan)
    
    /**
     * 根据ID删除计划
     */
    @Query("DELETE FROM study_plans WHERE id = :planId")
    suspend fun deletePlanById(planId: Long)
    
    /**
     * 根据ID获取计划
     */
    @Query("SELECT * FROM study_plans WHERE id = :planId")
    suspend fun getPlanById(planId: Long): StudyPlan?
    
    /**
     * 获取所有启用的计划
     */
    @Query("SELECT * FROM study_plans WHERE isEnabled = 1 ORDER BY startTime ASC")
    fun getAllEnabledPlans(): LiveData<List<StudyPlan>>
    
    /**
     * 获取今日应执行的计划
     */
    @Query("""
        SELECT * FROM study_plans 
        WHERE isEnabled = 1 
        AND (
            (repeatType = 0 AND date(createDate/1000, 'unixepoch') = date('now', 'localtime')) OR
            (repeatType = 1) OR 
            (repeatType = 2)
        )
        ORDER BY startTime ASC
    """)
    suspend fun getTodayPlans(): List<StudyPlan>
    
    /**
     * 获取今日应执行的计划（LiveData）
     */
    @Query("""
        SELECT * FROM study_plans 
        WHERE isEnabled = 1 
        AND (
            (repeatType = 0 AND date(createDate/1000, 'unixepoch') = date('now', 'localtime')) OR
            (repeatType = 1) OR 
            (repeatType = 2)
        )
        ORDER BY startTime ASC
    """)
    fun getTodayPlansLive(): LiveData<List<StudyPlan>>
    
    /**
     * 获取指定时间段内有冲突的计划
     */
    @Query("""
        SELECT * FROM study_plans 
        WHERE isEnabled = 1 
        AND (
            (repeatType = 0 AND date(createDate/1000, 'unixepoch') = date('now', 'localtime')) OR
            (repeatType = 1) OR 
            (repeatType = 2)
        )
        AND (
            (time(startTime/1000, 'unixepoch') <= time(:endTime/1000, 'unixepoch') AND 
             time(endTime/1000, 'unixepoch') >= time(:startTime/1000, 'unixepoch'))
        )
        AND id != :excludePlanId
    """)
    suspend fun getConflictPlans(startTime: Long, endTime: Long, excludePlanId: Long = -1): List<StudyPlan>
    
    /**
     * 更新计划完成状态
     */
    @Query("UPDATE study_plans SET isCompleted = :isCompleted, completedTime = :completedTime WHERE id = :planId")
    suspend fun updatePlanCompletion(planId: Long, isCompleted: Boolean, completedTime: Long?)
    
    /**
     * 更新开始提醒状态
     */
    @Query("UPDATE study_plans SET hasStartReminder = :hasReminder WHERE id = :planId")
    suspend fun updateStartReminderStatus(planId: Long, hasReminder: Boolean)
    
    /**
     * 更新结束提醒状态
     */
    @Query("UPDATE study_plans SET hasEndReminder = :hasReminder WHERE id = :planId")
    suspend fun updateEndReminderStatus(planId: Long, hasReminder: Boolean)
    
    /**
     * 重置今日所有计划的提醒状态（用于新的一天开始时）
     */
    @Query("""
        UPDATE study_plans 
        SET hasStartReminder = 0, hasEndReminder = 0, isCompleted = 0, completedTime = NULL
        WHERE repeatType > 0
    """)
    suspend fun resetDailyReminderStatus()
    
    /**
     * 获取需要开始提醒的计划
     */
    @Query("""
        SELECT * FROM study_plans 
        WHERE isEnabled = 1 
        AND hasStartReminder = 0 
        AND isCompleted = 0
        AND (
            (repeatType = 0 AND date(createDate/1000, 'unixepoch') = date('now', 'localtime')) OR
            (repeatType = 1) OR 
            (repeatType = 2)
        )
    """)
    suspend fun getPlansNeedStartReminder(): List<StudyPlan>
    
    /**
     * 获取需要结束提醒的计划
     */
    @Query("""
        SELECT * FROM study_plans 
        WHERE isEnabled = 1 
        AND hasEndReminder = 0 
        AND hasStartReminder = 1
        AND (
            (repeatType = 0 AND date(createDate/1000, 'unixepoch') = date('now', 'localtime')) OR
            (repeatType = 1) OR 
            (repeatType = 2)
        )
    """)
    suspend fun getPlansNeedEndReminder(): List<StudyPlan>
}