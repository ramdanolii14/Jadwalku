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
            // Tanpa style argument → dialog ikut tema sistem (light/dark otomatis)
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    etTanggal.setText("$day/${month + 1}/$year")
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        etWaktuMulai.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(
                this,
                { _, hour, minute ->
                    etWaktuMulai.setText(String.format("%02d:%02d", hour, minute))
                },
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true
            ).show()
        }

        etWaktuSelesai.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(
                this,
                { _, hour, minute ->
                    etWaktuSelesai.setText(String.format("%02d:%02d", hour, minute))
                },
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true
            ).show()
        }

        btnSimpan.setOnClickListener {
            val nama         = etNamaKegiatan.text.toString().trim()
            val tanggal      = etTanggal.text.toString().trim()
            val waktuMulai   = etWaktuMulai.text.toString().trim()
            val waktuSelesai = etWaktuSelesai.text.toString().trim()

            if (nama.isEmpty() || tanggal.isEmpty() || waktuMulai.isEmpty() || waktuSelesai.isEmpty()) {
                Toast.makeText(this, getString(R.string.jadwal_toast_kosong), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE
            btnSimpan.isEnabled    = false

            FirestoreHelper.addJadwal(nama, tanggal, waktuMulai, waktuSelesai) { success, docId ->
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    btnSimpan.isEnabled    = true

                    if (success && docId != null) {
                        scheduleJadwalAlarms(docId, nama, tanggal, waktuMulai)
                        Toast.makeText(this, getString(R.string.jadwal_toast_berhasil), Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, getString(R.string.jadwal_toast_gagal), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun scheduleJadwalAlarms(docId: String, nama: String, tanggal: String, waktuMulai: String) {
        val eventMs = parseDateTime(tanggal, waktuMulai)
        val offsets = listOf(
            2 * 60 * 60 * 1000L to "2 jam",
            60 * 60 * 1000L     to "1 jam",
            5 * 60 * 1000L      to "5 menit",
            3 * 60 * 1000L      to "3 menit"
        )
        offsets.forEachIndexed { index, (offset, label) ->
            val triggerAt = eventMs - offset
            if (triggerAt > System.currentTimeMillis()) {
                scheduleOneAlarm(
                    docId     = "${docId}_j$index",
                    title     = getString(R.string.alarm_jadwal_title),
                    message   = "$nama akan dimulai $label lagi.",
                    triggerAt = triggerAt
                )
            }
        }
    }

    private fun scheduleOneAlarm(docId: String, title: String, message: String, triggerAt: Long) {
        val intent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_TITLE,   title)
            putExtra(AlarmReceiver.EXTRA_MESSAGE, message)
            putExtra(AlarmReceiver.EXTRA_DOC_ID,  docId)
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        val pending = PendingIntent.getBroadcast(this, docId.hashCode(), intent, flags)
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