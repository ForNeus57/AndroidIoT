package android.iot.lists.devices

import android.iot.R
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DeviceViewHolder public constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val name: TextView;
    val uuid: TextView;
    val button: ImageButton
    val view: View = itemView;

    init {
        this.name = itemView.findViewById(R.id.deviceName)
        this.uuid = itemView.findViewById(R.id.uuid)
        this.button = itemView.findViewById(R.id.backButton)
    }
}