package com.studyplan.tiger

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.studyplan.tiger.adapter.PlanListAdapter
import com.studyplan.tiger.data.entity.StudyPlan
import com.studyplan.tiger.databinding.ActivityMainBinding
import com.studyplan.tiger.service.VoiceServiceManager
import com.studyplan.tiger.utils.VoiceParseUtils
import com.studyplan.tiger.utils.PermissionUtils
import com.studyplan.tiger.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * 主Activity
 */
class MainActivity : AppCompatActivity(), VoiceServiceManager.VoiceCallback {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var voiceManager: VoiceServiceManager
    private lateinit var planAdapter: PlanListAdapter
    
    // 语音交互状态
    private enum class VoiceState {
        IDLE,           // 空闲
        ADDING_PLAN,    // 添加计划
        CONFIRMING_AM_PM, // 确认上午下午
        ASKING_REPEAT,  // 询问重复设置
        MODIFYING_PLAN, // 修改计划
        CONFIRMING_DELETE, // 确认删除
        CONFIRMING_COMPLETION, // 确认完成状态
        MODIFYING_NAME, // 修改事项名称
        MODIFYING_TIME  // 修改时间
    }
    
    private var currentVoiceState = VoiceState.IDLE
    private var pendingPlanData: VoiceParseUtils.PlanParseResult? = null
    private var selectedPlan: StudyPlan? = null
    
