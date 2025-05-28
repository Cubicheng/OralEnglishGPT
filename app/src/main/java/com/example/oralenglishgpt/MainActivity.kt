package com.example.oralenglishgpt

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.Surface
import com.example.oralenglishgpt.theme.MainTheme
import com.example.oralenglishgpt.ui.SpeechRecognitionScreen
import com.iflytek.cloud.SpeechConstant
import com.iflytek.cloud.SpeechUtility

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=0be2db3b")

        super.onCreate(savedInstanceState)
        setContent {
            MainTheme {
                Surface {
//                    ChatScreen()
                    SpeechRecognitionScreen();
                }
            }
        }
    }
}

