package com.kelompok.jadwalku

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class TambahJadwalActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_jadwal)

        dbHelper = DatabaseHelper(this)

        val etNamaKegiatan = findViewById<EditText>(R.id.etNamaKegiatan)
        val etTanggal = findViewById<EditText>(R.id.etTanggal)
        val etWaktuMulai = findViewById<EditText>(R.id.etWaktuMulai)
        val etWaktuSelesai = findViewById<EditText>(R.id.etWaktuSelesai)
        val btnSimpan = findViewById<Button>(R.id.btnSimpanKalender)

        etTanggal.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    etTanggal.setText("$dayOfMonth/${month + 1}/$year")
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        etWaktuMulai.setOnClickListener {
            val calendar = Calendar.getInstance()
            TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    etWaktuMulai.setText(String.format("%02d:%02d", hourOfDay, minute))
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }

        etWaktuSelesai.setOnClickListener {
            val calendar = Calendar.getInstance()
            TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    etWaktuSelesai.setText(String.format("%02d:%02d", hourOfDay, minute))
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }

        btnSimpan.setOnClickListener {
            val namaKegiatan = etNamaKegiatan.text.toString()
            val tanggal = etTanggal.text.toString()
            val waktuMulai = etWaktuMulai.text.toString()
            val waktuSelesai = etWaktuSelesai.text.toString()

            if (namaKegiatan.isNotEmpty() && tanggal.isNotEmpty() &&
                waktuMulai.isNotEmpty() && waktuSelesai.isNotEmpty()) {

                val sharedPref = getSharedPreferences("SesiLogin", MODE_PRIVATE)
                val currentUserId = sharedPref.getInt("user_id", 1)

                val statusSimpan = dbHelper.insertJadwal(currentUserId, namaKegiatan, tanggal, waktuMulai, waktuSelesai)

                if (statusSimpan > -1) {
                    Toast.makeText(this, "Jadwal berhasil disimpan!", Toast.LENGTH_SHORT).show()
                    // Kembali ke MainActivity setelah berhasil simpan
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Gagal menyimpan jadwal.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Semua data harus diisi", Toast.LENGTH_SHORT).show()
            }
        }
    }
}