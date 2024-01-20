package android.iot.devices

import android.content.Context
import android.iot.R
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import java.util.ArrayList

class UserListDeviceAdapter(private val data: ArrayList<Device>, val context: Context, private val listener: UserRecyclerViewClickListener) :
    RecyclerView.Adapter<DeviceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.user_device_list_element, parent, false)


        return DeviceViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val index = holder.adapterPosition;

        holder.name.text = data[index].name
        holder.address.text = data[index].address
        holder.uuid.text = data[index].uuid

        holder.view.setOnClickListener {
            listener.onClick(index)
        }

    }

    override fun getItemCount(): Int {
        return data.size
    }

}