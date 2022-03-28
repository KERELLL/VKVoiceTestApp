package com.example.vkvoicetestapp.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vkvoicetestapp.R
import com.example.vkvoicetestapp.databinding.ActivityMainBinding
import com.example.vkvoicetestapp.model.AudioItem
import com.example.vkvoicetestapp.model.AudioPlayingState
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

const val REQUEST_CODE = 200

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val permissions = arrayOf(
        android.Manifest.permission.RECORD_AUDIO,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    private lateinit var audioViewModel: AudioViewModel
    private lateinit var mainHandler: Handler
    private var audioName: String = ""
    private var permissionGranted = false
    private var isRecording = false
    private var isEnd = false
    private var dirPath = ""
    private lateinit var adapter: RecyclerViewAdapter
    private var list: MutableList<AudioItem> = mutableListOf()


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkPermissions(permissions[0])
        checkPermissions(permissions[1])
        mainHandler = Handler(Looper.getMainLooper())
        audioViewModel = ViewModelProvider(this)[AudioViewModel::class.java]
        subscribeRecordingAudio()
        setUpButton()
        audioViewModel.initAudioPlayer()
        getAllAudiosFromDB()
        subscribePlayAudio()
        setUpRecyclerViewAdapter()
        subscribeProgressBar()
    }

    @SuppressLint("ShowToast")
    private fun subscribeRecordingAudio() {
        audioViewModel.publicAudioStateFlow.onEach {
            when (it) {
                is UIState.Recording -> {
                    Toast.makeText(this, "Recording", Toast.LENGTH_LONG).show()
                    isRecording = true
                    binding.recordingButton.setBackgroundResource(R.drawable.record)
                }
                is UIState.Success -> {
                    Toast.makeText(this, "Recorded", Toast.LENGTH_LONG).show()
                    isRecording = false
                    binding.recordingButton.setBackgroundResource(R.drawable.voice)
                }
                is UIState.Error -> {
                    Toast.makeText(this, "Error", Toast.LENGTH_LONG).show()
                }
            }
        }.launchIn(lifecycleScope)
    }

    private fun subscribePlayAudio() {
        audioViewModel.publicAudioPlayerStateFlow.onEach {
            when (it) {
                is UIState.Playing -> {
                    Toast.makeText(this, "Playing", Toast.LENGTH_LONG).show()
                }
                is UIState.Pause -> {
                    Toast.makeText(this, "Pause", Toast.LENGTH_LONG).show()
                }
                is UIState.End -> {
                    Toast.makeText(this, "End", Toast.LENGTH_LONG).show()
                }
            }
        }.launchIn(lifecycleScope)
    }

    private fun getAllAudiosFromDB() {
        lifecycle.coroutineScope.launch {
            audioViewModel.audiosList().collect {
                adapter.audiosList.clear()
                for (element in it) {
                    adapter.audiosList.add(
                        AudioItem(AudioPlayingState.START, element)
                    )
                    list.add(AudioItem(AudioPlayingState.START, element))
                }
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun setUpRecyclerViewAdapter() {
        adapter = RecyclerViewAdapter {
            when (it.audioPlayingState) {
                AudioPlayingState.START -> {
                    enableProgress(true)
                    audioViewModel.playAudio(it.audioEntity.filepath)
                }
                AudioPlayingState.PAUSE -> {
                    enableProgress(false)
                    audioViewModel.pauseRecord()
                }
                AudioPlayingState.RESUME -> {
                    enableProgress(true)
                    audioViewModel.resumeRecord()
                }
            }
        }
        binding.recyclerView.adapter = adapter
        val layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.layoutManager = layoutManager
    }

    private fun startRecording() {
        if (!permissionGranted) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE)
            return
        }
        dirPath = "${externalCacheDir?.absolutePath}/"
        audioViewModel.startRecording(dirPath, audioName)
    }


    @RequiresApi(Build.VERSION_CODES.N)
    private fun setUpButton() {
        binding.recordingButton.setOnClickListener {
            if (!isRecording) {
                setUpAlertDialog()
            } else {
                audioViewModel.stopRecording(audioName)
            }
        }
    }

    private fun setUpAlertDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Название")
        val input = EditText(this)
        input.hint = "Enter Text"
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)
        builder.setPositiveButton("Записать", DialogInterface.OnClickListener { dialog, which ->
            var isValid = true
            val name = input.text.toString()
            if (name.isEmpty()) {
                isValid = false
                Toast.makeText(this, "Назовите файл", Toast.LENGTH_LONG).show()
            }
            if (isValid) {
                audioName = name
                startRecording()
            }
        })
        builder.setNegativeButton("Отменить", DialogInterface.OnClickListener { dialog, which ->
            dialog.cancel()
        })

        builder.show()
    }
    private fun subscribeProgressBar(){
        audioViewModel.publicProgress.onEach {
            binding.progressBar.progress = it
        }
    }

    private fun enableProgress(bool: Boolean) {
        val runner = object : Runnable {
            override fun run() {
                audioViewModel.getLiveProgress()
                mainHandler.postDelayed(this, 1000)
            }
        }
        if (bool) {
            mainHandler.post(runner)
        } else {
            mainHandler.removeCallbacksAndMessages(null)
        }
    }


    private fun checkPermissions(permission: String) {
        permissionGranted = ActivityCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
        if (!permissionGranted) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            permissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED
        }
    }
}