package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

object GameSoundSynthesizer {

    private val scope = CoroutineScope(Dispatchers.Default)
    private var bgmJob: Job? = null
    private var isBgmPlaying = false
    var currentBgmTrack: String = ""
        private set

    enum class SoundFx(val title: String, val description: String, val category: String) {
        JUMP("Retro Jump", "High ascending pitch jump sound", "Action"),
        DOUBLE_JUMP("Double Jump", "Quick twin-chirp boost", "Action"),
        COIN("Coin Pickup", "Bright high-frequency bell chime", "Pickups"),
        GEM("Gem Sparkle", "Harmonic dual chime pickup", "Pickups"),
        KEY("Key Jingle", "Metallic unlock chime", "Pickups"),
        DOOR_OPEN("Door Unlock", "Heavy mechanical slide sound", "Environment"),
        CHEST_OPEN("Chest Fanfare", "Ascending triumphal melody", "Environment"),
        SWITCH_CLICK("Switch Click", "Crisp mechanical relay click", "Environment"),
        LASER_BLAST("Laser Blaster", "Descending retro pew pew sound", "Combat"),
        SWORD_SLASH("Sword Whoosh", "Fast white noise swoosh", "Combat"),
        ENEMY_HIT("Enemy Hit", "Punchy low-mid impact", "Combat"),
        EXPLOSION("Explosion", "Deep distorted rumble and crunch", "Combat"),
        HURT("Player Hurt", "Low dissonance buzz", "Combat"),
        SPRING_BOUNCE("Spring Pad", "Boing pitch glide", "Action"),
        POWERUP("Powerup Acquired", "Rising major arpeggio", "Pickups"),
        VICTORY("Level Victory", "Epic victory fanfare", "Game"),
        GAME_OVER("Game Over", "Descending minor gloom", "Game")
    }

    enum class MusicTheme(val title: String, val tempoBpm: Int, val description: String, val genreTag: String) {
        CYBER_SYNTH("Cyberpunk Neon Drive", 130, "Fast synthwave arpeggios & driving bass", "Synthwave"),
        HERO_QUEST("8-Bit Hero Journey", 120, "Uplifting chiptune melody for brave adventures", "Chiptune"),
        DUNGEON_MYSTERY("Crypt of Shadows", 95, "Eerie minor scale ambient dungeon chimes", "Atmospheric"),
        BOSS_Frenzy("Boss Rage Battle", 145, "Intense high-energy rhythmic combat pulse", "Combat"),
        ARCADE_FEVER("Arcade Fever", 128, "Bouncy retro classic breakout theme", "Retro Arcade"),
        PEACEFUL_HAVEN("Enchanted Glade", 88, "Gentle peaceful chime progression", "Calm")
    }

