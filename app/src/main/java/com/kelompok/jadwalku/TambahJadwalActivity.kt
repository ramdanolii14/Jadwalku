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

class TambahJadwalActivity : AppCompatActivity() {

    private var selectedYear = 0
    private var selectedMonth = 0
    private var selectedDay = 0
    private var startHour = 0
    private var startMinute = 0
    private var endHour = 0
    private var endMinute = 0

    // Deklarasi DatabaseHelper
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_jadwal)

        // Inisialisasi DatabaseHelper
        dbHelper = DatabaseHelper(this)

        val etNamaKegiatan = findViewById<EditText>(R.id.etNamaKegiatan)
        val etTanggal = findViewById<EditText>(R.id.etTanggal)
        val etWaktuMulai = findViewById<EditText>(R.id.etWaktuMulai)
        val etWaktuSelesai = findViewById<EditText>(R.id.etWaktuSelesai)
        val btnSimpan = findViewById<Button>(R.id.btnSimpanKalender)

        etTanggal.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    selectedYear = year
                    selectedMonth = month
                    selectedDay = dayOfMonth
                    etTanggal.setText("$dayOfMonth/${month + 1}/$year")
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        etWaktuMulai.setOnClickListener {
            val calendar = Calendar.getInstance()
            val timePickerDialog = TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    startHour = hourOfDay
                    startMinute = minute
                    etWaktuMulai.setText(String.format("%02d:%02d", hourOfDay, minute))
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            )
            timePickerDialog.show()
        }

        etWaktuSelesai.setOnClickListener {
            val calendar = Calendar.getInstance()
            val timePickerDialog = TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    endHour = hourOfDay
                    endMinute = minute
                    etWaktuSelesai.setText(String.format("%02d:%02d", hourOfDay, minute))
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            )
            timePickerDialog.show()
        }

        btnSimpan.setOnClickListener {
            val namaKegiatan = etNamaKegiatan.text.toString()
            val tanggal = etTanggal.text.toString()
            val waktuMulai = etWaktuMulai.text.toString()
            val waktuSelesai = etWaktuSelesai.text.toString()

            if (namaKegiatan.isNotEmpty() && tanggal.isNotEmpty() &&
                waktuMulai.isNotEmpty() && waktuSelesai.isNotEmpty()) {

                // 1. SIMPAN KE SQLITE DULU
                // Asumsi ID User sementara adalah 1
                val currentUserId = 1
                val statusSimpan = dbHelper.insertJadwal(currentUserId, namaKegiatan, tanggal, waktuMulai, waktuSelesai)

                if (statusSimpan > -1) {
                    Toast.makeText(this, "Jadwal tersimpan di lokal & dialihkan ke Kalender", Toast.LENGTH_SHORT).show()
                }

                // 2. SETELAH TERSIMPAN LOKAL, BUKA GOOGLE CALENDAR
                val beginTime = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay, startHour, startMinute)
                }

                val endTime = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay, endHour, endMinute)
                }

                val intent = Intent(Intent.ACTION_INSERT).apply {
                    data = CalendarContract.Events.CONTENT_URI
                    putExtra(CalendarContract.Events.TITLE, namaKegiatan)
                    putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.timeInMillis)
                    putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.timeInMillis)
                }

                try {
                    startActivity(intent)
                    finish() // Tutup activity ini setelah melempar intent
                } catch (e: android.content.ActivityNotFoundException) {
                    Toast.makeText(this, "Aplikasi Calendar tidak ditemukan", Toast.LENGTH_SHORT).show()
                }

            } else {
                Toast.makeText(this, "Semua data harus diisi", Toast.LENGTH_SHORT).show()
            }
        }
    }
}