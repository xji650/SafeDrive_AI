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

        // 1. Obtenemos información local para decidir si sobrescribir o no
        val unsyncedLocalIds = dao.getUnsyncedIncidents().map { it.id }.toSet()
        val locallyDeletedIds = dao.getDeletedIncidentsDirect().map { it.id }.toSet()

        for (cloudEntity in cloudIncidents) {
            // Regla 1: Si hay cambios locales sin subir (como un borrado reciente), NO tocar.
            if (unsyncedLocalIds.contains(cloudEntity.id)) continue

            // Regla 2: Si ya está borrado localmente y la nube dice que NO, respetamos el borrado local.
            // Esto evita que eventos borrados "resuciten" por desincronización de la nube.
            if (locallyDeletedIds.contains(cloudEntity.id) && !cloudEntity.isDeleted) {
                continue
            }

            // 2. Guardamos el estado de la nube (que puede ser isDeleted = true si ya se sincronizó el borrado)
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
     * Borrado suave (RGPD):
     * Oculta el registro de la vista del usuario y marca el inicio del periodo de 30 días.
     */
    override suspend fun deleteIncident(incidentId: String) {
        val currentTime = System.currentTimeMillis()
        dao.softDeleteIncident(incidentId, currentTime)
        syncWithCloud()
    }

    override suspend fun restoreIncident(incidentId: String) {
        dao.restoreIncident(incidentId)
        syncWithCloud()
    }

    override suspend fun restoreAllIncidents() {
        dao.restoreAllIncidents()
        syncWithCloud()
    }

    override suspend fun deleteAllIncidents() {
        val currentTime = System.currentTimeMillis()
        dao.softDeleteAllIncidents(currentTime)
        syncWithCloud()
    }

    override fun getDeletedIncidents(): Flow<List<EdrModel>> =
        dao.getDeletedIncidents().map { entities -> entities.map { it.toDomainModel() } }

    /**
     * Purga definitiva (Mantenimiento):
     * Borra FÍSICAMENTE los datos que llevan más de 30 días en la "papelera".
     */
    override suspend fun purgeDeletedData() {
        val thirtyDaysInMillis = 30L * 24 * 60 * 60 * 1000
        val threshold = System.currentTimeMillis() - thirtyDaysInMillis
        val dummyVehicleId = "vehiculo_prueba_01"

        // 1. Obtenemos los registros que ya han expirado para limpiar sus archivos
        val allRecords = dao.getAllIncidentsDirect()
        val toHardDelete = allRecords.filter { it.isDeleted && (it.deletedAt ?: 0) < threshold }

        toHardDelete.forEach { entity ->
            // Borrado físico del archivo JSON local
            blackBoxManager.deleteEventFile(entity.timestamp)
            // Borrado físico en la nube (Firestore + Storage)
            remoteDataSource.deleteIncidentFromCloud(entity.timestamp, dummyVehicleId)
        }

        // 2. Limpieza definitiva en la base de datos Room
        dao.purgeOldDeletedIncidents(threshold)
    }
}