    fun playSfx(sfx: SoundFx) {
        scope.launch {
            try {
                when (sfx) {
                    SoundFx.JUMP -> playToneSweep(300.0, 750.0, 120, WaveType.SQUARE)
                    SoundFx.DOUBLE_JUMP -> {
                        playToneSweep(450.0, 900.0, 80, WaveType.SQUARE)
                        delay(60)
                        playToneSweep(600.0, 1100.0, 90, WaveType.SQUARE)
                    }
                    SoundFx.COIN -> {
                        playTone(987.77, 80, WaveType.SINE) // B5
                        delay(70)
                        playTone(1318.51, 140, WaveType.SINE) // E6
                    }
                    SoundFx.GEM -> {
                        playTone(1046.50, 70, WaveType.SINE) // C6
                        playTone(1318.51, 70, WaveType.SINE) // E6
                        delay(60)
                        playTone(1567.98, 120, WaveType.SINE) // G6
                    }
                    SoundFx.KEY -> {
                        playTone(1500.0, 50, WaveType.TRIANGLE)
                        delay(40)
                        playTone(2000.0, 80, WaveType.TRIANGLE)
                    }
                    SoundFx.DOOR_OPEN -> {
                        playToneSweep(250.0, 120.0, 180, WaveType.SAWTOOTH)
                    }
                    SoundFx.CHEST_OPEN -> {
                        playTone(523.25, 70, WaveType.SINE) // C5
                        delay(60)
                        playTone(659.25, 70, WaveType.SINE) // E5
                        delay(60)
                        playTone(783.99, 70, WaveType.SINE) // G5
                        delay(60)
                        playTone(1046.50, 180, WaveType.SINE) // C6
                    }
                    SoundFx.SWITCH_CLICK -> playToneSweep(1200.0, 600.0, 45, WaveType.SQUARE)
                    SoundFx.LASER_BLAST -> playToneSweep(1200.0, 180.0, 140, WaveType.SAWTOOTH)
                    SoundFx.SWORD_SLASH -> playNoise(110)
                    SoundFx.ENEMY_HIT -> {
                        playToneSweep(280.0, 90.0, 90, WaveType.SQUARE)
                    }
                    SoundFx.EXPLOSION -> {
                        playNoise(220)
                    }
                    SoundFx.HURT -> {
                        playToneSweep(220.0, 75.0, 160, WaveType.SAWTOOTH)
                    }
                    SoundFx.SPRING_BOUNCE -> playToneSweep(180.0, 680.0, 160, WaveType.SINE)
                    SoundFx.POWERUP -> {
                        val notes = listOf(440.0, 554.37, 659.25, 880.0, 1108.73)
                        for (n in notes) {
                            playTone(n, 60, WaveType.SINE)
                            delay(50)
                        }
                    }
                    SoundFx.VICTORY -> {
                        val fanfare = listOf(
                            Pair(523.25, 120L),
                            Pair(659.25, 120L),
                            Pair(783.99, 120L),
                            Pair(1046.50, 260L)
                        )
                        for ((freq, dur) in fanfare) {
                            playTone(freq, dur.toInt(), WaveType.SINE)
                            delay(dur - 20)
                        }
                    }
                    SoundFx.GAME_OVER -> {
                        val notes = listOf(440.0, 415.30, 392.00, 349.23)
                        for (n in notes) {
                            playTone(n, 140, WaveType.SAWTOOTH)
                            delay(130)
                        }
                    }
                }
            } catch (_: Exception) {
            }
        }
    }

    fun startMusic(theme: MusicTheme) {
        stopMusic()
        isBgmPlaying = true
        currentBgmTrack = theme.title

        bgmJob = scope.launch {
            val sampleRate = 22050
            val melody = when (theme) {
                MusicTheme.CYBER_SYNTH -> listOf(
                    220.0, 220.0, 330.0, 440.0, 261.63, 330.0, 392.0, 440.0,
                    220.0, 293.66, 330.0, 493.88, 220.0, 330.0, 440.0, 523.25
                )
                MusicTheme.HERO_QUEST -> listOf(
                    261.63, 329.63, 392.00, 523.25, 392.00, 329.63, 261.63, 392.00,
                    349.23, 440.00, 523.25, 698.46, 523.25, 440.00, 392.00, 523.25
                )
                MusicTheme.DUNGEON_MYSTERY -> listOf(
                    174.61, 220.0, 261.63, 220.0, 164.81, 220.0, 246.94, 220.0,
                    155.56, 196.0, 233.08, 196.0, 146.83, 174.61, 220.0, 174.61
                )
                MusicTheme.BOSS_Frenzy -> listOf(
                    110.0, 110.0, 220.0, 110.0, 130.81, 110.0, 146.83, 110.0,
                    164.81, 110.0, 146.83, 110.0, 130.81, 110.0, 123.47, 110.0
                )
                MusicTheme.ARCADE_FEVER -> listOf(
                    392.0, 440.0, 493.88, 523.25, 587.33, 523.25, 493.88, 440.0,
                    392.0, 329.63, 349.23, 392.0, 440.0, 392.0, 349.23, 329.63
                )
                MusicTheme.PEACEFUL_HAVEN -> listOf(
                    261.63, 329.63, 392.0, 440.0, 392.0, 329.63, 293.66, 349.23,
                    392.0, 440.0, 392.0, 349.23, 261.63, 329.63, 392.0, 523.25
                )
            }

            val noteDurationMs = (60_000 / theme.tempoBpm / 2).coerceAtLeast(100)

            while (isActive && isBgmPlaying) {
                for (freq in melody) {
                    if (!isActive || !isBgmPlaying) break
                    playTone(freq, (noteDurationMs * 0.85).toInt(), WaveType.SINE, 0.18f)
                    delay(noteDurationMs.toLong())
                }
            }
        }
    }

