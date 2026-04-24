package com.kelompok.jadwalku

import android.database.Cursor
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import java.text.SimpleDateFormat
import java.util.Locale

class PengingatActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var container: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pengingat)

        dbHelper = DatabaseHelper(this)
        container = findViewById(R.id.containerPengingat)

        refreshData()
    }

    private fun getTimestamp(tanggal: String, waktu: String): Long {
        return try {
            val format = SimpleDateFormat("d/M/yyyy HH:mm", Locale.getDefault())
            val date = format.parse("$tanggal $waktu")
            date?.time ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    private fun refreshData() {
        container.removeAllViews()

        val currentTime = System.currentTimeMillis()
        val batasKedaluwarsa = 3 * 60 * 60 * 1000L
        val batasHapus = 48 * 60 * 60 * 1000L

        // --- SECTION: JADWAL ---
        val listJadwal = mutableListOf<Map<String, Any>>()
        val cursorJadwal: Cursor = dbHelper.getAllJadwal()
        if (cursorJadwal.moveToFirst()) {
            do {
                val id = cursorJadwal.getInt(cursorJadwal.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID))
                val nama = cursorJadwal.getString(cursorJadwal.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NAMA_KEGIATAN))
                val tgl = cursorJadwal.getString(cursorJadwal.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TANGGAL))
                val wkt = cursorJadwal.getString(cursorJadwal.getColumnIndexOrThrow(DatabaseHelper.COLUMN_WAKTU_MULAI))
                val wktSelesai = cursorJadwal.getString(cursorJadwal.getColumnIndexOrThrow(DatabaseHelper.COLUMN_WAKTU_SELESAI))

                val eventTime = getTimestamp(tgl, wkt)
                if (eventTime > 0L) {
                    val selisih = currentTime - eventTime
                    if (selisih > batasHapus) {
                        dbHelper.deleteJadwal(id)
                        continue
                    }
                    val status = when {
                        selisih > batasKedaluwarsa -> "KEDALUWARSA"
                        currentTime < eventTime -> "AKTIF"
                        else -> "BERLANGSUNG"
                    }
                    listJadwal.add(mapOf("id" to id, "nama" to nama, "tgl" to tgl, "wkt" to wkt, "wktSelesai" to wktSelesai, "status" to status))
                } else {
                    listJadwal.add(mapOf("id" to id, "nama" to nama, "tgl" to tgl, "wkt" to wkt, "wktSelesai" to wktSelesai, "status" to "AKTIF"))
                }
            } while (cursorJadwal.moveToNext())
        }
        cursorJadwal.close()

        // --- SECTION: TUGAS ---
        val listTugas = mutableListOf<Map<String, Any>>()
        val cursorTugas: Cursor = dbHelper.getAllTugas()
        if (cursorTugas.moveToFirst()) {
            do {
                val id = cursorTugas.getInt(cursorTugas.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID))
                val judul = cursorTugas.getString(cursorTugas.getColumnIndexOrThrow(DatabaseHelper.COLUMN_JUDUL))
                val deskripsi = cursorTugas.getString(cursorTugas.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESKRIPSI))
                val tgl = cursorTugas.getString(cursorTugas.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TANGGAL_DEADLINE))
                val wkt = cursorTugas.getString(cursorTugas.getColumnIndexOrThrow(DatabaseHelper.COLUMN_WAKTU_DEADLINE))

                val deadlineTime = getTimestamp(tgl, wkt)
                if (deadlineTime > 0L) {
                    val selisih = currentTime - deadlineTime
                    if (selisih > batasHapus) {
                        dbHelper.deleteTugas(id)
                        continue
                    }
                    val status = when {
                        selisih > batasKedaluwarsa -> "KEDALUWARSA"
                        currentTime < deadlineTime -> "AKTIF"
                        else -> "MENDEKATI"
                    }
                    listTugas.add(mapOf("id" to id, "judul" to judul, "deskripsi" to deskripsi, "tgl" to tgl, "wkt" to wkt, "status" to status))
                } else {
                    listTugas.add(mapOf("id" to id, "judul" to judul, "deskripsi" to deskripsi, "tgl" to tgl, "wkt" to wkt, "status" to "AKTIF"))
                }
            } while (cursorTugas.moveToNext())
        }
        cursorTugas.close()

        // Render Section Jadwal
        if (listJadwal.isNotEmpty()) {
            buatSectionHeader("📅  Jadwal Kegiatan")
            listJadwal.forEach { item ->
                buatKartuJadwal(
                    id = item["id"] as Int,
                    nama = item["nama"] as String,
                    tgl = item["tgl"] as String,
                    wktMulai = item["wkt"] as String,
                    wktSelesai = item["wktSelesai"] as String,
                    status = item["status"] as String
                )
            }
        }

        // Render Section Tugas
        if (listTugas.isNotEmpty()) {
            buatSectionHeader("📝  Tugas")
            listTugas.forEach { item ->
                buatKartuTugas(
                    id = item["id"] as Int,
                    judul = item["judul"] as String,
                    deskripsi = item["deskripsi"] as String,
                    tgl = item["tgl"] as String,
                    wkt = item["wkt"] as String,
                    status = item["status"] as String
                )
            }
        }

        // Tampilkan pesan kosong jika tidak ada data
        if (listJadwal.isEmpty() && listTugas.isEmpty()) {
            val txtKosong = TextView(this)
            txtKosong.text = "Tidak ada pengingat aktif saat ini."
            txtKosong.textSize = 14f
            txtKosong.setTextColor(Color.parseColor("#888888"))
            txtKosong.gravity = Gravity.CENTER
            val p = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            p.setMargins(0, 64.dp, 0, 0)
            txtKosong.layoutParams = p
            container.addView(txtKosong)
        }
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

    private fun buatKartuJadwal(id: Int, nama: String, tgl: String, wktMulai: String, wktSelesai: String, status: String) {
        val isExpired = status == "KEDALUWARSA"
        val isBerlangsung = status == "BERLANGSUNG"

        val bgColor = when {
            isExpired -> "#FFF3F3"
            isBerlangsung -> "#F0FFF4"
            isBerlangsung -> "#F0FFF4"
            else -> "#F5F5F5"
        }
        val badgeColor = when {
            isExpired -> "#FF4444"
            isBerlangsung -> "#00AA55"
            else -> "#4488FF"
        }
        val badgeText = when {
            isExpired -> "Kedaluwarsa"
            isBerlangsung -> "Berlangsung"
            else -> "Mendatang"
        }

        val card = buatCardBase(bgColor)
        val inner = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20.dp, 16.dp, 20.dp, 16.dp)
        }

        // Baris atas: nama + badge
        val barisTas = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val tvNama = TextView(this).apply {
            text = nama
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#222222"))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val tvBadge = buatBadge(badgeText, badgeColor)
        barisTas.addView(tvNama)
        barisTas.addView(tvBadge)

        // Detail waktu
        val tvDetail = TextView(this).apply {
            text = "📅 $tgl   🕐 $wktMulai – $wktSelesai"
            textSize = 13f
            setTextColor(Color.parseColor("#666666"))
            val p = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            p.setMargins(0, 6.dp, 0, 0)
            layoutParams = p
        }

        // Tombol hapus
        val btnHapus = buatTombolHapus()
        btnHapus.setOnClickListener {
            dbHelper.deleteJadwal(id)
            refreshData()
            Toast.makeText(this, "Jadwal berhasil dihapus.", Toast.LENGTH_SHORT).show()
        }

        inner.addView(barisTas)
        inner.addView(tvDetail)
        inner.addView(btnHapus)
        card.addView(inner)
        container.addView(card)
    }

    private fun buatKartuTugas(id: Int, judul: String, deskripsi: String, tgl: String, wkt: String, status: String) {
        val isExpired = status == "KEDALUWARSA"
        val isMendekati = status == "MENDEKATI"

        val bgColor = when {
            isExpired -> "#FFF3F3"
            isMendekati -> "#FFFBF0"
            else -> "#F5F5F5"
        }
        val badgeColor = when {
            isExpired -> "#FF4444"
            isMendekati -> "#FF8800"
            else -> "#4488FF"
        }
        val badgeText = when {
            isExpired -> "Kedaluwarsa"
            isMendekati -> "Segera!"
            else -> "Aktif"
        }

        val card = buatCardBase(bgColor)
        val inner = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20.dp, 16.dp, 20.dp, 16.dp)
        }

        // Baris atas: judul + badge
        val barisTas = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val tvJudul = TextView(this).apply {
            text = judul
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#222222"))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val tvBadge = buatBadge(badgeText, badgeColor)
        barisTas.addView(tvJudul)
        barisTas.addView(tvBadge)

        // Deskripsi
        val tvDeskripsi = TextView(this).apply {
            text = deskripsi
            textSize = 13f
            setTextColor(Color.parseColor("#555555"))
            val p = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            p.setMargins(0, 4.dp, 0, 0)
            layoutParams = p
        }

        // Detail deadline
        val tvDeadline = TextView(this).apply {
            text = "⏰ Deadline: $tgl pukul $wkt"
            textSize = 13f
            setTextColor(if (isExpired) Color.parseColor("#CC0000") else Color.parseColor("#666666"))
            val p = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            p.setMargins(0, 6.dp, 0, 0)
            layoutParams = p
        }

        // Tombol hapus
        val btnHapus = buatTombolHapus()
        btnHapus.setOnClickListener {
            dbHelper.deleteTugas(id)
            refreshData()
            Toast.makeText(this, "Tugas berhasil dihapus.", Toast.LENGTH_SHORT).show()
        }

        inner.addView(barisTas)
        inner.addView(tvDeskripsi)
        inner.addView(tvDeadline)
        inner.addView(btnHapus)
        card.addView(inner)
        container.addView(card)
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
            this.text = text
            textSize = 11f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor(colorHex))
            setPadding(10.dp, 4.dp, 10.dp, 4.dp)
            // Rounded badge menggunakan background drawable sederhana
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(Color.parseColor(colorHex))
                cornerRadius = 20f.dp
            }
        }
    }

    private fun buatTombolHapus(): Button {
        return Button(this).apply {
            text = "Hapus"
            textSize = 12f
            setTextColor(Color.parseColor("#FF4444"))
            setBackgroundColor(Color.TRANSPARENT)
            val p = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            p.gravity = Gravity.END
            p.setMargins(0, 4.dp, 0, 0)
            layoutParams = p
        }
    }

    private val Int.dp: Int get() = (this * resources.displayMetrics.density).toInt()
    private val Float.dp: Float get() = (this * resources.displayMetrics.density)
}