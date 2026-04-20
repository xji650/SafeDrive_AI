package com.example.safedriveai.data.repository

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
    private val remoteDataSource: IncidentRemoteData
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

        // 2. Intentamos sincronizar
        syncWithCloud()
    }

    override suspend fun syncWithCloud() {
        val unsynced = dao.getUnsyncedIncidents()
        val dummyVehicleId = "vehiculo_prueba_01"

        for (entity in unsynced) {
            // El Repositorio le delega el trabajo duro al RemoteDataSource
            val success = remoteDataSource.uploadIncidentAndTelemetry(entity, dummyVehicleId)

            // Si el trabajador de la nube confirma el éxito, el Repositorio actualiza Room
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

    override suspend fun markAsSynced(incidentId: Long) =
        dao.markAsSynced(incidentId.toString())
}