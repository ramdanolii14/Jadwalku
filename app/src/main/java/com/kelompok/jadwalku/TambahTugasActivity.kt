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

class TambahTugasActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_tugas)

        val etJudul     = findViewById<EditText>(R.id.etJudulTugas)
        val etDeskripsi = findViewById<EditText>(R.id.etDeskripsiTugas)
        val etDeadline  = findViewById<EditText>(R.id.etDeadline)
        val etWaktu     = findViewById<EditText>(R.id.etWaktuDeadline)
        val btnSimpan   = findViewById<Button>(R.id.btnSimpanTugas)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        etDeadline.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(this, android.R.style.Theme_Material_Light_Dialog, { _, year, month, day ->
                etDeadline.setText("$day/${month + 1}/$year")
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        etWaktu.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(this, android.R.style.Theme_Material_Light_Dialog, { _, hour, minute ->
                etWaktu.setText(String.format("%02d:%02d", hour, minute))
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }

        btnSimpan.setOnClickListener {
            val judul     = etJudul.text.toString().trim()
            val deskripsi = etDeskripsi.text.toString().trim()
            val tanggal   = etDeadline.text.toString().trim()
            val waktu     = etWaktu.text.toString().trim()

            if (judul.isEmpty() || deskripsi.isEmpty() || tanggal.isEmpty() || waktu.isEmpty()) {
                Toast.makeText(this, "Semua data harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE
            btnSimpan.isEnabled    = false

            FirestoreHelper.addTugas(judul, deskripsi, tanggal, waktu) { success, docId ->
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    btnSimpan.isEnabled    = true

                    if (success && docId != null) {
                        scheduleTugasAlarms(docId, judul, tanggal, waktu)
                        Toast.makeText(this, "Tugas berhasil disimpan!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Gagal menyimpan tugas.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    /**
     * Menjadwalkan 4 alarm lokal sebelum deadline:
     * 2 jam, 1 jam, 5 menit, dan 3 menit sebelumnya.
     * Alarm ini bersifat lokal — aktif meski tidak ada koneksi internet.
     */
    private fun scheduleTugasAlarms(docId: String, judul: String, tanggal: String, waktu: String) {
        val deadlineMs = parseDateTime(tanggal, waktu)
        val offsets = listOf(
            2 * 60 * 60 * 1000L to "2 jam",
            60 * 60 * 1000L     to "1 jam",
            5 * 60 * 1000L      to "5 menit",
            3 * 60 * 1000L      to "3 menit"
        )
        offsets.forEachIndexed { index, (offset, label) ->
            val triggerAt = deadlineMs - offset
            if (triggerAt > System.currentTimeMillis()) {
                scheduleOneAlarm(
                    docId     = "${docId}_t$index",
                    title     = "Deadline Tugas Mendekat!",
                    message   = "Tugas '$judul' deadline $tanggal pukul $waktu (sisa ~$label).",
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