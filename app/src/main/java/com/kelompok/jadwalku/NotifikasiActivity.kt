package com.kelompok.jadwalku

import android.content.Context
import android.graphics.Color
import android.os.Bundle
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

    private fun bersihkanSemuaNotifikasi() {
        val sharedPref = getSharedPreferences("NotifikasiPrefs", Context.MODE_PRIVATE)
        val clearedIds = sharedPref.getStringSet("cleared_ids", mutableSetOf())?.toMutableSet() ?: mutableSetOf()

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
                            val pesan = "Jangan lupa! Tugas '${item.judul}' harus diselesaikan sebelum ${item.tanggalDeadline} pukul ${item.waktuDeadline}."
                            buatKartuNotifikasi("Peringatan Deadline", pesan)
                            adaNotifikasi = true
                        }
                    }

                    listJadwal.forEach { item ->
                        if (!clearedIds.contains("jadwal_${item.id}")) {
                            val pesan = "Kamu memiliki jadwal '${item.namaKegiatan}' yang akan dilaksanakan pada ${item.tanggal} jam ${item.waktuMulai}."
                            buatKartuNotifikasi("Jadwal Mendatang", pesan)
                            adaNotifikasi = true
                        }
                    }

                    if (!adaNotifikasi) tampilkanPesanKosong()
                }
            }
        }
    }

    private fun tampilkanPesanKosong() {
        val tv = TextView(this)
        tv.text = "Tidak ada notifikasi saat ini."
        tv.textSize = 14f
        tv.setTextColor(Color.parseColor("#888888"))
        tv.gravity = Gravity.CENTER
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        params.setMargins(0, 48.dp, 0, 0)
        tv.layoutParams = params
        container.addView(tv)
    }

    private fun buatKartuNotifikasi(title: String, message: String) {
        val card = CardView(this)
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        params.setMargins(0, 0, 0, 16.dp)
        card.layoutParams = params
        card.setCardBackgroundColor(Color.parseColor("#EEEEEE"))
        card.cardElevation = 0f

        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(20.dp, 20.dp, 20.dp, 20.dp)
            gravity = Gravity.CENTER_VERTICAL
        }

        val icon = ImageView(this)
        icon.setImageResource(android.R.drawable.ic_dialog_info)
        icon.setColorFilter(Color.parseColor("#666666"))
        val iconParams = LinearLayout.LayoutParams(40.dp, 40.dp)
        iconParams.setMargins(0, 0, 16.dp, 0)
        icon.layoutParams = iconParams

        val textLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val txtTitle = TextView(this).apply {
            text = title; textSize = 16f; setTextColor(Color.BLACK)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        val txtMessage = TextView(this).apply {
            text = message; textSize = 14f; setTextColor(Color.parseColor("#444444"))
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