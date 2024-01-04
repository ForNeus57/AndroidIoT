package android.iot

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
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


        val loginButton = findViewById<View>(R.id.btnLogin)
        loginButton.setOnClickListener {
            val intentAccount = Intent(
                this@LoginActivity,
                AccountActivity::class.java
            )
            var etUsername = findViewById<View>(R.id.etUsername) as EditText
            var etPassword = findViewById<View>(R.id.etPassword) as EditText
            var username = etUsername.text.toString()
            var password = etPassword.text.toString()
            Log.i("Content ", username + " " + password)
            if (username == "admin" && password == "admin") {
                intentAccount.putExtra("userLoggedIn", true)
                intentAccount.putExtra("username", username)
                this@LoginActivity.startActivity(intentAccount)
                Log.i("Content ", "Account layout")
            } else {
                Toast.makeText(
                    this@LoginActivity,
                    "Username or password is incorrect",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}