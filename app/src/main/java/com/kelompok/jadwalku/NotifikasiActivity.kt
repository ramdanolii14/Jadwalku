package com.kelompok.jadwalku

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class NotifikasiActivity : AppCompatActivity() {

    private lateinit var container: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifikasi)

        container = findViewById(R.id.containerNotifikasi)

        val btnBersihkan = findViewById<Button>(R.id.btnBersihkanSemua)
        btnBersihkan.setOnClickListener { bersihkanSemuaNotifikasi() }

        muatNotifikasi()
    }

    // ── Helpers: ambil warna dari theme ──────────────────────────────────────

    /** Ambil warna dari attribute theme (misalnya R.attr.colorBgCard). */
    private fun themeColor(attrRes: Int): Int {
        val tv = TypedValue()
        theme.resolveAttribute(attrRes, tv, true)
        return tv.data
    }

    // ── Logika notifikasi ─────────────────────────────────────────────────────

    private fun bersihkanSemuaNotifikasi() {
        val sharedPref = getSharedPreferences("NotifikasiPrefs", Context.MODE_PRIVATE)
        val clearedIds = sharedPref.getStringSet("cleared_ids", mutableSetOf())?.toMutableSet()
            ?: mutableSetOf()

        FirestoreHelper.getTugas { listTugas ->
            listTugas.forEach { clearedIds.add("tugas_${it.id}") }
            FirestoreHelper.getJadwal { listJadwal ->
                listJadwal.forEach { clearedIds.add("jadwal_${it.id}") }
                sharedPref.edit().putStringSet("cleared_ids", clearedIds).apply()
                runOnUiThread {
                    container.removeAllViews()
                    tampilkanPesanKosong()
                }
            }
        }
    }

    private fun muatNotifikasi() {
        val sharedPref = getSharedPreferences("NotifikasiPrefs", Context.MODE_PRIVATE)
        val clearedIds = sharedPref.getStringSet("cleared_ids", mutableSetOf()) ?: mutableSetOf()

        FirestoreHelper.getTugas { listTugas ->
            FirestoreHelper.getJadwal { listJadwal ->
                runOnUiThread {
                    var adaNotifikasi = false

                    listTugas.forEach { item ->
                        if (!clearedIds.contains("tugas_${item.id}")) {
                            val pesan = getString(
                                R.string.notif_msg_tugas,
                                item.judul, item.tanggalDeadline, item.waktuDeadline
                            )
                            buatKartuNotifikasi(getString(R.string.notif_card_deadline), pesan)
                            adaNotifikasi = true
                        }
                    }

                    listJadwal.forEach { item ->
                        if (!clearedIds.contains("jadwal_${item.id}")) {
                            val pesan = getString(
                                R.string.notif_msg_jadwal,
                                item.namaKegiatan, item.tanggal, item.waktuMulai
                            )
                            buatKartuNotifikasi(getString(R.string.notif_card_jadwal), pesan)
                            adaNotifikasi = true
                        }
                    }

                    if (!adaNotifikasi) tampilkanPesanKosong()
                }
            }
        }
    }

    // ── View builders ─────────────────────────────────────────────────────────

    private fun tampilkanPesanKosong() {
        val tv = TextView(this)
        tv.text = getString(R.string.notif_kosong)
        tv.textSize = 14f
        tv.setTextColor(themeColor(R.attr.colorTextSecondary))
        tv.gravity = Gravity.CENTER
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 48.dp, 0, 0)
        tv.layoutParams = params
        container.addView(tv)
    }

    private fun buatKartuNotifikasi(title: String, message: String) {
        val bgCard    = themeColor(R.attr.colorBgCard)
        val textPrim  = themeColor(R.attr.colorTextPrimary)
        val textSec   = themeColor(R.attr.colorTextSecondary)

        val card = CardView(this)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 0, 0, 16.dp)
        card.layoutParams = params
        card.setCardBackgroundColor(bgCard)
        card.cardElevation = 0f

        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(20.dp, 20.dp, 20.dp, 20.dp)
            gravity = Gravity.CENTER_VERTICAL
        }

        val icon = ImageView(this)
        icon.setImageResource(android.R.drawable.ic_dialog_info)
        icon.setColorFilter(textSec)
        val iconParams = LinearLayout.LayoutParams(40.dp, 40.dp)
        iconParams.setMargins(0, 0, 16.dp, 0)
        icon.layoutParams = iconParams

        val textLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val txtTitle = TextView(this).apply {
            text = title
            textSize = 16f
            setTextColor(textPrim)
            setTypeface(null, Typeface.BOLD)
        }
        val txtMessage = TextView(this).apply {
            text = message
            textSize = 14f
            setTextColor(textSec)
            setPadding(0, 4.dp, 0, 0)
        }

        textLayout.addView(txtTitle)
        textLayout.addView(txtMessage)
        mainLayout.addView(icon)
        mainLayout.addView(textLayout)
        card.addView(mainLayout)
        container.addView(card)
    }

    private val Int.dp: Int get() = (this * resources.displayMetrics.density).toInt()
}