    fun stopMusic() {
        isBgmPlaying = false
        bgmJob?.cancel()
        bgmJob = null
        currentBgmTrack = ""
    }

    fun isMusicPlaying(): Boolean = isBgmPlaying

    private enum class WaveType {
        SINE, SQUARE, TRIANGLE, SAWTOOTH
    }

    private fun playTone(frequency: Double, durationMs: Int, wave: WaveType, volume: Float = 0.35f) {
        val sampleRate = 22050
        val numSamples = (sampleRate * durationMs / 1000)
        val generatedSnd = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val period = 1.0 / frequency
            val phase = (t % period) / period // 0 to 1

            val sampleVal: Double = when (wave) {
                WaveType.SINE -> sin(2.0 * PI * frequency * t)
                WaveType.SQUARE -> if (phase < 0.5) 1.0 else -1.0
                WaveType.TRIANGLE -> if (phase < 0.5) (phase * 4.0 - 1.0) else (3.0 - phase * 4.0)
                WaveType.SAWTOOTH -> 2.0 * phase - 1.0
            }

            // Envelope to avoid pop/clicks
            val attack = (numSamples * 0.05).coerceAtLeast(1.0)
            val release = (numSamples * 0.15).coerceAtLeast(1.0)
            val env = when {
                i < attack -> i / attack
                i > (numSamples - release) -> (numSamples - i) / release
                else -> 1.0
            }

            val finalVal = (sampleVal * env * volume * Short.MAX_VALUE).toInt()
            generatedSnd[i] = finalVal.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }

        playRawPcm(generatedSnd, sampleRate)
    }

    private fun playToneSweep(startFreq: Double, endFreq: Double, durationMs: Int, wave: WaveType) {
        val sampleRate = 22050
        val numSamples = (sampleRate * durationMs / 1000)
        val generatedSnd = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val currentFreq = startFreq + (endFreq - startFreq) * progress
            val t = i.toDouble() / sampleRate
            val sampleVal = sin(2.0 * PI * currentFreq * t)

            val env = 1.0 - (progress * 0.5)
            val finalVal = (sampleVal * env * 0.35f * Short.MAX_VALUE).toInt()
            generatedSnd[i] = finalVal.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }

        playRawPcm(generatedSnd, sampleRate)
    }

    private fun playNoise(durationMs: Int) {
        val sampleRate = 22050
        val numSamples = (sampleRate * durationMs / 1000)
        val generatedSnd = ShortArray(numSamples)
        val random = java.util.Random()

        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val noise = (random.nextFloat() * 2.0f - 1.0f)
            val env = (1.0 - progress) * (1.0 - progress)
            val finalVal = (noise * env * 0.4f * Short.MAX_VALUE).toInt()
            generatedSnd[i] = finalVal.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }

        playRawPcm(generatedSnd, sampleRate)
    }

    private fun playRawPcm(pcm: ShortArray, sampleRate: Int) {
        try {
            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
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
                .setBufferSizeInBytes(pcm.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(pcm, 0, pcm.size)
            audioTrack.play()
            scope.launch {
                delay((pcm.size * 1000L / sampleRate) + 50)
                try {
                    audioTrack.stop()
                    audioTrack.release()
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {
        }
    }
}
