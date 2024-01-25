package android.iot

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.iot.lists.ListViewClickListener
import android.iot.lists.bluetooth.BluetoothListDeviceAdapter
import android.iot.lists.bluetooth.Data
import android.os.Build
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PairedDeviceListActivity : AppCompatActivity() {

    /**
     * List of device MAC addresses that are forbidden to be shown in the list. I.E the devices that are already paired with the user.
     * Retrieved from the intent that started this View.
     */
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

        val listener = object : ListViewClickListener() {
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

    /**
     * Method that fills the data array with discovered devices from nearby surrounding.
     *
     * @param listAdapter The adapter that will be used to display the data. We need it to change the number of shown elements in View list.
     */
    private fun fillData(listAdapter: BluetoothListDeviceAdapter) {
        //  Get the adapter to perform bluetooth actions like connection / device discovery ...

        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter: BluetoothAdapter = bluetoothManager.adapter ?: run {
            //  Device doesn't support Bluetooth, i.e. does not have the ability to connect to bluetooth devices, bc it doesn't have bluetooth hardware.
            //  Inform the user and exit the View.
            //  aka. bluetoothManager.adapter is null
            Toast.makeText(
                this, "Your device does not have Bluetooth!", Toast.LENGTH_LONG
            ).show()
            finish()
            return
        }

        //  Check if bluetooth is enabled, if not inform / ask the user to enable it.
        if (!bluetoothAdapter.isEnabled) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                {
                    ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.BLUETOOTH_CONNECT), 2)
                    Toast.makeText(
                        this, "Please enable bluetooth connect to add device!", Toast.LENGTH_LONG
                    ).show()
                    finish()
                    return
                }
            }
            startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 1)
        }

        this.receiver = object : BroadcastReceiver() {

            override fun onReceive(context: Context, intent: Intent) {
                when(intent.action) {
                    BluetoothDevice.ACTION_FOUND -> {
                        if (ContextCompat.checkSelfPermission(this@PairedDeviceListActivity, android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                            {
                                ActivityCompat.requestPermissions(this@PairedDeviceListActivity, arrayOf(android.Manifest.permission.BLUETOOTH_CONNECT), 2)
                                Toast.makeText(
                                    this@PairedDeviceListActivity, "Please enable bluetooth to add device!", Toast.LENGTH_LONG
                                ).show()
                                finish()
                                return
                            }
                        }
                        val device: BluetoothDevice? =
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                        val deviceName = device?.name ?: "Unknown"
                        val deviceHardwareAddress = device?.address ?: ""
                        if (deviceHardwareAddress != "" && deviceHardwareAddress !in forbiddenDevicesAddresses) {
                            data.add(Data(deviceName, deviceHardwareAddress))
                            listAdapter.notifyItemInserted(data.size - 1)
                        }
                    }
                    BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                        Toast.makeText(
                            this@PairedDeviceListActivity, "Discovery started!", Toast.LENGTH_LONG
                        ).show()
                    }
                    BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                        Toast.makeText(
                            this@PairedDeviceListActivity, "Discovery finished!", Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

        val discoveryFilter = IntentFilter()

        discoveryFilter.addAction(BluetoothDevice.ACTION_FOUND)
        discoveryFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        discoveryFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        registerReceiver(receiver, discoveryFilter)


        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery()
        }
        bluetoothAdapter.startDiscovery()
    }

    /**
     * Unregister the broadcast receiver when the activity is destroyed.
     */
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }
}