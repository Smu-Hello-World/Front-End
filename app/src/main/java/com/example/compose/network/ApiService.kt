package com.example.compose.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("analyze")

    fun analyzeConsumption(

        @Body request: List<AnalyzeRequest>

    ): Call<AnalyzeResponse>
}