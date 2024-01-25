package android.iot.lists.devices

class Device(name: String, uuid: String, data: ArrayList<Device>) {
    var name: String
    var uuid: String
    var data: ArrayList<Device>

    init {
        this.name = name
        this.uuid = uuid
        this.data = data
    }
}