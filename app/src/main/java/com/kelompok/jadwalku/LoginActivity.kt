package com.kelompok.jadwalku

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Cek apakah sesi login masih aktif
        val sharedPref = getSharedPreferences("SesiLogin", MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("is_logged_in", false)
        if (isLoggedIn) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_login)

        val etUserId = findViewById<EditText>(R.id.etUserId)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val userIdStr = etUserId.text.toString()
            val password = etPassword.text.toString()

            if (userIdStr.isNotEmpty() && password.isNotEmpty()) {
                val userId = userIdStr.toIntOrNull()

                if (userId != null) {
                    // Simpan sesi login ke SharedPreferences
                    sharedPref.edit()
                        .putBoolean("is_logged_in", true)
                        .putInt("user_id", userId)
                        .apply()

                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "User ID harus berupa angka", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "User ID dan Password harus diisi", Toast.LENGTH_SHORT).show()
            }
        }
    }
}