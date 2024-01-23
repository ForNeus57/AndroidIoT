package android.iot

import android.content.Intent
import android.iot.lists.devices.Device
import android.iot.lists.devices.UserListDeviceAdapter
import android.iot.lists.devices.UserRecyclerViewClickListener
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
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

class UserDevices : AppCompatActivity() {

    companion object {
        const val SHARED_PREFS = "sharedPrefs"
        const val USERNAME = "username"
        const val LOGGED_IN = "loggedIn"
    }

    private val devices = ArrayList<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_devices)


        val back = findViewById<ImageButton>(R.id.backButton)
        back.setOnClickListener {
            val intentMain = Intent(
                this@UserDevices, MainActivity::class.java
            )
            this@UserDevices.startActivity(intentMain)
        }

        val addButton = findViewById<ImageButton>(R.id.addDevice)
        addButton.setOnClickListener {
            val intentPairedDeviceList = Intent(
                this@UserDevices, PairedDeviceListActivity::class.java
            )

            intentPairedDeviceList.putExtra("devicesAddresses", devices)
            this@UserDevices.startActivity(intentPairedDeviceList)
        }

        lifecycleScope.launch {
            val data = this@UserDevices.getData()
            val spinner = findViewById<ProgressBar>(R.id.progressBar)
            spinner.visibility = View.GONE

            if (data.isEmpty()) {
                Toast.makeText(
                    this@UserDevices, "No devices found!", Toast.LENGTH_LONG
                ).show()
            }

            val listener = object : UserRecyclerViewClickListener() {
                override fun onClick(index: Int) {
                    super.onClick(index)

                    val intentDeviceReadings = Intent(
                        this@UserDevices, DeviceReadingsActivity::class.java
                    )

                    intentDeviceReadings.putExtra("device_id", data[index].uuid)

                    this@UserDevices.startActivity(intentDeviceReadings)
                }
            }

            val adapter = UserListDeviceAdapter(data, this@UserDevices, listener)

            val recyclerView =
                findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.itemsList)

            recyclerView.adapter = adapter
            recyclerView.layoutManager =
                androidx.recyclerview.widget.LinearLayoutManager(this@UserDevices)
        }

    }


    private suspend fun getData(): ArrayList<Device> {
        val output = ArrayList<Device>()
        val response = sendListDevicesRequest(
            getSharedPreferences(SHARED_PREFS, MODE_PRIVATE).getString(
                USERNAME, ""
            )!!
        )
        val devices = getDeviceNameToDeviceIdMap(response["data"]!!)

        for (device in devices) {
            output.add(Device(device.key, device.value, device.value, output))
        }

        return output
    }

    private suspend fun sendListDevicesRequest(username: String): Map<String, String> {
        val apiUrl = "https://vye4bu6645.execute-api.eu-north-1.amazonaws.com/default"
        val devicesUrl = "$apiUrl/devices"

        val response = HttpClient(CIO).request(devicesUrl) {
            method = io.ktor.http.HttpMethod.Get
            headers.append("Content-Type", "application/json")
            url { parameters.append("username", username) }
        }

        val responseMap = Json.parseToJsonElement(response.bodyAsText()).jsonObject.toMap()

        return responseMap.mapValues { it.value.toString() }
    }

    private fun getDeviceNameToDeviceIdMap(data: String): Map<String, String> {
        val devices = Json.parseToJsonElement(data).jsonArray.map { it.jsonObject.toMap() }
        val devicesMap = buildMap {
            for ((index, device) in devices.withIndex()) {
                val deviceId = device["device_id"].toString().replace("\"", "")
                put("Device $index", deviceId)
            }
        }
        return devicesMap
    }
}