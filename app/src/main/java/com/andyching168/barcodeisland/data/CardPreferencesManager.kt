package com.andyching168.barcodeisland.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class CardPreferencesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )

    private val _cardsFlow = MutableStateFlow<List<CardData>>(emptyList())
    val cardsFlow: Flow<List<CardData>> = _cardsFlow.asStateFlow()

    init {
        _cardsFlow.value = loadCards()
    }

    private fun loadCards(): List<CardData> {
        val jsonString = prefs.getString(KEY_CARDS, null) ?: return emptyList()
        return try {
            val jsonArray = JSONArray(jsonString)
            (0 until jsonArray.length()).map { i ->
                CardData.fromJson(jsonArray.getJSONObject(i))
            }.sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveCards(cards: List<CardData>) {
        val jsonArray = JSONArray()
        cards.forEach { jsonArray.put(it.toJson()) }
        prefs.edit().putString(KEY_CARDS, jsonArray.toString()).apply()
        _cardsFlow.value = ArrayList(cards.sortedByDescending { it.createdAt })
    }

    fun getAllCards(): List<CardData> = _cardsFlow.value

    fun getCardById(id: Long): CardData? {
        return _cardsFlow.value.find { it.id == id }
    }

    fun isDuplicateBarcode(barcodeContent: String, excludeId: Long? = null): Boolean {
        return _cardsFlow.value.any { card ->
            card.barcodeContent.equals(barcodeContent, ignoreCase = true) &&
            (excludeId == null || card.id != excludeId)
        }
    }

    fun isDuplicateName(name: String, excludeId: Long? = null): Boolean {
        return _cardsFlow.value.any { card ->
            card.name.equals(name, ignoreCase = true) &&
            (excludeId == null || card.id != excludeId)
        }
    }

    fun addCard(card: CardData): Long? {
        if (isDuplicateBarcode(card.barcodeContent)) {
            return null
        }
        val cards = _cardsFlow.value.toMutableList()
        cards.add(0, card)
        saveCards(cards)
        return card.id
    }

    fun updateCard(card: CardData) {
        val currentCards = _cardsFlow.value
        val cards = currentCards.toMutableList()
        val index = cards.indexOfFirst { it.id == card.id }
        if (index >= 0) {
            cards[index] = card
            saveCards(cards)
        }
    }

    fun deleteCard(card: CardData) {
        val cards = _cardsFlow.value.filter { it.id != card.id }
        saveCards(cards)
    }

    fun deleteCardById(id: Long) {
        val cards = _cardsFlow.value.filter { it.id != id }
        saveCards(cards)
    }

    companion object {
        private const val PREFS_NAME = "barcode_cards_prefs"
        private const val KEY_CARDS = "cards_json"

        @Volatile
        private var INSTANCE: CardPreferencesManager? = null

        fun getInstance(context: Context): CardPreferencesManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: CardPreferencesManager(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }
}