    // 权限请求
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            initializeVoiceService()
        } else {
            // 显示权限说明
            showPermissionExplanation()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupViewModel()
        setupUI()
        checkPermissions()
    }
    
    /**
     * 设置ViewModel
     */
    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        
        // 观察今日计划
        viewModel.todayPlans.observe(this) { plans ->
            planAdapter.submitList(plans)
            updateWelcomeText(plans.size)
            updateEmptyState(plans.isEmpty())
        }
    }
    
    /**
     * 设置UI
     */
    private fun setupUI() {
        setSupportActionBar(binding.toolbar)
        
        // 设置当前时间
        updateCurrentTime()
        
        // 设置计划列表
        planAdapter = PlanListAdapter(
            onItemClick = { plan -> onPlanItemClick(plan) },
            onItemLongClick = { plan -> onPlanItemLongClick(plan) }
        )
        
        binding.plansList.apply {
            adapter = planAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
        
        // 设置按钮点击事件
        binding.voiceAddButton.setOnClickListener {
            startVoiceAddPlan()
        }
        
        binding.menuButton.setOnClickListener {
            // 显示菜单（历史记录、设置等）
            startActivity(Intent(this, HistoryActivity::class.java))
        }
        
        binding.stopVoiceButton.setOnClickListener {
            stopVoiceRecognition()
        }
    }
    
    /**
     * 检查权限
     */
    private fun checkPermissions() {
        val permissions = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        
        val needRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        
        if (needRequest.isNotEmpty()) {
            permissionLauncher.launch(needRequest.toTypedArray())
        } else {
            initializeVoiceService()
        }
    }
    
    /**
     * 初始化语音服务
     */
    private fun initializeVoiceService() {
        voiceManager = VoiceServiceManager.getInstance(this)
        voiceManager.initialize(this)
        
        // 检查并引导权限设置
        checkAndGuidePermissions()
        
        // 播放欢迎语
        val planCount = viewModel.todayPlans.value?.size ?: 0
        playWelcomeMessage(planCount)
    }
    
    /**
     * 检查并引导权限设置
     */
    private fun checkAndGuidePermissions() {
        if (PermissionUtils.isHuaweiDevice() && !PermissionUtils.checkAllPermissions(this)) {
            // 延迟2秒显示，等待界面完成初始化
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                PermissionUtils.showPermissionGuideDialog(this) {
                    // 权限设置完成后的回调
                    voiceManager.speak("权限设置完成，现在可以正常使用了！")
                }
            }, 2000)
        }
    }
    
    /**
     * 播放欢迎消息
     */
    private fun playWelcomeMessage(planCount: Int) {
        val message = if (planCount > 0) {
            getString(R.string.voice_welcome_with_plans, planCount)
        } else {
            getString(R.string.voice_welcome_no_plans)
        }
        voiceManager.speak(message)
    }
    
    /**
     * 开始语音添加计划
     */
    private fun startVoiceAddPlan() {
        currentVoiceState = VoiceState.ADDING_PLAN
        showVoiceStatus("请说：事项名称，开始时间到结束时间")
        voiceManager.speak("请说出计划内容，比如：口算，下午4点45到下午5点")
    }
    
    /**
     * 显示语音状态
     */
    private fun showVoiceStatus(text: String) {
        binding.voiceStatusCard.visibility = View.VISIBLE
        binding.voiceStatusText.text = text
        voiceManager.startListening()
    }
    
    /**
     * 隐藏语音状态
     */
    private fun hideVoiceStatus() {
        binding.voiceStatusCard.visibility = View.GONE
        currentVoiceState = VoiceState.IDLE
        pendingPlanData = null
        voiceManager.stopListening()
    }
    
    /**
     * 停止语音识别
     */
    private fun stopVoiceRecognition() {
        hideVoiceStatus()
        voiceManager.speak("已取消语音输入")
    }
    
    /**
     * 计划项点击事件
     */
    private fun onPlanItemClick(plan: StudyPlan) {
        selectedPlan = plan
        planAdapter.setSelectedPlan(plan)
    }
    
    /**
     * 计划项长按事件
     */
    private fun onPlanItemLongClick(plan: StudyPlan) {
        selectedPlan = plan
        planAdapter.setSelectedPlan(plan)
        
        // 开始语音操作
        currentVoiceState = VoiceState.MODIFYING_PLAN
        showVoiceStatus("请说：修改 或 删除")
        voiceManager.speak("请说修改或删除")
    }
    
    /**
     * 更新欢迎文本
     */
    private fun updateWelcomeText(planCount: Int) {
        val text = if (planCount > 0) {
            "小朋友，今天有${planCount}个学习计划哦！"
        } else {
            "今日还没有计划呢，要添加一个吗？"
        }
        binding.welcomeText.text = text
    }
    
    /**
     * 更新空状态显示
     */
    private fun updateEmptyState(isEmpty: Boolean) {
        binding.emptyStateLayout.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }
    
    /**
     * 更新当前时间
     */
    private fun updateCurrentTime() {
        val format = SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.getDefault())
        binding.currentTime.text = format.format(Date())
    }
    
    /**
     * 显示权限说明
     */
    private fun showPermissionExplanation() {
        // TODO: 显示权限说明对话框
        voiceManager = VoiceServiceManager.getInstance(this)
        voiceManager.speak("需要录音权限才能使用语音功能哦")
    }
    
    // ===== VoiceServiceManager.VoiceCallback 实现 =====
    
    override fun onSpeechResult(result: String) {
        handleSpeechResult(result)
    }
    
    override fun onSpeechError(error: String) {
        voiceManager.speak(getString(R.string.voice_not_understand))
        
        // 继续监听（除非用户主动取消）
        if (currentVoiceState != VoiceState.IDLE) {
            voiceManager.startListening()
        }
    }
    
    override fun onTTSCompleted() {
        // TTS完成后继续监听
        if (currentVoiceState != VoiceState.IDLE) {
            voiceManager.startListening()
        }
    }
    
    override fun onTTSError() {
        // TTS错误，继续监听
        if (currentVoiceState != VoiceState.IDLE) {
            voiceManager.startListening()
        }
    }
    
    /**
     * 处理语音识别结果
     */
    private fun handleSpeechResult(result: String) {
        when (currentVoiceState) {
            VoiceState.ADDING_PLAN -> handleAddPlanSpeech(result)
            VoiceState.CONFIRMING_AM_PM -> handleAmPmConfirmation(result)
            VoiceState.ASKING_REPEAT -> handleRepeatSelection(result)
            VoiceState.MODIFYING_PLAN -> handleModifyPlanSpeech(result)
            VoiceState.CONFIRMING_DELETE -> handleDeleteConfirmation(result)
            VoiceState.CONFIRMING_COMPLETION -> handleCompletionConfirmation(result)
            VoiceState.MODIFYING_NAME -> handleNameModification(result)
            VoiceState.MODIFYING_TIME -> handleTimeModification(result)
            else -> { /* 忽略 */ }
        }
    }
    
    /**
     * 处理添加计划的语音
     */
    private fun handleAddPlanSpeech(speech: String) {
        val parseResult = VoiceParseUtils.parsePlanFromSpeech(speech)
        
        when {
            parseResult.needAmPmConfirm -> {
                pendingPlanData = parseResult
                currentVoiceState = VoiceState.CONFIRMING_AM_PM
                showVoiceStatus("请确认是上午还是下午？")
                voiceManager.speak(parseResult.error ?: "请确认是上午还是下午？")
            }
            
            parseResult.isValid -> {
                pendingPlanData = parseResult
                currentVoiceState = VoiceState.ASKING_REPEAT
                showVoiceStatus("请选择：当天、学习日、每天")
                voiceManager.speak(getString(R.string.voice_ask_repeat))
            }
            
            else -> {
                voiceManager.speak(parseResult.error ?: "格式不正确，请重新说一遍")
                showVoiceStatus("请说：事项名称，开始时间到结束时间")
            }
        }
    }
    
    /**
     * 处理上午下午确认
     */
    private fun handleAmPmConfirmation(speech: String) {
        val amPm = VoiceParseUtils.parseAmPm(speech)
        if (amPm != null) {
            // 重新解析，补充上午下午信息
            pendingPlanData?.ambiguousTime?.let { originalSpeech ->
                val newSpeech = originalSpeech.replace(Regex("(\\d{1,2}点\\d{0,2}分?)"), "$amPm$1")
                handleAddPlanSpeech(newSpeech)
            }
        } else {
            voiceManager.speak("没有听清楚，请说上午或下午")
        }
    }
    
    /**
     * 处理重复类型选择
     */
    private fun handleRepeatSelection(speech: String) {
        val repeatType = VoiceParseUtils.parseRepeatType(speech)
        
        if (repeatType >= 0) {
            pendingPlanData?.let { data ->
                if (data.isValid && data.taskName != null && data.startTime != null && data.endTime != null) {
                    // 检查时间冲突
                    viewModel.checkTimeConflict(
                        data.startTime!!,
                        data.endTime!!,
                        onConflict = { conflictPlan ->
                            val message = getString(R.string.voice_time_conflict, conflictPlan.taskName)
                            voiceManager.speak(message)
                            hideVoiceStatus()
                        },
                        onNoConflict = {
                            // 创建计划
                            val plan = StudyPlan(
                                taskName = data.taskName!!,
                                startTime = data.startTime!!,
                                endTime = data.endTime!!,
                                repeatType = repeatType
                            )
                            
                            viewModel.addPlan(plan)
                            voiceManager.speak("计划添加成功！")
                            hideVoiceStatus()
                        }
                    )
                }
            }
        } else {
            voiceManager.speak("请选择：当天、学习日或每天")
        }
    }
    
    /**
     * 处理修改计划语音
     */
    private fun handleModifyPlanSpeech(speech: String) {
        when {
            speech.contains("修改") -> {
                currentVoiceState = VoiceState.ASKING_REPEAT // 复用状态，询问修改类型
                showVoiceStatus("请说：修改事项名称 或 修改时间")
                voiceManager.speak(getString(R.string.voice_ask_modify_type))
            }
            
            speech.contains("删除") -> {
                selectedPlan?.let { plan ->
                    currentVoiceState = VoiceState.CONFIRMING_DELETE
                    showVoiceStatus("确认删除吗？请说：是 或 否")
                    voiceManager.speak(getString(R.string.voice_confirm_delete, plan.taskName))
                }
            }
            
            speech.contains("事项") || speech.contains("名称") -> {
                currentVoiceState = VoiceState.MODIFYING_NAME
                showVoiceStatus("请说出新的事项名称")
                voiceManager.speak("请说出新的事项名称")
            }
            
            speech.contains("时间") -> {
                currentVoiceState = VoiceState.MODIFYING_TIME
                showVoiceStatus("请说：开始时间X点，结束时间X点")
                voiceManager.speak("请按格式说：开始时间下午四点，结束时间下午五点")
            }
            
            else -> {
                voiceManager.speak("请说修改或删除")
            }
        }
    }
    
    /**
     * 处理删除确认
     */
    private fun handleDeleteConfirmation(speech: String) {
        val confirmation = VoiceParseUtils.parseConfirmation(speech)
        
        when (confirmation) {
            true -> {
                selectedPlan?.let { plan ->
                    viewModel.deletePlan(plan)
                    voiceManager.speak("计划已删除")
                }
                hideVoiceStatus()
                planAdapter.setSelectedPlan(null)
            }
            
            false -> {
                voiceManager.speak("已取消删除")
                hideVoiceStatus()
            }
            
            null -> {
                voiceManager.speak("请说是或否")
            }
        }
    }
    
    /**
     * 处理完成状态确认
     */
    private fun handleCompletionConfirmation(speech: String) {
        val confirmation = VoiceParseUtils.parseConfirmation(speech)
        
        when (confirmation) {
            true -> {
                selectedPlan?.let { plan ->
                    viewModel.markPlanCompleted(plan.id, true)
                    voiceManager.speak("太棒了！继续加油哦！")
                    // 播报下一个计划
                    announceNextPlan()
                }
                hideVoiceStatus()
                planAdapter.setSelectedPlan(null)
            }
            
            false -> {
                selectedPlan?.let { plan ->
                    viewModel.markPlanCompleted(plan.id, false)
                    voiceManager.speak("没关系，下次继续努力！")
                    // 播报下一个计划
                    announceNextPlan()
                }
                hideVoiceStatus()
                planAdapter.setSelectedPlan(null)
            }
            
            null -> {
                voiceManager.speak("请说完成了或没完成")
            }
        }
    }
    
    /**
     * 处理事项名称修改
     */
    private fun handleNameModification(speech: String) {
        if (speech.isNotBlank()) {
            selectedPlan?.let { plan ->
                val updatedPlan = plan.copy(taskName = speech.trim())
                viewModel.updatePlan(updatedPlan)
                voiceManager.speak("事项名称修改成功")
            }
            hideVoiceStatus()
            planAdapter.setSelectedPlan(null)
        } else {
            voiceManager.speak("请说出新的事项名称")
        }
    }
    
    /**
     * 处理时间修改
     */
    private fun handleTimeModification(speech: String) {
        val parseResult = VoiceParseUtils.parseTimeModification(speech)
        
        when {
            parseResult.needAmPmConfirm -> {
                pendingPlanData = parseResult
                currentVoiceState = VoiceState.CONFIRMING_AM_PM
                showVoiceStatus("请确认是上午还是下午？")
                voiceManager.speak(parseResult.error ?: "请确认是上午还是下午？")
            }
            
            parseResult.isValid -> {
                selectedPlan?.let { plan ->
                    // 检查时间冲突
                    viewModel.checkTimeConflict(
                        parseResult.startTime!!,
                        parseResult.endTime!!,
                        plan.id,
                        onConflict = { conflictPlan ->
                            val message = getString(R.string.voice_time_conflict, conflictPlan.taskName)
                            voiceManager.speak(message)
                            hideVoiceStatus()
                        },
                        onNoConflict = {
                            val updatedPlan = plan.copy(
                                startTime = parseResult.startTime!!,
                                endTime = parseResult.endTime!!
                            )
                            viewModel.updatePlan(updatedPlan)
                            voiceManager.speak("时间修改成功")
                            hideVoiceStatus()
                            planAdapter.setSelectedPlan(null)
                        }
                    )
                }
            }
            
            else -> {
                voiceManager.speak(parseResult.error ?: "格式不正确，请重新说一遍")
                showVoiceStatus("请说：开始时间X点，结束时间X点")
            }
        }
    }
    
    /**
     * 播报下一个计划
     */
    private fun announceNextPlan() {
        lifecycleScope.launch {
            delay(2000) // 等待2秒再播报
            
            val nextPlan = viewModel.getNextPlan()
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
    }
    
    /**
     * 开始完成状态确认
     */
    fun startCompletionConfirmation(plan: StudyPlan) {
        selectedPlan = plan
        planAdapter.setSelectedPlan(plan)
        currentVoiceState = VoiceState.CONFIRMING_COMPLETION
        showVoiceStatus("请说：完成了 或 没完成")
        voiceManager.speak("${plan.taskName}已经到时啦，完成了吗？")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (::voiceManager.isInitialized) {
            voiceManager.release()
        }
    }
}