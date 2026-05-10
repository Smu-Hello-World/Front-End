package com.example.ocr3.utils

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object JsonManager {

    fun saveBankData(
        context: Context,
        type: String,
        money: String,
        sender: String,
        receiver: String
    ) {

        val jsonObject = JSONObject()

        jsonObject.put("type", type)
        jsonObject.put("money", money)
        jsonObject.put("sender", sender)

        jsonObject.put("receiver", receiver)
        jsonObject.put(
            "time",
            System.currentTimeMillis()
        )

        val file = File(
            context.filesDir,
            "bank_data.json"
        )

        val jsonArray = if (file.exists()) {

            JSONArray(file.readText())

        } else {

            JSONArray()
        }

        jsonArray.put(jsonObject)

        file.writeText(jsonArray.toString())
    }

    fun readBankData(
        context: Context
    ): String {

        val file = File(
            context.filesDir,
            "bank_data.json"
        )

        if (!file.exists()) {
            return "데이터 없음"
        }

        return file.readText()
    }
}