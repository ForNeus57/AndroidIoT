package android.iot.devices

class Device(name: String, address: String, uuid: String) {
    var name: String
    var address: String
    var uuid: String

    init {
        this.name = name;
        this.address = address;
        this.uuid = uuid
    }
}