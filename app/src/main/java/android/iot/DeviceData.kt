package android.iot

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton

class DeviceData : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_data)

        val back = findViewById<ImageButton>(R.id.backButton)
        back.setOnClickListener {
            val intentMain = Intent(
                this@DeviceData,
                UserDevices::class.java
            )
            this@DeviceData.startActivity(intentMain)
        }
    }
}