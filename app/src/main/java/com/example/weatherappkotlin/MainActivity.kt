package com.example.weatherappkotlin

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import com.example.weatherappkotlin.databinding.ActivityMainBinding
import com.example.weatherappkotlin.models.Sys
import com.example.weatherappkotlin.models.WeatherApp
import com.example.weatherappkotlin.retrofit.ApiInterface
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fetchWeatherData("Nukus")

        searchCity()

    }

    private fun searchCity() {
        binding.etSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    fetchWeatherData(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
    }

    private fun fetchWeatherData(cityName: String) {
        val retrofit = Retrofit.Builder().addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.openweathermap.org/data/2.5/").build()
            .create(ApiInterface::class.java)

        val response = retrofit.getWeatherData(
            cityName, "d556a2a7371b28a0a7ebce08a25a9331", "metric"
        )

        response.enqueue(object : Callback<WeatherApp> {
            @SuppressLint("SimpleDateFormat")
            override fun onResponse(call: Call<WeatherApp>, response: Response<WeatherApp>) {
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    val temperature = responseBody.main.temp
                    val humidity = responseBody.main.humidity
                    val windSpeed = responseBody.wind.speed
                    val sunrise = responseBody.sys.sunrise
                    val sunset = responseBody.sys.sunset
                    val seaLevel = responseBody.main.pressure
                    val condition = responseBody.weather.firstOrNull()?.main ?: "unknown"
                    val maxTemp = responseBody.main.temp_max
                    val minTemp = responseBody.main.temp_min

                    binding.tvWeather.text = condition
                    binding.tvMax.text = "Max: ${maxTemp.toInt()}"
                    binding.tvMin.text = "Min: ${minTemp.toInt()}"
                    binding.tvNumberHumidity.text = "$humidity%"
                    binding.tvNumberWind.text = "$windSpeed m/s"
                    binding.tvNumberSunrise.text =
                        SimpleDateFormat("HH:mm").format(Date(("$sunrise" + "000").toLong()))
                    binding.tvNumberSunset.text =
                        SimpleDateFormat("HH:mm").format(Date(("$sunset" + "000").toLong()))
                    binding.tvDayMonthYear.text =
                        SimpleDateFormat("dd - MMMM, yyyy").format(Date(System.currentTimeMillis()))
                    binding.tvWeekday.text =
                        SimpleDateFormat("EEEE").format(Date(System.currentTimeMillis()))
                    binding.tvTemperature.text = temperature.toInt().toString()
                    binding.tvNumberSea.text = seaLevel.toString()
                    binding.cityName.text = cityName

                    changeImagesAccordingToWeatherCondition(condition)
                }
            }

            override fun onFailure(call: Call<WeatherApp>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Not found!!!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun changeImagesAccordingToWeatherCondition(condition: String) {
        when (condition) {
            "Clear Sky", "Sunny", "Clear" -> {
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.sun)
            }

            "Partly Clouds", "Clouds", "Overcast", "Mist", "Foggy" -> {
                binding.root.setBackgroundResource(R.drawable.colud_background)
                binding.lottieAnimationView.setAnimation(R.raw.cloud)
            }

            "Light Rain", "Drizzle", "Moderate Rain", "Showers", "Heavy Rain" -> {
                binding.root.setBackgroundResource(R.drawable.rain_background)
                binding.lottieAnimationView.setAnimation(R.raw.rain)
            }

            "Light Snow", "Blizzard", "Moderate Snow", "Heavy Snow" -> {
                binding.root.setBackgroundResource(R.drawable.snow_background)
                binding.lottieAnimationView.setAnimation(R.raw.snow)
            }


        }
        binding.ivTypeOfWeather.visibility = View.INVISIBLE
        binding.lottieAnimationView.playAnimation()
    }

}