package com.senshuu.voicecloud

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.view.MotionEvent
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.senshuu.voicecloud.databinding.ActivityMainBinding

import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import java.io.File



class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val smartData = SmartData()
    private lateinit var voice: Voice
    private val REQUEST_RECORD_AUDIO_PERMISSION = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (! Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }
        val py = Python.getInstance()
        val module = py.getModule("AipSpeech")
        val filePath = File(Environment.getExternalStorageDirectory(), "Android/data/com.senshuu.voicecloud/files/recording.pcm").path

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.smartData = smartData

        smartData.smartData()

        val permissions = arrayOf(Manifest.permission.RECORD_AUDIO)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)
        }

        binding.btnRecord.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                voice = Voice(getExternalFilesDir(null)?.absolutePath + "/recording.pcm")
                voice.startRecording()
                v.setBackgroundResource(R.drawable.circle_button_pressed)
                smartData.VoiceDataText = "识别中..."
                val voiceDataTextView = findViewById<TextView>(R.id.VoiceDatas)
                voiceDataTextView.text = smartData.VoiceDataText
            } else if (event.action == MotionEvent.ACTION_UP) {
                voice.stopRecording()
                v.setBackgroundResource(R.drawable.circle_button_normal)
                v.performClick()

                val filePath = "${getExternalFilesDir(null)?.absolutePath}/recording.pcm"
                val file = File(filePath)
                if (file.exists()) {
                    val result = module.callAttr("baidu_Speech_To_Text", filePath)
                    smartData.VoiceDataText = result.toString()
                    val voiceDataTextView = findViewById<TextView>(R.id.VoiceDatas)
                    voiceDataTextView.text = smartData.VoiceDataText
                } else {
                    // Handle the case where the file does not exist
                }

            }
            true
        }


    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
            } else {
                // Permission denied
            }
        }
    }





}
