package com.example.vkvoicetestapp.ui

import java.lang.Exception

sealed class UIState<out S: Any?> {
    object NotRecording : UIState<Nothing>()
    object Recording : UIState<Nothing>()
    data class Success<out S: Any?>(val data: S) : UIState<S>()
    data class Error(val data: Exception) : UIState<Nothing>()

    object Playing : UIState<Nothing>()
    object Pause : UIState<Nothing>()
    object End : UIState<Nothing>()
}