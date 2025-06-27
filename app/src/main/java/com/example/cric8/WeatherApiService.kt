package com.example.cric8

import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("current")
    suspend fun getWeather(
        @Query("access_key") accessKey: String = "9692e11792b09b9c9e48f00092eefc7e",
        @Query("query") city: String
    ): WeatherResponse
}
