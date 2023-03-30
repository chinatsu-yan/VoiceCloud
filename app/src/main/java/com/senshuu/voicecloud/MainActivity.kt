package com.senshuu.voicecloud

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.senshuu.voicecloud.databinding.ActivityMainBinding

import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import kotlinx.coroutines.delay
import java.io.File

import kotlinx.coroutines.launch

private const val TAG = "MyActivity"
class MainActivity : AppCompatActivity() {

    // 使用 lateinit 延迟初始化绑定实例
    lateinit var binding: ActivityMainBinding
    // 创建 SmartData 的实例
    val smartData = SmartData()
    // 创建 Voice 的实例
    private lateinit var voice: Voice
    // 请求录音权限的请求码
    private val REQUEST_RECORD_AUDIO_PERMISSION = 200
    // 创建 TCP 服务器实例并绑定到 8080 端口
    private val server = TCPServer(8080)




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 如果 Python 尚未启动，则启动 Python
        if (! Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }
        // 获取 AipSpeech 模块的实例
        val py = Python.getInstance()
        val module = py.getModule("AipSpeech")
        // 获取 PCM 文件路径
        val filePath = File(Environment.getExternalStorageDirectory(), "Android/data/com.senshuu.voicecloud/files/recording.pcm").path
        // 启动 TCP 服务器
        server.start()

        // 使用 DataBindingUtil 绑定布局
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        // 绑定 SmartData 实例到布局中
        binding.smartData = smartData

        // 调用 SmartData 的方法初始化数据
        smartData.smartData()

