package com.example.vkvoicetestapp.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audios_table")
data class AudioEntity(
    @PrimaryKey(autoGenerate = true)
    val id : Int,
    @ColumnInfo(name = "name")
    val name : String,
    @ColumnInfo(name = "date")
    val date : String,
    @ColumnInfo(name = "duration")
    var duration : Int,
    @ColumnInfo(name = "path")
    val filepath : String
) {
    constructor(name : String, date: String, duration: Int, filepath: String) : this(0, name, date,duration, filepath)
}