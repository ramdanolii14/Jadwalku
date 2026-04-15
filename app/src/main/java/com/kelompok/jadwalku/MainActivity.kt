package com.kelompok.jadwalku

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val cardJadwal = findViewById<CardView>(R.id.cardJadwal)
        val cardTugas = findViewById<CardView>(R.id.cardTugas)
        val cardPengingat = findViewById<CardView>(R.id.cardPengingat)
        val cardNotifikasi = findViewById<CardView>(R.id.cardNotifikasi)

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
    }
}