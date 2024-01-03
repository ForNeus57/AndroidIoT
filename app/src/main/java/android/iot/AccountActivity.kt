package android.iot

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class AccountActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        val button = findViewById<View>(R.id.backButton) as ImageButton
        button.setOnClickListener {
            val intentMain = Intent(
                this@AccountActivity,
                MainActivity::class.java
            )
            this@AccountActivity.startActivity(intentMain)
            Log.i("Content ", " Main layout ")
        }
    }
}