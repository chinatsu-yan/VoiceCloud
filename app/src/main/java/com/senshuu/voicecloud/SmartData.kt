package com.senshuu.voicecloud

class SmartData {

    var CurtainsText = ""
    var WateringText = ""
    var DataText = ""
    var VoiceDataText = "长按下方按钮开始录音识别"

    fun smartData() {
        val CurtainsNum = 80
        CurtainsText = "实时进度：$CurtainsNum %"

        val WateringNum = 12
        WateringText = "实时进度：$WateringNum 分钟"

        val Equipments = "窗帘" //窗帘、浇水
        val Commands = "拉上"   //拉上、打开
        val Parameters = 50
        val Units = "%" // % 、 分钟
        DataText = "设备：$Equipments | 命令：$Commands | 参数：$Parameters | 单位：$Units"

//        val VoiceData = "打开水泵浇水10分钟"
//        VoiceDataText = "$VoiceData"
    }
}
