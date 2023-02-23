package com.senshuu.voicecloud

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import java.io.*

class Voice(private val outputFileName: String) {

    private val audioSource = MediaRecorder.AudioSource.MIC
    private val sampleRateInHz = 44100
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat)
    private var audioRecord: AudioRecord? = null
    private var recordingThread: Thread? = null
    private var isRecording = false


    fun startRecording() {
        audioRecord = AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes)
        audioRecord?.startRecording()
        isRecording = true
        recordingThread = Thread {
            writeAudioDataToFile()
        }
        recordingThread?.start()
    }

    fun stopRecording() {
        isRecording = false
        recordingThread?.join()
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
    }

    private fun writeAudioDataToFile() {
        val file = File(outputFileName)
        if (file.exists()) {
            file.delete()
        }
        val outputStream: OutputStream = BufferedOutputStream(FileOutputStream(file))
        val data = ByteArray(bufferSizeInBytes)
        while (isRecording) {
            val read = audioRecord?.read(data, 0, bufferSizeInBytes) ?: 0
            if (read != AudioRecord.ERROR_INVALID_OPERATION) {
                outputStream.write(data, 0, read)
            }
        }
        outputStream.close()
    }



}
