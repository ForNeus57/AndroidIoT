package android.iot.lists.devices

class Device(name: String, address: String, uuid: String, data: ArrayList<Device>) {
    var name: String
    var address: String
    var uuid: String
    var data: ArrayList<Device>

    init {
        this.name = name
        this.address = address
        this.uuid = uuid
        this.data = data
    }
}