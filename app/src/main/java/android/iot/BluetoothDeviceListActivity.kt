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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import org.json.JSONObject
import java.io.OutputStream
import java.security.MessageDigest
import java.security.PublicKey


class BluetoothDeviceListActivity : AppCompatActivity() {

    private var ssid = "ssid"
    private var password = "password"
    private var username = "username"

    private var deviceMac = ""

    //  https://stackoverflow.com/questions/27652105/arduino-to-android-secure-bluetooth-connection

    private var listString = ArrayList<String>()
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
        setContentView(R.layout.activity_bluetooth_device_list)

        val button = findViewById<View>(R.id.backButton) as ImageButton
        button.setOnClickListener {
            val intentMain = Intent(
                this@BluetoothDeviceListActivity,
                DeviceListActivity::class.java
            )
            this@BluetoothDeviceListActivity.startActivity(intentMain)
        }

        val ssid = findViewById<View>(R.id.ssidEditText) as EditText
        val password = findViewById<View>(R.id.wifiPasswordEditTextPassword) as EditText
        val connect = findViewById<View>(R.id.connectButton) as Button
        val wifiConnectionStatusText = findViewById<View>(R.id.connectionStatusTextView) as TextView
        this.deviceMac = intent.getStringExtra("deviceMac") ?: ""

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
            this.listString.add(packToJSON())

            val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter.bondedDevices
            val list = pairedDevices?.filter { it.address == this.deviceMac }?.map { it.address }
            val deviceName = list?.firstOrNull() ?: return@setOnClickListener

            val device = bluetoothAdapter.getRemoteDevice(deviceName)  //  Fotorezystor
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
                        this@BluetoothDeviceListActivity,
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

}