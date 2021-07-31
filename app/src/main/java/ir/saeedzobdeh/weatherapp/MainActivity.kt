package ir.saeedzobdeh.weatherapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.google.android.gms.location.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import ir.saeedzobdeh.weatherapp.databinding.ActivityMainBinding
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private var currentCity = "tehran" //"tehran"
    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase!!))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        getData()
        setupNavigation()
    }

    fun setupNavigation(){
        val nav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val infoLayout = findViewById<View>(R.id.info_parent)
        val homeLayout = findViewById<View>(R.id.home_parent)
        nav.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.menu_home->{
                    Log.d("menu","home")
                    infoLayout.visibility = View.GONE
                    homeLayout.visibility = View.VISIBLE
                }
                R.id.menu_info->{
                    Log.d("menu","info")
                    infoLayout.visibility = View.VISIBLE
                    homeLayout.visibility = View.GONE
                }
            }
            true
        }
    }

    private fun showContent(
        cityName: String,
        weatherDescription: String,
        imageUrl: String,
        sunrise: Int,
        sunset: Int,
        temp: Double,
        feelsLike: Double,
        tempMin: Double,
        tempMax: Double,
        pressure: Int,
        humidity: Int,
        windDeg: Int,
        windSpeed: Int
    ) {

        binding.refresh.setOnClickListener() {
            reLoadData()
        }

//--------   search by the City ----------------
//        binding.buttonMashhad.setOnClickListener() {
//            currentCity = "mashhad"
//            reLoadData()
//        }
// --------   search by the City ----------------

        binding.refresh.visibility = View.VISIBLE
        binding.progressBar.visibility = View.INVISIBLE

        binding.textViewCityName.text = cityName
        binding.textViewWeatherDescription.text = weatherDescription
        binding.textViewSunrise.text = getTimeFromUnixTime(sunrise)
        binding.textViewSunset.text = getTimeFromUnixTime(sunset)

        binding.textViewTemp.text = " °C دما : ${temp}"
        binding.textViewfeelsLike.text = " °C دما مستقیم : ${feelsLike}"
        binding.textViewtempMin.text = " °C حداقل دما : ${tempMin}"
        binding.textViewtempMax.text = " °C حداکثر دما : ${tempMax}"

    }

    private fun getTimeFromUnixTime(unixTime: Int): String {
        val time = unixTime * 1000.toLong() // int -> long
        val date = Date(time) // mon 2021/05/01 12:15:54 Am
        val formatter = SimpleDateFormat("HH:mm a")
        return formatter.format(date) // 12:15 Am
    }

    private fun getData() {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://api.openweathermap.org/data/2.5/weather?q=${currentCity}&appid=40c2fae6f3611c044dde04b13bde5451&lang=fa&units=metric")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(
                        applicationContext,
                        "ارتباط اینترنت را بررسی کنید",
                        Toast.LENGTH_LONG
                    ).show()
                    binding.refresh.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.INVISIBLE
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val rawContent = response.body!!.string()
                getDataToShow(rawContent)
            }

        })
    }

    private fun getDataToShow(rawData: String) {
        val jsonObject = JSONObject(rawData)

        val sunrise = jsonObject.getJSONObject("sys").getInt("sunrise")
        val sunset = jsonObject.getJSONObject("sys").getInt("sunset")

        val temp = jsonObject.getJSONObject("main").getDouble("temp")
        val feelsLike = jsonObject.getJSONObject("main").getDouble("feels_like")
        val tempMin = jsonObject.getJSONObject("main").getDouble("temp_min")
        val tempMax = jsonObject.getJSONObject("main").getDouble("temp_max")
        val pressure = jsonObject.getJSONObject("main").getInt("pressure")
        val humidity = jsonObject.getJSONObject("main").getInt("humidity")

        val windSpeed = jsonObject.getJSONObject("wind").getInt("speed")
        val windDeg = jsonObject.getJSONObject("wind").getInt("deg")

        val weatherArray = jsonObject.getJSONArray("weather")
        val weatherObject = weatherArray.getJSONObject(0)
        val iconId = weatherObject.getString("icon")
        val imageUrl = "https://openweathermap.org/img/wn/${iconId}@2x.png"
        runOnUiThread {
            showContent(
                jsonObject.getString("name"),
                weatherObject.getString("description"),
                imageUrl,
                sunrise,
                sunset,
                temp,
                feelsLike,
                tempMin,
                tempMax,
                pressure,
                humidity,
                windDeg,
                windSpeed
            )
        }
    }

    fun reLoadData() {
        binding.refresh.visibility = View.INVISIBLE
        binding.progressBar.visibility = View.VISIBLE

        binding.textViewTemp.text = "--"
        binding.textViewfeelsLike.text = "--"
        binding.textViewtempMin.text = "--"
        binding.textViewtempMax.text = "--"
        binding.textViewSunset.text = "--"
        binding.textViewSunrise.text = "--"
        binding.textViewWeatherDescription.text = "--"
        getData()
    }


}