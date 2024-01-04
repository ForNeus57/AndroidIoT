package android.iot

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AccountActivity : AppCompatActivity() {
    private var userLoggedIn: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userLoggedIn = intent.getBooleanExtra("userLoggedIn", false)

        if (!userLoggedIn) {
            val intentLogin = Intent(
                this@AccountActivity,
                LoginActivity::class.java
            )
            this@AccountActivity.startActivity(intentLogin)
        }

        setContentView(R.layout.activity_account)
        val username = intent.getStringExtra("username")

        val tvUsername = findViewById<View>(R.id.tvUsername) as TextView
        tvUsername.text = username

        val backButton = findViewById<View>(R.id.backButton) as ImageButton
        backButton.setOnClickListener {
            val intentMain = Intent(
                this@AccountActivity,
                MainActivity::class.java
            )
            this@AccountActivity.startActivity(intentMain)
            Log.i("Content ", " Main layout ")
        }

        val logoutButton = findViewById<View>(R.id.btnLogout)
        logoutButton.setOnClickListener {
            val intentMain = Intent(
                this@AccountActivity,
                MainActivity::class.java
            )
            intentMain.putExtra("userLoggedIn", false)
            this@AccountActivity.startActivity(intentMain)
            Log.i("Content ", "Login layout")
        }
    }
}