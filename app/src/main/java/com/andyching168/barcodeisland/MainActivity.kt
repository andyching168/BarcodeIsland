package com.andyching168.barcodeisland

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import android.widget.Toast
import com.andyching168.barcodeisland.data.BarcodeType
import com.andyching168.barcodeisland.data.CardData
import com.andyching168.barcodeisland.data.CardPreferencesManager
import com.andyching168.barcodeisland.ui.components.AddCardDialog
import com.andyching168.barcodeisland.ui.components.CardItem
import com.andyching168.barcodeisland.ui.components.DeleteConfirmDialog
import com.andyching168.barcodeisland.ui.components.EditCardDialog
import com.andyching168.barcodeisland.ui.theme.BarcodeIslandTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val prefs by lazy { getSharedPreferences("barcode_prefs", Context.MODE_PRIVATE) }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            lastPendingBarcode?.let { (content, bitmap, name, color, barcodeType) ->
                showBarcodeNotification(content, bitmap, name, color, barcodeType)
            }
        }
        lastPendingBarcode = null
    }

    private var lastPendingBarcode: Quintuple<String, Bitmap, String, String, BarcodeType>? = null

    private data class Quintuple<A, B, C, D, E>(
        val first: A,
        val second: B,
        val third: C,
        val fourth: D,
        val fifth: E
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificationManager.createNotificationChannel(this)
        setContent {
            BarcodeIslandTheme {
                BarcodeScreen(
                    cardManager = CardPreferencesManager.getInstance(this),
                    savedBarcode = prefs.getString("last_barcode", "") ?: "",
                    onSaveBarcode = { content ->
                        prefs.edit().putString("last_barcode", content).apply()
                    },
                    onGenerateBarcode = { content, bitmap, name, color, barcodeType ->
                        requestNotificationPermission(content, bitmap, name, color, barcodeType)
                    },
                    onCloseAllIslands = {
                        NotificationManager.cancelAllIslands(this)
                    }
                )
            }
        }
    }

    private fun requestNotificationPermission(content: String, bitmap: Bitmap, name: String, color: String, barcodeType: BarcodeType) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            lastPendingBarcode = Quintuple(content, bitmap, name, color, barcodeType)
        } else {
            showBarcodeNotification(content, bitmap, name, color, barcodeType)
        }
    }

    private fun showBarcodeNotification(content: String, bitmap: Bitmap, name: String, color: String, barcodeType: BarcodeType) {
        NotificationManager.showBarcodeNotification(
            context = this,
            barcodeContent = content,
            barcodeBitmap = bitmap,
            cardName = name,
            colorHex = color,
            barcodeType = barcodeType
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeScreen(
    cardManager: CardPreferencesManager,
    savedBarcode: String = "",
    onSaveBarcode: (String) -> Unit = {},
    onGenerateBarcode: (String, Bitmap, String, String, BarcodeType) -> Unit = { _, _, _, _, _ -> },
    onCloseAllIslands: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val cards by cardManager.cardsFlow.collectAsState(initial = emptyList())

    var showAddDialog by remember { mutableStateOf(false) }
    var cardToEdit by remember { mutableStateOf<CardData?>(null) }
    var cardToDelete by remember { mutableStateOf<CardData?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Barcode Island") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    IconButton(onClick = onCloseAllIslands) {
                        Icon(Icons.Default.Close, contentDescription = "Close All Islands")
                    }
                }
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                if (cards.isNotEmpty()) {
                    Row(
                        modifier = Modifier.padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                scope.launch {
                                    var successCount = 0
                                    cards.forEach { card ->
                                        val bitmap = BarcodeGenerator.generate(card.barcodeContent, card.barcodeType)
                                        if (bitmap != null) {
                                            onGenerateBarcode(card.barcodeContent, bitmap, card.name, card.colorHex, card.barcodeType)
                                            successCount++
                                        }
                                    }
                                    if (successCount > 0) {
                                        Toast.makeText(context, "顯示全部卡片", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Text(" 啟用全部", modifier = Modifier.padding(start = 4.dp))
                        }
                    }
                }
                FloatingActionButton(
                    onClick = { showAddDialog = true }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Card")
                }
            }
        }
    ) { innerPadding ->
        if (cards.isEmpty()) {
            EmptyState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cards, key = { it.id }) { card ->
                    CardItemWithMenu(
                        card = card,
                        onClick = {
                            scope.launch {
                                val bitmap = BarcodeGenerator.generate(card.barcodeContent, card.barcodeType)
                                if (bitmap != null) {
                                    onGenerateBarcode(card.barcodeContent, bitmap, card.name, card.colorHex, card.barcodeType)
                                }
                            }
                        },
                        onEdit = { cardToEdit = card },
                        onDelete = { cardToDelete = card }
                    )
                }
            }
        }
    }

    val existingBarcodeContents = cards.map { it.barcodeContent }.toSet()

    if (showAddDialog) {
        AddCardDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, barcodeContent, colorHex, barcodeType ->
                val newCard = CardData(
                    name = name,
                    barcodeContent = barcodeContent,
                    colorHex = colorHex,
                    barcodeType = barcodeType
                )
                cardManager.addCard(newCard)
                showAddDialog = false
            },
            onDuplicateError = {
                Toast.makeText(context, "Barcode already exists", Toast.LENGTH_SHORT).show()
            },
            existingBarcodeContents = existingBarcodeContents
        )
    }

    cardToEdit?.let { card ->
        val contentsForEdit = existingBarcodeContents.filterNot {
            it.equals(card.barcodeContent, ignoreCase = true)
        }.toSet()
        EditCardDialog(
            card = card,
            onDismiss = { cardToEdit = null },
            onConfirm = { updatedCard ->
                cardManager.updateCard(updatedCard)
                cardToEdit = null
            },
            existingBarcodeContents = contentsForEdit
        )
    }

    cardToDelete?.let { card ->
        DeleteConfirmDialog(
            cardName = card.name,
            onDismiss = { cardToDelete = null },
            onConfirm = {
                cardManager.deleteCard(card)
                cardToDelete = null
            }
        )
    }
}

@Composable
private fun CardItemWithMenu(
    card: CardData,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Box {
        CardItem(
            card = card,
            onClick = onClick,
            onLongClick = { showMenu = true }
        )
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Edit") },
                onClick = {
                    showMenu = false
                    onEdit()
                },
                leadingIcon = {
                    Icon(Icons.Default.Edit, contentDescription = null)
                }
            )
            DropdownMenuItem(
                text = { Text("Delete") },
                onClick = {
                    showMenu = false
                    onDelete()
                },
                leadingIcon = {
                    Icon(Icons.Default.Delete, contentDescription = null)
                }
            )
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No cards yet",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tap + to add a barcode card",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
