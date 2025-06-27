package com.example.cric8

data class WeatherResponse(
    val location: Location,
    val current: Current
)

data class Location(
    val name: String,
    val country: String,
    val localtime: String
)

data class Current(
    val temperature: Int,
    val weather_descriptions: List<String>,
    val weather_icons: List<String>,
    val humidity: Int,
    val wind_speed: Int,
    val pressure: Int,
    val feelslike: Int
)
