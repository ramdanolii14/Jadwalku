package com.kelompok.jadwalku

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Menjadwalkan ulang semua alarm setelah device reboot.
 * AlarmManager kehilangan semua alarm ketika device dimatikan, sehingga
 * receiver ini diperlukan agar notifikasi tetap berfungsi.
 *
 * Daftarkan di AndroidManifest dengan:
 *   <receiver android:name=".BootReceiver">
 *     <intent-filter>
 *       <action android:name="android.intent.action.BOOT_COMPLETED"/>
 *       <action android:name="android.intent.action.QUICKBOOT_POWERON"/>
 *     </intent-filter>
 *   </receiver>
 *
 * Dan tambahkan permission:
 *   <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED"/>
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != "android.intent.action.QUICKBOOT_POWERON") return

        // Ambil data dari Firestore dan jadwalkan ulang alarm
        rescheduleJadwal(context)
        rescheduleTugas(context)
    }

    private fun rescheduleJadwal(context: Context) {
        FirestoreHelper.getJadwal { list ->
            list.forEach { item ->
                val triggerAt = parseDateTime(item.tanggal, item.waktuMulai) - 15 * 60 * 1000L
                if (triggerAt > System.currentTimeMillis()) {
                    scheduleAlarm(
                        context    = context,
                        docId      = item.id,
                        title      = "Jadwal Segera Dimulai",
                        message    = "${item.namaKegiatan} akan dimulai 15 menit lagi.",
                        triggerAt  = triggerAt
                    )
                }
            }
        }
    }

    private fun rescheduleTugas(context: Context) {
        FirestoreHelper.getTugas { list ->
            list.forEach { item ->
                val triggerAt = parseDateTime(item.tanggalDeadline, item.waktuDeadline) - 60 * 60 * 1000L
                if (triggerAt > System.currentTimeMillis()) {
                    scheduleAlarm(
                        context   = context,
                        docId     = item.id,
                        title     = "Deadline Tugas Mendekat!",
                        message   = "Tugas '${item.judul}' deadline ${item.tanggalDeadline} pukul ${item.waktuDeadline}.",
                        triggerAt = triggerAt
                    )
                }
            }
        }
    }

    private fun scheduleAlarm(context: Context, docId: String, title: String, message: String, triggerAt: Long) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_TITLE,   title)
            putExtra(AlarmReceiver.EXTRA_MESSAGE, message)
            putExtra(AlarmReceiver.EXTRA_DOC_ID,  docId)
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0

        val pending = PendingIntent.getBroadcast(context, docId.hashCode(), intent, flags)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pending)
        }
    }

    private fun parseDateTime(tanggal: String, waktu: String): Long {
        return try {
            val sdf = SimpleDateFormat("d/M/yyyy HH:mm", Locale.getDefault())
            sdf.parse("$tanggal $waktu")?.time ?: 0L
        } catch (e: Exception) { 0L }
    }
}