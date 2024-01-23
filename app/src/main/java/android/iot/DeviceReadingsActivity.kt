package android.iot

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ListView
import android.widget.ProgressBar
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

    private suspend fun sendListDeviceReadingsRequest(deviceId: String): Map<String, String> {
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
                this@DeviceReadingsActivity, UserDevices::class.java
            )
            this@DeviceReadingsActivity.startActivity(intentMain)
        }

        val deviceId = intent.getStringExtra("device_id")
        println(deviceId)

        lifecycleScope.launch {
            val response = sendListDeviceReadingsRequest(deviceId!!)
            val spinner = findViewById<ProgressBar>(R.id.progressBar)
            spinner.visibility = View.GONE
            Log.i("Content ", response.toString())

            val readings = getReadings(response["data"]!!)

            if (readings.isEmpty()) {
                val builder: AlertDialog.Builder = AlertDialog.Builder(this@DeviceReadingsActivity)
                builder.setMessage("Please wait for device to send data!").setTitle("No readings!")

                val dialog: AlertDialog = builder.create()
                dialog.show()
            } else {

                val devicesListView = findViewById<ListView>(R.id.lvDevices)
                val arrayAdapter: ArrayAdapter<*>
                arrayAdapter =
                    ArrayAdapter(this@DeviceReadingsActivity, R.layout.basic_list_element, readings)
                devicesListView.adapter = arrayAdapter
            }

        }
    }

    private fun getReadings(data: String): List<String> {
        val readings = Json.parseToJsonElement(data).jsonArray.map { it.jsonObject.toMap() }
        return buildList {
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