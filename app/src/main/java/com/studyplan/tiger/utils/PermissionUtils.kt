package com.studyplan.tiger.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import com.studyplan.tiger.R

/**
 * 权限管理工具类
 * 专门处理华为手机的权限设置引导
 */
object PermissionUtils {
    
    /**
     * 检查是否为华为手机
     */
    fun isHuaweiDevice(): Boolean {
        return Build.MANUFACTURER.lowercase().contains("huawei") ||
               Build.BRAND.lowercase().contains("huawei") ||
               Build.BRAND.lowercase().contains("honor")
    }
    
    /**
     * 检查是否忽略了电池优化
     */
    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            return powerManager.isIgnoringBatteryOptimizations(context.packageName)
        }
        return true
    }
    
    /**
     * 显示权限设置引导对话框
     */
    fun showPermissionGuideDialog(activity: Activity, onCompleted: () -> Unit) {
        val message = if (isHuaweiDevice()) {
            buildHuaweiPermissionMessage()
        } else {
            buildGeneralPermissionMessage()
        }
        
        AlertDialog.Builder(activity)
            .setTitle(activity.getString(R.string.permission_guide_title))
            .setMessage(message)
            .setPositiveButton(activity.getString(R.string.btn_ok)) { _, _ ->
                guidePermissionSettings(activity, onCompleted)
            }
            .setNegativeButton(activity.getString(R.string.btn_cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }
    
    /**
     * 构建华为手机权限提示信息
     */
    private fun buildHuaweiPermissionMessage(): String {
        return """
为了确保提醒功能正常工作，请按以下步骤设置：

📱 **后台运行权限**
设置 → 应用 → 应用启动管理 → 找到"小虎学习计划" → 开启"手动管理" → 勾选"允许后台活动"

🔋 **电池优化设置**
设置 → 电池 → 耗电管理 → 找到"小虎学习计划" → 选择"不允许"

🔔 **通知权限**
设置 → 通知 → 找到"小虎学习计划" → 开启所有通知权限

⚠️ **重要提示**
华为/荣耀手机的后台限制较严格，必须完成以上设置才能正常接收提醒！
        """.trimIndent()
    }
    
    /**
     * 构建通用权限提示信息
     */
    private fun buildGeneralPermissionMessage(): String {
        return """
为了确保提醒功能正常工作，请设置：

🔋 忽略电池优化
🔔 允许通知权限
📱 允许后台运行

点击确定前往设置
        """.trimIndent()
    }
    
    /**
     * 引导权限设置
     */
    private fun guidePermissionSettings(activity: Activity, onCompleted: () -> Unit) {
        if (isHuaweiDevice()) {
            showStepByStepGuide(activity, onCompleted)
        } else {
            openAppDetailSettings(activity)
            onCompleted()
        }
    }
    
    /**
     * 显示分步引导
     */
    private fun showStepByStepGuide(activity: Activity, onCompleted: () -> Unit) {
        AlertDialog.Builder(activity)
            .setTitle("第1步：设置后台运行")
            .setMessage("即将打开应用信息页面，请找到电池选项，设置为不限制")
            .setPositiveButton("前往设置") { _, _ ->
                openAppDetailSettings(activity)
                showBatteryOptimizationStep(activity, onCompleted)
            }
            .setNegativeButton("跳过") { _, _ ->
                showBatteryOptimizationStep(activity, onCompleted)
            }
            .setCancelable(false)
            .show()
    }
    
    private fun showBatteryOptimizationStep(activity: Activity, onCompleted: () -> Unit) {
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            AlertDialog.Builder(activity)
                .setTitle("第2步：忽略电池优化")
                .setMessage("即将打开电池优化设置，请选择不优化")
                .setPositiveButton("前往设置") { _, _ ->
                    requestIgnoreBatteryOptimization(activity)
                    showNotificationStep(activity, onCompleted)
                }
                .setNegativeButton("跳过") { _, _ ->
                    showNotificationStep(activity, onCompleted)
                }
                .setCancelable(false)
                .show()
        }, 2000)
    }
    
    private fun showNotificationStep(activity: Activity, onCompleted: () -> Unit) {
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            AlertDialog.Builder(activity)
                .setTitle("第3步：通知权限")
                .setMessage("即将打开通知设置，请开启所有通知权限")
                .setPositiveButton("前往设置") { _, _ ->
                    openNotificationSettings(activity)
                    showCompletionDialog(activity, onCompleted)
                }
                .setNegativeButton("跳过") { _, _ ->
                    showCompletionDialog(activity, onCompleted)
                }
                .setCancelable(false)
                .show()
        }, 2000)
    }
    
    private fun showCompletionDialog(activity: Activity, onCompleted: () -> Unit) {
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            AlertDialog.Builder(activity)
                .setTitle("设置完成")
                .setMessage("权限设置完成！现在可以正常使用提醒功能了。")
                .setPositiveButton("好的") { _, _ ->
                    onCompleted()
                }
                .setCancelable(false)
                .show()
        }, 2000)
    }
    
    /**
     * 打开应用详细设置页面
     */
    fun openAppDetailSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:${context.packageName}")
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val intent = Intent(Settings.ACTION_SETTINGS)
                context.startActivity(intent)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }
    
    /**
     * 请求忽略电池优化
     */
    fun requestIgnoreBatteryOptimization(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                if (!isIgnoringBatteryOptimizations(activity)) {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                    intent.data = Uri.parse("package:${activity.packageName}")
                    activity.startActivity(intent)
                }
            } catch (e: Exception) {
                try {
                    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                    activity.startActivity(intent)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }
    }
    
    /**
     * 打开通知设置页面
     */
    fun openNotificationSettings(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                context.startActivity(intent)
            } else {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.parse("package:${context.packageName}")
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 检查所有必要权限是否已授予
     */
    fun checkAllPermissions(context: Context): Boolean {
        return isIgnoringBatteryOptimizations(context)
    }
}