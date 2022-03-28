package com.example.vkvoicetestapp.model

import com.example.vkvoicetestapp.data.AudioEntity

data class AudioItem(
    var audioPlayingState: AudioPlayingState,
    val audioEntity: AudioEntity
)

enum class AudioPlayingState{
    START,
    RESUME,
    PAUSE,
    END
}
