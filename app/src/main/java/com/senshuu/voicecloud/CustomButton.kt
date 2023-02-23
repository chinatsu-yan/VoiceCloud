package com.senshuu.voicecloud

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatButton

class CustomButton(context: Context, attrs: AttributeSet?) : AppCompatButton(context, attrs) {

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    init {
        setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                // do something
            } else if (event.action == MotionEvent.ACTION_UP) {
                // do something
                v.performClick() // 添加 performClick() 调用
            }
            true
        }
    }
}
