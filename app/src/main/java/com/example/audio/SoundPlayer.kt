package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.random.Random

class SoundPlayer {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var ambientTrack: AudioTrack? = null
    private var ambientJob: Job? = null
    var isPlayingAmbient: Boolean = false
        private set

    var ambientVolume: Float = 0.75f
        private set

    fun setVolume(vol: Float) {
        val clamped = vol.coerceIn(0.1f, 1.0f)
        ambientVolume = clamped
        try {
            ambientTrack?.setVolume(clamped)
        } catch (_: Exception) {}
    }

    fun toggleAmbient(onStateChange: (Boolean) -> Unit) {
        if (isPlayingAmbient) {
            stopAmbient()
            isPlayingAmbient = false
        } else {
            startAmbient()
            isPlayingAmbient = true
        }
        onStateChange(isPlayingAmbient)
    }

    private fun startAmbient() {
        stopAmbient()
        ambientJob = scope.launch {
            val sampleRate = 22050
            val bufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(4096)

            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            ambientTrack = track
            try {
                track.setVolume(ambientVolume)
                track.play()
            } catch (_: Exception) {}

            val buffer = ShortArray(bufferSize / 2)
            var pinkFilter1 = 0.0
            var pinkFilter2 = 0.0
            var dronePhase = 0.0

            while (isActive) {
                // Synthesize rich, soothing rainfall:
                // Combination of filtered pink noise, soft high-frequency raindrops, and a gentle calming drone
                for (i in buffer.indices) {
                    val white = (Random.nextDouble() * 2.0 - 1.0)
                    pinkFilter1 = (pinkFilter1 + 0.06 * white) / 1.06
                    pinkFilter2 = (pinkFilter2 + 0.03 * pinkFilter1) / 1.03

                    // Occasional delicate raindrop droplet impact
                    val droplet = if (Random.nextDouble() < 0.015) {
                        (Random.nextDouble() * 2.0 - 1.0) * 0.35
                    } else 0.0

                    // Gentle subtle 55Hz soothing drone (A1 note)
                    dronePhase += (2.0 * Math.PI * 55.0 / sampleRate)
                    if (dronePhase > 2.0 * Math.PI) dronePhase -= 2.0 * Math.PI
                    val drone = sin(dronePhase) * 0.08

                    val rawRain = (pinkFilter1 * 0.55 + pinkFilter2 * 0.35 + droplet + drone)
                    // Louder, rich volume requested by user (~0.60 master gain)
                    val sampleValue = (rawRain * 32767.0 * 0.60).coerceIn(-32767.0, 32767.0).toInt().toShort()
                    buffer[i] = sampleValue
                }
                track.write(buffer, 0, buffer.size)
            }
        }
    }

    fun stopAmbient() {
        ambientJob?.cancel()
        ambientJob = null
        try {
            ambientTrack?.stop()
            ambientTrack?.release()
        } catch (_: Exception) {}
        ambientTrack = null
        isPlayingAmbient = false
    }

    fun playCompletionChime() {
        scope.launch {
            val sampleRate = 44100
            val durationMs = 600
            val numSamples = (sampleRate * durationMs / 1000)
            val buffer = ShortArray(numSamples)

            // Pleasant dual harmonic chime (880Hz + 1320Hz)
            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val envelope = (1.0 - (i.toDouble() / numSamples)).coerceIn(0.0, 1.0)
                val wave1 = sin(2.0 * Math.PI * 880.0 * t)
                val wave2 = 0.5 * sin(2.0 * Math.PI * 1320.0 * t)
                val combined = (wave1 + wave2) * envelope * 0.4
                buffer[i] = (combined * 32767.0).toInt().toShort()
            }

            try {
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(buffer, 0, buffer.size)
                track.play()
            } catch (_: Exception) {}
        }
    }
}
