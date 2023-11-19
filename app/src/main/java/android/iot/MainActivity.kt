package android.iot

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button = findViewById<View>(R.id.buttonAdder) as ImageButton
        button.setOnClickListener {
            val intentMain = Intent(
                this@MainActivity,
                BluetoothDeviceListActivity::class.java
            )
            this@MainActivity.startActivity(intentMain)
            Log.i("Content:", "Changed to BluetoothDeviceListActivity")
        }
    }
}