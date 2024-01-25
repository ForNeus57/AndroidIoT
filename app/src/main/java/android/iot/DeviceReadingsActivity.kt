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

    // API call to list device's readings.
    private suspend fun sendListDeviceReadingsRequest(
        deviceId: String,
        sessionId: String,
        username: String
    ): Map<String, String> {
        val apiUrl = "https://vye4bu6645.execute-api.eu-north-1.amazonaws.com/default"
        val dataUrl = "$apiUrl/data"

        val response = HttpClient(CIO).request(dataUrl) {
            method = io.ktor.http.HttpMethod.Get
            headers.append("Content-Type", "application/json")
            headers.append("session_id", sessionId)
            url {
                parameters.append("username", username)
                parameters.append("device_id", deviceId)
                protocol = io.ktor.http.URLProtocol.HTTPS
            }
        }

        val responseMap = Json.parseToJsonElement(response.bodyAsText()).jsonObject.toMap()
        println(responseMap)

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

        val refreshButton = findViewById<View>(R.id.refreshButton) as ImageButton
        refreshButton.setOnClickListener {
            val intentMain = Intent(
                this@DeviceReadingsActivity, DeviceReadingsActivity::class.java
            )
            intentMain.putExtra("device_id", intent.getStringExtra("device_id"))
            this@DeviceReadingsActivity.startActivity(intentMain)
        }

        // Get the device ID from the intent that started this View.
        val deviceId = intent.getStringExtra("device_id")
        // Get the session ID and username from the shared preferences.
        val sessionId = getSharedPreferences(
            UserDevices.SHARED_PREFS,
            MODE_PRIVATE
        ).getString(UserDevices.SESSION_ID, null)
        val username = getSharedPreferences(
            UserDevices.SHARED_PREFS,
            MODE_PRIVATE
        ).getString(UserDevices.USERNAME, null)

        // API call to list device's readings. We do this in a coroutine.
        lifecycleScope.launch {
            val response = sendListDeviceReadingsRequest(deviceId!!, sessionId!!, username!!)
            val spinner = findViewById<ProgressBar>(R.id.progressBar)
            spinner.visibility = View.GONE
            Log.i("Content ", response.toString())

            var readings = getReadings(response["data"]!!)
            readings = readings.sorted()

            // If there are no readings, show a dialog.
            if (readings.isEmpty()) {
                val builder: AlertDialog.Builder = AlertDialog.Builder(this@DeviceReadingsActivity)
                builder.setMessage("Please wait for device to send data!").setTitle("No readings!")

                val dialog: AlertDialog = builder.create()
                dialog.show()
            } else {
                // Show the readings in a list.
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
                // Convert timestamp to date
                val timestamp = reading["time"].toString().toLong()
                val instant = Instant.ofEpochMilli(timestamp)
                val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                val formattedDate = localDateTime.format(formatter)

                val value = reading["value"].toString()

                add(
                    "$formattedDate: $value " + (if (value.toDouble() > 700) "Light!" else "Darkness...")
                )
            }
        }
    }
}