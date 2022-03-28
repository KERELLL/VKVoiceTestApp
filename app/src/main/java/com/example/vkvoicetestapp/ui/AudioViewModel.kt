package com.example.vkvoicetestapp.ui

import android.annotation.SuppressLint
import android.app.Application
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vkvoicetestapp.data.AudioEntity
import com.example.vkvoicetestapp.data.AudiosDatabase
import com.example.vkvoicetestapp.data.AudiosRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class AudioViewModel(application: Application) : AndroidViewModel(application) {
    private val audiosRepository : AudiosRepository
    private lateinit var mediaRecorder: MediaRecorder
    private lateinit var mediaPlayer : MediaPlayer
    private lateinit var filePath: String
    var recordDuration: Int = 0
    private lateinit var audio : AudioEntity

    private val audioStateFlow : MutableStateFlow<UIState<AudioEntity>> =
        MutableStateFlow(UIState.NotRecording)
    val publicAudioStateFlow = audioStateFlow.asStateFlow()

    private val audioPlayerStateFlow : MutableStateFlow<UIState<AudioEntity>> =
        MutableStateFlow(UIState.NotRecording)
    val publicAudioPlayerStateFlow = audioPlayerStateFlow.asStateFlow()

    private val progress: MutableStateFlow<Int> = MutableStateFlow(0)
    val publicProgress = progress.asStateFlow()

    init {
        val audiosDao = AudiosDatabase.create(application).audiosDao()
        audiosRepository = AudiosRepository(audiosDao)
    }

   fun audiosList(): Flow<List<AudioEntity>> = audiosRepository.getAllAudios()

    fun startRecording(dirPath : String, name : String){
        viewModelScope.launch{
            filePath = "$dirPath$name.mp3"
            mediaRecorder = MediaRecorder()
            setUpMediaRecorder(filePath)
            try {
                mediaRecorder.prepare()
                mediaRecorder.start()
            }catch (e: IOException) {
                e.printStackTrace()
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }
            audioStateFlow.value = UIState.Recording
        }
    }

    fun initAudioPlayer(){
        mediaPlayer = MediaPlayer()

        mediaPlayer.setOnCompletionListener {
            audioPlayerStateFlow.value = UIState.End
        }

    }

    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.N)
    fun stopRecording(name : String) {
        viewModelScope.launch{
            try {
                mediaRecorder.stop()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }
            val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm")
            val date = sdf.format(Date())

            audio = AudioEntity(name = name, date = date, duration = 0, filepath = filePath)
            viewModelScope.launch {
                audiosRepository.addAudio(audio)
            }
            audioStateFlow.value = UIState.Success(audio)
        }
    }

    fun playAudio(filePath: String) {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
            audioPlayerStateFlow.value = UIState.End
        }
        try {
            initAudioPlayer()
            mediaPlayer.setDataSource(filePath)
            mediaPlayer.prepare()
            mediaPlayer.start()
            recordDuration = mediaPlayer.duration
            audioPlayerStateFlow.value = UIState.Playing

        } catch (e: java.lang.IllegalStateException) {
            e.printStackTrace()

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun getLiveProgress() {
        progress.value = mediaPlayer.currentPosition
    }

    fun resumeRecord() {
        mediaPlayer.seekTo(progress.value!!)
        mediaPlayer.start()
        if (mediaPlayer.isPlaying) {
            audioPlayerStateFlow.value = UIState.Playing
        }
    }

    fun pauseRecord() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
        }
        audioPlayerStateFlow.value = UIState.Pause
    }


    private fun setUpMediaRecorder(filePath : String){
        mediaRecorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(filePath)
        }
    }

}