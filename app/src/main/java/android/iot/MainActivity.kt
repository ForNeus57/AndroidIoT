package android.iot

import android.content.Intent
import android.iot.secret.Encryption
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * Main entry to the application. Shows the main menu.
 * I.E. the buttons to add a device and to go to the account page.
 */
class MainActivity : AppCompatActivity() {
    //  This is a companion object. It is similar to static variables in Java.
    //  We use it to pass data between activities.
    companion object {
        const val SHARED_PREFS = "sharedPrefs"
        const val USERNAME = "username"
        const val LOGGED_IN = "loggedIn"
        const val SESSION_ID = "sessionId"
    }

    /**
     * Javascript equivalent of component life-cycle hook.
     * Called when the component is created.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)      //  Set the layout to activity_main.xml

        Encryption.decrypt()

        //  Set the onClickListener for the account button.
        //  Same as in JS / TS, the button is in layout.
        val accountButton = findViewById<View>(R.id.buttonAccount) as ImageButton
        accountButton.setOnClickListener {
            val intentMain = Intent(
                this@MainActivity,
                AccountActivity::class.java
            )
            this@MainActivity.startActivity(intentMain)
        }

        //  Set the onClickListener for the add device button.
        //  Same as in JS / TS, the is button in layout.
        //  This one first checks if the user is logged in.
        //  If the user is logged in, the user is taken to the device list.
        //  If the user is not logged in, a toast (Small information at the bottom of a screen) is shown.
        val pairDeviceButton = findViewById<View>(R.id.buttonAdder) as ImageButton
        pairDeviceButton.setOnClickListener {
            //  For more info, about this condition see: Dominik's Documentation.
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
    }
}