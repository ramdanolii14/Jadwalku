package com.kelompok.jadwalku

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.provider.CalendarContract
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class TambahTugasActivity : AppCompatActivity() {

    private var selectedYear = 0
    private var selectedMonth = 0
    private var selectedDay = 0
    private var endHour = 0
    private var endMinute = 0

    // Deklarasi helper database
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_tugas)

        // Inisialisasi helper database
        dbHelper = DatabaseHelper(this)

        val etJudulTugas = findViewById<EditText>(R.id.etJudulTugas)
        val etDeskripsiTugas = findViewById<EditText>(R.id.etDeskripsiTugas)
        val etDeadline = findViewById<EditText>(R.id.etDeadline)
        val etWaktuDeadline = findViewById<EditText>(R.id.etWaktuDeadline)
        val btnSimpanTugas = findViewById<Button>(R.id.btnSimpanTugas)

        etDeadline.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    selectedYear = year
                    selectedMonth = month
                    selectedDay = dayOfMonth
                    etDeadline.setText("$dayOfMonth/${month + 1}/$year")
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        etWaktuDeadline.setOnClickListener {
            val calendar = Calendar.getInstance()
            val timePickerDialog = TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    endHour = hourOfDay
                    endMinute = minute
                    etWaktuDeadline.setText(String.format("%02d:%02d", hourOfDay, minute))
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            )
            timePickerDialog.show()
        }

        btnSimpanTugas.setOnClickListener {
            val judul = etJudulTugas.text.toString()
            val deskripsi = etDeskripsiTugas.text.toString()
            val tanggal = etDeadline.text.toString()
            val waktu = etWaktuDeadline.text.toString()

            if (judul.isNotEmpty() && deskripsi.isNotEmpty() &&
                tanggal.isNotEmpty() && waktu.isNotEmpty()) {

                // 1. SIMPAN KE SQLITE
                // Menggunakan User ID default (misalnya ID 1) sesuai struktur tabel user
                val currentUserId = 1
                val statusSimpan = dbHelper.insertTugas(currentUserId, judul, deskripsi, tanggal, waktu)

                if (statusSimpan > -1) {
                    Toast.makeText(this, "Tugas tersimpan di lokal & dialihkan ke Kalender", Toast.LENGTH_SHORT).show()
                }

                // 2. KIRIM KE GOOGLE CALENDAR
                val deadlineTime = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay, endHour, endMinute)
                }

                val intent = Intent(Intent.ACTION_INSERT).apply {
                    data = CalendarContract.Events.CONTENT_URI
                    putExtra(CalendarContract.Events.TITLE, "Tugas: $judul")
                    putExtra(CalendarContract.Events.DESCRIPTION, deskripsi)
                    putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, deadlineTime.timeInMillis)
                    // Set durasi event di kalender selama 30 menit untuk deadline ini
                    putExtra(CalendarContract.EXTRA_EVENT_END_TIME, deadlineTime.timeInMillis + (30 * 60 * 1000))
                }

                try {
                    startActivity(intent)
                    finish() // Tutup halaman setelah melempar intent
                } catch (e: android.content.ActivityNotFoundException) {
                    Toast.makeText(this, "Aplikasi Calendar tidak ditemukan", Toast.LENGTH_SHORT).show()
                }

            } else {
                Toast.makeText(this, "Semua data harus diisi", Toast.LENGTH_SHORT).show()
            }
        }
    }
}