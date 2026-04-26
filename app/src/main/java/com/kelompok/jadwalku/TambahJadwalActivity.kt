package com.kelompok.jadwalku

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TambahJadwalActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_jadwal)

        val etNamaKegiatan = findViewById<EditText>(R.id.etNamaKegiatan)
        val etTanggal      = findViewById<EditText>(R.id.etTanggal)
        val etWaktuMulai   = findViewById<EditText>(R.id.etWaktuMulai)
        val etWaktuSelesai = findViewById<EditText>(R.id.etWaktuSelesai)
        val btnSimpan      = findViewById<Button>(R.id.btnSimpanKalender)
        val progressBar    = findViewById<ProgressBar>(R.id.progressBar)

        etTanggal.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                etTanggal.setText("$day/${month + 1}/$year")
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        etWaktuMulai.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(this, { _, hour, minute ->
                etWaktuMulai.setText(String.format("%02d:%02d", hour, minute))
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }

        etWaktuSelesai.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(this, { _, hour, minute ->
                etWaktuSelesai.setText(String.format("%02d:%02d", hour, minute))
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }

        btnSimpan.setOnClickListener {
            val nama       = etNamaKegiatan.text.toString().trim()
            val tanggal    = etTanggal.text.toString().trim()
            val waktuMulai = etWaktuMulai.text.toString().trim()
            val waktuSelesai = etWaktuSelesai.text.toString().trim()

            if (nama.isEmpty() || tanggal.isEmpty() || waktuMulai.isEmpty() || waktuSelesai.isEmpty()) {
                Toast.makeText(this, "Semua data harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE
            btnSimpan.isEnabled    = false

            FirestoreHelper.addJadwal(nama, tanggal, waktuMulai, waktuSelesai) { success, docId ->
                progressBar.visibility = View.GONE
                btnSimpan.isEnabled    = true

                if (success && docId != null) {
                    // Jadwalkan alarm lokal agar notifikasi tetap muncul saat offline
                    scheduleAlarm(docId, nama, tanggal, waktuMulai)
                    Toast.makeText(this, "Jadwal berhasil disimpan!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Gagal menyimpan jadwal.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * Menjadwalkan AlarmManager 15 menit sebelum jadwal mulai.
     * Alarm ini bersifat lokal — aktif meski tidak ada koneksi internet.
     */
    private fun scheduleAlarm(docId: String, nama: String, tanggal: String, waktuMulai: String) {
        val triggerAt = parseDateTime(tanggal, waktuMulai) - 15 * 60 * 1000L
        if (triggerAt <= System.currentTimeMillis()) return  // Sudah lewat, skip

        val intent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_TITLE, "Jadwal Segera Dimulai")
            putExtra(AlarmReceiver.EXTRA_MESSAGE, "$nama akan dimulai 15 menit lagi.")
            putExtra(AlarmReceiver.EXTRA_DOC_ID, docId)
        }

        // Gunakan hash docId sebagai requestCode agar tiap alarm unik
        val requestCode = docId.hashCode()
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0

        val pending = PendingIntent.getBroadcast(this, requestCode, intent, flags)
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pending)
        }
    }

    private fun parseDateTime(tanggal: String, waktu: String): Long {
        return try {
            val sdf = SimpleDateFormat("d/M/yyyy HH:mm", Locale.getDefault())
            sdf.parse("$tanggal $waktu")?.time ?: 0L
        } catch (e: Exception) { 0L }
    }
}