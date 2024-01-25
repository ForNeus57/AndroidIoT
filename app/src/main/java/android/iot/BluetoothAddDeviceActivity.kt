package android.iot

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.json.JSONObject
import java.io.OutputStream
import java.security.MessageDigest


class BluetoothAddDeviceActivity : AppCompatActivity() {

    companion object {
        const val SHARED_PREFS = "sharedPrefs"
        const val USERNAME = "username"
        const val LOGGED_IN = "loggedIn"
        const val SESSION_ID = "sessionId"
    }

    private var username = ""
    private var deviceId = ""
    private var deviceMac = ""
    private var sessionId = ""

    private var history = HashSet<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth_add_device)
        this.deviceMac = intent.getStringExtra("deviceMac") ?: ""
        val preferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
        this.username = preferences.getString(USERNAME, "") ?: ""
        this.sessionId = preferences.getString(SESSION_ID, "") ?: ""
        runBlocking {
            this@BluetoothAddDeviceActivity.deviceId = getDeviceId(this@BluetoothAddDeviceActivity.deviceMac)
        }
        val ssid = findViewById<View>(R.id.ssidEditText) as EditText
        val password = findViewById<View>(R.id.wifiPasswordEditTextPassword) as EditText
        val wifiConnectionStatusText = findViewById<View>(R.id.connectionStatusTextView) as TextView
        val connect = findViewById<View>(R.id.connectButton) as Button
        val backButton = findViewById<View>(R.id.backButton) as ImageButton
        backButton.setOnClickListener {
            val intentMain = Intent(
                this@BluetoothAddDeviceActivity,
                PairedDeviceListActivity::class.java
            )
            this@BluetoothAddDeviceActivity.startActivity(intentMain)
        }

        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter: BluetoothAdapter = bluetoothManager.adapter ?: run {
            //  Device doesn't support Bluetooth
            Toast.makeText(
                this, "Your device does not have Bluetooth!", Toast.LENGTH_LONG
            ).show()
            finish()
            return
        }

        //  Check if bluetooth is enabled, if not inform / ask the user to enable it.
        if (!bluetoothAdapter.isEnabled) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.BLUETOOTH_CONNECT), 2)
                    Toast.makeText(
                        this, "Please enable bluetooth connect to add device!", Toast.LENGTH_LONG
                    ).show()
                    finish()
                    return
                }
            }
            ActivityCompat.startActivityForResult(this, Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 1, null)
        }

        connect.setOnClickListener {

//            val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter.bondedDevices
//            val list = pairedDevices?.filter { it.address == this.deviceMac }?.map { it.address }
//            val deviceName = list?.firstOrNull() ?: return@setOnClickListener

            val device = bluetoothAdapter.getRemoteDevice(this.deviceMac)
            val socket = device.createRfcommSocketToServiceRecord(device.uuids[0].uuid)

            try {
                bluetoothAdapter.cancelDiscovery()
                socket.connect()
                if (socket.isConnected) {
                    val writer: OutputStream = socket.outputStream

                    writer.write(packToJSON(ssid.text.toString(), password.text.toString()).toByteArray())

                    val reader = socket.inputStream

                    val buffer = ByteArray(8192) // or 4096, or more
                    val length = reader.read(buffer)
                    val text = String(buffer, 0, length)
                    val jsonObject = JSONObject(text)
                    val key = jsonObject.getString("key")
                    if (history.contains(key)) {
                        wifiConnectionStatusText.text = "Hash already used! Detected replay attack!"
                    } else {
                        wifiConnectionStatusText.text = text
                        history.add(key)
                    }

                    writer.close()
                    reader.close()
                    socket.close()

                    ssid.text.clear()
                    password.text.clear()
                }
            } catch (e: Exception) {
                Log.wtf("android.iot", e.toString())
            }
        }
    }

    private fun packToJSON(ssid: String, password: String): String {
        return "{\"device_id\":\"${this.deviceId}\",\"username\":\"${this.username}\",\"ssid\":\"${ssid}\",\"password\":\"${password}\",\"hash\":\"${this.getHash(System.currentTimeMillis().toString())}\"}" + Char(
            10
        )
    }

    private fun getHash(value: String): String {
        val bytes = value.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold(StringBuilder()) { sb, it -> sb.append("%02x".format(it)) }.toString()
    }

    private suspend fun getDeviceId(macAddress: String): String {
        return sendCreateUserDeviceBindingRequest(macAddress)["device_id"]!!.replace("\"", "")
    }

    private suspend fun sendCreateUserDeviceBindingRequest(macAddress: String) : Map<String, String> {
        val apiUrl = "https://vye4bu6645.execute-api.eu-north-1.amazonaws.com/default"
        val devicesUrl = "$apiUrl/devices"

        val response = HttpClient(CIO).request(devicesUrl) {
            method = io.ktor.http.HttpMethod.Post
            headers.append("Content-Type", "application/json")
            setBody("""{"username":"$username", "MAC":"$macAddress", "session_id":"$sessionId"}""")
        }

        val responseMap = Json.parseToJsonElement(response.bodyAsText()).jsonObject.toMap()

        return responseMap.mapValues { it.value.toString() }
    }

    private suspend fun sendCancelBindingRequest(deviceId: String): Map<String, String> {
        val apiUrl = "https://vye4bu6645.execute-api.eu-north-1.amazonaws.com/default"
        val devicesUrl = "$apiUrl/devices"

        val response = HttpClient(CIO).request(devicesUrl) {
            method = io.ktor.http.HttpMethod.Delete
            headers.append("Content-Type", "application/json")
            headers.append("session_id", sessionId)
            url {
                parameters.append("device_id", deviceId)
                parameters.append("username", username)
            }
        }

        val responseMap = Json.parseToJsonElement(response.bodyAsText()).jsonObject.toMap()

        return responseMap.mapValues { it.value.toString() }
    }
}