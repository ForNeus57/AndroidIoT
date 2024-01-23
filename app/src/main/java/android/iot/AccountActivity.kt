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


    private suspend fun sendListDevicesRequest(username: String) : Map<String, String> {
        val apiUrl = "https://vye4bu6645.execute-api.eu-north-1.amazonaws.com/default"
        val devicesUrl = "$apiUrl/devices"

        val response = HttpClient(CIO).request(devicesUrl) {
            method = io.ktor.http.HttpMethod.Get
            headers.append("Content-Type", "application/json")
            url { parameters.append("username", username) }
        }

        val responseMap = Json.parseToJsonElement(response.bodyAsText()).jsonObject.toMap()

        return responseMap.mapValues { it.value.toString() }
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