package com.example.compose.utils

object NotificationParser {

    data class ParseResult(

        val type: String,

        val money: String,

        val sender: String,

        val receiver: String
    )

    fun parse(text: String): ParseResult {

        val type = when {

            text.contains("입금") -> "입금"

            text.contains("출금") -> "출금"

            else -> "알 수 없음"
        }

        val moneyRegex =
            Regex("(입금|출금)\\s*([0-9,]+)원")

        val money =
            moneyRegex.find(text)
                ?.groupValues?.get(2)
                ?.replace(",", "")
                ?: "0"

        var sender = ""
        var receiver = ""

        val arrowRegex =
            Regex("(.+)\\s→\\s(.+)")

        val match =
            arrowRegex.find(text)

        if (match != null) {

            sender =
                match.groupValues[1]
                    .trim()

            receiver =
                match.groupValues[2]
                    .trim()
        }

        return ParseResult(

            type = type,

            money = money,

            sender = sender,

            receiver = receiver
        )
    }
}