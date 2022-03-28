package com.example.vkvoicetestapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [
    AudioEntity::class
], version = 3, exportSchema = true)
abstract class AudiosDatabase: RoomDatabase() {
    abstract fun audiosDao() : AudiosDao

    companion object{

        @Volatile
        private var INSTANCE: AudiosDatabase? = null

        fun create(context: Context): AudiosDatabase{

            synchronized(this){
                if(INSTANCE == null){
                    INSTANCE =  Room.databaseBuilder(context.applicationContext, AudiosDatabase::class.java, "audios_table").fallbackToDestructiveMigration().build()
                }
            }
            return INSTANCE!!
        }
    }
}