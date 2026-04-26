package com.kelompok.jadwalku

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

/**
 * FirestoreHelper menggantikan DatabaseHelper (SQLite).
 * Semua operasi CRUD jadwal & tugas dilakukan ke Firestore.
 * Data diorganisir per user: users/{uid}/jadwal/{id} dan users/{uid}/tugas/{id}
 */
object FirestoreHelper {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun currentUid(): String? = auth.currentUser?.uid

    // ───────────────────────── JADWAL ─────────────────────────

    fun addJadwal(
        namaKegiatan: String,
        tanggal: String,
        waktuMulai: String,
        waktuSelesai: String,
        onResult: (success: Boolean, docId: String?) -> Unit
    ) {
        val uid = currentUid() ?: return onResult(false, null)
        val data = hashMapOf(
            "nama_kegiatan" to namaKegiatan,
            "tanggal" to tanggal,
            "waktu_mulai" to waktuMulai,
            "waktu_selesai" to waktuSelesai,
            "created_at" to System.currentTimeMillis()
        )
        db.collection("users").document(uid).collection("jadwal")
            .add(data)
            .addOnSuccessListener { ref -> onResult(true, ref.id) }
            .addOnFailureListener { onResult(false, null) }
    }

    fun getJadwal(onResult: (List<JadwalItem>) -> Unit) {
        val uid = currentUid() ?: return onResult(emptyList())
        db.collection("users").document(uid).collection("jadwal")
            .orderBy("created_at", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snap ->
                val list = snap.documents.mapNotNull { doc ->
                    JadwalItem(
                        id = doc.id,
                        namaKegiatan = doc.getString("nama_kegiatan") ?: return@mapNotNull null,
                        tanggal = doc.getString("tanggal") ?: return@mapNotNull null,
                        waktuMulai = doc.getString("waktu_mulai") ?: return@mapNotNull null,
                        waktuSelesai = doc.getString("waktu_selesai") ?: return@mapNotNull null
                    )
                }
                onResult(list)
            }
            .addOnFailureListener { onResult(emptyList()) }
    }

    fun deleteJadwal(docId: String, onResult: (Boolean) -> Unit = {}) {
        val uid = currentUid() ?: return onResult(false)
        db.collection("users").document(uid).collection("jadwal")
            .document(docId)
            .delete()
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    // ───────────────────────── TUGAS ─────────────────────────

    fun addTugas(
        judul: String,
        deskripsi: String,
        tanggalDeadline: String,
        waktuDeadline: String,
        onResult: (success: Boolean, docId: String?) -> Unit
    ) {
        val uid = currentUid() ?: return onResult(false, null)
        val data = hashMapOf(
            "judul" to judul,
            "deskripsi" to deskripsi,
            "tanggal_deadline" to tanggalDeadline,
            "waktu_deadline" to waktuDeadline,
            "created_at" to System.currentTimeMillis()
        )
        db.collection("users").document(uid).collection("tugas")
            .add(data)
            .addOnSuccessListener { ref -> onResult(true, ref.id) }
            .addOnFailureListener { onResult(false, null) }
    }

    fun getTugas(onResult: (List<TugasItem>) -> Unit) {
        val uid = currentUid() ?: return onResult(emptyList())
        db.collection("users").document(uid).collection("tugas")
            .orderBy("created_at", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snap ->
                val list = snap.documents.mapNotNull { doc ->
                    TugasItem(
                        id = doc.id,
                        judul = doc.getString("judul") ?: return@mapNotNull null,
                        deskripsi = doc.getString("deskripsi") ?: return@mapNotNull null,
                        tanggalDeadline = doc.getString("tanggal_deadline") ?: return@mapNotNull null,
                        waktuDeadline = doc.getString("waktu_deadline") ?: return@mapNotNull null
                    )
                }
                onResult(list)
            }
            .addOnFailureListener { onResult(emptyList()) }
    }

    fun deleteTugas(docId: String, onResult: (Boolean) -> Unit = {}) {
        val uid = currentUid() ?: return onResult(false)
        db.collection("users").document(uid).collection("tugas")
            .document(docId)
            .delete()
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }
}

// ───────────────────────── Data Classes ─────────────────────────

data class JadwalItem(
    val id: String,
    val namaKegiatan: String,
    val tanggal: String,
    val waktuMulai: String,
    val waktuSelesai: String
)

data class TugasItem(
    val id: String,
    val judul: String,
    val deskripsi: String,
    val tanggalDeadline: String,
    val waktuDeadline: String
)