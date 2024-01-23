package android.iot

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

class LoginActivity : AppCompatActivity() {
    companion object {
        const val SHARED_PREFS = "sharedPrefs"
        const val USERNAME = "username"
        const val LOGGED_IN = "loggedIn"
    }
    private suspend fun sendLoginRequest(username: String, password: String) : Map<String, String> {
        val apiUrl = "https://vye4bu6645.execute-api.eu-north-1.amazonaws.com/default"
        val loginUrl = "$apiUrl/login"

        val response = HttpClient(CIO).request(loginUrl) {
            method = io.ktor.http.HttpMethod.Post
            headers.append("Content-Type", "application/json")
            setBody("""{"username":"$username", "password":"$password"}""")
        }

        println(response)
        println(response.bodyAsText())

        val responseMap = Json.parseToJsonElement(response.bodyAsText()).jsonObject.toMap()

        return responseMap.mapValues { it.value.toString() }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val backButton = findViewById<View>(R.id.backButton) as ImageButton
        backButton.setOnClickListener {
            val intentMain = Intent(
                this@LoginActivity,
                MainActivity::class.java
            )
            this@LoginActivity.startActivity(intentMain)
            Log.i("Content ", "Main layout")
        }

        val registerButton = findViewById<View>(R.id.btnRegister)
        registerButton.setOnClickListener {
            val intentRegister = Intent(
                this@LoginActivity,
                RegisterActivity::class.java
            )
            this@LoginActivity.startActivity(intentRegister)
            Log.i("Content ", "Register layout")
        }

        val sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)

        val loginButton = findViewById<View>(R.id.btnLogin)
        loginButton.setOnClickListener {
            val intentAccount = Intent(
                this@LoginActivity,
                AccountActivity::class.java
            )
            val etUsername = findViewById<View>(R.id.etUsername) as EditText
            val etPassword = findViewById<View>(R.id.etPassword) as EditText
            val username = etUsername.text.toString()
            val password = etPassword.text.toString()
            Log.i("Content ", "$username $password")
            lifecycleScope.launch {
                val response = sendLoginRequest(username, password)
                val loggedIn = response["success"] == "true"
                if (loggedIn) {
                    val editor = sharedPreferences.edit()
                    editor.putString(USERNAME, username)
                    editor.putBoolean(LOGGED_IN, true)
                    editor.apply()

                    this@LoginActivity.startActivity(intentAccount)
                    Log.i("Content ", "Account layout")
                } else {
                    Toast.makeText(
                        this@LoginActivity,
                        response["message"],
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}