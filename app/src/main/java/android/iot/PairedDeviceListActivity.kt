package android.iot

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.iot.lists.bluetooth.BluetoothListDeviceAdapter
import android.iot.lists.bluetooth.Data
import android.iot.lists.bluetooth.RecyclerViewClickListener
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class PairedDeviceListActivity : AppCompatActivity() {

    private var forbiddenDevicesAddresses = ArrayList<String>()
    private var data = ArrayList<Data>()
    private lateinit var receiver: BroadcastReceiver

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

        val listener = object : RecyclerViewClickListener() {
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

        val adapter = BluetoothListDeviceAdapter(this.data, this, listener)

        val recyclerView =
            findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.itemsList)

        recyclerView.adapter = adapter
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        this.fillData(adapter)
    }

    private fun fillData(listAdapter: BluetoothListDeviceAdapter) {
        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter: BluetoothAdapter = bluetoothManager.adapter ?: run {
            //  Device doesn't support Bluetooth
            Toast.makeText(
                this, "Your device does not have Bluetooth!", Toast.LENGTH_LONG
            ).show()
            finish()
            return
        }

        if (!bluetoothAdapter.isEnabled) {
            try {
                startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 1)
            } catch (e: SecurityException) {
                //  User rejected the request
                Toast.makeText(
                    this, "User rejected ask for permission pop-up!", Toast.LENGTH_LONG
                ).show()
                finish()
                return
            }
        }

        this.receiver = object : BroadcastReceiver() {

            override fun onReceive(context: Context, intent: Intent) {
                when(intent.action) {
                    BluetoothDevice.ACTION_FOUND -> {
                        try {
                            val device: BluetoothDevice? =
                                intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                            val deviceName = device?.name ?: "Unknown"
                            val deviceHardwareAddress = device?.address ?: ""
                            if (deviceHardwareAddress != "" && deviceHardwareAddress !in forbiddenDevicesAddresses) {
                                data.add(Data(deviceName, deviceHardwareAddress))
                                listAdapter.notifyItemInserted(data.size - 1)
                            }
                        } catch (_: SecurityException) {
                        }
                    }
                }
            }
        }

        val discoveryFilter = IntentFilter()

        discoveryFilter.addAction(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, discoveryFilter)

        try {
            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery()
            }
            bluetoothAdapter.startDiscovery()
        } catch (e: SecurityException) {
            //  User rejected the request
            Toast.makeText(
                this, "Started Discovery process rejected due to user!", Toast.LENGTH_LONG
            ).show()
            bluetoothAdapter.cancelDiscovery()
            return
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }
}