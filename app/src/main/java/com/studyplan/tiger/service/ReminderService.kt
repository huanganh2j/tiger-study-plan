package com.studyplan.tiger.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.studyplan.tiger.MainActivity
import com.studyplan.tiger.R
import com.studyplan.tiger.data.database.AppDatabase
import com.studyplan.tiger.repository.StudyPlanRepository
import com.studyplan.tiger.data.entity.StudyPlan
import kotlinx.coroutines.*
import java.util.*

/**
 * 定时提醒服务
 * 负责管理所有计划的提醒功能
 */
class ReminderService : LifecycleService() {
    
    companion object {
        const val CHANNEL_ID = "reminder_channel"
        const val NOTIFICATION_ID = 1001
        
        const val ACTION_START_REMINDER = "start_reminder"
        const val ACTION_STOP_REMINDER = "stop_reminder"
        const val ACTION_PLAN_COMPLETED = "plan_completed"
        
        const val EXTRA_PLAN_ID = "plan_id"
        const val EXTRA_IS_COMPLETED = "is_completed"
    }
    
    private lateinit var repository: StudyPlanRepository
    private lateinit var voiceManager: VoiceServiceManager
    private var reminderJob: Job? = null
    
    override fun onCreate() {
        super.onCreate()
        
        // 初始化数据库和仓库
        val database = AppDatabase.getDatabase(this)
        repository = StudyPlanRepository(database.studyPlanDao(), database.planRecordDao())
        
        // 初始化语音管理器
        voiceManager = VoiceServiceManager.getInstance(this)
        voiceManager.initialize(null)
        
        // 创建通知渠道
        createNotificationChannel()
        
        // 启动前台服务
        startForegroundService()
        
        // 开始提醒检查
        startReminderChecker()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        
        intent?.let { handleIntent(it) }
        
        return START_STICKY // 服务被杀死后自动重启
    }
    
    /**
     * 处理Intent
     */
    private fun handleIntent(intent: Intent) {
        when (intent.action) {
            ACTION_START_REMINDER -> {
                startReminderChecker()
            }
            ACTION_STOP_REMINDER -> {
                stopReminderChecker()
            }
            ACTION_PLAN_COMPLETED -> {
                val planId = intent.getLongExtra(EXTRA_PLAN_ID, -1)
                val isCompleted = intent.getBooleanExtra(EXTRA_IS_COMPLETED, false)
                if (planId != -1L) {
                    handlePlanCompletion(planId, isCompleted)
                }
            }
        }
    }
    
    /**
     * 创建通知渠道
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "学习计划提醒",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "用于学习计划的提醒通知"
                enableLights(true)
                enableVibration(true)
                setBypassDnd(true) // 绕过勿扰模式
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * 启动前台服务
     */
    private fun startForegroundService() {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("小虎学习计划")
            .setContentText("正在为您守护学习计划")
            .setSmallIcon(R.drawable.ic_tiger_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        
        startForeground(NOTIFICATION_ID, notification)
    }
    
    /**
     * 开始提醒检查器
     */
    private fun startReminderChecker() {
        reminderJob?.cancel()
        reminderJob = lifecycleScope.launch {
            while (isActive) {
                try {
                    checkAndTriggerReminders()
                    delay(30000) // 每30秒检查一次
                } catch (e: Exception) {
                    e.printStackTrace()
                    delay(60000) // 出错时等待1分钟再重试
                }
            }
        }
    }
    
    /**
     * 停止提醒检查器
     */
    private fun stopReminderChecker() {
        reminderJob?.cancel()
    }
    
    /**
     * 检查并触发提醒
     */
    private suspend fun checkAndTriggerReminders() {
        val now = Date()
        val currentTime = now.time
        
        // 检查需要开始提醒的计划
        val plansNeedStartReminder = repository.getPlansNeedStartReminder()
        for (plan in plansNeedStartReminder) {
            val (todayStart, _) = plan.getTodayPlanTime(now)
            val reminderTime = todayStart.time - 60 * 1000 // 提前1分钟
            
            if (currentTime >= reminderTime && currentTime < todayStart.time) {
                triggerStartReminder(plan)
                repository.updateStartReminderStatus(plan.id, true)
            }
        }
        
        // 检查需要结束提醒的计划
        val plansNeedEndReminder = repository.getPlansNeedEndReminder()
        for (plan in plansNeedEndReminder) {
            val (_, todayEnd) = plan.getTodayPlanTime(now)
            
            if (currentTime >= todayEnd.time) {
                triggerEndReminder(plan)
                repository.updateEndReminderStatus(plan.id, true)
            }
        }
    }
    
    /**
     * 触发开始提醒
     */
    private fun triggerStartReminder(plan: StudyPlan) {
        // 语音提醒
        val message = getString(R.string.voice_start_reminder, plan.taskName)
        voiceManager.speak(message)
        
        // 通知提醒
        showReminderNotification(
            title = "学习提醒",
            content = message,
            planId = plan.id
        )
    }
    
    /**
     * 触发结束提醒
     */
    private fun triggerEndReminder(plan: StudyPlan) {
        // 语音提醒
        val message = getString(R.string.voice_end_reminder, plan.taskName)
        voiceManager.speak(message)
        
        // 通知提醒
        showCompletionNotification(plan)
        
        // 3分钟后自动标记为未完成
        lifecycleScope.launch {
            delay(3 * 60 * 1000) // 3分钟
            
            // 检查是否已经确认完成
            val currentPlan = repository.getPlanById(plan.id)
            if (currentPlan != null && !currentPlan.isCompleted) {
                // 自动标记为未完成
                repository.updatePlanCompletion(plan.id, false, null)
                
                // 播报下一个计划
                announceNextPlan()
            }
        }
    }
    
    /**
     * 显示提醒通知
     */
    private fun showReminderNotification(title: String, content: String, planId: Long) {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, planId.toInt(), notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_tiger_notification)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(planId.toInt() + 2000, notification)
    }
    
