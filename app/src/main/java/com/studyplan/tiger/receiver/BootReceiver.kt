package com.studyplan.tiger.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.studyplan.tiger.service.ReminderService

/**
 * 开机启动接收器
 * 确保应用在开机后自动启动提醒服务
 */
class BootReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_PACKAGE_REPLACED -> {
                startReminderService(context)
            }
        }
    }
    
    /**
     * 启动提醒服务
     */
    private fun startReminderService(context: Context) {
        val serviceIntent = Intent(context, ReminderService::class.java).apply {
            action = ReminderService.ACTION_START_REMINDER
        }
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}