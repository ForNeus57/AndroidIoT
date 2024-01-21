package android.iot

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AccountActivity : AppCompatActivity() {
    companion object {
        const val SHARED_PREFS = "sharedPrefs"
        const val USERNAME = "username"
        const val LOGGED_IN = "loggedIn"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
        val userLoggedIn = sharedPreferences.getBoolean(LOGGED_IN, false)

        if (!userLoggedIn) {
            val intentLogin = Intent(
                this@AccountActivity, LoginActivity::class.java
            )
            this@AccountActivity.startActivity(intentLogin)
        } else {

            setContentView(R.layout.activity_account)
            val username = sharedPreferences.getString(USERNAME, "")

            val tvUsername = findViewById<View>(R.id.tvUsername) as TextView
            tvUsername.text = username

            val backButton = findViewById<View>(R.id.backButton) as ImageButton
            backButton.setOnClickListener {
                val intentMain = Intent(
                    this@AccountActivity, MainActivity::class.java
                )
                this@AccountActivity.startActivity(intentMain)
                Log.i("Content ", " Main layout ")
            }

            val logoutButton = findViewById<View>(R.id.btnLogout)
            logoutButton.setOnClickListener {
                val intentMain = Intent(
                    this@AccountActivity, MainActivity::class.java
                )
                val editor = sharedPreferences.edit()
                editor.putBoolean(LOGGED_IN, false)
                editor.putString(USERNAME, "")
                editor.apply()
                this@AccountActivity.startActivity(intentMain)
                Log.i("Content ", "Login layout")
            }
        }
    }
}