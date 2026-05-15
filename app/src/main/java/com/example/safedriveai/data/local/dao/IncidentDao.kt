package com.example.safedriveai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.safedriveai.data.local.entity.IncidentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IncidentDao {

    // 1. Guardar un nuevo accidente en la base de datos local
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncident(incident: IncidentEntity)

    // 2. Leer todos los accidentes (Para mostrarlos en la pantalla EDR) - Solo los NO borrados suavemente
    @Query("SELECT * FROM incidents_table WHERE isDeleted = 0 ORDER BY timestamp DESC")
    fun getAllIncidents(): Flow<List<IncidentEntity>>

    @Query("SELECT * FROM incidents_table")
    suspend fun getAllIncidentsDirect(): List<IncidentEntity>

    // 3. Buscar solo los que NO se han subido a Firebase (Para el Repository)
    @Query("SELECT * FROM incidents_table WHERE isSynced = 0")
    suspend fun getUnsyncedIncidents(): List<IncidentEntity>

    // 4. Marcar un accidente como "Subido a la nube"
    @Query("UPDATE incidents_table SET isSynced = 1 WHERE id = :incidentId")
    suspend fun markAsSynced(incidentId: String)

    // 5. Borrado suave (Soft Delete)
    @Query("UPDATE incidents_table SET isDeleted = 1, deletedAt = :deletedAt, isSynced = 0 WHERE id = :incidentId AND isDeleted = 0")
    suspend fun softDeleteIncident(incidentId: String, deletedAt: Long)

    @Query("UPDATE incidents_table SET isDeleted = 1, deletedAt = :deletedAt, isSynced = 0 WHERE isDeleted = 0")
    suspend fun softDeleteAllIncidents(deletedAt: Long)

    // 6. Purga definitiva de registros antiguos (> 30 días en la papelera)
    @Query("UPDATE incidents_table SET isDeleted = 0, deletedAt = NULL, isSynced = 0 WHERE id = :incidentId")
    suspend fun restoreIncident(incidentId: String)

    @Query("UPDATE incidents_table SET isDeleted = 0, deletedAt = NULL, isSynced = 0 WHERE isDeleted = 1")
    suspend fun restoreAllIncidents()

    @Query("UPDATE incidents_table SET type = :newType, isSynced = 0 WHERE id = :incidentId")
    suspend fun updateIncidentType(incidentId: String, newType: Int)

    @Query("SELECT * FROM incidents_table WHERE isDeleted = 1 ORDER BY deletedAt DESC")
    fun getDeletedIncidents(): Flow<List<IncidentEntity>>

    @Query("SELECT * FROM incidents_table WHERE isDeleted = 1")
    suspend fun getDeletedIncidentsDirect(): List<IncidentEntity>

    @Query("SELECT * FROM incidents_table WHERE id = :incidentId")
    suspend fun getIncidentById(incidentId: String): IncidentEntity?

    @Query("SELECT * FROM incidents_table WHERE id = :incidentId")
    fun getIncidentByIdFlow(incidentId: String): Flow<IncidentEntity?>

    @Query("DELETE FROM incidents_table WHERE isDeleted = 1 AND deletedAt < :threshold")
    suspend fun purgeOldDeletedIncidents(threshold: Long)
}