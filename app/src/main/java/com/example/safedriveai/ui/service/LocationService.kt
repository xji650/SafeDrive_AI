package com.example.safedriveai.ui.service

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.safedriveai.data.repository.SensorRepository

class LocationService : Service() {

    private lateinit var repository: SensorRepository
    private val CHANNEL_ID = "SafeDrive_Channel"

    override fun onCreate() {
        super.onCreate()
        // ¡AQUÍ ESTÁ LA MAGIA! Pedimos la instancia ÚNICA del repositorio
        repository = SensorRepository.getInstance(applicationContext)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 1. Creamos la notificación persistente
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SafeDrive AI Activo")
            .setContentText("Grabando telemetría en segundo plano...")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation) // Puedes poner el logo de tu app
            .setOngoing(true) // No se puede deslizar para borrar
            .build()

        // 2. Arrancamos el escudo protector de Android
        startForeground(1, notification)

        // 3. Encendemos los sensores
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            repository.startListening()
        }

        // START_STICKY hace que si el sistema mata el servicio por falta extrema de RAM, lo reinicie luego
        return START_STICKY
    }

    override fun onDestroy() {
        // Apagamos los sensores solo cuando el servicio se destruye
        repository.stopListening()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID, "Seguridad SafeDrive",
                NotificationManager.IMPORTANCE_LOW // LOW para que no suene todo el rato al actualizar
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }
}