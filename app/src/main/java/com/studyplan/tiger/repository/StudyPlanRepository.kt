package com.studyplan.tiger.repository

import androidx.lifecycle.LiveData
import com.studyplan.tiger.data.dao.StudyPlanDao
import com.studyplan.tiger.data.dao.PlanRecordDao
import com.studyplan.tiger.data.entity.StudyPlan
import com.studyplan.tiger.data.entity.PlanRecord
import java.util.*

/**
 * 学习计划仓库
 */
class StudyPlanRepository(
    private val studyPlanDao: StudyPlanDao,
    private val planRecordDao: PlanRecordDao
) {
    
    /**
     * 获取今日计划（LiveData）
     */
    fun getTodayPlansLive(): LiveData<List<StudyPlan>> {
        return studyPlanDao.getTodayPlansLive()
    }
    
    /**
     * 获取今日计划（挂起函数）
     */
    suspend fun getTodayPlans(): List<StudyPlan> {
        return studyPlanDao.getTodayPlans().filter { it.shouldExecuteToday() }
    }
    
    /**
     * 插入计划
     */
    suspend fun insertPlan(plan: StudyPlan): Long {
        return studyPlanDao.insertPlan(plan)
    }
    
    /**
     * 更新计划
     */
    suspend fun updatePlan(plan: StudyPlan) {
        studyPlanDao.updatePlan(plan)
    }
    
    /**
     * 删除计划
     */
    suspend fun deletePlan(plan: StudyPlan) {
        studyPlanDao.deletePlan(plan)
    }
    
    /**
     * 根据ID获取计划
     */
    suspend fun getPlanById(planId: Long): StudyPlan? {
        return studyPlanDao.getPlanById(planId)
    }
    
    /**
     * 获取冲突的计划
     */
    suspend fun getConflictPlans(startTime: Date, endTime: Date, excludePlanId: Long = -1): List<StudyPlan> {
        val conflicts = studyPlanDao.getConflictPlans(startTime.time, endTime.time, excludePlanId)
        return conflicts.filter { it.shouldExecuteToday() }
    }
    
    /**
     * 更新计划完成状态
     */
    suspend fun updatePlanCompletion(planId: Long, isCompleted: Boolean, completedTime: Date?) {
        studyPlanDao.updatePlanCompletion(planId, isCompleted, completedTime?.time)
        
        // 同时记录到历史记录
        val plan = studyPlanDao.getPlanById(planId)
        plan?.let {
            val (todayStart, todayEnd) = it.getTodayPlanTime()
            val record = PlanRecord(
                planId = planId,
                taskName = it.taskName,
                planDate = Date(),
                startTime = todayStart,
                endTime = todayEnd,
                isCompleted = isCompleted,
                actualCompletedTime = completedTime
            )
            
            // 检查是否已有今日记录
            val existingRecord = planRecordDao.getRecordByPlanAndDate(planId, Date().time)
            if (existingRecord != null) {
                planRecordDao.updateRecord(record.copy(id = existingRecord.id))
            } else {
                planRecordDao.insertRecord(record)
            }
        }
    }
    
    /**
     * 获取下一个待执行的计划
     */
    suspend fun getNextPlan(currentTime: Date = Date()): StudyPlan? {
        val todayPlans = getTodayPlans()
        return todayPlans
            .filter { !it.isCompleted }
            .map { plan ->
                val (todayStart, _) = plan.getTodayPlanTime(currentTime)
                Pair(plan, todayStart)
            }
            .filter { (_, startTime) -> startTime.after(currentTime) }
            .minByOrNull { (_, startTime) -> startTime.time }
            ?.first
    }
    
    /**
     * 获取需要开始提醒的计划
     */
    suspend fun getPlansNeedStartReminder(): List<StudyPlan> {
        return studyPlanDao.getPlansNeedStartReminder().filter { it.shouldExecuteToday() }
    }
    
    /**
     * 获取需要结束提醒的计划
     */
    suspend fun getPlansNeedEndReminder(): List<StudyPlan> {
        return studyPlanDao.getPlansNeedEndReminder().filter { it.shouldExecuteToday() }
    }
    
    /**
     * 更新开始提醒状态
     */
    suspend fun updateStartReminderStatus(planId: Long, hasReminder: Boolean) {
        studyPlanDao.updateStartReminderStatus(planId, hasReminder)
    }
    
    /**
     * 更新结束提醒状态
     */
    suspend fun updateEndReminderStatus(planId: Long, hasReminder: Boolean) {
        studyPlanDao.updateEndReminderStatus(planId, hasReminder)
    }
    
    /**
     * 重置每日提醒状态
     */
    suspend fun resetDailyReminderStatus() {
        studyPlanDao.resetDailyReminderStatus()
    }
    
    /**
     * 获取最近记录
     */
    fun getRecentRecords(): LiveData<List<PlanRecord>> {
        return planRecordDao.getRecentRecords()
    }
    
    /**
     * 获取最近记录列表
     */
    suspend fun getRecentRecordsList(): List<PlanRecord> {
        return planRecordDao.getRecentRecordsList()
    }
    
    /**
     * 删除过期记录
     */
    suspend fun deleteOldRecords() {
        planRecordDao.deleteOldRecords()
    }
}