package com.kelompok.jadwalku

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Service yang menerima pesan FCM dari server (Cloud Functions).
 * Daftarkan di AndroidManifest:
 *
 *   <service
 *       android:name=".MyFirebaseMessagingService"
 *       android:exported="false">
 *       <intent-filter>
 *           <action android:name="com.google.firebase.MESSAGING_EVENT" />
 *       </intent-filter>
 *   </service>
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val title   = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "Pengingat Jadwalku"
        val message = remoteMessage.notification?.body
            ?: remoteMessage.data["message"]
            ?: ""

        tampilkanNotifikasi(title, message)
    }

    /**
     * Dipanggil saat token FCM diperbarui.
     * Simpan token baru ke Firestore agar Cloud Functions bisa kirim notif.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        FcmTokenHelper.simpanToken(token)
    }

    private fun tampilkanNotifikasi(title: String, message: String) {
        buatNotificationChannel()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val notif = NotificationCompat.Builder(this, AlarmReceiver.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val notifId = System.currentTimeMillis().toInt()
        NotificationManagerCompat.from(this).notify(notifId, notif)
    }

    private fun buatNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                AlarmReceiver.CHANNEL_ID,
                "Pengingat Jadwal & Tugas",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi pengingat jadwal dan deadline tugas"
            }
            val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }
}