package com.studyplan.tiger.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.studyplan.tiger.data.database.AppDatabase
import com.studyplan.tiger.data.entity.StudyPlan
import com.studyplan.tiger.repository.StudyPlanRepository
import kotlinx.coroutines.launch
import java.util.*

/**
 * 主界面ViewModel
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: StudyPlanRepository
    
    // 今日计划列表
    val todayPlans: LiveData<List<StudyPlan>>
    
    init {
        val database = AppDatabase.getDatabase(application)
        repository = StudyPlanRepository(database.studyPlanDao(), database.planRecordDao())
        todayPlans = repository.getTodayPlansLive()
    }
    
    /**
     * 添加计划
     */
    fun addPlan(plan: StudyPlan) {
        viewModelScope.launch {
            repository.insertPlan(plan)
        }
    }
    
    /**
     * 更新计划
     */
    fun updatePlan(plan: StudyPlan) {
        viewModelScope.launch {
            repository.updatePlan(plan)
        }
    }
    
    /**
     * 删除计划
     */
    fun deletePlan(plan: StudyPlan) {
        viewModelScope.launch {
            repository.deletePlan(plan)
        }
    }
    
    /**
     * 检查时间冲突
     */
    fun checkTimeConflict(
        startTime: Date,
        endTime: Date,
        excludePlanId: Long = -1,
        onConflict: (StudyPlan) -> Unit,
        onNoConflict: () -> Unit
    ) {
        viewModelScope.launch {
            val conflicts = repository.getConflictPlans(startTime, endTime, excludePlanId)
            if (conflicts.isNotEmpty()) {
                onConflict(conflicts.first())
            } else {
                onNoConflict()
            }
        }
    }
    
    /**
     * 标记计划完成
     */
    fun markPlanCompleted(planId: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            val completedTime = if (isCompleted) Date() else null
            repository.updatePlanCompletion(planId, isCompleted, completedTime)
        }
    }
    
    /**
     * 获取下一个待执行的计划
     */
    suspend fun getNextPlan(currentTime: Date = Date()): StudyPlan? {
        return repository.getNextPlan(currentTime)
    }
    
    /**
     * 重置每日提醒状态
     */
    fun resetDailyReminderStatus() {
        viewModelScope.launch {
            repository.resetDailyReminderStatus()
        }
    }
}