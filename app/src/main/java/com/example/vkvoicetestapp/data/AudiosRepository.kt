package com.example.vkvoicetestapp.data

import kotlinx.coroutines.flow.Flow

class AudiosRepository(private val audiosDao: AudiosDao) {

    suspend fun addAudio(audio: AudioEntity){
        audiosDao.addAudio(audio)
    }

    fun getAllAudios() : Flow<List<AudioEntity>> {
        return audiosDao.getAllAudios()
    }

    suspend fun getRecord(name: String): AudioEntity {
        return audiosDao.getRecord(name)
    }
}