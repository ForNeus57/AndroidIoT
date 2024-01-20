package android.iot

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.request
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class DeviceReadingsActivity : AppCompatActivity() {

    suspend fun sendListDeviceReadingsRequest(deviceId: String) : Map<String, String> {
        val apiUrl = "https://vye4bu6645.execute-api.eu-north-1.amazonaws.com/default"
        val dataUrl = "$apiUrl/data"

        val response = HttpClient(CIO).request(dataUrl) {
            method = io.ktor.http.HttpMethod.Get
            headers.append("Content-Type", "application/json")
            url { parameters.append("device_id", deviceId) }
        }

        val responseMap = Json.parseToJsonElement(response.bodyAsText()).jsonObject.toMap()

        return responseMap.mapValues { it.value.toString() }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_device_readings)

        val backButton = findViewById<View>(R.id.backButton) as ImageButton
        backButton.setOnClickListener {
            val intentMain = Intent(
                this@DeviceReadingsActivity,
                AccountActivity::class.java
            )
            this@DeviceReadingsActivity.startActivity(intentMain)
            Log.i("Content ", " Main layout ")
        }

        val deviceId = intent.getStringExtra("device_id")
        println(deviceId)

        lifecycleScope.launch {
            val response = sendListDeviceReadingsRequest(deviceId!!)
            Log.i("Content ", response.toString())

            val readings = getReadings(response["data"]!!)

            var devicesListView = findViewById<ListView>(R.id.lvDevices)
            val arrayAdapter: ArrayAdapter<*>
            arrayAdapter = ArrayAdapter(this@DeviceReadingsActivity, R.layout.basic_list_element, readings)
            devicesListView.adapter = arrayAdapter
        }
    }

    private fun getReadings(data: String): List<String> {
        var readings =
            Json.parseToJsonElement(data).jsonArray.map { it.jsonObject.toMap() }
        return buildList<String> {
            for (reading in readings) {
                val timestamp = reading["time"].toString().toLong()
                val instant = Instant.ofEpochMilli(timestamp)
                val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                val formattedDate = localDateTime.format(formatter)

                add(formattedDate + ": " + reading["value"].toString())
            }
        }
    }
}