package android.iot.lists.devices

import android.content.Context
import android.iot.R
import android.iot.lists.ListViewClickListener
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
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
            if (this.sendStopSignalToDevice()) {
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
    private fun sendStopSignalToDevice(): Boolean {
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
            }
        }

        val responseMap = Json.parseToJsonElement(response.bodyAsText()).jsonObject.toMap()

        return responseMap.mapValues { it.value.toString() }
    }
}