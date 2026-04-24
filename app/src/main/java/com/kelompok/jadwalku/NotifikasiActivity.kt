package com.kelompok.jadwalku

import android.content.Context
import android.database.Cursor
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

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var container: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifikasi)

        dbHelper = DatabaseHelper(this)
        container = findViewById(R.id.containerNotifikasi)

        // Tombol "Bersihkan Semua" sudah ada di XML (btnBersihkanSemua), tidak perlu dibuat ulang lewat kode
        val btnBersihkan = findViewById<Button>(R.id.btnBersihkanSemua)
        btnBersihkan.setOnClickListener {
            bersihkanSemuaNotifikasi()
        }

        muatNotifikasi()
    }

    private fun bersihkanSemuaNotifikasi() {
        val sharedPref = getSharedPreferences("NotifikasiPrefs", Context.MODE_PRIVATE)
        val clearedIds = sharedPref.getStringSet("cleared_ids", mutableSetOf())?.toMutableSet() ?: mutableSetOf()

        val cursorTugas = dbHelper.getAllTugas()
        if (cursorTugas.moveToFirst()) {
            do {
                val id = cursorTugas.getInt(cursorTugas.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID))
                clearedIds.add("tugas_$id")
            } while (cursorTugas.moveToNext())
        }
        cursorTugas.close()

        val cursorJadwal = dbHelper.getAllJadwal()
        if (cursorJadwal.moveToFirst()) {
            do {
                val id = cursorJadwal.getInt(cursorJadwal.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID))
                clearedIds.add("jadwal_$id")
            } while (cursorJadwal.moveToNext())
        }
        cursorJadwal.close()

        sharedPref.edit().putStringSet("cleared_ids", clearedIds).apply()

        // Kosongkan tampilan kartu notifikasi saja (bukan seluruh container termasuk tombol)
        container.removeAllViews()
        tampilkanPesanKosong()
    }

    private fun muatNotifikasi() {
        val sharedPref = getSharedPreferences("NotifikasiPrefs", Context.MODE_PRIVATE)
        val clearedIds = sharedPref.getStringSet("cleared_ids", mutableSetOf()) ?: mutableSetOf()

        var adaNotifikasi = false

        // 1. Muat notifikasi Tugas
        val cursorTugas: Cursor = dbHelper.getAllTugas()
        if (cursorTugas.moveToFirst()) {
            do {
                val id = cursorTugas.getInt(cursorTugas.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID))
                if (!clearedIds.contains("tugas_$id")) {
                    val judul = cursorTugas.getString(cursorTugas.getColumnIndexOrThrow(DatabaseHelper.COLUMN_JUDUL))
                    val tgl = cursorTugas.getString(cursorTugas.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TANGGAL_DEADLINE))
                    val wkt = cursorTugas.getString(cursorTugas.getColumnIndexOrThrow(DatabaseHelper.COLUMN_WAKTU_DEADLINE))

                    val pesan = "Jangan lupa! Tugas '$judul' harus segera diselesaikan sebelum tanggal $tgl pukul $wkt."
                    buatKartuNotifikasi("Peringatan Deadline", pesan)
                    adaNotifikasi = true
                }
            } while (cursorTugas.moveToNext())
        }
        cursorTugas.close()

        // 2. Muat notifikasi Jadwal
        val cursorJadwal: Cursor = dbHelper.getAllJadwal()
        if (cursorJadwal.moveToFirst()) {
            do {
                val id = cursorJadwal.getInt(cursorJadwal.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID))
                if (!clearedIds.contains("jadwal_$id")) {
                    val nama = cursorJadwal.getString(cursorJadwal.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NAMA_KEGIATAN))
                    val tgl = cursorJadwal.getString(cursorJadwal.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TANGGAL))
                    val wktMulai = cursorJadwal.getString(cursorJadwal.getColumnIndexOrThrow(DatabaseHelper.COLUMN_WAKTU_MULAI))

                    val pesan = "Kamu memiliki jadwal '$nama' yang akan dilaksanakan pada $tgl jam $wktMulai."
                    buatKartuNotifikasi("Jadwal Mendatang", pesan)
                    adaNotifikasi = true
                }
            } while (cursorJadwal.moveToNext())
        }
        cursorJadwal.close()

        if (!adaNotifikasi) {
            tampilkanPesanKosong()
        }
    }

    private fun tampilkanPesanKosong() {
        val txtKosong = TextView(this)
        txtKosong.text = "Tidak ada notifikasi saat ini."
        txtKosong.textSize = 14f
        txtKosong.setTextColor(Color.parseColor("#888888"))
        txtKosong.gravity = Gravity.CENTER
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 48.dp, 0, 0)
        txtKosong.layoutParams = params
        container.addView(txtKosong)
    }

    private fun buatKartuNotifikasi(title: String, message: String) {
        val card = CardView(this)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 0, 0, 16.dp)
        card.layoutParams = params
        card.setCardBackgroundColor(Color.parseColor("#EEEEEE"))
        card.cardElevation = 0f

        val mainLayout = LinearLayout(this)
        mainLayout.orientation = LinearLayout.HORIZONTAL
        mainLayout.setPadding(20.dp, 20.dp, 20.dp, 20.dp)
        mainLayout.gravity = Gravity.CENTER_VERTICAL

        val icon = ImageView(this)
        icon.setImageResource(android.R.drawable.ic_dialog_info)
        icon.setColorFilter(Color.parseColor("#666666"))
        val iconParams = LinearLayout.LayoutParams(40.dp, 40.dp)
        iconParams.setMargins(0, 0, 16.dp, 0)
        icon.layoutParams = iconParams

        val textLayout = LinearLayout(this)
        textLayout.orientation = LinearLayout.VERTICAL
        textLayout.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

        val txtTitle = TextView(this)
        txtTitle.text = title
        txtTitle.textSize = 16f
        txtTitle.setTextColor(Color.BLACK)
        txtTitle.setTypeface(null, android.graphics.Typeface.BOLD)

        val txtMessage = TextView(this)
        txtMessage.text = message
        txtMessage.textSize = 14f
        txtMessage.setTextColor(Color.parseColor("#444444"))
        txtMessage.setPadding(0, 4.dp, 0, 0)

        textLayout.addView(txtTitle)
        textLayout.addView(txtMessage)

        mainLayout.addView(icon)
        mainLayout.addView(textLayout)
        card.addView(mainLayout)
        container.addView(card)
    }

    private val Int.dp: Int get() = (this * resources.displayMetrics.density).toInt()
    private val Float.dp: Float get() = (this * resources.displayMetrics.density)
}