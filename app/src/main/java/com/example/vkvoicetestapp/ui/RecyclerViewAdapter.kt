package com.example.vkvoicetestapp.ui

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.vkvoicetestapp.R
import com.example.vkvoicetestapp.model.AudioItem
import com.example.vkvoicetestapp.model.AudioPlayingState

class RecyclerViewAdapter(private val click : (AudioItem) -> Unit) : RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {
    var audiosList: MutableList<AudioItem> = mutableListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_audio_list, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.audioNameTextView.text = audiosList[position].audioEntity.name
        holder.audioDateTextView.text = audiosList[position].audioEntity.date
        holder.progressBar.max = audiosList[position].audioEntity.duration
        audiosList[position].audioPlayingState = AudioPlayingState.START
        holder.playButton.setOnClickListener {
            click.invoke(audiosList[position])
            when (audiosList[position].audioPlayingState) {
                AudioPlayingState.START -> {
                    holder.playButton.setBackgroundResource(R.drawable.pause)
                    audiosList[position].audioPlayingState = AudioPlayingState.PAUSE
                }
                AudioPlayingState.PAUSE -> {
                    holder.playButton.setBackgroundResource(R.drawable.resume)
                    audiosList[position].audioPlayingState = AudioPlayingState.RESUME
                }
                AudioPlayingState.RESUME -> {
                    holder.playButton.setBackgroundResource(R.drawable.pause)
                    audiosList[position].audioPlayingState = AudioPlayingState.PAUSE
                }
                AudioPlayingState.END ->{
                    holder.playButton.setBackgroundResource(R.drawable.resume)
                    audiosList[position].audioPlayingState = AudioPlayingState.START
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return audiosList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val audioNameTextView: TextView = itemView.findViewById(R.id.audioNameTextView)
        val playButton : Button = itemView.findViewById(R.id.playButton)
        val audioTimerTextView: TextView = itemView.findViewById(R.id.audioTimerTextView)
        val progressBar : ProgressBar = itemView.findViewById(R.id.progressBar)
        val audioDateTextView: TextView = itemView.findViewById(R.id.audioDateTextView)
    }

}