package com.example.safedriveai.sensors

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.log10
import kotlin.math.sqrt

class AudioProvider {

    private val _amplitude = MutableStateFlow(0f)
    val amplitude = _amplitude.asStateFlow()

    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private val isRecording get() = recordingJob?.isActive == true

    private val sampleRate = 44100
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

    @SuppressLint("MissingPermission")
    fun start() {
        if (isRecording) {
            Log.d("AudioProvider", "Already recording")
            return
        }

        if (bufferSize <= 0) {
            Log.e("AudioProvider", "Invalid buffer size: $bufferSize")
            return
        }

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.UNPROCESSED,
            sampleRate,
            channelConfig,
            audioFormat,
            bufferSize
        )

        if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
            Log.w("AudioProvider", "UNPROCESSED not available, using VOICE_RECOGNITION")
            // PARCHE 1: Liberar el hardware antes de crear el nuevo
            audioRecord?.release()

            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.VOICE_RECOGNITION,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )
        }

        if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
            Log.e("AudioProvider", "Failed to initialize AudioRecord")
            audioRecord?.release() // Aseguramos que no quede nada colgado
            audioRecord = null
            return
        }

        audioRecord?.startRecording()
        Log.d("AudioProvider", "Recording started, bufferSize=$bufferSize")

        recordingJob = CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            val buffer = ShortArray(bufferSize)
            while (isActive) {
                // El read() ya bloquea el hilo hasta tener datos, no se necesita delay
                val readSize = audioRecord?.read(buffer, 0, buffer.size) ?: 0

                if (readSize > 0) {
                    var sum = 0.0
                    for (i in 0 until readSize) {
                        val sample = buffer[i].toDouble()
                        sum += sample * sample
                    }
                    val rms = sqrt(sum / readSize)

                    var db = 0.0
                    if (rms > 1.0 && !rms.isInfinite() && !rms.isNaN()) {
                        db = 20.0 * log10(rms) + 10.0
                    }

                    val finalDb = db.coerceIn(0.0, 120.0)

                    // PARCHE 2: Actualizamos directamente. StateFlow es Thread-Safe.
                    // Esto ahorra mucho rendimiento de CPU.
                    _amplitude.value = finalDb.toFloat()

                } else if (readSize < 0) {
                    Log.e("AudioProvider", "AudioRecord read error: $readSize")
                    break
                }

                // PARCHE 3: delay(20) eliminado. El read() actúa como nuestro marcapasos natural.
            }
        }
    }

    fun stop() {
        recordingJob?.cancel()
        recordingJob = null
        audioRecord?.apply {
            stop()
            release()
        }
        audioRecord = null
        _amplitude.value = 0f
        Log.d("AudioProvider", "Recording stopped")
    }
}