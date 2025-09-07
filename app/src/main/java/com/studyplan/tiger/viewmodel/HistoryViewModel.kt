package com.studyplan.tiger.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.studyplan.tiger.data.database.AppDatabase
import com.studyplan.tiger.data.dao.CompletionStats
import com.studyplan.tiger.data.entity.PlanRecord
import com.studyplan.tiger.repository.StudyPlanRepository
import com.studyplan.tiger.utils.ExcelExporter
import kotlinx.coroutines.launch

/**
 * 历史记录ViewModel
 */
class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: StudyPlanRepository
    private val excelExporter: ExcelExporter
    
    // 历史记录
    val historyRecords: LiveData<List<PlanRecord>>
    
    // 完成统计
    private val _completionStats = MutableLiveData<CompletionStats>()
    val completionStats: LiveData<CompletionStats> = _completionStats
    
    init {
        val database = AppDatabase.getDatabase(application)
        repository = StudyPlanRepository(database.studyPlanDao(), database.planRecordDao())
        excelExporter = ExcelExporter(application)
        
        historyRecords = repository.getRecentRecords()
        
        // 获取统计数据
        loadCompletionStats()
    }
    
    /**
     * 加载完成统计
     */
    private fun loadCompletionStats() {
        viewModelScope.launch {
            try {
                val stats = repository.planRecordDao.getCompletionStats()
                _completionStats.value = stats
            } catch (e: Exception) {
                _completionStats.value = CompletionStats(0, 0)
            }
        }
    }
    
    /**
     * 导出记录
     */
    fun exportRecords(callback: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val records = repository.getRecentRecordsList()
                val filePath = excelExporter.exportRecords(records)
                callback(true, filePath)
            } catch (e: Exception) {
                e.printStackTrace()
                callback(false, null)
            }
        }
    }
    
    /**
     * 清理过期记录
     */
    fun cleanOldRecords() {
        viewModelScope.launch {
            repository.deleteOldRecords()
        }
    }
}