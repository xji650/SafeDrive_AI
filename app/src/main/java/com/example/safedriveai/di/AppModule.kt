package com.example.safedriveai.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import android.content.Context
import androidx.room.Room
import com.example.safedriveai.data.local.AppDatabase
import com.example.safedriveai.data.local.dao.IncidentDao
import com.example.safedriveai.data.remote.IncidentRemoteData
import com.example.safedriveai.data.repository.IncidentRepositoryImpl
import com.example.safedriveai.domain.repository.IncidentRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
     //1. FIREBASE
    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }

    // 2. EL ALMACÉN (Room Database)
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "safedrive_db"
        ).fallbackToDestructiveMigration()
            .build()
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
        remoteDataSource: IncidentRemoteData // <-- Le pasamos el nuevo trabajador
    ): IncidentRepository {
        return IncidentRepositoryImpl(dao, remoteDataSource)
    }
}