        // 检查是否具有录音权限，如果没有，请求权限
        val permissions = arrayOf(Manifest.permission.RECORD_AUDIO)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)
        }

        // 监听录音按钮的 onTouch 事件
        binding.btnRecord.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                // 如果按下了录音按钮，则开始录音
                voice = Voice(getExternalFilesDir(null)?.absolutePath + "/recording.pcm")
                voice.startRecording()
                v.setBackgroundResource(R.drawable.circle_button_pressed)
                // 显示“识别中...”文本
                smartData.VoiceDataText = "识别中..."
                val voiceDataTextView = findViewById<TextView>(R.id.VoiceDatas)
                voiceDataTextView.text = smartData.VoiceDataText
            } else if (event.action == MotionEvent.ACTION_UP) {
                // 如果松开了录音按钮，则停止录音，同时将按钮背景设置为普通状态，并模拟点击按钮
                voice.stopRecording()
                v.setBackgroundResource(R.drawable.circle_button_normal)
                v.performClick()

                // 获取录音文件的路径
                val filePath = "${getExternalFilesDir(null)?.absolutePath}/recording.pcm"
                val file = File(filePath)
                // 如果录音文件存在，则调用百度语音识别 API 进行语音识别
                if (file.exists()) {
                    val result = module.callAttr("baidu_Speech_To_Text", filePath)
                    smartData.VoiceDataText = result.toString()
                    val voiceDataTextView = findViewById<TextView>(R.id.VoiceDatas)
                    voiceDataTextView.text = smartData.VoiceDataText



                    // 使用正则表达式匹配语音指令中的参数和单位
                    val regex1 = Regex("^(打开浇水|开启浇水).*?(\\d+).*?(分钟|秒).*$")
                    val regex2 = Regex("^(打开|关闭)窗帘.*$")
                    //val pattern = Regex("(打开|关闭)窗帘|(打开|开启)浇水(\\d+)(秒|分钟)")

                    if (regex1.matches(smartData.VoiceDataText)) {
                        println("Matched regex1: ${smartData.VoiceDataText}")
                        // 如果输入字符串匹配第一个正则表达式，执行以下语句
                        val matchResult = regex1.matchEntire(smartData.VoiceDataText)
                        val command = matchResult?.groupValues?.get(1)
                        val time = matchResult?.groupValues?.get(2)//?.toInt()
                        val unit = when (matchResult?.groupValues?.get(3)) {
                            "分钟" -> "min"
                            "秒" -> "s"
                            else -> ""
                        }
                        if (time != null) {
                            lifecycleScope.launch {
                                countdown(time, unit)
                            }
                        }
                        println("Command: $command, Duration: $time $unit")
                    } else if (regex2.matches(smartData.VoiceDataText)) {
                        println("Matched regex2: ${smartData.VoiceDataText}")
                        // 如果输入字符串匹配第二个正则表达式，执行以下语句
                        val matchResult = regex2.matchEntire(smartData.VoiceDataText)
                        val command = matchResult?.value
                        if (command != null) {
                            lifecycleScope.launch {
                                useCurtains(command)
                            }
                        }
                        println("Command: $command")
                    } else {
                        println("Input does not match any pattern: ${smartData.VoiceDataText}")
                        // 如果输入字符串都不匹配，执行以下语句
                        println("Input does not match any pattern.")
                    }



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


    // 倒计时功能，接收参数和单位，计算出总秒数并进行倒计时
    private fun countdown(time: String, unit: String) {

        Log.d(TAG, "执行了 countdown 函数，time=$time, unit=$unit")
        // 将字符串类型的时间参数转为整型
        val timeValue = try {
            time.toInt()
        } catch (e: NumberFormatException) {
            throw IllegalArgumentException("Invalid time format")
        }

        // 根据单位计算出总秒数
        val totalSeconds = when (unit) {
            "min" -> timeValue * 60
            "s" -> timeValue
            else -> throw IllegalArgumentException("Invalid unit")
        }
        var remainingSeconds = totalSeconds

        // 在 UI 线程外进行倒计时，每隔一秒更新一次 UI 上的倒计时信息
        val handler = Handler()

        lifecycleScope.launch {

            // 在倒计时开始时更新 UI 上的提示信息
            smartData.DataText = "设备：浇水 | 命令：打开 | 参数：$time | 单位：$unit"
            val dataTextView = findViewById<TextView>(R.id.Datas)
            dataTextView.text = smartData.DataText

            // 向服务器发送浇水指令，启动浇水功能
            server.broadcast("waterStart\r\n")

            // 开始倒计时
            while (remainingSeconds > 0) {
                val minutes = remainingSeconds / 60
                val seconds = remainingSeconds % 60

                smartData.WateringText = "$minutes 分 $seconds 秒"
                val waterDataTextView = findViewById<TextView>(R.id.Watering)
                waterDataTextView.text = smartData.WateringText

                delay(1000)
                remainingSeconds--
            }

            // 向服务器发送停止浇水指令，关闭浇水功能
            server.broadcast("waterStop\r\n")
            smartData.WateringText = "已关闭"
            val waterDataTextView = findViewById<TextView>(R.id.Watering)
            waterDataTextView.text = smartData.WateringText
        }
    }

    private fun useCurtains(command1: String) {
        Log.d(TAG, "执行了 useCurtains 函数，command=$command1")
        var remainingSeconds = 6
        lifecycleScope.launch {
            // 在倒计时开始时更新 UI 上的提示信息
            smartData.DataText = "设备：窗帘 | 命令：$command1 "
            val dataTextView = findViewById<TextView>(R.id.Datas)
            dataTextView.text = smartData.DataText

            // 向服务器发送指令，启动浇水功能
            if (command1 == "打开窗帘。") {
                // 执行打开的操作
                server.broadcast("curtainsOpen\r\n")
                while (remainingSeconds > 0) {

                    val per = when (remainingSeconds) {
                        6 -> 0
                        5 -> 20
                        4 -> 40
                        3 -> 60
                        2 -> 80
                        1 -> 100
                        else -> 200
                    }

                    smartData.CurtainsText = "实时进度：$per%"
                    val curtainsDataTextView = findViewById<TextView>(R.id.Curtains)
                    curtainsDataTextView.text = smartData.CurtainsText

                    delay(1000)
                    remainingSeconds--
                }
                server.broadcast("curtainsStop\r\n")
                smartData.CurtainsText = "已开启"
                val curtainsDataTextView = findViewById<TextView>(R.id.Curtains)
                curtainsDataTextView.text = smartData.CurtainsText
            } else if (command1 == "关闭窗帘。") {
                // 执行关闭的操作
                server.broadcast("curtainsClose\r\n")
                while (remainingSeconds > 0) {

                    val per = when (remainingSeconds) {
                        6 -> 0
                        5 -> 20
                        4 -> 40
                        3 -> 60
                        2 -> 80
                        1 -> 100
                        else -> 200
                    }

                    smartData.CurtainsText = "实时进度：$per%"
                    val curtainsDataTextView = findViewById<TextView>(R.id.Curtains)
                    curtainsDataTextView.text = smartData.CurtainsText

                    delay(1000)
                    remainingSeconds--
                }
                server.broadcast("curtainsStop\r\n")
                smartData.CurtainsText = "已关闭"
                val curtainsDataTextView = findViewById<TextView>(R.id.Curtains)
                curtainsDataTextView.text = smartData.CurtainsText
            } else {
                // 其他操作
            }

        }
    }



}
