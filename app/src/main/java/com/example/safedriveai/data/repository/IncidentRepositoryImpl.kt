package com.example.safedriveai.data.repository

import com.example.safedriveai.data.local.dao.IncidentDao
import com.example.safedriveai.data.local.entity.IncidentEntity
import com.example.safedriveai.data.local.mapper.toDomainModel
import com.example.safedriveai.domain.model.EdrModel
import com.example.safedriveai.domain.repository.IncidentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class IncidentRepositoryImpl @Inject constructor(
    private val dao: IncidentDao
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
        dao.insertIncident(entity)
    }

    override suspend fun syncWithCloud() {
        // Próximo paso: Lógica de Firebase
    }

    override fun getAllIncidents(): Flow<List<EdrModel>> =
        dao.getAllIncidents().map { entities -> entities.map { it.toDomainModel() } }

    override suspend fun getUnsyncedIncidents(): List<EdrModel> =
        dao.getUnsyncedIncidents().map { it.toDomainModel() }

    override suspend fun markAsSynced(incidentId: Long) =
        dao.markAsSynced(incidentId.toInt())
}