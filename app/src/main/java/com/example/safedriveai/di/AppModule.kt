package com.example.safedriveai.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import android.content.Context
import androidx.room.Room
import com.example.safedriveai.data.local.AppDatabase
import com.example.safedriveai.data.local.dao.IncidentDao
import com.example.safedriveai.data.repository.IncidentRepositoryImpl
import com.example.safedriveai.domain.repository.IncidentRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    // 1. FIREBASE
//    @Provides
//    @Singleton
//    fun provideFirestore(): FirebaseFirestore {
//        return FirebaseFirestore.getInstance()
//    }

    // 2. EL ALMACÉN (Room Database)
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "safedrive_db" // Nombre del archivo de la base de datos en el móvil
        ).build()
    }

    // 3. EL LIBRO DE ÓRDENES (Dao)
    @Provides
    fun provideIncidentDao(db: AppDatabase): IncidentDao {
        return db.incidentDao()
    }

    // 4. EL CONTRATO (Unimos la Interfaz con la Implementación)
    @Provides
    @Singleton
    fun provideIncidentRepository(
        dao: IncidentDao,
//        firestore: FirebaseFirestore
    ): IncidentRepository {
        // Le decimos a Hilt: "Cuando alguien pida un IncidentRepository (interfaz),
        // entrégale un IncidentRepositoryImpl (trabajador)".
//        return IncidentRepositoryImpl(dao, firestore)
        return IncidentRepositoryImpl(dao)
    }
}