package android.iot

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
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
            if (getSharedPreferences(SHARED_PREFS, MODE_PRIVATE).getBoolean(LOGGED_IN, false)) {
                val intentMain = Intent(
                    this@MainActivity,
                    UserDevices::class.java
                )
                this@MainActivity.startActivity(intentMain)
            } else {
                //  User is not logged in
                Toast.makeText(
                    this@MainActivity, "Not logged in!", Toast.LENGTH_LONG
                ).show()
            }
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