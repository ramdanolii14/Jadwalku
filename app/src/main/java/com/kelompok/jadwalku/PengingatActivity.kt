package com.kelompok.jadwalku

import android.content.Intent
import android.database.Cursor
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

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

    private fun refreshData() {
        container.removeAllViews() // Kosongkan tampilan sebelum memuat ulang

        // Memuat Data Jadwal
        val cursorJadwal: Cursor = dbHelper.getAllJadwal()
        if (cursorJadwal.moveToFirst()) {
            do {
                val id = cursorJadwal.getInt(cursorJadwal.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID))
                val nama = cursorJadwal.getString(cursorJadwal.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NAMA_KEGIATAN))
                val tgl = cursorJadwal.getString(cursorJadwal.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TANGGAL))

                buatKartu(id, "JADWAL: $nama", tgl, true)
            } while (cursorJadwal.moveToNext())
        }
        cursorJadwal.close()

        // Memuat Data Tugas
        val cursorTugas: Cursor = dbHelper.getAllTugas()
        if (cursorTugas.moveToFirst()) {
            do {
                val id = cursorTugas.getInt(cursorTugas.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID))
                val judul = cursorTugas.getString(cursorTugas.getColumnIndexOrThrow(DatabaseHelper.COLUMN_JUDUL))
                val tgl = cursorTugas.getString(cursorTugas.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TANGGAL_DEADLINE))

                buatKartu(id, "TUGAS: $judul", "Deadline: $tgl", false)
            } while (cursorTugas.moveToNext())
        }
        cursorTugas.close()
    }

    private fun buatKartu(id: Int, title: String, subtitle: String, isJadwal: Boolean) {
        val card = CardView(this)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 0, 0, 16.dp)
        card.layoutParams = params
        card.radius = 16f.dp
        card.setCardBackgroundColor(Color.parseColor("#EEEEEE"))
        card.cardElevation = 0f

        // Layout utama di dalam kartu (Horizontal untuk teks dan tombol hapus)
        val mainLayout = LinearLayout(this)
        mainLayout.orientation = LinearLayout.HORIZONTAL
        mainLayout.setPadding(20.dp, 20.dp, 20.dp, 20.dp)
        mainLayout.gravity = Gravity.CENTER_VERTICAL

        // Layout teks (Vertical)
        val textLayout = LinearLayout(this)
        textLayout.orientation = LinearLayout.VERTICAL
        textLayout.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

        val txtTitle = TextView(this)
        txtTitle.text = title
        txtTitle.textSize = 16f
        txtTitle.setTextColor(Color.BLACK)

        val txtSub = TextView(this)
        txtSub.text = subtitle
        txtSub.textSize = 14f
        txtSub.setTextColor(Color.GRAY)

        textLayout.addView(txtTitle)
        textLayout.addView(txtSub)

        // Tombol Hapus
        val btnHapus = Button(this)
        btnHapus.text = "Hapus"
        btnHapus.textSize = 12f
        btnHapus.setTextColor(Color.RED)
        btnHapus.setBackgroundColor(Color.TRANSPARENT)

        btnHapus.setOnClickListener {
            // 1. Hapus dari SQLite
            if (isJadwal) {
                dbHelper.deleteJadwal(id)
            } else {
                dbHelper.deleteTugas(id)
            }

            // 2. Refresh UI
            refreshData()
            Toast.makeText(this, "Berhasil dihapus. Silakan hapus di Kalender.", Toast.LENGTH_LONG).show()

            // 3. Redirect ke aplikasi Kalender
            val calendarIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("content://com.android.calendar/time/")
            }
            startActivity(calendarIntent)
        }

        mainLayout.addView(textLayout)
        mainLayout.addView(btnHapus)
        card.addView(mainLayout)
        container.addView(card)
    }

    private val Int.dp: Int get() = (this * resources.displayMetrics.density).toInt()
    private val Float.dp: Float get() = (this * resources.displayMetrics.density)
}