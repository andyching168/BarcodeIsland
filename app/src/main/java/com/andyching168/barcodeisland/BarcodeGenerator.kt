package com.andyching168.barcodeisland

import android.graphics.Bitmap
import com.andyching168.barcodeisland.data.BarcodeType
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object BarcodeGenerator {

    private const val DEFAULT_WIDTH = 400
    private const val DEFAULT_HEIGHT = 120

    suspend fun generate(
        content: String,
        barcodeType: BarcodeType,
        width: Int = DEFAULT_WIDTH,
        height: Int = DEFAULT_HEIGHT
    ): Bitmap? = withContext(Dispatchers.Default) {
        try {
            val format = when (barcodeType) {
                BarcodeType.CODE_39 -> BarcodeFormat.CODE_39
                BarcodeType.CODE_128 -> BarcodeFormat.CODE_128
            }
            val result = MultiFormatWriter().encode(
                content,
                format,
                width,
                height
            )
            createBitmap(result)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun generateCode39(
        content: String,
        width: Int = DEFAULT_WIDTH,
        height: Int = DEFAULT_HEIGHT
    ): Bitmap? = generate(content, BarcodeType.CODE_39, width, height)

    private fun createBitmap(matrix: BitMatrix): Bitmap {
        val width = matrix.width
        val height = matrix.height
        val pixels = IntArray(width * height)

        for (y in 0 until height) {
            val offset = y * width
            for (x in 0 until width) {
                pixels[offset + x] = if (matrix[x, y]) {
                    android.graphics.Color.BLACK
                } else {
                    android.graphics.Color.WHITE
                }
            }
        }

        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
            setPixels(pixels, 0, width, 0, 0, width, height)
        }
    }
}
