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

class TambahTugasActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_tugas)

        dbHelper = DatabaseHelper(this)

        val etJudulTugas = findViewById<EditText>(R.id.etJudulTugas)
        val etDeskripsiTugas = findViewById<EditText>(R.id.etDeskripsiTugas)
        val etDeadline = findViewById<EditText>(R.id.etDeadline)
        val etWaktuDeadline = findViewById<EditText>(R.id.etWaktuDeadline)
        val btnSimpanTugas = findViewById<Button>(R.id.btnSimpanTugas)

        etDeadline.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    etDeadline.setText("$dayOfMonth/${month + 1}/$year")
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        etWaktuDeadline.setOnClickListener {
            val calendar = Calendar.getInstance()
            TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    etWaktuDeadline.setText(String.format("%02d:%02d", hourOfDay, minute))
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }

        btnSimpanTugas.setOnClickListener {
            val judul = etJudulTugas.text.toString()
            val deskripsi = etDeskripsiTugas.text.toString()
            val tanggal = etDeadline.text.toString()
            val waktu = etWaktuDeadline.text.toString()

            if (judul.isNotEmpty() && deskripsi.isNotEmpty() &&
                tanggal.isNotEmpty() && waktu.isNotEmpty()) {

                val sharedPref = getSharedPreferences("SesiLogin", MODE_PRIVATE)
                val currentUserId = sharedPref.getInt("user_id", 1)

                val statusSimpan = dbHelper.insertTugas(currentUserId, judul, deskripsi, tanggal, waktu)

                if (statusSimpan > -1) {
                    Toast.makeText(this, "Tugas berhasil disimpan!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Gagal menyimpan tugas.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Semua data harus diisi", Toast.LENGTH_SHORT).show()
            }
        }
    }
}