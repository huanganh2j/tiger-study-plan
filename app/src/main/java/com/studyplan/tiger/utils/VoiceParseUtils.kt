package com.studyplan.tiger.utils

import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

/**
 * 语音识别文本解析工具
 */
object VoiceParseUtils {
    
    // 时间格式正则表达式
    private val timePattern = Pattern.compile("(上午|下午|早上|晚上)?(\\d{1,2})点(\\d{1,2}分?|半)?")
    private val rangePattern = Pattern.compile("(.+)[，,]\\s*(上午|下午|早上|晚上)?(\\d{1,2})点(\\d{1,2}分?|半)?到(上午|下午|早上|晚上)?(\\d{1,2})点(\\d{1,2}分?|半)?")
    
    /**
     * 计划解析结果
     */
    data class PlanParseResult(
        val taskName: String? = null,
        val startTime: Date? = null,
        val endTime: Date? = null,
        val isValid: Boolean = false,
        val error: String? = null,
        val needAmPmConfirm: Boolean = false,
        val ambiguousTime: String? = null
    )
    
    /**
     * 解析语音识别结果为计划信息
     * 格式：事项名称，开始时间到结束时间
     * 例如：口算，下午4点45到下午5点
     */
    fun parsePlanFromSpeech(speech: String): PlanParseResult {
        try {
            val matcher = rangePattern.matcher(speech.trim())
            
            if (matcher.find()) {
                val taskName = matcher.group(1)?.trim()
                val startPeriod = matcher.group(2) // 开始时间的上午/下午
                val startHour = matcher.group(3)?.toIntOrNull()
                val startMinuteStr = matcher.group(4)
                val endPeriod = matcher.group(5) // 结束时间的上午/下午
                val endHour = matcher.group(6)?.toIntOrNull()
                val endMinuteStr = matcher.group(7)
                
                if (taskName.isNullOrBlank() || startHour == null || endHour == null) {
                    return PlanParseResult(error = "无法识别完整的计划信息，请按格式说：事项名称，开始时间到结束时间")
                }
                
                // 解析分钟
                val startMinute = parseMinute(startMinuteStr)
                val endMinute = parseMinute(endMinuteStr)
                
                // 检查是否需要确认上午下午
                if (startPeriod.isNullOrBlank() || endPeriod.isNullOrBlank()) {
                    return PlanParseResult(
                        taskName = taskName,
                        needAmPmConfirm = true,
                        ambiguousTime = speech,
                        error = "请确认是上午还是下午？"
                    )
                }
                
                // 创建今天的时间
                val today = Calendar.getInstance()
                val startTime = createTimeToday(startHour, startMinute, startPeriod)
                val endTime = createTimeToday(endHour, endMinute, endPeriod)
                
                // 验证时间逻辑
                if (startTime.after(endTime)) {
                    return PlanParseResult(
                        taskName = taskName,
                        error = "结束时间不能早于开始时间哦，请重新说一遍"
                    )
                }
                
                return PlanParseResult(
                    taskName = taskName,
                    startTime = startTime,
                    endTime = endTime,
                    isValid = true
                )
                
            } else {
                return PlanParseResult(error = "格式不正确，请说：事项名称，开始时间到结束时间")
            }
            
        } catch (e: Exception) {
            return PlanParseResult(error = "解析失败：${e.message}")
        }
    }
    
