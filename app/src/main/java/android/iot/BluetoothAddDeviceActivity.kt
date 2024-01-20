package android.iot

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import org.json.JSONObject
import java.io.OutputStream
import java.security.MessageDigest


class BluetoothAddDeviceActivity : AppCompatActivity() {

    companion object {
        const val SHARED_PREFS = "sharedPrefs"
        const val USERNAME = "username"
        const val LOGGED_IN = "loggedIn"
    }

    private var ssid = "ssid"
    private var password = "password"
    private var username = ""
    private var deviceId = ""
    private var deviceMac = ""

    private var history = HashSet<String>()


    private lateinit var bluetoothAdapter: BluetoothAdapter

    private var requestBluetooth =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                //granted
            } else {
                //deny
            }
        }

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                Log.d("test006", "${it.key} = ${it.value}")
            }
        }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth_add_device)
        this.deviceMac = intent.getStringExtra("deviceMac") ?: ""
        val preferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
        this.username = preferences.getString(USERNAME, "") ?: ""
        this.deviceId = getDeviceId(this.deviceMac, this.username)

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestMultiplePermissions.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
                )
            )
        } else {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            requestBluetooth.launch(enableBtIntent)
        }

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        if (!bluetoothAdapter.isEnabled) {
            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            startActivityForResult(enableBluetoothIntent, 1)
        }

        val discoverDevicesIntent = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, discoverDevicesIntent)
        try {
            bluetoothAdapter.startDiscovery()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.wtf("android.iot", e.toString())
        }

        connect.setOnClickListener {
            this.ssid = ssid.text.toString()
            this.password = password.text.toString()

            ssid.text.clear()
            password.text.clear()

            val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter.bondedDevices
            val list = pairedDevices?.filter { it.address == this.deviceMac }?.map { it.address }
            val deviceName = list?.firstOrNull() ?: return@setOnClickListener

            val device = bluetoothAdapter.getRemoteDevice(deviceName)
            val socket = device.createRfcommSocketToServiceRecord(device.uuids[0].uuid)

            try {
                socket.connect()
                if (socket.isConnected) {
                    val writer: OutputStream = socket.outputStream

                    writer.write(packToJSON().toByteArray())

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
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.wtf("android.iot", e.toString())
            }
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device: BluetoothDevice? =
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                val deviceName = if (ActivityCompat.checkSelfPermission(
                        this@BluetoothAddDeviceActivity,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                } else {
                    device?.name
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)

        this.removeDeviceId(this.deviceMac, this.username, this.deviceId)
    }

    private fun packToJSON(): String {
        return "{\"username\":\"${this.username}\",\"ssid\":\"${this.ssid}\",\"password\":\"${this.password}\",\"hash\":\"${this.getHash(System.currentTimeMillis().toString())}\"}" + Char(
            10
        )
    }

    private fun getHash(value: String): String {
        val bytes = value.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold(StringBuilder()) { sb, it -> sb.append("%02x".format(it)) }.toString()
    }


    private fun getDeviceId(macAddress: String, username: String): String {
        //  TODO: Implement this function
        //  Remember that device id may be invalidated if connection is not successful and user quits the app
        //  Implement the second function for this case.
        return ""
    }

    private fun removeDeviceId(macAddress: String, username: String, deviceId: String) {
        //  TODO: Implement this function
    }
}