    /**
     * 显示完成确认通知
     */
    private fun showCompletionNotification(plan: StudyPlan) {
        val completedIntent = Intent(this, ReminderService::class.java).apply {
            action = ACTION_PLAN_COMPLETED
            putExtra(EXTRA_PLAN_ID, plan.id)
            putExtra(EXTRA_IS_COMPLETED, true)
        }
        val completedPendingIntent = PendingIntent.getService(
            this, plan.id.toInt(), completedIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notCompletedIntent = Intent(this, ReminderService::class.java).apply {
            action = ACTION_PLAN_COMPLETED
            putExtra(EXTRA_PLAN_ID, plan.id)
            putExtra(EXTRA_IS_COMPLETED, false)
        }
        val notCompletedPendingIntent = PendingIntent.getService(
            this, plan.id.toInt() + 1000, notCompletedIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("${plan.taskName}已经到时")
            .setContentText("您完成了吗？")
            .setSmallIcon(R.drawable.ic_tiger_notification)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(R.drawable.ic_check, "完成了", completedPendingIntent)
            .addAction(R.drawable.ic_close, "没完成", notCompletedPendingIntent)
            .setAutoCancel(true)
            .build()
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(plan.id.toInt() + 3000, notification)
    }
    
    /**
     * 处理计划完成确认
     */
    private fun handlePlanCompletion(planId: Long, isCompleted: Boolean) {
        lifecycleScope.launch {
            repository.updatePlanCompletion(planId, isCompleted, if (isCompleted) Date() else null)
            
            // 取消通知
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(planId.toInt() + 3000)
            
            // 语音反馈
            if (isCompleted) {
                voiceManager.speak("太棒了！继续加油哦！")
            } else {
                voiceManager.speak("没关系，下次继续努力！")
            }
            
            // 播报下一个计划
            announceNextPlan()
        }
    }
    
    /**
     * 播报下一个计划
     */
    private suspend fun announceNextPlan() {
        delay(2000) // 等待2秒再播报
        
        val nextPlan = repository.getNextPlan()
        if (nextPlan != null) {
            val (nextStart, _) = nextPlan.getTodayPlanTime()
            val now = Date()
            val minutesUntilNext = ((nextStart.time - now.time) / (1000 * 60)).toInt()
            
            if (minutesUntilNext > 0) {
                val message = getString(R.string.voice_next_plan, nextPlan.taskName, minutesUntilNext)
                voiceManager.speak(message)
            }
        } else {
            // 没有下一个计划了
            voiceManager.speak(getString(R.string.voice_all_done))
        }
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        super.onBind(intent)
        return null
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopReminderChecker()
        voiceManager.release()
    }
}