    /**
     * 解析修改时间的语音
     * 格式：开始时间下午4点，结束时间下午5点
     */
    fun parseTimeModification(speech: String): PlanParseResult {
        try {
            val startPattern = Pattern.compile("开始时间(上午|下午|早上|晚上)?(\\d{1,2})点(\\d{1,2}分?|半)?")
            val endPattern = Pattern.compile("结束时间(上午|下午|早上|晚上)?(\\d{1,2})点(\\d{1,2}分?|半)?")
            
            val startMatcher = startPattern.matcher(speech)
            val endMatcher = endPattern.matcher(speech)
            
            if (!startMatcher.find() || !endMatcher.find()) {
                return PlanParseResult(error = "请按格式说：开始时间X点X分，结束时间X点X分")
            }
            
            val startPeriod = startMatcher.group(1)
            val startHour = startMatcher.group(2)?.toIntOrNull()
            val startMinuteStr = startMatcher.group(3)
            
            val endPeriod = endMatcher.group(1)
            val endHour = endMatcher.group(2)?.toIntOrNull()
            val endMinuteStr = endMatcher.group(3)
            
            if (startHour == null || endHour == null) {
                return PlanParseResult(error = "无法识别时间，请重新说一遍")
            }
            
            // 检查是否需要确认上午下午
            if (startPeriod.isNullOrBlank() || endPeriod.isNullOrBlank()) {
                return PlanParseResult(
                    needAmPmConfirm = true,
                    ambiguousTime = speech,
                    error = "请确认是上午还是下午？"
                )
            }
            
            val startMinute = parseMinute(startMinuteStr)
            val endMinute = parseMinute(endMinuteStr)
            
            val startTime = createTimeToday(startHour, startMinute, startPeriod)
            val endTime = createTimeToday(endHour, endMinute, endPeriod)
            
            if (startTime.after(endTime)) {
                return PlanParseResult(error = "结束时间不能早于开始时间哦，请重新说一遍")
            }
            
            return PlanParseResult(
                startTime = startTime,
                endTime = endTime,
                isValid = true
            )
            
        } catch (e: Exception) {
            return PlanParseResult(error = "解析失败：${e.message}")
        }
    }
    
    /**
     * 解析分钟
     */
    private fun parseMinute(minuteStr: String?): Int {
        return when {
            minuteStr.isNullOrBlank() -> 0
            minuteStr == "半" -> 30
            minuteStr.endsWith("分") -> {
                minuteStr.substring(0, minuteStr.length - 1).toIntOrNull() ?: 0
            }
            else -> minuteStr.toIntOrNull() ?: 0
        }
    }
    
    /**
     * 创建今天的指定时间
     */
    private fun createTimeToday(hour: Int, minute: Int, period: String): Date {
        val calendar = Calendar.getInstance()
        
        val hour24 = when {
            period in listOf("上午", "早上") -> {
                if (hour == 12) 0 else hour
            }
            period in listOf("下午", "晚上") -> {
                if (hour == 12) 12 else hour + 12
            }
            else -> hour
        }
        
        calendar.set(Calendar.HOUR_OF_DAY, hour24)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        return calendar.time
    }
    
    /**
     * 解析重复类型
     */
    fun parseRepeatType(speech: String): Int {
        return when {
            speech.contains("当天") || speech.contains("今天") -> 0
            speech.contains("学习日") || speech.contains("工作日") -> 1
            speech.contains("每天") || speech.contains("天天") -> 2
            else -> -1 // 无法识别
        }
    }
    
    /**
     * 解析修改类型
     */
    fun parseModifyType(speech: String): String? {
        return when {
            speech.contains("事项") || speech.contains("名称") -> "name"
            speech.contains("时间") -> "time"
            else -> null
        }
    }
    
    /**
     * 解析确认回答
     */
    fun parseConfirmation(speech: String): Boolean? {
        return when {
            speech.contains("是") || speech.contains("对") || speech.contains("确认") || 
            speech.contains("好") || speech.contains("嗯") -> true
            speech.contains("不") || speech.contains("否") || speech.contains("取消") -> false
            else -> null
        }
    }
    
    /**
     * 解析上午下午
     */
    fun parseAmPm(speech: String): String? {
        return when {
            speech.contains("上午") || speech.contains("早上") -> "上午"
            speech.contains("下午") || speech.contains("晚上") -> "下午"
            else -> null
        }
    }
    
    /**
     * 格式化时间显示
     */
    fun formatTime(date: Date): String {
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        return format.format(date)
    }
    
    /**
     * 格式化时间范围显示
     */
    fun formatTimeRange(startTime: Date, endTime: Date): String {
        return "${formatTime(startTime)}-${formatTime(endTime)}"
    }
    
    /**
     * 获取时间的中文描述
     */
    fun getTimeDescription(date: Date): String {
        val calendar = Calendar.getInstance()
        calendar.time = date
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        
        val period = if (hour < 12) "上午" else "下午"
        val hour12 = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
        
        return if (minute == 0) {
            "${period}${hour12}点"
        } else {
            "${period}${hour12}点${minute}分"
        }
    }
}