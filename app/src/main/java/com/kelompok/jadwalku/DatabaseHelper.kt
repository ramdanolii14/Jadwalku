package com.kelompok.jadwalku

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "Jadwalku.db"
        private const val DATABASE_VERSION = 2

        const val COLUMN_ID = "id"
        const val COLUMN_USER_ID = "user_id"

        const val TABLE_TUGAS = "tugas"
        const val COLUMN_JUDUL = "judul"
        const val COLUMN_DESKRIPSI = "deskripsi"
        const val COLUMN_TANGGAL_DEADLINE = "tanggal_deadline"
        const val COLUMN_WAKTU_DEADLINE = "waktu_deadline"

        const val TABLE_JADWAL = "jadwal"
        const val COLUMN_NAMA_KEGIATAN = "nama_kegiatan"
        const val COLUMN_TANGGAL = "tanggal"
        const val COLUMN_WAKTU_MULAI = "waktu_mulai"
        const val COLUMN_WAKTU_SELESAI = "waktu_selesai"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableTugas = ("CREATE TABLE " + TABLE_TUGAS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USER_ID + " INTEGER,"
                + COLUMN_JUDUL + " TEXT,"
                + COLUMN_DESKRIPSI + " TEXT,"
                + COLUMN_TANGGAL_DEADLINE + " TEXT,"
                + COLUMN_WAKTU_DEADLINE + " TEXT" + ")")
        db.execSQL(createTableTugas)

        val createTableJadwal = ("CREATE TABLE " + TABLE_JADWAL + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USER_ID + " INTEGER,"
                + COLUMN_NAMA_KEGIATAN + " TEXT,"
                + COLUMN_TANGGAL + " TEXT,"
                + COLUMN_WAKTU_MULAI + " TEXT,"
                + COLUMN_WAKTU_SELESAI + " TEXT" + ")")
        db.execSQL(createTableJadwal)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TUGAS)
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_JADWAL)
        onCreate(db)
    }

    fun insertTugas(userId: Int, judul: String, deskripsi: String, tanggal: String, waktu: String): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_USER_ID, userId)
        values.put(COLUMN_JUDUL, judul)
        values.put(COLUMN_DESKRIPSI, deskripsi)
        values.put(COLUMN_TANGGAL_DEADLINE, tanggal)
        values.put(COLUMN_WAKTU_DEADLINE, waktu)
        return db.insert(TABLE_TUGAS, null, values)
    }

    fun insertJadwal(userId: Int, namaKegiatan: String, tanggal: String, waktuMulai: String, waktuSelesai: String): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_USER_ID, userId)
        values.put(COLUMN_NAMA_KEGIATAN, namaKegiatan)
        values.put(COLUMN_TANGGAL, tanggal)
        values.put(COLUMN_WAKTU_MULAI, waktuMulai)
        values.put(COLUMN_WAKTU_SELESAI, waktuSelesai)
        return db.insert(TABLE_JADWAL, null, values)
    }

    fun getAllJadwal(): Cursor {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_JADWAL", null)
    }

    fun getAllTugas(): Cursor {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_TUGAS", null)
    }

    // Fungsi hapus Jadwal berdasarkan ID
    fun deleteJadwal(id: Int): Int {
        val db = this.writableDatabase
        return db.delete(TABLE_JADWAL, "$COLUMN_ID=?", arrayOf(id.toString()))
    }

    // Fungsi hapus Tugas berdasarkan ID
    fun deleteTugas(id: Int): Int {
        val db = this.writableDatabase
        return db.delete(TABLE_TUGAS, "$COLUMN_ID=?", arrayOf(id.toString()))
    }
}