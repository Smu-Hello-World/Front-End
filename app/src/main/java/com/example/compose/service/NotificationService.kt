package com.example.compose.service

import com.example.compose.data.DataManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.compose.receiver.BootReceiver
import com.example.compose.utils.NotificationParser
import com.example.compose.utils.JsonManager


class NotificationService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        if (sbn == null) return

        val extras = sbn.notification.extras

        val title = extras.getString("android.title") ?: ""
        val text = extras.getCharSequence("android.text")?.toString() ?: ""
        val fullText =
            "$title\n$text"

        Log.d("알림", "제목: $title")
        Log.d("알림", "내용: $text")

        if (
            text.contains("입금") ||
            text.contains("출금")
        ) {

            val result = NotificationParser.parse(fullText)

            Log.d("OCR_RESULT", result.toString())

            saveData(
                result.type,
                result.money,
                result.sender,
                result.receiver
            )
        }
    }

    private fun saveData(
        type: String,
        money: String,
        sender: String,
        receiver: String,
    ) {
        Log.d("SAVE", "저장 실행됨")

        DataManager.lastType = type

        DataManager.lastMoney = money

        DataManager.lastSender = sender

        DataManager.lastReceiver = receiver

        DataManager.lastMessage =
            "$type $money $sender → $receiver"

        JsonManager.saveBankData(
            this,
            type,
            money,
            sender,
            receiver
        )
    }
}