package com.example.safedriveai.ml

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IncidentClassifier @Inject constructor(context: Context) {

    private var interpreter: Interpreter? = null

    init {
        try {
            interpreter = Interpreter(loadModelFile(context, "safedrive_v4.tflite"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadModelFile(context: Context, modelPath: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    /**
     * Clasifica la telemetría actual.
     * Retorna: 0 (Normal), 1 (Susto/Agresiva), 2 (Choque/Accidente)
     */
    fun classify(gForce: Float, jerk: Float, audio: Float, angular: Float, speed: Float): Int {
        val input = arrayOf(floatArrayOf(gForce, jerk, audio, angular, speed))
        val output = Array(1) { FloatArray(3) }

        interpreter?.run(input, output)

        // Devolvemos el índice con mayor probabilidad
        return output[0].indices.maxByOrNull { output[0][it] } ?: 0
    }

    fun close() {
        interpreter?.close()
        interpreter = null
    }
}
