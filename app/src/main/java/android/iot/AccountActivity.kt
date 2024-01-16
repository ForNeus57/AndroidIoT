package android.iot

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
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

class AccountActivity : AppCompatActivity() {
    companion object {
        const val SHARED_PREFS = "sharedPrefs"
        const val USERNAME = "username"
        const val LOGGED_IN = "loggedIn"
    }

    suspend fun sendListDevicesRequest(username: String) : Map<String, String> {
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
        val userLoggedIn = sharedPreferences.getBoolean(LOGGED_IN, false)

        if (!userLoggedIn) {
            val intentLogin = Intent(
                this@AccountActivity,
                LoginActivity::class.java
            )
            this@AccountActivity.startActivity(intentLogin)
        } else {

            setContentView(R.layout.activity_account)
            val username = sharedPreferences.getString(USERNAME, "")

            val tvUsername = findViewById<View>(R.id.tvUsername) as TextView
            tvUsername.text = username

            val backButton = findViewById<View>(R.id.backButton) as ImageButton
            backButton.setOnClickListener {
                val intentMain = Intent(
                    this@AccountActivity,
                    MainActivity::class.java
                )
                this@AccountActivity.startActivity(intentMain)
                Log.i("Content ", " Main layout ")
            }

            val logoutButton = findViewById<View>(R.id.btnLogout)
            logoutButton.setOnClickListener {
                val intentMain = Intent(
                    this@AccountActivity,
                    MainActivity::class.java
                )
                val editor = sharedPreferences.edit()
                editor.putBoolean(LOGGED_IN, false)
                editor.apply()
                this@AccountActivity.startActivity(intentMain)
                Log.i("Content ", "Login layout")
            }

            lifecycleScope.launch {
                var response = sendListDevicesRequest(username!!)
                var success = response["success"] == "true"

                if (!success) {
                    Log.i("Content ", "Failed to list devices")
                    return@launch
                } else {
                    Log.i("Content ", "Successfully listed devices")
                    var data = response["data"]!!
                    val deviceNameToId = getDeviceNameToDeviceIdMap(data)

                    var devicesListView = findViewById<ListView>(R.id.lvDevices)
                    val arrayAdapter: ArrayAdapter<*>
                    arrayAdapter = ArrayAdapter(this@AccountActivity, R.layout.basic_list_element, deviceNameToId.keys.toList())
                    devicesListView.adapter = arrayAdapter

                    devicesListView.setOnItemClickListener { parent, view, position, id ->
                        val element = arrayAdapter.getItem(position)
                        val intent = Intent(this@AccountActivity, DeviceReadingsActivity::class.java)
                        intent.putExtra("device_id", deviceNameToId[element])
                        this@AccountActivity.startActivity(intent)
                        Log.i("Content ", "element " + element + deviceNameToId[element])
                    }

                }

            }
        }
    }

    private fun getDeviceNameToDeviceIdMap(data: String): Map<String, String> {
        var devices =
            Json.parseToJsonElement(data).jsonArray.map { it.jsonObject.toMap() }
        var devicesMap = buildMap<String, String> {
            for ((index, device) in devices.withIndex()) {
                var deviceId = device["device_id"].toString().replace("\"", "")
                put("Device " + index.toString(), deviceId)
            }
        }
        return devicesMap
    }
}