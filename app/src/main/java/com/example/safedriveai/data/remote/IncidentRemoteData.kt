package com.example.safedriveai.data.remote

import android.content.Context
import android.net.Uri
import com.example.safedriveai.data.local.entity.IncidentEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject

class IncidentRemoteData @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    @ApplicationContext private val context: Context
) {
    // Esta función intenta subir los datos y devuelve true si lo logra, o false si falla
    suspend fun uploadIncidentAndTelemetry(entity: IncidentEntity, vehicleId: String): Boolean {
        return try {
            // 1. SUBIR EL ARCHIVO JSON A STORAGE
            val fileName = "EDR_EVENT_${entity.timestamp}.json"
            val file = File(context.filesDir, fileName)

            if (file.exists()) {
                val storageRef = storage.reference
                    .child("telemetry")
                    .child(vehicleId)
                    .child(fileName)
                storageRef.putFile(Uri.fromFile(file)).await()
            }

            // 2. SUBIR EL RESUMEN A FIRESTORE
            firestore.collection("vehiculos")
                .document(vehicleId)
                .collection("accidentes")
                .document(entity.timestamp.toString())
                .set(entity)
                .await()

            true // Si llega aquí sin petar, es un éxito
        } catch (e: Exception) {
            e.printStackTrace()
            false // Si no hay internet o falla algo
        }
    }
}