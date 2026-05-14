package com.example.safedriveai.data.repository

import com.example.safedriveai.data.local.BlackBoxManager
import com.example.safedriveai.data.local.dao.IncidentDao
import com.example.safedriveai.data.local.entity.IncidentEntity
import com.example.safedriveai.data.local.mapper.toDomainModel
import com.example.safedriveai.data.remote.IncidentRemoteData
import com.example.safedriveai.domain.model.EdrModel
import com.example.safedriveai.domain.repository.IncidentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class IncidentRepositoryImpl @Inject constructor(
    private val dao: IncidentDao,
    private val remoteDataSource: IncidentRemoteData,
    private val blackBoxManager: BlackBoxManager
) : IncidentRepository {

    override suspend fun saveIncident(incident: EdrModel) {
        val entity = IncidentEntity(
            id = incident.id,
            timestamp = incident.rawTimestamp,
            amplitudeMicrophone = incident.audioAmplitude,
            maxGForce = incident.gForce,
            speedAtImpact = incident.speed,
            latitude = incident.latitude,
            longitude = incident.longitude,
            isSynced = incident.isSynced
        )
        dao.insertIncident(entity)
        syncWithCloud()
    }

    override suspend fun syncWithCloud() {
        val unsynced = dao.getUnsyncedIncidents()
        val dummyVehicleId = "vehiculo_prueba_01"

        for (entity in unsynced) {
            val success = remoteDataSource.uploadIncidentAndTelemetry(entity, dummyVehicleId)
            if (success) {
                dao.markAsSynced(entity.id)
            }
        }
    }

    override suspend fun fetchHistoryFromCloud() {
        val dummyVehicleId = "vehiculo_prueba_01"
        val cloudIncidents = remoteDataSource.getAllAccidentsFromCloud(dummyVehicleId)
        for (cloudEntity in cloudIncidents) {
            val entityToSave = cloudEntity.copy(isSynced = true)
            dao.insertIncident(entityToSave)
        }
    }

    override suspend fun getTelemetryFile(timestamp: Long): java.io.File? {
        val dummyVehicleId = "vehiculo_prueba_01"
        return remoteDataSource.downloadTelemetryFile(dummyVehicleId, timestamp)
    }

    override fun getAllIncidents(): Flow<List<EdrModel>> =
        dao.getAllIncidents().map { entities -> entities.map { it.toDomainModel() } }

    override suspend fun getUnsyncedIncidents(): List<EdrModel> =
        dao.getUnsyncedIncidents().map { it.toDomainModel() }

    override suspend fun markAsSynced(incidentId: String) {
        dao.markAsSynced(incidentId)
    }

    /**
     * Borrado completo (CRUD):
     * Borra el registro de Room y el archivo físico del disco.
     */
    override suspend fun deleteIncident(incidentId: String) {
        // Obtenemos el registro antes de borrarlo para saber su timestamp
        dao.getAllIncidentsDirect().find { it.id == incidentId }?.let { entity ->
            blackBoxManager.deleteEventFile(entity.timestamp)
        }
        dao.deleteIncident(incidentId)
    }

    override suspend fun deleteAllIncidents() {
        // Borramos todos los archivos físicos primero
        dao.getAllIncidentsDirect().forEach { entity ->
            blackBoxManager.deleteEventFile(entity.timestamp)
        }
        dao.deleteAllIncidents()
    }
}