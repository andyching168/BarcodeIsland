package com.andyching168.barcodeisland.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.andyching168.barcodeisland.data.BarcodeType
import com.andyching168.barcodeisland.data.CardData

@Composable
fun AddCardDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, barcodeContent: String, colorHex: String, barcodeType: BarcodeType) -> Unit,
    onDuplicateError: (() -> Unit)? = null,
    existingBarcodeContents: Set<String> = emptySet()
) {
    var name by remember { mutableStateOf("") }
    var barcodeContent by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(PRESET_COLORS[0]) }
    var selectedBarcodeType by remember { mutableStateOf(BarcodeType.CODE_39) }
    var barcodeError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Card") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = barcodeContent,
                    onValueChange = { newValue ->
                        val filtered = newValue.filter { char ->
                            char.isLetterOrDigit() || char in "@#$%^&*()_+-=[]{};':\"\\|,.<>/?"
                        }
                        barcodeContent = filtered
                        barcodeError = false
                    },
                    label = { Text("Barcode Content") },
                    placeholder = { Text("A-Z, 0-9, symbols only") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        keyboardType = KeyboardType.Ascii
                    ),
                    isError = barcodeError,
                    supportingText = if (barcodeError) {
                        { Text("Barcode already exists") }
                    } else null
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Barcode Type",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Column {
                    BarcodeType.entries.forEach { type ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedBarcodeType = type }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedBarcodeType == type,
                                onClick = { selectedBarcodeType = type }
                            )
                            Column(modifier = Modifier.padding(start = 8.dp)) {
                                Text(
                                    text = type.displayName,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = type.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Color",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                ColorPicker(
                    selectedColor = selectedColor,
                    onColorSelected = { selectedColor = it }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        existingBarcodeContents.any { it.equals(barcodeContent, ignoreCase = true) } -> {
                            barcodeError = true
                            onDuplicateError?.invoke()
                        }
                        name.isNotBlank() && barcodeContent.isNotBlank() -> {
                            onConfirm(name, barcodeContent, selectedColor, selectedBarcodeType)
                        }
                    }
                },
                enabled = name.isNotBlank() && barcodeContent.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditCardDialog(
    card: CardData,
    onDismiss: () -> Unit,
    onConfirm: (CardData) -> Unit,
    existingBarcodeContents: Set<String> = emptySet()
) {
    var name by remember { mutableStateOf(card.name) }
    var barcodeContent by remember { mutableStateOf(card.barcodeContent) }
    var selectedColor by remember { mutableStateOf(card.colorHex) }
    var selectedBarcodeType by remember { mutableStateOf(card.barcodeType) }
    var barcodeError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Card") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = barcodeContent,
                    onValueChange = { newValue ->
                        val filtered = newValue.filter { char ->
                            char.isLetterOrDigit() || char in "@#$%^&*()_+-=[]{};':\"\\|,.<>/?"
                        }
                        barcodeContent = filtered
                        barcodeError = false
                    },
                    label = { Text("Barcode Content") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        keyboardType = KeyboardType.Ascii
                    ),
                    isError = barcodeError,
                    supportingText = if (barcodeError) {
                        { Text("Barcode already exists") }
                    } else null
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Barcode Type",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Column {
                    BarcodeType.entries.forEach { type ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedBarcodeType = type }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedBarcodeType == type,
                                onClick = { selectedBarcodeType = type }
                            )
                            Column(modifier = Modifier.padding(start = 8.dp)) {
                                Text(
                                    text = type.displayName,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = type.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Color",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                ColorPicker(
                    selectedColor = selectedColor,
                    onColorSelected = { selectedColor = it }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val filteredContents = existingBarcodeContents.filterNot { 
                        it.equals(card.barcodeContent, ignoreCase = true) 
                    }.toSet()
                    
                    when {
                        filteredContents.any { it.equals(barcodeContent, ignoreCase = true) } -> {
                            barcodeError = true
                        }
                        name.isNotBlank() && barcodeContent.isNotBlank() -> {
                            onConfirm(
                                card.copy(
                                    name = name,
                                    barcodeContent = barcodeContent,
                                    colorHex = selectedColor,
                                    barcodeType = selectedBarcodeType
                                )
                            )
                        }
                    }
                },
                enabled = name.isNotBlank() && barcodeContent.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DeleteConfirmDialog(
    cardName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Card") },
        text = {
            Text("Are you sure you want to delete \"$cardName\"? This action cannot be undone.")
        },
        confirmButton = {
            Button(
                onClick = onConfirm
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
