package com.studyplan.tiger.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.studyplan.tiger.R
import com.studyplan.tiger.data.entity.StudyPlan
import com.studyplan.tiger.databinding.ItemPlanBinding
import com.studyplan.tiger.utils.VoiceParseUtils
import java.util.*

/**
 * 计划列表适配器
 */
class PlanListAdapter(
    private val onItemClick: (StudyPlan) -> Unit,
    private val onItemLongClick: (StudyPlan) -> Unit
) : ListAdapter<StudyPlan, PlanListAdapter.PlanViewHolder>(PlanDiffCallback()) {
    
    private var selectedPlan: StudyPlan? = null
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanViewHolder {
        val binding = ItemPlanBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PlanViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: PlanViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    /**
     * 设置选中的计划
     */
    fun setSelectedPlan(plan: StudyPlan?) {
        val oldSelected = selectedPlan
        selectedPlan = plan
        
        // 刷新旧的选中项
        oldSelected?.let { old ->
            val oldIndex = currentList.indexOfFirst { it.id == old.id }
            if (oldIndex != -1) {
                notifyItemChanged(oldIndex)
            }
        }
        
        // 刷新新的选中项
        plan?.let { new ->
            val newIndex = currentList.indexOfFirst { it.id == new.id }
            if (newIndex != -1) {
                notifyItemChanged(newIndex)
            }
        }
    }
    
    /**
     * 获取选中的计划
     */
    fun getSelectedPlan(): StudyPlan? = selectedPlan
    
    inner class PlanViewHolder(private val binding: ItemPlanBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(plan: StudyPlan) {
            with(binding) {
                // 设置基本信息
                taskName.text = plan.taskName
                
                // 获取今日的计划时间
                val (todayStart, todayEnd) = plan.getTodayPlanTime()
                timeRange.text = VoiceParseUtils.formatTimeRange(todayStart, todayEnd)
                repeatType.text = plan.getRepeatTypeText()
                
                // 设置状态
                updateStatus(plan, todayStart, todayEnd)
                
                // 设置选中状态
                val isSelected = selectedPlan?.id == plan.id
                selectedOverlay.visibility = if (isSelected) View.VISIBLE else View.GONE
                actionButtonsLayout.visibility = if (isSelected) View.VISIBLE else View.GONE
                
                // 设置进度条
                updateProgress(plan, todayStart, todayEnd)
                
                // 点击事件
                planCard.setOnClickListener {
                    if (isSelected) {
                        setSelectedPlan(null) // 取消选中
                    } else {
                        setSelectedPlan(plan) // 选中当前项
                    }
                    onItemClick(plan)
                }
                
                planCard.setOnLongClickListener {
                    setSelectedPlan(plan)
                    onItemLongClick(plan)
                    true
                }
                
                // 操作按钮事件
                modifyButton.setOnClickListener {
                    onItemClick(plan)
                }
                
                deleteButton.setOnClickListener {
                    onItemClick(plan)
                }
            }
        }
        
        /**
         * 更新状态显示
         */
        private fun updateStatus(plan: StudyPlan, startTime: Date, endTime: Date) {
            val now = Date()
            val context = binding.root.context
            
            val (statusText, statusColor) = when {
                plan.isCompleted -> {
                    Pair(context.getString(R.string.completed), R.color.completed_color)
                }
                now.before(startTime) -> {
                    Pair("待开始", R.color.gray_500)
                }
                now.after(endTime) -> {
                    Pair(context.getString(R.string.not_completed), R.color.not_completed_color)
                }
                else -> {
                    Pair(context.getString(R.string.in_progress), R.color.in_progress_color)
                }
            }
            
            binding.statusText.text = statusText
            binding.statusText.setBackgroundColor(ContextCompat.getColor(context, statusColor))
        }
        
        /**
         * 更新进度条
         */
        private fun updateProgress(plan: StudyPlan, startTime: Date, endTime: Date) {
            val now = Date()
            
            if (now.after(startTime) && now.before(endTime) && !plan.isCompleted) {
                // 正在进行中，显示进度
                val totalDuration = endTime.time - startTime.time
                val currentDuration = now.time - startTime.time
                val progress = ((currentDuration.toFloat() / totalDuration.toFloat()) * 100).toInt()
                
                binding.progressBar.visibility = View.VISIBLE
                binding.progressBar.progress = progress
            } else {
                binding.progressBar.visibility = View.GONE
            }
        }
    }
    
    /**
     * DiffUtil回调
     */
    private class PlanDiffCallback : DiffUtil.ItemCallback<StudyPlan>() {
        override fun areItemsTheSame(oldItem: StudyPlan, newItem: StudyPlan): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: StudyPlan, newItem: StudyPlan): Boolean {
            return oldItem == newItem
        }
    }
}