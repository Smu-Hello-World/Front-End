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

    val saving_rate: Double,

    val play_money: Int,

    val spending_rate: Double,
    val spending_status: String,

    val food_rate: Double,
    val food_status: String,

    val cafe_rate: Double,
    val cafe_status: String,

    val taxi_rate: Double,
    val taxi_status: String,

    val shop_rate: Double,
    val shop_status: String,

    val cvs_rate: Double,
    val cvs_status: String,

    val play_rate: Double,
    val play_status: String
)