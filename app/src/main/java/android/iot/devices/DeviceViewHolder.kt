package android.iot.devices

import android.iot.R
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DeviceViewHolder public constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val name: TextView;
    val address: TextView;
    val uuid: TextView;
    val view: View = itemView;

    init {
        this.name = itemView.findViewById(R.id.deviceName)
        this.address = itemView.findViewById(R.id.macAddress)
        this.uuid = itemView.findViewById(R.id.uuid)
    }
}