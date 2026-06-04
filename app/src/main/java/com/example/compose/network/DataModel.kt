package com.example.compose.network

data class AnalyzeRequest(

    val money: String,
    val type: String,
    val receiver: String
)

data class AnalyzeResponse(

    val income_money: Int,

    val outcome_money: Int,

    val taxi_money: Int,

    val cvs_money: Int,

    val shop_money: Int,

    val cafe_money: Int,

    val food_money: Int,

    val saving_rate: Double
)