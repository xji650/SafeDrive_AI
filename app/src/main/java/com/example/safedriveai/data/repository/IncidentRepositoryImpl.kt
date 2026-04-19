package com.example.safedriveai.data.repository

import com.example.safedriveai.data.local.dao.IncidentDao
import com.example.safedriveai.data.local.entity.IncidentEntity
import com.example.safedriveai.domain.repository.IncidentRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class IncidentRepositoryImpl @Inject constructor(
    private val dao: IncidentDao,
//    private val firestore: FirebaseFirestore
) : IncidentRepository {

    override suspend fun saveIncident(incident: IncidentEntity) {
        dao.insertIncident(incident)
    }
    override fun getAllIncidents(): Flow<List<IncidentEntity>> {
        return dao.getAllIncidents()
    }

    override suspend fun syncWithCloud() {
        // Dejamos esto vacío por ahora. ¡Aquí irá la magia de Firebase más adelante!
    }
}