from aip import AipSpeech

def baidu_Speech_To_Text(filePath):  # 百度语音识别
    """ 你的 APPID AK SK """
    APP_ID = 'xxx'
    API_KEY = 'xxx'
    SECRET_KEY = 'xxx'
    aipSpeech = AipSpeech(APP_ID, API_KEY, SECRET_KEY)  # 初始化AipSpeech对象
    # 读取文件
    with open(filePath, 'rb') as fp:
        audioPcm = fp.read()
    json = aipSpeech.asr(audioPcm, 'pcm', 16000, {'dev_pid':1537,})
    print(json)
    if 'success' in json['err_msg']:
        context = json['result'][0]
        print('成功，返回结果为：', context)
    else:
        context = '=====识别失败====='
        print('识别失败！')
    return context