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

            // 2. SUBIR EL RESUMEN A FIRESTORE (Mapeo manual para evitar errores con prefijo "is")
            val incidentData = hashMapOf(
                "id" to entity.id,
                "timestamp" to entity.timestamp,
                "amplitudeMicrophone" to entity.amplitudeMicrophone,
                "maxGForce" to entity.maxGForce,
                "speedAtImpact" to entity.speedAtImpact,
                "angleAtImpact" to entity.angleAtImpact,
                "jerkAtImpact" to entity.jerkAtImpact,
                "latitude" to entity.latitude,
                "longitude" to entity.longitude,
                "isSynced" to true,
                "type" to entity.type,
                "isDeleted" to entity.isDeleted,
                "deletedAt" to entity.deletedAt
            )

            firestore.collection("vehiculos")
                .document(vehicleId)
                .collection("accidentes")
                .document(entity.timestamp.toString())
                .set(incidentData)
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

            android.util.Log.d("FIREBASE_DEBUG", "¡GET con éxito! Documentos encontrados: ${snapshot.size()}")

            snapshot.documents.mapNotNull { doc ->
                // Mapeo manual campo a campo para máxima seguridad ante fallos de Firebase
                try {
                    IncidentEntity(
                        id = doc.getString("id") ?: "",
                        timestamp = doc.getLong("timestamp") ?: 0L,
                        amplitudeMicrophone = doc.getDouble("amplitudeMicrophone")?.toFloat() ?: 0f,
                        maxGForce = doc.getDouble("maxGForce")?.toFloat() ?: 0f,
                        speedAtImpact = doc.getDouble("speedAtImpact")?.toFloat() ?: 0f,
                        angleAtImpact = doc.getDouble("angleAtImpact")?.toFloat() ?: 0f,
                        jerkAtImpact = doc.getDouble("jerkAtImpact")?.toFloat() ?: 0f,
                        latitude = doc.getDouble("latitude") ?: 0.0,
                        longitude = doc.getDouble("longitude") ?: 0.0,
                        isSynced = true, // Si viene de la nube, está sincronizado
                        type = doc.getLong("type")?.toInt(),
                        isDeleted = doc.getBoolean("isDeleted") ?: doc.getBoolean("deleted") ?: false,
                        deletedAt = doc.getLong("deletedAt")
                    )
                } catch (e: Exception) {
                    android.util.Log.e("FIREBASE_MAP_ERROR", "Error mapeando documento ${doc.id}: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
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

    /**
     * Elimina el registro de Firestore y el archivo JSON de Storage.
     */
    suspend fun deleteIncidentFromCloud(timestamp: Long, vehicleId: String): Boolean {
        return try {
            // 1. Borrar de Firestore
            firestore.collection("vehiculos")
                .document(vehicleId)
                .collection("accidentes")
                .document(timestamp.toString())
                .delete()
                .await()

            // 2. Borrar de Storage
            val fileName = "EDR_EVENT_$timestamp.json"
            storage.reference
                .child("telemetry")
                .child(vehicleId)
                .child(fileName)
                .delete()
                .await()

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}