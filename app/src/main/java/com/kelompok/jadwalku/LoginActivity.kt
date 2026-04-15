package com.kelompok.jadwalku

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kelompok.jadwalku.R
class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                    // Integrasi SQL SELECT * FROM user WHERE id = userId diletakkan di sini nantinya
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