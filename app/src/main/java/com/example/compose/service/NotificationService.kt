package com.example.compose.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.compose.data.DataManager
import com.example.compose.utils.JsonManager
import com.example.compose.utils.NotificationParser

class NotificationService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {

        Log.d(
            "NOTI_TEST",
            "알림 수신됨"
        )
        super.onNotificationPosted(sbn)

        if (sbn == null) return

        // 카카오뱅크만 허용
        val packageName = sbn.packageName

        if (packageName != "com.kakaobank.channel") {
            return
        }

        val extras = sbn.notification.extras

        val title =
            extras.getString("android.title") ?: ""

        val text =
            extras.getCharSequence("android.text")
                ?.toString() ?: ""

        val fullText =
            "$title\n$text"

        Log.d("RAW_TEXT", fullText)

        // 입금/출금 알림만 처리
        if (
            !fullText.contains("입금") &&
            !fullText.contains("출금")
        ) {
            return
        }

        val result =
            NotificationParser.parse(fullText)

        Log.d(
            "OCR_RESULT",
            result.toString()
        )

        // 금액 파싱 실패 시 저장 안함
        if (
            result.money.isBlank() ||
            result.money == "0"
        ) {

            Log.d(
                "SAVE",
                "금액 없음 → 저장 안함"
            )

            return
        }

        saveData(
            result.type,
            result.money,
            result.sender,
            result.receiver
        )
    }

    private fun saveData(
        type: String,
        money: String,
        sender: String,
        receiver: String
    ) {
        Log.d(
            "SAVE_TEST",
            "saveData 호출"
        )

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