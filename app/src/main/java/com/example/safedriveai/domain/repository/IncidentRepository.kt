package com.example.safedriveai.domain.repository

import com.example.safedriveai.domain.model.EdrModel
import kotlinx.coroutines.flow.Flow

interface IncidentRepository {
    // Guardamos el modelo de dominio
    suspend fun saveIncident(incident: EdrModel)
    suspend fun fetchHistoryFromCloud()
    // Devolvemos el modelo de dominio ya "mapeado"
    fun getAllIncidents(): Flow<List<EdrModel>>
    suspend fun syncWithCloud()
    suspend fun getTelemetryFile(timestamp: Long): java.io.File?
    suspend fun getUnsyncedIncidents(): List<EdrModel>
    suspend fun markAsSynced(incidentId: String)

    // Nuevos métodos para completar el CRUD con cumplimiento RGPD (Soft Delete)
    suspend fun deleteIncident(incidentId: String)
    suspend fun restoreIncident(incidentId: String)
    suspend fun restoreAllIncidents()
    suspend fun deleteAllIncidents()
    suspend fun purgeDeletedData()
    fun getDeletedIncidents(): Flow<List<EdrModel>>

    // Actualiza el tipo de incidente según el feedback del usuario
    suspend fun updateIncidentFeedback(incidentId: String, feedbackType: Int)
}