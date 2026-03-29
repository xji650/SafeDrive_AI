package com.example.safedriveai.sensors

import android.annotation.SuppressLint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.*

class AudioProvider {
    private val _amplitude = MutableStateFlow(0f)
    val amplitude = _amplitude.asStateFlow()
    private var audioRecord: AudioRecord? = null
    private var job: Job? = null

    @SuppressLint("MissingPermission")
    fun start() {
        val bufferSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
        audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, 44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize)
        audioRecord?.startRecording()

        job = CoroutineScope(Dispatchers.Default).launch {
            val buffer = ShortArray(bufferSize)
            while (isActive) {
                val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                if (read > 0) {
                    val avg = buffer.map { Math.abs(it.toInt()) }.average()
                    _amplitude.value = (avg / 3000.0).toFloat().coerceIn(0f, 1f)
                }
                delay(100)
            }
        }
    }

    fun stop() {
        job?.cancel()
        audioRecord?.apply { stop(); release() }
    }
}