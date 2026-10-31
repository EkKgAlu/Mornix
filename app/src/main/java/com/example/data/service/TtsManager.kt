package com.example.data.service

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class TtsManager(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isInitialized = false

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private val _speechRate = MutableStateFlow(1.0f)
    val speechRate: StateFlow<Float> = _speechRate.asStateFlow()

    private var currentText: String = ""
    private var textChunks: List<String> = emptyList()
    private var currentChunkIndex = 0

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.let { engine ->
                val result = engine.setLanguage(Locale.US)
                if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                    isInitialized = true
                    engine.setSpeechRate(_speechRate.value)
                    setupUtteranceListener()
                }
            }
        }
    }

    private fun setupUtteranceListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _isPlaying.value = true
                _isPaused.value = false
            }

            override fun onDone(utteranceId: String?) {
                currentChunkIndex++
                if (currentChunkIndex < textChunks.size && !_isPaused.value) {
                    speakNextChunk()
                } else {
                    _isPlaying.value = false
                    _isPaused.value = false
                    currentChunkIndex = 0
                }
            }

            override fun onError(utteranceId: String?) {
                _isPlaying.value = false
                _isPaused.value = false
            }
        })
    }

    fun play(text: String) {
        if (!isInitialized || text.isBlank()) return
        currentText = text
        textChunks = text.split(". ").filter { it.isNotBlank() }.map { "$it." }
        currentChunkIndex = 0
        _isPaused.value = false
        speakNextChunk()
    }

    private fun speakNextChunk() {
        if (currentChunkIndex in textChunks.indices) {
            val chunk = textChunks[currentChunkIndex]
            val params = android.os.Bundle()
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "mornix_chunk_$currentChunkIndex")
            tts?.speak(chunk, TextToSpeech.QUEUE_FLUSH, params, "mornix_chunk_$currentChunkIndex")
            _isPlaying.value = true
        }
    }

    fun pause() {
        if (_isPlaying.value) {
            tts?.stop()
            _isPlaying.value = false
            _isPaused.value = true
        }
    }

    fun resume() {
        if (_isPaused.value && currentChunkIndex < textChunks.size) {
            _isPaused.value = false
            speakNextChunk()
        } else if (currentText.isNotBlank()) {
            play(currentText)
        }
    }

    fun stop() {
        tts?.stop()
        _isPlaying.value = false
        _isPaused.value = false
        currentChunkIndex = 0
    }

    fun setSpeechRate(rate: Float) {
        _speechRate.value = rate.coerceIn(0.5f, 2.0f)
        tts?.setSpeechRate(_speechRate.value)
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
