package com.studyplan.tiger

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.studyplan.tiger.adapter.HistoryDateAdapter
import com.studyplan.tiger.data.entity.PlanRecord
import com.studyplan.tiger.databinding.ActivityHistoryBinding
import com.studyplan.tiger.service.VoiceServiceManager
import com.studyplan.tiger.utils.ExcelExporter
import com.studyplan.tiger.viewmodel.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * 历史记录Activity
 */
class HistoryActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityHistoryBinding
    private lateinit var viewModel: HistoryViewModel
    private lateinit var historyAdapter: HistoryDateAdapter
    private lateinit var voiceManager: VoiceServiceManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupViewModel()
        setupUI()
        setupVoiceManager()
    }
    
    /**
     * 设置ViewModel
     */
    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[HistoryViewModel::class.java]
        
        // 观察历史记录
        viewModel.historyRecords.observe(this) { records ->
            updateHistoryList(records)
        }
        
        // 观察统计数据
        viewModel.completionStats.observe(this) { stats ->
            updateStats(stats.total, stats.completed)
        }
    }
    
    /**
     * 设置UI
     */
    private fun setupUI() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        // 设置历史记录列表
        historyAdapter = HistoryDateAdapter()
        binding.historyList.apply {
            adapter = historyAdapter
            layoutManager = LinearLayoutManager(this@HistoryActivity)
        }
        
        // 设置导出按钮
        binding.exportButton.setOnClickListener {
            exportToExcel()
        }
        
        // 设置返回按钮
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }
    
    /**
     * 设置语音管理器
     */
    private fun setupVoiceManager() {
        voiceManager = VoiceServiceManager.getInstance(this)
        voiceManager.initialize(null)
    }
    
    /**
     * 更新历史记录列表
     */
    private fun updateHistoryList(records: List<PlanRecord>) {
        if (records.isEmpty()) {
            binding.emptyStateLayout.visibility = View.VISIBLE
            binding.historyList.visibility = View.GONE
            return
        }
        
        binding.emptyStateLayout.visibility = View.GONE
        binding.historyList.visibility = View.VISIBLE
        
        // 按日期分组
        val dateGroups = groupRecordsByDate(records)
        historyAdapter.submitList(dateGroups)
    }
    
    /**
     * 按日期分组记录
     */
    private fun groupRecordsByDate(records: List<PlanRecord>): List<HistoryDateAdapter.DateGroup> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        return records
            .groupBy { record ->
                // 按日期分组
                val dateString = dateFormat.format(record.planDate)
                dateFormat.parse(dateString) ?: record.planDate
            }
            .map { (date, dayRecords) ->
                HistoryDateAdapter.DateGroup(date, dayRecords.sortedBy { it.startTime })
            }
            .sortedByDescending { it.date }
    }
    
    /**
     * 更新统计数据
     */
    private fun updateStats(total: Int, completed: Int) {
        binding.totalPlansText.text = total.toString()
        binding.completedPlansText.text = completed.toString()
        
        val completionRate = if (total == 0) 0f else (completed.toFloat() / total.toFloat()) * 100
        binding.completionRateText.text = "${completionRate.toInt()}%"
    }
    
    /**
     * 导出到Excel
     */
    private fun exportToExcel() {
        viewModel.exportRecords { success, filePath ->
            if (success) {
                voiceManager.speak(getString(R.string.voice_export_success))
                
                // 可以添加分享功能
                // shareExcelFile(filePath)
            } else {
                voiceManager.speak("导出失败，请稍后重试")
            }
        }
    }
    
    /**
     * 分享Excel文件
     */
    private fun shareExcelFile(filePath: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            putExtra(Intent.EXTRA_STREAM, android.net.Uri.parse(filePath))
            putExtra(Intent.EXTRA_SUBJECT, "学习计划记录")
        }
        startActivity(Intent.createChooser(shareIntent, "分享记录"))
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (::voiceManager.isInitialized) {
            voiceManager.release()
        }
    }
}