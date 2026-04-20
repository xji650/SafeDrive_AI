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
    // Dentro de IncidentRemoteDataSource.kt
    suspend fun getAllAccidentsFromCloud(vehicleId: String): List<IncidentEntity> {
        return try {
            val snapshot = firestore.collection("vehiculos")
                .document(vehicleId)
                .collection("accidentes")
                .get()
                .await()

            // CHIVATO 1: Nos dirá cuántos ha encontrado
            android.util.Log.d("FIREBASE_DEBUG", "¡GET con éxito! Documentos encontrados: ${snapshot.size()}")

            snapshot.toObjects(IncidentEntity::class.java)
        } catch (e: Exception) {
            // CHIVATO 2: Si falla el GET, nos dirá por qué
            android.util.Log.e("FIREBASE_DEBUG", "Fallo al hacer el GET: ${e.message}")
            emptyList()
        }
    }

    // Intenta descargar el JSON pesado de Storage
    suspend fun downloadTelemetryFile(vehicleId: String, timestamp: Long): File? {
        return try {
            val fileName = "EDR_EVENT_${timestamp}.json"
            val localFile = File(context.filesDir, fileName)

            // Si ya lo tenemos descargado en el móvil, ahorramos internet
            if (localFile.exists()) return localFile

            // Si no, lo descargamos de Firebase Storage y lo guardamos
            val storageRef = storage.reference
                .child("telemetry")
                .child(vehicleId)
                .child(fileName)

            // Esto baja el archivo y lo guarda en tu móvil
            storageRef.getFile(localFile).await()
            localFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}