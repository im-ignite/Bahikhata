package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_SYNC = "channel_sync_google_drive"
        const val CHANNEL_INVENTORY = "channel_inventory_alerts"
        const val CHANNEL_DAILY = "channel_daily_reminders"
        private const val NOTIF_SYNC_ID = 1001
        private const val NOTIF_INVENTORY_ID = 1002
        private const val NOTIF_DAILY_ID = 1003
        private const val NOTIF_SALE_ID = 1004
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val syncChannel = NotificationChannel(
                CHANNEL_SYNC,
                "Cloud & Google Drive Sync",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications when real-time data syncs with Google Drive or Cloud"
            }

            val inventoryChannel = NotificationChannel(
                CHANNEL_INVENTORY,
                "Inventory & Low Stock Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when product stock weight or pieces drop below threshold"
            }

            val dailyChannel = NotificationChannel(
                CHANNEL_DAILY,
                "Daily Trade Logging Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders to log daily piece and weight counts"
            }

            notificationManager.createNotificationChannels(
                listOf(syncChannel, inventoryChannel, dailyChannel)
            )
        }
    }

    private fun getPendingIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        return PendingIntent.getActivity(context, 0, intent, flags)
    }

    fun sendSyncNotification(title: String, message: String) {
        try {
            val builder = NotificationCompat.Builder(context, CHANNEL_SYNC)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(getPendingIntent())
                .setAutoCancel(true)

            NotificationManagerCompat.from(context).notify(NOTIF_SYNC_ID, builder.build())
        } catch (_: SecurityException) {
            // Handled when notification permission is not yet granted
        }
    }

    fun sendLowStockNotification(itemName: String, currentWeight: Double, currentPieces: Int) {
        try {
            val builder = NotificationCompat.Builder(context, CHANNEL_INVENTORY)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Low Inventory Warning: $itemName")
                .setContentText("Remaining: ${String.format("%.1f", currentWeight)} kg ($currentPieces pcs). Consider restocking.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(getPendingIntent())
                .setAutoCancel(true)

            NotificationManagerCompat.from(context).notify(NOTIF_INVENTORY_ID, builder.build())
        } catch (_: SecurityException) {
        }
    }

    fun sendSaleRecordedNotification(customerName: String, amount: Double, weight: Double) {
        try {
            val builder = NotificationCompat.Builder(context, CHANNEL_SYNC)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("New Sale Recorded")
                .setContentText("Customer: $customerName • ${String.format("%.1f", weight)} kg • $${String.format("%.2f", amount)}")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(getPendingIntent())
                .setAutoCancel(true)

            NotificationManagerCompat.from(context).notify(NOTIF_SALE_ID, builder.build())
        } catch (_: SecurityException) {
        }
    }
}
