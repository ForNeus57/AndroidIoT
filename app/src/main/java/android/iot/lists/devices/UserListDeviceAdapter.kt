package android.iot.lists.devices

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.iot.R
import android.iot.lists.ListViewClickListener
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.recyclerview.widget.RecyclerView
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.request
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

class UserListDeviceAdapter(
    private val data: ArrayList<Device>,
    private val context: Context,
    private val listener: ListViewClickListener
) : RecyclerView.Adapter<DeviceViewHolder>() {

    val username = context.getSharedPreferences(
        "sharedPrefs", Context.MODE_PRIVATE
    ).getString("username", null)!!

    val sessionId = context.getSharedPreferences(
        "sharedPrefs", Context.MODE_PRIVATE
    ).getString("sessionId", null)!!

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.user_device_list_element, parent, false)


        return DeviceViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val index = holder.adapterPosition;

        holder.name.text = data[index].name
        holder.uuid.text = data[index].uuid

        holder.view.setOnClickListener {
            listener.onClick(index)
        }

        holder.button.setOnClickListener {
            if (this.sendStopSignalToDevice(this.data[index].address)) {
                if (this.unBindTheDevice(data[index].uuid)) {
                    data[index].data.removeAt(index)
                    this.notifyItemRemoved(index)
                } else {
                    Toast.makeText(
                        context, "Couldn't delete!", Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                Toast.makeText(
                    context, "Couldn't delete, because ESP-32 could not get the stop communicate!", Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    /**
     * Method that sends a stop signal to the device.
     * If successful return true, false otherwise.
     */
    private fun sendStopSignalToDevice(macAddress: String): Boolean {
        val bluetoothManager: BluetoothManager? = getSystemService(this.context, BluetoothManager::class.java)
        val bluetoothAdapter: BluetoothAdapter = bluetoothManager?.adapter ?: run {
            //  Device doesn't support Bluetooth
            Toast.makeText(
                this.context, "Your device does not have Bluetooth!", Toast.LENGTH_LONG
            ).show()
            return false
        }

        //  Check if bluetooth is enabled, if not inform / ask the user to enable it.
        if (!bluetoothAdapter.isEnabled) {
            if (ContextCompat.checkSelfPermission(this.context, android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    ActivityCompat.requestPermissions(this.context as Activity, arrayOf(android.Manifest.permission.BLUETOOTH_CONNECT), 2)
                    Toast.makeText(
                        this.context, "Please enable bluetooth connect to add device!", Toast.LENGTH_LONG
                    ).show()
                    return false
                }
            }
            ActivityCompat.startActivityForResult(this.context as Activity, Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 1, null)
        }


        val device = bluetoothAdapter.getRemoteDevice(macAddress)
        val socket = device.createRfcommSocketToServiceRecord(device.uuids[0].uuid)

        try {
            socket.connect()
            socket.outputStream.write("stop".toByteArray())
            socket.close()

        } catch (e: Exception) {
            return false
        }


        return true
    }

    private fun unBindTheDevice(uuid: String): Boolean {
        return runBlocking {
            val response = sendUnBindDeviceRequest(uuid)
            return@runBlocking response["success"] == "true"
        }
    }

    private suspend fun sendUnBindDeviceRequest(deviceId: String): Map<String, String> {
        val apiUrl = "https://vye4bu6645.execute-api.eu-north-1.amazonaws.com/default"
        val devicesUrl = "$apiUrl/devices"

        val response = HttpClient(CIO).request(devicesUrl) {
            method = io.ktor.http.HttpMethod.Delete
            headers.append("Content-Type", "application/json")
            headers.append("session_id", sessionId)
            url {
                parameters.append("device_id", deviceId)
                parameters.append("username", username)
                protocol = io.ktor.http.URLProtocol.HTTPS
            }
        }

        val responseMap = Json.parseToJsonElement(response.bodyAsText()).jsonObject.toMap()

        return responseMap.mapValues { it.value.toString() }
    }
}