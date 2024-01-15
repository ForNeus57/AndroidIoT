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

    private var readingState = false
    //  https://stackoverflow.com/questions/27652105/arduino-to-android-secure-bluetooth-connection

    private var listString = ArrayList<String>()
    private var history = HashSet<String>()

    private var publicArduinoClientKey = """
        -----BEGIN PUBLIC KEY-----
        MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA6nQzww+6NA5zGQy9Jqhv
        7KxV8UjmwgKio9wyTFFD964YRk9zer8s+rPxJ82Vu2Dh5pQHDE6q/MFBhh6oEc6h
        wDWdJ4FUMBNOArxxFe6BDTFEkHwWckPimrZiFwCBeInVONL9jLYqWjttpm356+9C
        7PEqh47VZJXJOjdLHrkTDqdOwtJfeL1uiMiGtQt9fJ7lRMypgvYY8tCgLrUoD2P0
        VlwmLkMWQTRWy4sRZVB/tmcpH+n33E9hnBlmk4Ccp7QS7o/VSNMKSymfE840UMQr
        6vvdhibx62kVzjoRKEEMKWV/m4rE5wPe4zoSiUXG2xlTQKagFU9z95JrGxG+bR+r
        TQIDAQAB
        -----END PUBLIC KEY-----
    """.trimIndent()

    private var privateServerKey = """
        -----BEGIN ENCRYPTED PRIVATE KEY-----
        MIIFHDBOBgkqhkiG9w0BBQ0wQTApBgkqhkiG9w0BBQwwHAQIdAJ15rkXaOACAggA
        MAwGCCqGSIb3DQIJBQAwFAYIKoZIhvcNAwcECEXYSerqKfvGBIIEyPolIJ2+3iLL
        89oGFATgiSNCz03z3v7+3mq85UlfyZlG2ARElkmd9dWUBOnaKyCNsDhd5u9wdtEp
        CDeAs9deZH2OaDnqlqhFtnS3gOFIbC0nVdEntlS/A9y05wfvrTv8DkELxbYU47nO
        /W4f+eCaO91Z2mv0A9i7vUGGDUrQCG1Ij/IHhqBBMUN2jALzJBNpKa4hjp/Z5dKx
        jvb/Fn+FnxM+ad8yCPItdBjgKbyzePZd7wlBdlfAU0bXgLe9e9UCCkjvkRxabCUl
        uzC360BY+aMDYpk05LZV0Ap2e/0WmcMv+Dh+P8P4xcdFRlpR10GtIg+mb5E6NN0e
        2VJTO83AzHMRzmFX6yzdSVoQ9TQk0Yw8ybmcXKB2JXIvR62q+56Q+O/QSR3xDf6w
        fitUaTjLPObWmUzoweNzKOSkD50B+gMjqj+PaSEIRZQun9QKBhtAJjh2bDMbap5i
        ta2mOoxJbpYaox+TTewodqvqm71hxXNOxFYkpMYorHgLGdHelGHxzvFhXdhi0zVq
        +4ipd/77qRc+MCU2b/RC0z5dCcBwn4cebDrF53gtts6jfHLuiBxumwu03kAU4tRt
        lRa7NiHIpGiapLYvlngPWxYuNBZ6VleKPnBRrq2jQYJ9g9BZF+ffkfpn5/d1pI3q
        hiiEOoX9kM0gsjb/x2VwNx4vMs8Z8Pl00pAx0TlLW6sq5o6BFJzeNYu5Af+Q12OJ
        cIRK6CxmZlUfPvugobLALi+uQx2/8AVc4A/+RvPWMsSHFBZe4Ijf99un+L8yqxCL
        c1hgXXvs9GOcJfBw0HGlTDL3f5wBdOpHJgO9EdBmUzgxzhuwLuRoq0KGeet0SZap
        xw0zhKn65i+QDkM1066Cw82lpiFkWuF5bDBmcbR6KwoPG2fpMhX4OL8nn7F5yewn
        OSRcQV6Fr5OdxMWaGjTa3xeanDOWaiaOGOZHzvX8c/rdCu5bsF8rVAVSKWQwKixv
        mogCbCTCfw+yG2qBSJAnaNrb4IbyfMFZygqPJunFJ/UPxPZ40tooboHuX1tuFJqV
        FMpoevy3JD03JGUWhnFCd2z+MldEDR3f4GVtLlPM3Fm3pFA6nZyFQaNw9FngE0DT
        dFzNTka5q9yOgY1wfVM0gmRJ5+tXgsndIa6XVfejTKNDnjlyHK6h0GEXVnqeWOLu
        EFMT5tMSW6KbqjSd5LH0hhpbAoBo4Q8H7Qg5mhomL2Bub5rEmw+X+rkqFUgv4haH
        2ZrAtDCQTv6E7fHsglMfDJ+w5zZvKfM60vPhT3MbWof8CrLX0HkvqQ76f+9DDvAp
        LM6xsx5UQFV695fCbKH/ciboPkun1kJkB21IH0gI5UORkggRL9mbxhqN3WHAVFzb
        xRwfshMggyV5zsBRVTBIIMPcvezsrwLPtEj8eXXMUQ4ODN2plo435OP3XkkOpueE
        S6sgcGX6vigKBCr2EWRMTyABXrnmosqaILTwxiNS5/xYY3WTyAjl5kdenzrCnfhR
        eZHLy1sMwCK2QSlRPG5zO8mvkCslqfu1B7CnN23KeT+icTNknByBHDHfT97jZ4Mu
        WG5VDW7tLK/PJ+F6bpuEQ7jTgfvRBaX0o7l013n1AStvp4htSgoZKXxGsO4OI/fU
        AnU9zTfAHO8AmTaGieG0yw==
        -----END ENCRYPTED PRIVATE KEY-----
    """.trimIndent()

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

        val submit = findViewById<View>(R.id.submitButton) as Button
        val ssid = findViewById<View>(R.id.ssidEditText) as EditText
        val password = findViewById<View>(R.id.wifiPasswordEditTextPassword) as EditText
        val connect = findViewById<View>(R.id.connectButton) as Button
        val stop = findViewById<View>(R.id.stopButton) as Button
        val start = findViewById<View>(R.id.startButton) as Button
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
        submit.setOnClickListener {
            this.ssid = ssid.text.toString()
            this.password = password.text.toString()
            this.listString.add(packToJSON())
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
            val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter.bondedDevices
            val list = pairedDevices?.filter { it.address == this.deviceMac }?.map { it.address }
            val deviceName = list?.firstOrNull()

            if (deviceName == null) {
                val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                builder
                    .setMessage("Device not found!")
                    .setTitle("Error")
                    .setNegativeButton("Ok") { dialog, _ ->
                        dialog.dismiss()
                    }

                val dialog: AlertDialog = builder.create()
                dialog.show()
                wifiConnectionStatusText.text = "Device not found!"
                return@setOnClickListener
            }

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

        stop.setOnClickListener {
            this.readingState = false

            val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter.bondedDevices
            val list = pairedDevices?.filter { it.address == this.deviceMac }?.map { it.address }
            val deviceName = list?.firstOrNull()

            if (deviceName == null) {
                val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                builder
                    .setMessage("Device not found!")
                    .setTitle("Error")
                    .setNegativeButton("Ok") { dialog, _ ->
                        dialog.dismiss()
                    }

                val dialog: AlertDialog = builder.create()
                dialog.show()
                wifiConnectionStatusText.text = "Device not found!"
                return@setOnClickListener
            }

            val device = bluetoothAdapter.getRemoteDevice(deviceName)  //  Fotorezystor
            val socket = device.createRfcommSocketToServiceRecord(device.uuids[0].uuid)
            var status = true
            var number = 0

            while (status && number < 20) {
                try {
                    socket.connect()
                    if (socket.isConnected) {
                        val writer: OutputStream = socket.outputStream

                        writer.write(
                            ("{\"username\":\"${this.username}\",\"doReadValue\":${this.readingState},\"hash\":\"${
                                this.getHash(
                                    System.currentTimeMillis().toString()
                                )
                            }\"}" + Char(
                                10
                            )).toByteArray()
                        )
                        val reader = socket.inputStream
                        val buffer = ByteArray(8192) // or 4096, or more
                        val length: Int = reader.read(buffer)
                        val text = String(buffer, 0, length)

                        if (text.isNotEmpty()) {
                            val jsonObject = JSONObject(text)
                            val key = jsonObject.getString("key")
                            if (history.contains(key)) {
                                wifiConnectionStatusText.text = "Hash already used! Detected replay attack!"
                            } else {
                                wifiConnectionStatusText.text = text
                                history.add(key)
                            }
                            status = false
                        }

                        writer.close()
                        reader.close()
                        socket.close()
                    }
                    number++;
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.wtf("android.iot", e.toString())
                }
            }


        }

        start.setOnClickListener {
            this.readingState = true

            val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter.bondedDevices
            val list = pairedDevices?.filter { it.address == this.deviceMac }?.map { it.address }
            val deviceName = list?.firstOrNull()

            if (deviceName == null) {
                val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                builder
                    .setMessage("Device not found!")
                    .setTitle("Error")
                    .setNegativeButton("Ok") { dialog, _ ->
                        dialog.dismiss()
                    }

                val dialog: AlertDialog = builder.create()
                dialog.show()
                wifiConnectionStatusText.text = "Device not found!"
                return@setOnClickListener
            }

            val device = bluetoothAdapter.getRemoteDevice(deviceName)  //  Fotorezystor
            val socket = device.createRfcommSocketToServiceRecord(device.uuids[0].uuid)
            var status = true
            var number = 0

            while (status && number < 20) {
                try {
                    socket.connect()
                    if (socket.isConnected) {
                        val writer: OutputStream = socket.outputStream

                        writer.write(
                            ("{\"username\":\"${this.username}\",\"doReadValue\":${this.readingState},\"hash\":\"${
                                this.getHash(
                                    System.currentTimeMillis().toString()
                                )
                            }\"}" + Char(
                                10
                            )).toByteArray()
                        )
                        val reader = socket.inputStream
                        val buffer = ByteArray(8192) // or 4096, or more
                        val length = reader.read(buffer)
                        val text = String(buffer, 0, length)

                        if (text.isNotEmpty()) {
                            val jsonObject = JSONObject(text)
                            val key = jsonObject.getString("key")
                            if (history.contains(key)) {
                                wifiConnectionStatusText.text = "Hash already used! Detected replay attack!"
                            } else {
                                wifiConnectionStatusText.text = text
                                history.add(key)
                            }
                            status = false
                        }

                        writer.close()
                        reader.close()
                        socket.close()
                    }
                    number++;
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.wtf("android.iot", e.toString())
                }
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