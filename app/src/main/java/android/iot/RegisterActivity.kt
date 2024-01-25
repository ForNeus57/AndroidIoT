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

class RegisterActivity : AppCompatActivity() {
    private suspend fun sendRegisterRequest(username: String, password: String): Map<String, String> {
        val apiUrl = "https://vye4bu6645.execute-api.eu-north-1.amazonaws.com/default"
        val usersUrl = "$apiUrl/users"

        val response = HttpClient(CIO).request(usersUrl) {
            method = io.ktor.http.HttpMethod.Post
            headers.append("Content-Type", "application/json")
            setBody("""{"username":"$username", "password":"$password"}""")
            url {
                protocol = io.ktor.http.URLProtocol.HTTPS
            }
        }

        println(response)
        println(response.bodyAsText())

        val responseMap = Json.parseToJsonElement(response.bodyAsText()).jsonObject.toMap()

        return responseMap.mapValues { it.value.toString() }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val backButton = findViewById<View>(R.id.backButton) as ImageButton
        backButton.setOnClickListener {
            val intentMain = Intent(
                this@RegisterActivity,
                LoginActivity::class.java
            )
            this@RegisterActivity.startActivity(intentMain)
            Log.i("Content ", "Main layout")
        }


        val registerButton = findViewById<View>(R.id.btnRegister)
        registerButton.setOnClickListener {
            val intentLogin = Intent(
                this@RegisterActivity,
                LoginActivity::class.java
            )
            val etUsername = findViewById<View>(R.id.etUsername) as EditText
            val etPassword = findViewById<View>(R.id.etPassword) as EditText
            val etConfirmPassword = findViewById<View>(R.id.etConfirmPassword) as EditText
            val username = etUsername.text.toString()
            val password = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(
                    this@RegisterActivity,
                    "Please fill out all fields",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(
                    this@RegisterActivity,
                    "Passwords do not match",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val response = sendRegisterRequest(username, password)
                val createdAccount = response["success"] == "true"
                if (createdAccount) {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Account created!",
                        Toast.LENGTH_LONG
                    ).show()
                    this@RegisterActivity.startActivity(intentLogin)
                } else {
                    Toast.makeText(
                        this@RegisterActivity,
                        response["message"],
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}