package com.studyplan.tiger.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.studyplan.tiger.service.ReminderService

/**
 * 闹钟接收器
 * 处理系统闹钟触发的提醒
 */
class AlarmReceiver : BroadcastReceiver() {
    
    companion object {
        const val ACTION_START_REMINDER = "action_start_reminder"
        const val ACTION_END_REMINDER = "action_end_reminder"
        const val EXTRA_PLAN_ID = "plan_id"
        const val EXTRA_TASK_NAME = "task_name"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_START_REMINDER -> {
                handleStartReminder(context, intent)
            }
            ACTION_END_REMINDER -> {
                handleEndReminder(context, intent)
            }
        }
    }
    
    /**
     * 处理开始提醒
     */
    private fun handleStartReminder(context: Context, intent: Intent) {
        val planId = intent.getLongExtra(EXTRA_PLAN_ID, -1)
        val taskName = intent.getStringExtra(EXTRA_TASK_NAME) ?: "学习任务"
        
        if (planId != -1L) {
            // 启动提醒服务来处理
            val serviceIntent = Intent(context, ReminderService::class.java).apply {
                action = ReminderService.ACTION_START_REMINDER
                putExtra(ReminderService.EXTRA_PLAN_ID, planId)
            }
            context.startService(serviceIntent)
        }
    }
    
    /**
     * 处理结束提醒
     */
    private fun handleEndReminder(context: Context, intent: Intent) {
        val planId = intent.getLongExtra(EXTRA_PLAN_ID, -1)
        val taskName = intent.getStringExtra(EXTRA_TASK_NAME) ?: "学习任务"
        
        if (planId != -1L) {
            // 启动提醒服务来处理
            val serviceIntent = Intent(context, ReminderService::class.java).apply {
                action = ReminderService.ACTION_STOP_REMINDER
                putExtra(ReminderService.EXTRA_PLAN_ID, planId)
            }
            context.startService(serviceIntent)
        }
    }
}