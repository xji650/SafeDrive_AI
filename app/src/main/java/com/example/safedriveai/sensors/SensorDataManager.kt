package com.example.safedriveai.sensors

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import com.google.android.gms.location.ActivityRecognition
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.sqrt
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.safedriveai.ui.edr.BlackBoxManager
import kotlinx.coroutines.*

class SensorDataManager(private val context: Context) : SensorEventListener, LocationListener {

    // 1. MOTORES DE HARDWARE
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    // 2. FLUJOS DE DATOS (ACELERÓMETRO)
    private val _accelX = MutableStateFlow(0f)
    val accelX = _accelX.asStateFlow()

    private val _accelY = MutableStateFlow(0f)
    val accelY = _accelY.asStateFlow()

    private val _totalG = MutableStateFlow(1f)
    val totalG = _totalG.asStateFlow()

    // 3. FLUJOS DE DATOS (GPS)
    private val _speed = MutableStateFlow(0f) // Velocidad en km/h
    val speed = _speed.asStateFlow()

    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation = _currentLocation.asStateFlow()

    private val blackBox = BlackBoxManager(context)
    private var lastCrashTime: Long = 0L


    // --- MÉTODOS DE CONTROL FUSIONADOS ---

    @SuppressLint("MissingPermission") // Seguro gracias a tu GatekeeperScreen
    fun startListening() {
        // 1. Encendemos Acelerómetro
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }

        // 2. Encendemos GPS (Actualiza cada 1 seg o cada 1 metro)
        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000L,
                    1f,
                    this
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 3. Encendemos Activity Recognition (El "sobre" para Google)
        try {
            val client = ActivityRecognition.getClient(context)
            val intent = Intent(context, ActivityReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            // Le pedimos a Google que evalúe la actividad cada 3 segundos
            client.requestActivityUpdates(3000L, pendingIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        startAudioMonitoring()
    }

    @SuppressLint("MissingPermission")
    fun stopListening() {
        // 1 y 2. Apagamos Acelerómetro y GPS
        sensorManager.unregisterListener(this)
        locationManager.removeUpdates(this)

        // 3. APAGAMOS LA ACTIVIDAD FÍSICA (Ahorro de batería)
        try {
            val client = ActivityRecognition.getClient(context)
            val intent = Intent(context, ActivityReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            client.removeActivityUpdates(pendingIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        stopAudioMonitoring()
    }

    // --- CALLBACKS ACELERÓMETRO (Inercia) ---

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            // Gravedad estándar ~ 9.81 m/s²
            _accelX.value = x / 9.81f
            _accelY.value = y / 9.81f
            val currentG = sqrt(x*x + y*y + z*z) / 9.81f

            _totalG.value = currentG

            // 1. ALIMENTAR LA CAJA NEGRA (EDR)
            // Le pasamos la fuerza G actual, la velocidad actual y el audio actual
            blackBox.addPoint(
                g = currentG,
                s = _speed.value,
                a = _amplitude.value
            )

            // 2. TRIGGER DE EMERGENCIA (CU-03)
            // Si superamos los 4.5G (un impacto muy fuerte), guardamos el evento
            val currentTime = System.currentTimeMillis()

            // Si la G es alta Y han pasado más de 10 segundos (10000 ms) desde el último guardado
            if (currentG > 1.8f && (currentTime - lastCrashTime > 10000)) {

                Log.e("SafeDriveAI", "¡Impacto detectado! Guardando archivo...")
                blackBox.saveEventToDisk()

                // Actualizamos el reloj para que no vuelva a guardar en los próximos 10 segundos
                lastCrashTime = currentTime
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    // --- CALLBACKS GPS (Velocidad y Posición) ---

    override fun onLocationChanged(location: Location) {
        _currentLocation.value = location

        // El GPS devuelve m/s. Convertimos a km/h para el Dashboard
        // Fórmula: m/s * 3.6
        val speedInKmH = location.speed * 3.6f
        _speed.value = if (speedInKmH < 1f) 0f else speedInKmH // Filtro de ruido para coche parado
    }

    // Métodos requeridos por la interfaz
    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}

    private val _amplitude = MutableStateFlow(0f)
    val amplitude = _amplitude.asStateFlow()

    private var audioRecord: AudioRecord? = null
    private var audioJob: Job? = null

    @SuppressLint("MissingPermission")
    fun startAudioMonitoring() {
        val sampleRate = 44100 // Más compatible que 8000
        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)

        try {
            audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize)
            audioRecord?.startRecording()

            audioJob = CoroutineScope(Dispatchers.Default).launch {
                val buffer = ShortArray(bufferSize)
                while (isActive) {
                    val readSize = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    if (readSize > 0) {
                        var sum = 0.0
                        for (i in 0 until readSize) sum += Math.abs(buffer[i].toInt())
                        val avg = sum / readSize
                        // Normalizamos para el Aura (valor de 0.0 a 1.0)
                        _amplitude.value = (avg / 3000.0).toFloat().coerceIn(0f, 1f)
                    }
                    delay(100)
                }
            }
        } catch (e: Exception) { Log.e("Audio", "Error micro: ${e.message}") }
    }

    private fun stopAudioMonitoring() {
        audioJob?.cancel()
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (e: Exception) {}
        audioRecord = null
    }
}