package android.iot

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    companion object {
        const val SHARED_PREFS = "sharedPrefs"
        const val USERNAME = "username"
        const val LOGGED_IN = "loggedIn"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val pairDeviceButton = findViewById<View>(R.id.buttonAdder) as ImageButton
        pairDeviceButton.setOnClickListener {
            val intentMain = Intent(
                this@MainActivity,
                BluetoothDeviceListActivity::class.java
            )
            this@MainActivity.startActivity(intentMain)
            println("Changed to BluetoothDeviceListActivity")
            Log.i("Content:", "Changed to BluetoothDeviceListActivity")
        }

        val accountButton = findViewById<View>(R.id.buttonAccount) as ImageButton
        accountButton.setOnClickListener {
            val intentMain = Intent(
                this@MainActivity,
                AccountActivity::class.java
            )
            this@MainActivity.startActivity(intentMain)
        }
    }
}