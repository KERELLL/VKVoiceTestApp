package com.example.vkvoicetestapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AudiosDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addAudio(audio: AudioEntity)

    @Query(value = "SELECT* FROM audios_table WHERE name = :name")
    suspend fun getRecord(name: String): AudioEntity

    @Query("SELECT* FROM audios_table")
    fun getAllAudios(): Flow<List<AudioEntity>>

}