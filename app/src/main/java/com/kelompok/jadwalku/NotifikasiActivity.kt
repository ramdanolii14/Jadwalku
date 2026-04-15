package com.kelompok.jadwalku

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifikasi)

        dbHelper = DatabaseHelper(this)
        val container = findViewById<LinearLayout>(R.id.containerNotifikasi)

        // Tambahan: Memanggil fungsi untuk memunculkan tombol bersihkan
        tambahTombolBersihkan(container)

        muatNotifikasi(container)
    }

    // Tambahan: Fungsi untuk membuat tombol "Bersihkan Semua"
    private fun tambahTombolBersihkan(container: LinearLayout) {
        val btnBersihkan = Button(this)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.gravity = Gravity.END // Posisi di kanan
        params.setMargins(0, 0, 0, 16.dp)
        btnBersihkan.layoutParams = params
        btnBersihkan.text = "Bersihkan Semua"
        btnBersihkan.setTextColor(Color.RED)
        btnBersihkan.setBackgroundColor(Color.TRANSPARENT)

        btnBersihkan.setOnClickListener {
            // Mengosongkan seluruh isi container notifikasi
            container.removeAllViews()
            // Memunculkan kembali tombol bersihkan agar tidak ikut hilang selamanya
            tambahTombolBersihkan(container)
        }
        container.addView(btnBersihkan)
    }

    private fun muatNotifikasi(container: LinearLayout) {
        // 1. Muat pesan untuk Tugas
        val cursorTugas: Cursor = dbHelper.getAllTugas()
        if (cursorTugas.moveToFirst()) {
            do {
                val judul = cursorTugas.getString(cursorTugas.getColumnIndexOrThrow(DatabaseHelper.COLUMN_JUDUL))
                val tgl = cursorTugas.getString(cursorTugas.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TANGGAL_DEADLINE))
                val wkt = cursorTugas.getString(cursorTugas.getColumnIndexOrThrow(DatabaseHelper.COLUMN_WAKTU_DEADLINE))

                val pesan = "Jangan lupa! Tugas '$judul' harus segera diselesaikan sebelum tanggal $tgl pukul $wkt."
                buatKartuNotifikasi(container, "Peringatan Deadline", pesan)
            } while (cursorTugas.moveToNext())
        }
        cursorTugas.close()

        // 2. Muat pesan untuk Jadwal
        val cursorJadwal: Cursor = dbHelper.getAllJadwal()
        if (cursorJadwal.moveToFirst()) {
            do {
                val nama = cursorJadwal.getString(cursorJadwal.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NAMA_KEGIATAN))
                val tgl = cursorJadwal.getString(cursorJadwal.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TANGGAL))
                val wktMulai = cursorJadwal.getString(cursorJadwal.getColumnIndexOrThrow(DatabaseHelper.COLUMN_WAKTU_MULAI))

                val pesan = "Kamu memiliki jadwal '$nama' yang akan dilaksanakan pada $tgl jam $wktMulai."
                buatKartuNotifikasi(container, "Jadwal Mendatang", pesan)
            } while (cursorJadwal.moveToNext())
        }
        cursorJadwal.close()
    }

    private fun buatKartuNotifikasi(container: LinearLayout, title: String, message: String) {
        val card = CardView(this)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 0, 0, 16.dp)
        card.layoutParams = params
        card.radius = 16f.dp
        card.setCardBackgroundColor(Color.parseColor("#EEEEEE")) // Warna abu-abu murni
        card.cardElevation = 0f

        // Layout Horizontal (Kiri: Icon, Kanan: Teks)
        val mainLayout = LinearLayout(this)
        mainLayout.orientation = LinearLayout.HORIZONTAL
        mainLayout.setPadding(20.dp, 20.dp, 20.dp, 20.dp)
        mainLayout.gravity = Gravity.CENTER_VERTICAL

        // Icon Lonceng / Peringatan
        val icon = ImageView(this)
        icon.setImageResource(android.R.drawable.ic_dialog_info) // Icon bawaan Android
        icon.setColorFilter(Color.parseColor("#666666")) // Warna icon abu-abu gelap
        val iconParams = LinearLayout.LayoutParams(40.dp, 40.dp)
        iconParams.setMargins(0, 0, 16.dp, 0)
        icon.layoutParams = iconParams

        // Layout Teks
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

    // Helper konversi DP
    private val Int.dp: Int get() = (this * resources.displayMetrics.density).toInt()
    private val Float.dp: Float get() = (this * resources.displayMetrics.density)
}