package android.iot.lists.bluetooth

import android.content.Context
import android.iot.R
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class BluetoothListDeviceAdapter(private val data: ArrayList<Data>, val context: Context, private val listener: RecyclerViewClickListener) :
    RecyclerView.Adapter<DataViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.device_list_element, parent, false)

//        view.findViewById<TextView>(R.id.deviceName).text = "Device 1"
//        view.findViewById<TextView>(R.id.macAddress).text = "00:00:00:00:00:00"

        return DataViewHolder(view)
    }

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        val index = holder.adapterPosition

        holder.name.text = data[index].name
        holder.address.text = data[index].address

        holder.view.setOnClickListener {
            listener.onClick(index)
        }

    }

    override fun getItemCount(): Int {
        return data.size
    }

//    override fun onAttachedToRecyclerView(RecyclerView recyclerView) {
//        super.onAttachedToRecyclerView(recyclerView)
//    }
}