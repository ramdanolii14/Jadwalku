package com.kelompok.jadwalku

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PengingatActivity : AppCompatActivity() {

    private lateinit var container: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pengingat)

        container = findViewById(R.id.containerPengingat)
        refreshData()
    }

    private fun refreshData() {
        container.removeAllViews()

        val currentTime      = System.currentTimeMillis()
        val batasKedaluwarsa = 3 * 60 * 60 * 1000L
        val batasHapus       = 48 * 60 * 60 * 1000L

        // ── Jadwal dari Firestore ──
        FirestoreHelper.getJadwal { listJadwal ->
            val filtered = listJadwal.mapNotNull { item ->
                val eventTime = getTimestamp(item.tanggal, item.waktuMulai)
                if (eventTime == 0L) return@mapNotNull item to "AKTIF"

                val selisih = currentTime - eventTime
                if (selisih > batasHapus) {
                    FirestoreHelper.deleteJadwal(item.id)
                    return@mapNotNull null
                }
                val status = when {
                    selisih > batasKedaluwarsa -> "KEDALUWARSA"
                    currentTime < eventTime    -> "AKTIF"
                    else                       -> "BERLANGSUNG"
                }
                item to status
            }

            runOnUiThread {
                if (filtered.isNotEmpty()) {
                    buatSectionHeader("📅  Jadwal Kegiatan")
                    filtered.forEach { (item, status) ->
                        buatKartuJadwal(item, status)
                    }
                }
            }
        }

        // ── Tugas dari Firestore ──
        FirestoreHelper.getTugas { listTugas ->
            val filtered = listTugas.mapNotNull { item ->
                val deadlineTime = getTimestamp(item.tanggalDeadline, item.waktuDeadline)
                if (deadlineTime == 0L) return@mapNotNull item to "AKTIF"

                val selisih = currentTime - deadlineTime
                if (selisih > batasHapus) {
                    FirestoreHelper.deleteTugas(item.id)
                    return@mapNotNull null
                }
                val status = when {
                    selisih > batasKedaluwarsa -> "KEDALUWARSA"
                    currentTime < deadlineTime -> "AKTIF"
                    else                       -> "MENDEKATI"
                }
                item to status
            }

            runOnUiThread {
                if (filtered.isNotEmpty()) {
                    buatSectionHeader("📝  Tugas")
                    filtered.forEach { (item, status) ->
                        buatKartuTugas(item, status)
                    }
                }
                if (container.childCount == 0) tampilkanKosong()
            }
        }
    }

    // ─────────────────── DIALOG EDIT JADWAL ───────────────────

    private fun tampilkanDialogEditJadwal(item: JadwalItem) {
        val dialogLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 32, 48, 16)
        }

        fun buatLabel(teks: String): TextView = TextView(this).apply {
            text = teks; textSize = 13f; setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#333333"))
            val p = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            p.setMargins(0, 12.dp, 0, 4.dp); layoutParams = p
        }

        fun buatInput(nilai: String, focusable: Boolean = true): EditText = EditText(this).apply {
            setText(nilai); textSize = 14f
            setTextColor(Color.parseColor("#111111"))
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(Color.parseColor("#EEEEEE")); cornerRadius = 8f.dp
            }
            isFocusable = focusable; isFocusableInTouchMode = focusable
            setPadding(16.dp, 12.dp, 16.dp, 12.dp)
        }

        val etNama        = buatInput(item.namaKegiatan)
        val etTanggal     = buatInput(item.tanggal, focusable = false)
        val etWaktuMulai  = buatInput(item.waktuMulai, focusable = false)
        val etWaktuSelesai= buatInput(item.waktuSelesai, focusable = false)

        etTanggal.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(this, { _, y, m, d ->
                etTanggal.setText("$d/${m + 1}/$y")
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }
        etWaktuMulai.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(this, { _, h, min ->
                etWaktuMulai.setText(String.format("%02d:%02d", h, min))
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }
        etWaktuSelesai.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(this, { _, h, min ->
                etWaktuSelesai.setText(String.format("%02d:%02d", h, min))
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }

        dialogLayout.addView(buatLabel("Nama Kegiatan"));  dialogLayout.addView(etNama)
        dialogLayout.addView(buatLabel("Tanggal"));         dialogLayout.addView(etTanggal)
        dialogLayout.addView(buatLabel("Waktu Mulai"));     dialogLayout.addView(etWaktuMulai)
        dialogLayout.addView(buatLabel("Waktu Selesai"));   dialogLayout.addView(etWaktuSelesai)

        AlertDialog.Builder(this)
            .setTitle("Edit Jadwal")
            .setView(dialogLayout)
            .setPositiveButton("Simpan") { _, _ ->
                val nama       = etNama.text.toString().trim()
                val tanggal    = etTanggal.text.toString().trim()
                val waktuMulai = etWaktuMulai.text.toString().trim()
                val waktuSelesai = etWaktuSelesai.text.toString().trim()

                if (nama.isEmpty() || tanggal.isEmpty() || waktuMulai.isEmpty() || waktuSelesai.isEmpty()) {
                    Toast.makeText(this, "Semua field harus diisi.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                FirestoreHelper.updateJadwal(item.id, nama, tanggal, waktuMulai, waktuSelesai) { success ->
                    runOnUiThread {
                        if (success) {
                            // Batalkan alarm lama lalu jadwalkan ulang dengan data baru
                            cancelAlarm(item.id)
                            scheduleJadwalAlarms(item.id, nama, tanggal, waktuMulai)
                            Toast.makeText(this, "Jadwal berhasil diperbarui.", Toast.LENGTH_SHORT).show()
                            refreshData()
                        } else {
                            Toast.makeText(this, "Gagal memperbarui jadwal.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    // ─────────────────── DIALOG EDIT TUGAS ───────────────────

    private fun tampilkanDialogEditTugas(item: TugasItem) {
        val dialogLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 32, 48, 16)
        }

        fun buatLabel(teks: String): TextView = TextView(this).apply {
            text = teks; textSize = 13f; setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#333333"))
            val p = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            p.setMargins(0, 12.dp, 0, 4.dp); layoutParams = p
        }

        fun buatInput(nilai: String, focusable: Boolean = true): EditText = EditText(this).apply {
            setText(nilai); textSize = 14f
            setTextColor(Color.parseColor("#111111"))
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(Color.parseColor("#EEEEEE")); cornerRadius = 8f.dp
            }
            isFocusable = focusable; isFocusableInTouchMode = focusable
            setPadding(16.dp, 12.dp, 16.dp, 12.dp)
        }

        val etJudul     = buatInput(item.judul)
        val etDeskripsi = buatInput(item.deskripsi)
        val etTanggal   = buatInput(item.tanggalDeadline, focusable = false)
        val etWaktu     = buatInput(item.waktuDeadline, focusable = false)

        etTanggal.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(this, { _, y, m, d ->
                etTanggal.setText("$d/${m + 1}/$y")
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }
        etWaktu.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(this, { _, h, min ->
                etWaktu.setText(String.format("%02d:%02d", h, min))
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }

        dialogLayout.addView(buatLabel("Judul Tugas"));    dialogLayout.addView(etJudul)
        dialogLayout.addView(buatLabel("Deskripsi"));      dialogLayout.addView(etDeskripsi)
        dialogLayout.addView(buatLabel("Tanggal Deadline")); dialogLayout.addView(etTanggal)
        dialogLayout.addView(buatLabel("Waktu Deadline")); dialogLayout.addView(etWaktu)

        AlertDialog.Builder(this)
            .setTitle("Edit Tugas")
            .setView(dialogLayout)
            .setPositiveButton("Simpan") { _, _ ->
                val judul     = etJudul.text.toString().trim()
                val deskripsi = etDeskripsi.text.toString().trim()
                val tanggal   = etTanggal.text.toString().trim()
                val waktu     = etWaktu.text.toString().trim()

                if (judul.isEmpty() || deskripsi.isEmpty() || tanggal.isEmpty() || waktu.isEmpty()) {
                    Toast.makeText(this, "Semua field harus diisi.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                FirestoreHelper.updateTugas(item.id, judul, deskripsi, tanggal, waktu) { success ->
                    runOnUiThread {
                        if (success) {
                            cancelAlarm(item.id)
                            scheduleTugasAlarms(item.id, judul, tanggal, waktu)
                            Toast.makeText(this, "Tugas berhasil diperbarui.", Toast.LENGTH_SHORT).show()
                            refreshData()
                        } else {
                            Toast.makeText(this, "Gagal memperbarui tugas.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    // ─────────────────── ALARM HELPERS ───────────────────

    /**
     * Jadwalkan 4 alarm lokal untuk jadwal:
     * 2 jam, 1 jam, 5 menit, 3 menit sebelum mulai.
     */
    private fun scheduleJadwalAlarms(docId: String, nama: String, tanggal: String, waktuMulai: String) {
        val eventMs = getTimestamp(tanggal, waktuMulai)
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
                    docId       = "${docId}_j$index",
                    title       = "Jadwal Segera Dimulai",
                    message     = "$nama akan dimulai $label lagi.",
                    triggerAt   = triggerAt
                )
            }
        }
    }

    /**
     * Jadwalkan 4 alarm lokal untuk tugas:
     * 2 jam, 1 jam, 5 menit, 3 menit sebelum deadline.
     */
    private fun scheduleTugasAlarms(docId: String, judul: String, tanggal: String, waktu: String) {
        val deadlineMs = getTimestamp(tanggal, waktu)
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

    /** Batalkan semua 4 alarm (j0–j3 atau t0–t3) untuk satu item. */
    private fun cancelAlarm(docId: String) {
        val suffixes = listOf("_j0","_j1","_j2","_j3","_t0","_t1","_t2","_t3")
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        suffixes.forEach { suffix ->
            val id = "$docId$suffix"
            val intent = Intent(this, AlarmReceiver::class.java)
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
            val pending = PendingIntent.getBroadcast(this, id.hashCode(), intent, flags)
            alarmManager.cancel(pending)
        }
    }

    // ─────────────────── UI BUILDERS ───────────────────

    private fun tampilkanKosong() {
        val tv = TextView(this)
        tv.text = "Tidak ada pengingat aktif saat ini."
        tv.textSize = 14f
        tv.setTextColor(Color.parseColor("#888888"))
        tv.gravity = Gravity.CENTER
        val p = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        p.setMargins(0, 64.dp, 0, 0)
        tv.layoutParams = p
        container.addView(tv)
    }

    private fun getTimestamp(tanggal: String, waktu: String): Long {
        return try {
            val sdf = SimpleDateFormat("d/M/yyyy HH:mm", Locale.getDefault())
            sdf.parse("$tanggal $waktu")?.time ?: 0L
        } catch (e: Exception) { 0L }
    }

    private fun buatSectionHeader(judul: String) {
        val tv = TextView(this)
        tv.text = judul
        tv.textSize = 15f
        tv.setTypeface(null, Typeface.BOLD)
        tv.setTextColor(Color.parseColor("#333333"))
        val p = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        p.setMargins(0, 8.dp, 0, 8.dp)
        tv.layoutParams = p
        container.addView(tv)
    }

    private fun buatKartuJadwal(item: JadwalItem, status: String) {
        val isExpired     = status == "KEDALUWARSA"
        val isBerlangsung = status == "BERLANGSUNG"

        val bgColor    = when { isExpired -> "#FFF3F3"; isBerlangsung -> "#F0FFF4"; else -> "#F5F5F5" }
        val badgeColor = when { isExpired -> "#FF4444"; isBerlangsung -> "#00AA55"; else -> "#4488FF" }
        val badgeText  = when { isExpired -> "Kedaluwarsa"; isBerlangsung -> "Berlangsung"; else -> "Mendatang" }

        val card  = buatCardBase(bgColor)
        val inner = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(20.dp, 16.dp, 20.dp, 16.dp) }

        val barisTas = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val tvNama   = TextView(this).apply {
            text = item.namaKegiatan; textSize = 16f; setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#222222"))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        barisTas.addView(tvNama)
        barisTas.addView(buatBadge(badgeText, badgeColor))

        val tvDetail = TextView(this).apply {
            text = "📅 ${item.tanggal}   🕐 ${item.waktuMulai} – ${item.waktuSelesai}"
            textSize = 13f; setTextColor(Color.parseColor("#666666"))
            val p = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            p.setMargins(0, 6.dp, 0, 0); layoutParams = p
        }

        // Baris tombol Edit & Hapus
        val barisBtn = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.END
            val p = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            p.setMargins(0, 4.dp, 0, 0); layoutParams = p
        }
        val btnEdit  = buatTombolEdit()
        val btnHapus = buatTombolHapus()

        btnEdit.setOnClickListener { tampilkanDialogEditJadwal(item) }
        btnHapus.setOnClickListener {
            FirestoreHelper.deleteJadwal(item.id) { success ->
                runOnUiThread {
                    if (success) {
                        cancelAlarm(item.id)
                        refreshData()
                        Toast.makeText(this, "Jadwal berhasil dihapus.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        barisBtn.addView(btnEdit); barisBtn.addView(btnHapus)
        inner.addView(barisTas); inner.addView(tvDetail); inner.addView(barisBtn)
        card.addView(inner); container.addView(card)
    }

    private fun buatKartuTugas(item: TugasItem, status: String) {
        val isExpired   = status == "KEDALUWARSA"
        val isMendekati = status == "MENDEKATI"

        val bgColor    = when { isExpired -> "#FFF3F3"; isMendekati -> "#FFFBF0"; else -> "#F5F5F5" }
        val badgeColor = when { isExpired -> "#FF4444"; isMendekati -> "#FF8800"; else -> "#4488FF" }
        val badgeText  = when { isExpired -> "Kedaluwarsa"; isMendekati -> "Segera!"; else -> "Aktif" }

        val card  = buatCardBase(bgColor)
        val inner = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(20.dp, 16.dp, 20.dp, 16.dp) }

        val barisTas = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val tvJudul  = TextView(this).apply {
            text = item.judul; textSize = 16f; setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#222222"))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        barisTas.addView(tvJudul)
        barisTas.addView(buatBadge(badgeText, badgeColor))

        val tvDeskripsi = TextView(this).apply {
            text = item.deskripsi; textSize = 13f; setTextColor(Color.parseColor("#555555"))
            val p = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            p.setMargins(0, 4.dp, 0, 0); layoutParams = p
        }
        val tvDeadline = TextView(this).apply {
            text = "⏰ Deadline: ${item.tanggalDeadline} pukul ${item.waktuDeadline}"
            textSize = 13f
            setTextColor(if (isExpired) Color.parseColor("#CC0000") else Color.parseColor("#666666"))
            val p = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            p.setMargins(0, 6.dp, 0, 0); layoutParams = p
        }

        val barisBtn = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.END
            val p = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            p.setMargins(0, 4.dp, 0, 0); layoutParams = p
        }
        val btnEdit  = buatTombolEdit()
        val btnHapus = buatTombolHapus()

        btnEdit.setOnClickListener { tampilkanDialogEditTugas(item) }
        btnHapus.setOnClickListener {
            FirestoreHelper.deleteTugas(item.id) { success ->
                runOnUiThread {
                    if (success) {
                        cancelAlarm(item.id)
                        refreshData()
                        Toast.makeText(this, "Tugas berhasil dihapus.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        barisBtn.addView(btnEdit); barisBtn.addView(btnHapus)
        inner.addView(barisTas); inner.addView(tvDeskripsi); inner.addView(tvDeadline); inner.addView(barisBtn)
        card.addView(inner); container.addView(card)
    }

    private fun buatCardBase(bgColorHex: String): CardView {
        val card = CardView(this)
        val p = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        p.setMargins(0, 0, 0, 12.dp)
        card.layoutParams = p
        card.setCardBackgroundColor(Color.parseColor(bgColorHex))
        card.cardElevation = 2f.dp
        return card
    }

    private fun buatBadge(text: String, colorHex: String): TextView {
        return TextView(this).apply {
            this.text = text; textSize = 11f; setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(Color.parseColor(colorHex)); cornerRadius = 20f.dp
            }
            setPadding(10.dp, 4.dp, 10.dp, 4.dp)
        }
    }

    private fun buatTombolEdit(): Button {
        return Button(this).apply {
            text = "Edit"; textSize = 12f
            setTextColor(Color.parseColor("#444444"))
            setBackgroundColor(Color.TRANSPARENT)
            val p = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            p.setMargins(0, 0, 8.dp, 0); layoutParams = p
        }
    }

    private fun buatTombolHapus(): Button {
        return Button(this).apply {
            text = "Hapus"; textSize = 12f
            setTextColor(Color.parseColor("#FF4444"))
            setBackgroundColor(Color.TRANSPARENT)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        }
    }

    private val Int.dp: Int     get() = (this * resources.displayMetrics.density).toInt()
    private val Float.dp: Float get() = (this * resources.displayMetrics.density)
}