package com.example.compose.screens

data class Transaction(

    val type: String,

    val money: String,

    val sender: String,

    val receiver: String,

    val time: Long
)