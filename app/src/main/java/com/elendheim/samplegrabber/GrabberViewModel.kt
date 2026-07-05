package com.elendheim.samplegrabber

import android.app.Application
import android.media.MediaPlayer
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.elendheim.samplegrabber.audio.SampleRecorder
import com.elendheim.samplegrabber.audio.SilenceTrimmer
import com.elendheim.samplegrabber.audio.WavCodec
import com.elendheim.samplegrabber.data.Sample
import com.elendheim.samplegrabber.data.SampleStore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface RecorderState {
    data object Idle : RecorderState
    data class Recording(
        val elapsedMs: Int,
        val level: Float,
        val stopping: Boolean
    ) : RecorderState

    data object Saving : RecorderState
}

class GrabberViewModel(application: Application) : AndroidViewModel(application) {

    private val recorder = SampleRecorder()
    private var player: MediaPlayer? = null

    private val _state = MutableStateFlow<RecorderState>(RecorderState.Idle)
    val state: StateFlow<RecorderState> = _state.asStateFlow()

    private val _samples = MutableStateFlow<List<Sample>>(emptyList())
    val samples: StateFlow<List<Sample>> = _samples.asStateFlow()

    private val _playingUri = MutableStateFlow<Uri?>(null)
    val playingUri: StateFlow<Uri?> = _playingUri.asStateFlow()

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    init {
        refreshSamples()
    }

    fun toggleRecord() {
        when (_state.value) {
            is RecorderState.Idle -> startRecording()
            is RecorderState.Recording -> {
                recorder.requestStop()
                (_state.value as? RecorderState.Recording)?.let {
                    _state.value = it.copy(stopping = true)
                }
            }

            else -> Unit
        }
    }

    private fun startRecording() {
        stopPlayback()
        _state.value = RecorderState.Recording(0, 0f, stopping = false)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val raw = recorder.record { elapsedMs, level ->
                    _state.value = RecorderState.Recording(
                        elapsedMs = elapsedMs,
                        level = level,
                        stopping = recorder.stopRequested
                    )
                }
                _state.value = RecorderState.Saving
                val trimmed = SilenceTrimmer.trim(raw, SampleRecorder.SAMPLE_RATE)
                val wav = WavCodec.encode(trimmed, SampleRecorder.SAMPLE_RATE)
                val stamp = SimpleDateFormat("yyyy-MM-dd HH-mm-ss", Locale.US).format(Date())
                val name = "Grab $stamp.wav"
                SampleStore.save(getApplication(), name, wav)
                refreshSamples()
                _messages.tryEmit(getApplication<Application>().getString(R.string.saved_as, name))
            } catch (e: Exception) {
                _messages.tryEmit(e.message ?: "Recording failed")
            } finally {
                _state.value = RecorderState.Idle
            }
        }
    }

    fun togglePlay(sample: Sample) {
        if (_playingUri.value == sample.uri) {
            stopPlayback()
            return
        }
        stopPlayback()
        val mp = MediaPlayer()
        player = mp
        try {
            mp.setDataSource(getApplication(), sample.uri)
            mp.setOnPreparedListener { it.start() }
            mp.setOnCompletionListener { stopPlayback() }
            mp.prepareAsync()
            _playingUri.value = sample.uri
        } catch (e: Exception) {
            stopPlayback()
            _messages.tryEmit(e.message ?: "Playback failed")
        }
    }

    fun delete(sample: Sample) {
        if (_playingUri.value == sample.uri) stopPlayback()
        viewModelScope.launch(Dispatchers.IO) {
            SampleStore.delete(getApplication(), sample.uri)
            refreshSamples()
        }
    }

    fun notifyPermissionDenied() {
        _messages.tryEmit(getApplication<Application>().getString(R.string.mic_permission_needed))
    }

    private fun stopPlayback() {
        player?.let {
            runCatching { it.stop() }
            it.release()
        }
        player = null
        _playingUri.value = null
    }

    private fun refreshSamples() {
        viewModelScope.launch(Dispatchers.IO) {
            _samples.value = SampleStore.list(getApplication())
        }
    }

    override fun onCleared() {
        stopPlayback()
    }
}
