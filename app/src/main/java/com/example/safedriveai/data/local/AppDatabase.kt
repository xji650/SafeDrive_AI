package com.example.safedriveai.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.safedriveai.data.local.dao.IncidentDao
import com.example.safedriveai.data.local.entity.IncidentEntity

// 1. Le decimos qué "Fichas" (Entities) va a guardar y la versión de la base de datos
@Database(entities = [IncidentEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    // 2. Le decimos qué "Libro de órdenes" (DAO) va a usar
    abstract fun incidentDao(): IncidentDao
}