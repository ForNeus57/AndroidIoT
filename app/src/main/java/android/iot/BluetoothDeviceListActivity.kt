package android.iot

import android.Manifest
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.io.OutputStream


class BluetoothDeviceListActivity: AppCompatActivity() {

    private var ssid = "TP-Link_4AD8"
    private var password = "37683924"

    private var listString = ArrayList<String>()

    private lateinit var bluetoothAdapter: BluetoothAdapter

    private var requestBluetooth = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            //granted
        }else{
            //deny
        }
    }

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                Log.d("test006", "${it.key} = ${it.value}")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth_device_list)

        val button = findViewById<View>(R.id.backButton) as ImageButton
        button.setOnClickListener {
            val intentMain = Intent(
                this@BluetoothDeviceListActivity,
                MainActivity::class.java
            )
            this@BluetoothDeviceListActivity.startActivity(intentMain)
//            Log.i("Content ", " Main layout ")
        }

        val submit = findViewById<View>(R.id.submitButton) as Button
        val ssid = findViewById<View>(R.id.ssidEditText) as EditText
        val password = findViewById<View>(R.id.wifiPasswordEditTextPassword) as EditText
        val connect = findViewById<View>(R.id.connectButton) as Button

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestMultiplePermissions.launch(arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT))
        }
        else{
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
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            startActivityForResult(enableBluetoothIntent, 1)
        }
//        val adapter: ArrayAdapter<String?> = ArrayAdapter<String?>(
//        list.adapter = adapter

        submit.setOnClickListener {
            this.ssid = ssid.text.toString()
            this.password = password.text.toString()
            this.listString.add(packToJSON())
//            adapter.notifyDataSetChanged()
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
            val device = bluetoothAdapter.getRemoteDevice("0C:B8:15:F3:79:AE")  //  Fotorezystor
            val socket = device.createRfcommSocketToServiceRecord(device.uuids[0].uuid)

            try {
                socket.connect()
                if (socket.isConnected) {
                    val writer: OutputStream = socket.outputStream
                    writer.write(packToJSON().toByteArray())

                    val reader = socket.inputStream

                    val buffer = ByteArray(8192) // or 4096, or more
                    val length: Int = reader.read(buffer)
                    val text: String = String(buffer, 0, length)

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
                val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
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
        return "{\"ssid\":\"${this.ssid}\",\"password\":\"${this.password}\"}" + Char(10)
    }

}