package com.andyching168.barcodeisland

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Icon
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.andyching168.barcodeisland.data.BarcodeType
import io.github.d4viddf.hyperisland_kit.HyperIslandNotification
import io.github.d4viddf.hyperisland_kit.HyperPicture
import io.github.d4viddf.hyperisland_kit.models.ImageTextInfoLeft
import io.github.d4viddf.hyperisland_kit.models.PicInfo
import io.github.d4viddf.hyperisland_kit.models.TextInfo

object NotificationManager {

    private const val CHANNEL_ID = "barcode_channel"
    private const val NOTIFICATION_ID_BASE = 1001

    fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Barcode Notifications",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "HyperIsland barcode notifications"
            setShowBadge(true)
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    fun showBarcodeNotification(
        context: Context,
        barcodeContent: String,
        barcodeBitmap: Bitmap,
        cardName: String = "Barcode",
        colorHex: String = "#2196F3",
        barcodeType: BarcodeType = BarcodeType.CODE_39
    ) {
        if (!HyperIslandNotification.isSupported(context)) {
            fallbackNotification(context, barcodeContent, barcodeBitmap, barcodeType)
            return
        }

        val barcodeKey = "barcode_image_${System.currentTimeMillis()}"

        val coloredSquare = createColoredSquareBitmap(context, colorHex)
        val squareKey = "square_${System.currentTimeMillis()}"

        val remoteView = RemoteViews(context.packageName, R.layout.notification_barcode).apply {
            setTextViewText(R.id.tv_card_info, "$cardName - $barcodeContent")
            setTextColor(R.id.tv_card_info, android.graphics.Color.WHITE)
            setTextViewText(R.id.tv_barcode_type, barcodeType.displayName)
            setTextColor(R.id.tv_barcode_type, android.graphics.Color.rgb(224, 224, 224))
            setImageViewBitmap(R.id.iv_barcode, barcodeBitmap)
            try {
                val bgColor = android.graphics.Color.parseColor(colorHex)
                setInt(R.id.container, "setBackgroundColor", bgColor)
            } catch (e: Exception) {
                setInt(R.id.container, "setBackgroundColor", android.graphics.Color.parseColor("#2196F3"))
            }
        }

        val icon = createColoredSquareIcon(context, colorHex)
        val coverPic = HyperPicture(barcodeKey, barcodeBitmap)
        val squarePic = HyperPicture(squareKey, coloredSquare)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = HyperIslandNotification.Builder(context, "barcode_${System.currentTimeMillis()}", cardName)
            .setTickerIcon(icon)
            .addPicture(coverPic)
            .addPicture(squarePic)
            .setCustomRemoteView(remoteView)
            .setSmallIsland(squareKey)
            .setBigIslandInfo(
                left = ImageTextInfoLeft(
                    type = 1,
                    picInfo = PicInfo(type = 1, pic = squareKey),
                    textInfo = TextInfo(title = cardName)
                )
            )
            .setEnableFloat(true)
            .setShowNotification(false)

        val notificationId = (NOTIFICATION_ID_BASE..NOTIFICATION_ID_BASE + 100).random()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("$cardName (${barcodeType.displayName})")
            .setContentText(barcodeContent)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addExtras(builder.buildCustomExtras())
            .build()

        context.getSystemService(NotificationManager::class.java).notify(notificationId, notification)
    }

    private fun createColoredSquareBitmap(context: Context, colorHex: String, size: Int = 128): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        val paint = android.graphics.Paint().apply {
            isAntiAlias = true
            style = android.graphics.Paint.Style.FILL
            try {
                color = android.graphics.Color.parseColor(colorHex)
            } catch (e: Exception) {
                color = android.graphics.Color.parseColor("#2196F3")
            }
        }
        canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), paint)
        return bitmap
    }

    private fun createColoredSquareIcon(context: Context, colorHex: String): Icon {
        val bitmap = createColoredSquareBitmap(context, colorHex)
        return Icon.createWithBitmap(bitmap)
    }

    fun cancelAllIslands(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        for (id in NOTIFICATION_ID_BASE..NOTIFICATION_ID_BASE + 100) {
            manager.cancel(id)
        }
    }

    private fun fallbackNotification(
        context: Context,
        barcodeContent: String,
        barcodeBitmap: Bitmap,
        barcodeType: BarcodeType = BarcodeType.CODE_39
    ) {
        val remoteViews = RemoteViews(context.packageName, R.layout.notification_barcode).apply {
            setTextViewText(R.id.tv_card_info, "Barcode - $barcodeContent")
            setTextColor(R.id.tv_card_info, android.graphics.Color.WHITE)
            setTextViewText(R.id.tv_barcode_type, barcodeType.displayName)
            setTextColor(R.id.tv_barcode_type, android.graphics.Color.rgb(224, 224, 224))
            setImageViewBitmap(R.id.iv_barcode, barcodeBitmap)
            setInt(R.id.container, "setBackgroundColor", android.graphics.Color.parseColor("#2196F3"))
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Barcode (${barcodeType.displayName})")
            .setContentText(barcodeContent)
            .setContentIntent(pendingIntent)
            .setCustomBigContentView(remoteViews)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify((NOTIFICATION_ID_BASE..NOTIFICATION_ID_BASE + 100).random(), notification)
    }
}
