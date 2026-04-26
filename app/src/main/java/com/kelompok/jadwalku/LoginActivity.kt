package com.kelompok.jadwalku

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    // UI refs
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button
    private lateinit var tvToggle: TextView
    private lateinit var progressBar: ProgressBar

    /** true = tampil form login, false = tampil form register */
    private var isLoginMode = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        // Jika sudah login, langsung ke MainActivity
        if (auth.currentUser != null) {
            goToMain()
            return
        }

        setContentView(R.layout.activity_login)

        etEmail      = findViewById(R.id.etEmail)
        etPassword   = findViewById(R.id.etPassword)
        btnLogin     = findViewById(R.id.btnLogin)
        btnRegister  = findViewById(R.id.btnRegister)
        tvToggle     = findViewById(R.id.tvToggleMode)
        progressBar  = findViewById(R.id.progressBar)

        updateUiMode()

        btnLogin.setOnClickListener { handleLogin() }
        btnRegister.setOnClickListener { handleRegister() }

        tvToggle.setOnClickListener {
            isLoginMode = !isLoginMode
            updateUiMode()
        }
    }

    private fun updateUiMode() {
        if (isLoginMode) {
            btnLogin.visibility    = View.VISIBLE
            btnRegister.visibility = View.GONE
            tvToggle.text          = "Belum punya akun? Daftar di sini"
        } else {
            btnLogin.visibility    = View.GONE
            btnRegister.visibility = View.VISIBLE
            tvToggle.text          = "Sudah punya akun? Masuk di sini"
        }
    }

    private fun handleLogin() {
        val email    = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (!validateInput(email, password)) return

        setLoading(true)
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                setLoading(false)
                goToMain()
            }
            .addOnFailureListener { e ->
                setLoading(false)
                Toast.makeText(this, "Login gagal: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    private fun handleRegister() {
        val email    = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (!validateInput(email, password)) return
        if (password.length < 6) {
            etPassword.error = "Password minimal 6 karakter"
            return
        }

        setLoading(true)
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                setLoading(false)
                Toast.makeText(this, "Akun berhasil dibuat! Silakan masuk.", Toast.LENGTH_SHORT).show()
                // Setelah register, langsung masuk
                goToMain()
            }
            .addOnFailureListener { e ->
                setLoading(false)
                Toast.makeText(this, "Registrasi gagal: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    private fun validateInput(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            etEmail.error = "Email tidak boleh kosong"
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Format email tidak valid"
            return false
        }
        if (password.isEmpty()) {
            etPassword.error = "Password tidak boleh kosong"
            return false
        }
        return true
    }

    private fun setLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        btnLogin.isEnabled     = !loading
        btnRegister.isEnabled  = !loading
        tvToggle.isEnabled     = !loading
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}