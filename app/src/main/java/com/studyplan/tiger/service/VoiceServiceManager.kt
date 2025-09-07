package com.studyplan.tiger.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.*
import java.util.*

/**
 * 语音服务管理器
 * 整合语音识别和TTS功能
 */
class VoiceServiceManager private constructor(private val context: Context) {
    
    companion object {
        @Volatile
        private var INSTANCE: VoiceServiceManager? = null
        
        fun getInstance(context: Context): VoiceServiceManager {
            return INSTANCE ?: synchronized(this) {
                val instance = VoiceServiceManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
    
    // TTS引擎
    private var textToSpeech: TextToSpeech? = null
    private var isTTSInitialized = false
    
    // 语音识别器
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    
    // 回调接口
    interface VoiceCallback {
        fun onSpeechResult(result: String)
        fun onSpeechError(error: String)
        fun onTTSCompleted()
        fun onTTSError()
    }
    
    private var voiceCallback: VoiceCallback? = null
    
    /**
     * 初始化语音服务
     */
    fun initialize(callback: VoiceCallback?) {
        this.voiceCallback = callback
        initializeTTS()
        initializeSpeechRecognizer()
    }
    
    /**
     * 初始化TTS
     */
    private fun initializeTTS() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech?.setLanguage(Locale.CHINESE)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    // 如果不支持中文，使用默认语言
                    textToSpeech?.setLanguage(Locale.getDefault())
                }
                
                // 设置语音参数
                textToSpeech?.setSpeechRate(0.9f) // 语速稍慢一点，适合孩子
                textToSpeech?.setPitch(1.1f) // 音调稍高一点，更卡通
                
                // 设置回调监听
                textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    
                    override fun onDone(utteranceId: String?) {
                        voiceCallback?.onTTSCompleted()
                    }
                    
                    override fun onError(utteranceId: String?) {
                        voiceCallback?.onTTSError()
                    }
                })
                
                isTTSInitialized = true
            }
        }
    }
    
    /**
     * 初始化语音识别器
     */
    private fun initializeSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    isListening = true
                }
                
                override fun onBeginningOfSpeech() {}
                
                override fun onRmsChanged(rmsdB: Float) {}
                
                override fun onBufferReceived(buffer: ByteArray?) {}
                
                override fun onEndOfSpeech() {
                    isListening = false
                }
                
                override fun onError(error: Int) {
                    isListening = false
                    val errorMessage = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "录音错误"
                        SpeechRecognizer.ERROR_CLIENT -> "客户端错误"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "权限不足"
                        SpeechRecognizer.ERROR_NETWORK -> "网络错误"
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "网络超时"
                        SpeechRecognizer.ERROR_NO_MATCH -> "没有匹配的结果"
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "识别器忙碌"
                        SpeechRecognizer.ERROR_SERVER -> "服务器错误"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "语音超时"
                        else -> "未知错误"
                    }
                    voiceCallback?.onSpeechError(errorMessage)
                }
                
                override fun onResults(results: Bundle?) {
                    isListening = false
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val result = matches[0]
                        voiceCallback?.onSpeechResult(result)
                    } else {
                        voiceCallback?.onSpeechError("没有识别到语音内容")
                    }
                }
                
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }
    }
    
    /**
     * 开始语音识别
     */
    fun startListening() {
        if (!isListening && speechRecognizer != null) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "请说话...")
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 3000)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 3000)
            }
            speechRecognizer?.startListening(intent)
        }
    }
    
    /**
     * 停止语音识别
     */
    fun stopListening() {
        if (isListening) {
            speechRecognizer?.stopListening()
            isListening = false
        }
    }
    
    /**
     * 语音播报
     */
    fun speak(text: String, utteranceId: String = UUID.randomUUID().toString()) {
        if (isTTSInitialized && textToSpeech != null) {
            val params = Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
            }
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
        }
    }
    
    /**
     * 停止语音播报
     */
    fun stopSpeaking() {
        textToSpeech?.stop()
    }
    
    /**
     * 检查是否正在播报
     */
    fun isSpeaking(): Boolean {
        return textToSpeech?.isSpeaking == true
    }
    
    /**
     * 检查是否正在监听
     */
    fun isListening(): Boolean {
        return isListening
    }
    
    /**
     * 释放资源
     */
    fun release() {
        speechRecognizer?.destroy()
        speechRecognizer = null
        
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        
        voiceCallback = null
        isListening = false
        isTTSInitialized = false
    }
    
    /**
     * 设置回调
     */
    fun setCallback(callback: VoiceCallback?) {
        this.voiceCallback = callback
    }
}