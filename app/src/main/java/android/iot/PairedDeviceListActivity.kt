package android.iot

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.iot.bluetooth.BluetoothListDeviceAdapter
import android.iot.bluetooth.Data
import android.iot.bluetooth.RecyclerViewClickListener
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat

class PairedDeviceListActivity : AppCompatActivity() {

    private var forbiddenDevicesAddresses = ArrayList<String>()

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

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device: BluetoothDevice? =
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                val deviceName = if (ActivityCompat.checkSelfPermission(
                        this@PairedDeviceListActivity,
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_paired_device_list)
        this.forbiddenDevicesAddresses = intent.getStringArrayListExtra("devicesAddresses") ?: ArrayList()


        val back: ImageButton = findViewById(R.id.backButton)
        back.setOnClickListener {
            val intentMain = Intent(
                this@PairedDeviceListActivity,
                UserDevices::class.java
            )
            this@PairedDeviceListActivity.startActivity(intentMain)
        }

        val data = this.getData()

        val listener = object: RecyclerViewClickListener() {
            override fun onClick(index: Int) {
                super.onClick(index)

                val intentMain = Intent(
                    this@PairedDeviceListActivity,
                    BluetoothAddDeviceActivity::class.java
                )
                intentMain.putExtra("deviceMac", data[index].address)

                this@PairedDeviceListActivity.startActivity(intentMain)
            }
        }


        val adapter = BluetoothListDeviceAdapter(data, this, listener)

        val recyclerView = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.itemsList)

        recyclerView.adapter = adapter
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
    }

    private fun getData(): ArrayList<Data> {
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

        val bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        if (!bluetoothAdapter.isEnabled) {
            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return ArrayList()
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

        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter.bondedDevices

        val output =  ArrayList(pairedDevices?.filter { device -> device.address !in this.forbiddenDevicesAddresses }?.map { Data(it.name, it.address) }?.toList() ?: ArrayList<Data>())

        //  TODO: Remove this
        if (output.size == 0) {
            output.add(Data("Example1", "00:11:22:33:FF:EE"))
            output.add(Data("Example2", "01:11:22:33:FF:EE"))
            output.add(Data("Example3", "02:11:22:33:FF:EE"))
            output.add(Data("Example4", "03:11:22:33:FF:EE"))
        }

        return output
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }
}