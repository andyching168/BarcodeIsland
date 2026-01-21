package com.andyching168.barcodeisland.data

import androidx.compose.runtime.Stable
import org.json.JSONArray
import org.json.JSONObject

enum class BarcodeType(val displayName: String, val description: String) {
    CODE_39("Code-39", "財政部發票載具"),
    CODE_128("Code-128", "7-11,全家會員...");
    
    companion object {
        fun fromString(value: String): BarcodeType {
            return entries.find { it.name == value } ?: CODE_39
        }
    }
}

@Stable
data class CardData(
    val id: Long = System.currentTimeMillis(),
    val name: String,
    val barcodeContent: String,
    val colorHex: String,
    val barcodeType: BarcodeType = BarcodeType.CODE_39,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("name", name)
            put("barcodeContent", barcodeContent)
            put("colorHex", colorHex)
            put("barcodeType", barcodeType.name)
            put("createdAt", createdAt)
        }
    }

    companion object {
        fun fromJson(json: JSONObject): CardData {
            return CardData(
                id = json.optLong("id", System.currentTimeMillis()),
                name = json.optString("name", ""),
                barcodeContent = json.optString("barcodeContent", ""),
                colorHex = json.optString("colorHex", "#2196F3"),
                barcodeType = BarcodeType.fromString(json.optString("barcodeType", "CODE_39")),
                createdAt = json.optLong("createdAt", System.currentTimeMillis())
            )
        }
    }
}
