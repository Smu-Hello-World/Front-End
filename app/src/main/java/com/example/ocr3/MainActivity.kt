package com.example.ocr3

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

import com.example.ocr3.utils.JsonManager

class MainActivity : AppCompatActivity() {

    private lateinit var btnLoad: Button
    private lateinit var txtResult: TextView

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        btnLoad =
            findViewById(R.id.btnLoad)

        txtResult =
            findViewById(R.id.txtResult)

        // 알림 접근 권한 설정창 열기
        openNotificationAccessSettings()

        // 버튼 누르면 저장된 JSON 출력
        btnLoad.setOnClickListener {

            val data =
                JsonManager.readBankData(this)

            txtResult.text = data
        }
    }

    private fun openNotificationAccessSettings() {

        startActivity(
            Intent(
                "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"
            )
        )
    }
}