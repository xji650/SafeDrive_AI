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

    // 2. Leer todos los accidentes (Para mostrarlos en la pantalla EDR)
    @Query("SELECT * FROM incidents_table ORDER BY timestamp DESC")
    fun getAllIncidents(): Flow<List<IncidentEntity>>

    // 3. Buscar solo los que NO se han subido a Firebase (Para el Repository)
    @Query("SELECT * FROM incidents_table WHERE isSynced = 0")
    suspend fun getUnsyncedIncidents(): List<IncidentEntity>

    // 4. Marcar un accidente como "Subido a la nube"
    @Query("UPDATE incidents_table SET isSynced = 1 WHERE id = :incidentId")
    suspend fun markAsSynced(incidentId: Int)
}