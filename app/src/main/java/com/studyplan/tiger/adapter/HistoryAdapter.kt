package com.studyplan.tiger.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.studyplan.tiger.R
import com.studyplan.tiger.data.entity.PlanRecord
import com.studyplan.tiger.databinding.ItemHistoryDateBinding
import java.text.SimpleDateFormat
import java.util.*

/**
 * 历史记录日期分组适配器
 */
class HistoryDateAdapter : ListAdapter<HistoryDateAdapter.DateGroup, HistoryDateAdapter.DateViewHolder>(DateGroupDiffCallback()) {
    
    /**
     * 日期分组数据类
     */
    data class DateGroup(
        val date: Date,
        val records: List<PlanRecord>
    ) {
        val completedCount: Int
            get() = records.count { it.isCompleted }
        
        val totalCount: Int
            get() = records.size
        
        val completionRate: Float
            get() = if (totalCount == 0) 0f else completedCount.toFloat() / totalCount.toFloat()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateViewHolder {
        val binding = ItemHistoryDateBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DateViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: DateViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class DateViewHolder(private val binding: ItemHistoryDateBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        private val planAdapter = HistoryPlanAdapter()
        
        init {
            binding.dayPlansList.apply {
                adapter = planAdapter
                layoutManager = LinearLayoutManager(binding.root.context)
            }
        }
        
        fun bind(dateGroup: DateGroup) {
            with(binding) {
                // 设置日期
                val dateFormat = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())
                dateText.text = dateFormat.format(dateGroup.date)
                
                // 设置完成统计
                dayCompletionText.text = "${dateGroup.completedCount}/${dateGroup.totalCount}完成"
                
                // 设置计划列表
                planAdapter.submitList(dateGroup.records)
            }
        }
    }
    
    /**
     * DiffUtil回调
     */
    private class DateGroupDiffCallback : DiffUtil.ItemCallback<DateGroup>() {
        override fun areItemsTheSame(oldItem: DateGroup, newItem: DateGroup): Boolean {
            return oldItem.date == newItem.date
        }
        
        override fun areContentsTheSame(oldItem: DateGroup, newItem: DateGroup): Boolean {
            return oldItem == newItem
        }
    }
}

/**
 * 历史记录计划适配器
 */
class HistoryPlanAdapter : ListAdapter<PlanRecord, HistoryPlanAdapter.PlanViewHolder>(PlanRecordDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanViewHolder {
        val binding = com.studyplan.tiger.databinding.ItemHistoryPlanBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PlanViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: PlanViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class PlanViewHolder(private val binding: com.studyplan.tiger.databinding.ItemHistoryPlanBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(record: PlanRecord) {
            with(binding) {
                // 设置任务名称
                taskNameText.text = record.taskName
                
                // 设置时间
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                timeText.text = "${timeFormat.format(record.startTime)} - ${timeFormat.format(record.endTime)}"
                
                // 设置完成状态
                val context = binding.root.context
                if (record.isCompleted) {
                    statusIcon.setImageResource(R.drawable.ic_check_circle)
                    statusIcon.setColorFilter(ContextCompat.getColor(context, R.color.completed_color))
                    statusText.text = context.getString(R.string.completed)
                    statusText.setTextColor(ContextCompat.getColor(context, R.color.completed_color))
                } else {
                    statusIcon.setImageResource(R.drawable.ic_cancel_circle)
                    statusIcon.setColorFilter(ContextCompat.getColor(context, R.color.not_completed_color))
                    statusText.text = context.getString(R.string.not_completed)
                    statusText.setTextColor(ContextCompat.getColor(context, R.color.not_completed_color))
                }
            }
        }
    }
    
    /**
     * DiffUtil回调
     */
    private class PlanRecordDiffCallback : DiffUtil.ItemCallback<PlanRecord>() {
        override fun areItemsTheSame(oldItem: PlanRecord, newItem: PlanRecord): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: PlanRecord, newItem: PlanRecord): Boolean {
            return oldItem == newItem
        }
    }
}