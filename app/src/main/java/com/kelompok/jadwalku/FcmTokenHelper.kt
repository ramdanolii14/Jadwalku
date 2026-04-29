package com.kelompok.jadwalku

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

/**
 * Helper untuk menyimpan FCM token ke Firestore.
 * Cloud Functions membaca token ini untuk kirim notifikasi ke device yang benar.
 *
 * Struktur Firestore: users/{uid}/fcm_tokens/{token}
 */
object FcmTokenHelper {

    private val db   = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    /** Ambil token terbaru lalu simpan ke Firestore. Panggil setelah login berhasil. */
    fun ambilDanSimpanToken() {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            simpanToken(token)
        }
    }

    /** Simpan token ke Firestore. Dipanggil juga dari onNewToken saat token diperbarui. */
    fun simpanToken(token: String) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid)
            .update("fcm_token", token)
            .addOnFailureListener {
                // Jika dokumen belum ada, buat dulu
                db.collection("users").document(uid)
                    .set(mapOf("fcm_token" to token))
            }
    }
}