package com.example.safedriveai.sensors

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// Un "Almacén Global" para que la UI pueda leer el estado fácilmente
object ActivityState {
    private val _currentActivity = MutableStateFlow("UNKNOWN")
    val currentActivity = _currentActivity.asStateFlow()

    fun updateActivity(activity: String) {
        _currentActivity.value = activity
    }
}

class ActivityReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (ActivityRecognitionResult.hasResult(intent)) {
            val result = ActivityRecognitionResult.extractResult(intent)
            val mostProbableActivity = result?.mostProbableActivity

            Log.d("SensorActividad", "Detectado: ${mostProbableActivity?.type} con confianza: ${mostProbableActivity?.confidence}%")

            // Traducimos el código de Google a texto para tu Dashboard
            val activityName = when (mostProbableActivity?.type) {
                DetectedActivity.IN_VEHICLE -> "IN_VEHICLE"
                DetectedActivity.ON_BICYCLE -> "ON_BICYCLE"
                DetectedActivity.ON_FOOT -> "ON_FOOT"
                DetectedActivity.STILL -> "STILL (PARADO)"
                DetectedActivity.WALKING -> "WALKING"
                DetectedActivity.RUNNING -> "RUNNING"
                else -> "UNKNOWN"
            }

            // Actualizamos el estado global (solo si la confianza es mayor al 50%)
            if (mostProbableActivity != null && mostProbableActivity.confidence > 50) {
                ActivityState.updateActivity(activityName)
            }
        }
    }
}