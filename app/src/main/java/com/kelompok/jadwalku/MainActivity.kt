package com.kelompok.jadwalku

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    // Launcher untuk minta izin POST_NOTIFICATIONS (Android 13+)
    private val notifPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* hasil izin tidak perlu diproses lebih lanjut */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Minta izin notifikasi di Android 13+
        mintaIzinNotifikasi()

        // Simpan FCM token ke Firestore agar Cloud Functions bisa kirim notif
        FcmTokenHelper.ambilDanSimpanToken()

        val cardJadwal     = findViewById<CardView>(R.id.cardJadwal)
        val cardTugas      = findViewById<CardView>(R.id.cardTugas)
        val cardPengingat  = findViewById<CardView>(R.id.cardPengingat)
        val cardNotifikasi = findViewById<CardView>(R.id.cardNotifikasi)
        val cardLogout     = findViewById<CardView>(R.id.cardLogout)

        cardJadwal.setOnClickListener {
            startActivity(Intent(this, TambahJadwalActivity::class.java))
        }
        cardTugas.setOnClickListener {
            startActivity(Intent(this, TambahTugasActivity::class.java))
        }
        cardPengingat.setOnClickListener {
            startActivity(Intent(this, PengingatActivity::class.java))
        }
        cardNotifikasi.setOnClickListener {
            startActivity(Intent(this, NotifikasiActivity::class.java))
        }
        cardLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun mintaIzinNotifikasi() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}