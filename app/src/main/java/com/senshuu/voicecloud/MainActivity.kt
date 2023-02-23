package com.senshuu.voicecloud

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import androidx.databinding.DataBindingUtil
import com.senshuu.voicecloud.databinding.ActivityMainBinding
import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.IOException


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val smartData = SmartData()
    private lateinit var voice: Voice
    private val REQUEST_RECORD_AUDIO_PERMISSION = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            } else if (event.action == MotionEvent.ACTION_UP) {
                voice.stopRecording()
                v.setBackgroundResource(R.drawable.circle_button_normal)
                v.performClick()
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
