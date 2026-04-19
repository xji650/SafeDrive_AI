package com.example.safedriveai.domain.repository

import com.example.safedriveai.data.local.entity.IncidentEntity
import kotlinx.coroutines.flow.Flow

interface IncidentRepository {
    suspend fun saveIncident(incident: IncidentEntity)
    fun getAllIncidents(): Flow<List<IncidentEntity>>
    suspend fun syncWithCloud()
}