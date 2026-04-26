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

        val etJudul       = findViewById<EditText>(R.id.etJudulTugas)
        val etDeskripsi   = findViewById<EditText>(R.id.etDeskripsiTugas)
        val etDeadline    = findViewById<EditText>(R.id.etDeadline)
        val etWaktu       = findViewById<EditText>(R.id.etWaktuDeadline)
        val btnSimpan     = findViewById<Button>(R.id.btnSimpanTugas)
        val progressBar   = findViewById<ProgressBar>(R.id.progressBar)

        etDeadline.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                etDeadline.setText("$day/${month + 1}/$year")
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        etWaktu.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(this, { _, hour, minute ->
                etWaktu.setText(String.format("%02d:%02d", hour, minute))
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }

        btnSimpan.setOnClickListener {
            val judul    = etJudul.text.toString().trim()
            val deskripsi = etDeskripsi.text.toString().trim()
            val tanggal  = etDeadline.text.toString().trim()
            val waktu    = etWaktu.text.toString().trim()

            if (judul.isEmpty() || deskripsi.isEmpty() || tanggal.isEmpty() || waktu.isEmpty()) {
                Toast.makeText(this, "Semua data harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE
            btnSimpan.isEnabled    = false

            FirestoreHelper.addTugas(judul, deskripsi, tanggal, waktu) { success, docId ->
                progressBar.visibility = View.GONE
                btnSimpan.isEnabled    = true

                if (success && docId != null) {
                    scheduleDeadlineAlarm(docId, judul, tanggal, waktu)
                    Toast.makeText(this, "Tugas berhasil disimpan!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Gagal menyimpan tugas.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * Alarm 1 jam sebelum deadline — tetap aktif saat offline.
     */
    private fun scheduleDeadlineAlarm(docId: String, judul: String, tanggal: String, waktu: String) {
        val deadlineMs = parseDateTime(tanggal, waktu)
        val triggerAt  = deadlineMs - 60 * 60 * 1000L  // 1 jam sebelum deadline
        if (triggerAt <= System.currentTimeMillis()) return

        val intent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_TITLE, "Deadline Tugas Mendekat!")
            putExtra(AlarmReceiver.EXTRA_MESSAGE, "Tugas '$judul' deadline $tanggal pukul $waktu (sisa ~1 jam).")
            putExtra(AlarmReceiver.EXTRA_DOC_ID, docId)
        }

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