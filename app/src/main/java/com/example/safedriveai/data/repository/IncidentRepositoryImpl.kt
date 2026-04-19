package com.example.safedriveai.data.repository

import android.content.Context
import com.example.safedriveai.data.local.dao.IncidentDao
import com.example.safedriveai.data.local.entity.IncidentEntity
import com.example.safedriveai.data.local.mapper.toDomainModel
import com.example.safedriveai.domain.model.EdrModel
import com.example.safedriveai.domain.repository.IncidentRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext

class IncidentRepositoryImpl @Inject constructor(
    private val dao: IncidentDao,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    @ApplicationContext private val context: Context
) : IncidentRepository {

    override suspend fun saveIncident(incident: EdrModel) {
        val entity = IncidentEntity(
            timestamp = incident.rawTimestamp,
            amplitudeMicrophone = incident.audioAmplitude,
            maxGForce = incident.gForce,
            speedAtImpact = incident.speed,
            latitude = incident.latitude,
            longitude = incident.longitude,
            isSynced = incident.isSynced
        )
        // 1. Guardamos localmente (Room)
        dao.insertIncident(entity)

        // 2. INTENTO DE SUBIDA INMEDIATO (Temporal para pruebas)
        syncWithCloud()
    }

    override suspend fun syncWithCloud() {
        try {
            val unsynced = dao.getUnsyncedIncidents()
            val dummyVehicleId = "vehiculo_prueba_01"

            for (entity in unsynced) {
                // --- PARTE 1: SUBIR EL ARCHIVO JSON A FIREBASE STORAGE ---
                val fileName = "EDR_EVENT_${entity.timestamp}.json"
                val file = File(context.filesDir, fileName)

                if (file.exists()) {
                    // Ruta en Storage: telemetry/vehiculo_prueba_01/EDR_EVENT_1713532...json
                    val storageRef = storage.reference
                        .child("telemetry")
                        .child(dummyVehicleId)
                        .child(fileName)

                    // Subimos el archivo
                    storageRef.putFile(Uri.fromFile(file)).await()
                }

                // --- PARTE 2: SUBIR EL RESUMEN A FIRESTORE ---
                // Ruta en Firestore: vehiculos -> vehiculo_prueba_01 -> accidentes -> 1713532...
                firestore.collection("vehiculos")
                    .document(dummyVehicleId)
                    .collection("accidentes")
                    .document(entity.timestamp.toString())
                    .set(entity)
                    .await()

                // --- PARTE 3: MARCAR COMO SINCRONIZADO EN ROOM ---
                dao.markAsSynced(entity.id)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getAllIncidents(): Flow<List<EdrModel>> =
        dao.getAllIncidents().map { entities -> entities.map { it.toDomainModel() } }

    override suspend fun getUnsyncedIncidents(): List<EdrModel> =
        dao.getUnsyncedIncidents().map { it.toDomainModel() }

    override suspend fun markAsSynced(incidentId: Long) =
        dao.markAsSynced(incidentId.toInt())
}