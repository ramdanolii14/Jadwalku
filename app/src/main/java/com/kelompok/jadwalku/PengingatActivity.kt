package com.kelompok.jadwalku

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

    private lateinit var container: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pengingat)

        container = findViewById(R.id.containerPengingat)
        refreshData()
    }

    private fun refreshData() {
        container.removeAllViews()

        val currentTime       = System.currentTimeMillis()
        val batasKedaluwarsa  = 3 * 60 * 60 * 1000L
        val batasHapus        = 48 * 60 * 60 * 1000L

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
                    selisih > batasKedaluwarsa  -> "KEDALUWARSA"
                    currentTime < deadlineTime  -> "AKTIF"
                    else                        -> "MENDEKATI"
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

                // Cek apakah semua kosong (setelah kedua fetch selesai)
                if (container.childCount == 0) {
                    tampilkanKosong()
                }
            }
        }
    }

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

        val btnHapus = buatTombolHapus()
        btnHapus.setOnClickListener {
            FirestoreHelper.deleteJadwal(item.id) { success ->
                runOnUiThread {
                    if (success) {
                        refreshData()
                        Toast.makeText(this, "Jadwal berhasil dihapus.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        inner.addView(barisTas); inner.addView(tvDetail); inner.addView(btnHapus)
        card.addView(inner); container.addView(card)
    }

    private fun buatKartuTugas(item: TugasItem, status: String) {
        val isExpired  = status == "KEDALUWARSA"
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

        val btnHapus = buatTombolHapus()
        btnHapus.setOnClickListener {
            FirestoreHelper.deleteTugas(item.id) { success ->
                runOnUiThread {
                    if (success) {
                        refreshData()
                        Toast.makeText(this, "Tugas berhasil dihapus.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        inner.addView(barisTas); inner.addView(tvDeskripsi); inner.addView(tvDeadline); inner.addView(btnHapus)
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

    private fun buatTombolHapus(): Button {
        return Button(this).apply {
            text = "Hapus"; textSize = 12f
            setTextColor(Color.parseColor("#FF4444"))
            setBackgroundColor(Color.TRANSPARENT)
            val p = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            p.gravity = Gravity.END; p.setMargins(0, 4.dp, 0, 0); layoutParams = p
        }
    }

    private val Int.dp: Int     get() = (this * resources.displayMetrics.density).toInt()
    private val Float.dp: Float get() = (this * resources.displayMetrics.density)
}