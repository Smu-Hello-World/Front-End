package com.example.ocr3.utils

object NotificationParser {

    data class ParseResult(

        val type: String,

        val money: String,

        val sender: String,

        val receiver: String
    )

    fun parse(text: String): ParseResult {

        // 입금 / 출금 추출
        val type = when {

            text.contains("입금") -> "입금"

            text.contains("출금") -> "출금"

            else -> "알 수 없음"
        }

        // 금액 추출
        val moneyRegex = Regex("([0-9,]+)원")

        val money = moneyRegex
            .find(text)
            ?.groupValues?.get(1)
            ?.replace(",", "")
            ?: "0"

        // 보내는 사람 → 받는 사람
        val arrowRegex =
            Regex("(.+)\\s→\\s(.+)")

        val arrowMatch =
            arrowRegex.find(text)

        var sender = ""
        var receiver = ""

        if (arrowMatch != null) {

            sender =
                arrowMatch.groupValues[1]
                    .trim()

            receiver =
                arrowMatch.groupValues[2]
                    .trim()
        }

        return ParseResult(

            type,
            money,
            sender,
            receiver
        )
    }
}