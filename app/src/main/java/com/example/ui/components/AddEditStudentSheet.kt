package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.Student

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditStudentSheet(
    student: Student?,
    onDismiss: () -> Unit,
    onSave: (Student) -> Unit
) {
    var name by remember { mutableStateOf(student?.name ?: "") }
    var fatherName by remember { mutableStateOf(student?.fatherName ?: "") }
    var className by remember { mutableStateOf(student?.className ?: "Class 6") }
    var section by remember { mutableStateOf(student?.section ?: "A") }
    var rollNo by remember { mutableStateOf(student?.rollNo ?: "") }
    var mobile by remember { mutableStateOf(student?.mobile ?: "") }
    var address by remember { mutableStateOf(student?.address ?: "") }
    var monthlyFeeText by remember { mutableStateOf(student?.monthlyFee?.toString() ?: "1500") }
    var photoUri by remember { mutableStateOf(student?.photoUri) }
    var selectedAvatarPreset by remember { mutableStateOf(student?.avatarPreset ?: 0) }

    var nameError by remember { mutableStateOf(false) }
    var fatherError by remember { mutableStateOf(false) }
    var mobileError by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            photoUri = uri.toString()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (student == null) "Add New Student" else "Edit Student Profile",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            // Photo Selection Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    contentAlignment = Alignment.BottomEnd,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    StudentAvatar(
                        photoUri = photoUri,
                        studentName = if (name.isNotBlank()) name else "S",
                        avatarPreset = selectedAvatarPreset,
                        size = 84.dp
                    )
                    SmallFloatingActionButton(
                        onClick = { photoPickerLauncher.launch("image/*") },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = "Pick Photo", modifier = Modifier.size(18.dp))
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(onClick = { photoPickerLauncher.launch("image/*") }) {
                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Select Photo")
                    }
                    if (!photoUri.isNull_or_blank()) {
                        TextButton(onClick = { photoUri = null }) {
                            Text("Remove Photo", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                Text(
                    text = "Or choose avatar color:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(AVATAR_COLORS) { index, color ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (selectedAvatarPreset == index && photoUri.isNull_or_blank()) 3.dp else 0.dp,
                                    color = if (selectedAvatarPreset == index && photoUri.isNull_or_blank()) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable {
                                    selectedAvatarPreset = index
                                    photoUri = null
                                }
                        )
                    }
                }
            }

            Divider()

            // Student Name
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = false
                },
                label = { Text("Student Name *") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                isError = nameError,
                supportingText = if (nameError) { { Text("Student name is required") } } else null,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Father Name
            OutlinedTextField(
                value = fatherName,
                onValueChange = {
                    fatherName = it
                    fatherError = false
                },
                label = { Text("Father's Name *") },
                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                isError = fatherError,
                supportingText = if (fatherError) { { Text("Father's name is required") } } else null,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Class and Section Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = className,
                    onValueChange = { className = it },
                    label = { Text("Class") },
                    leadingIcon = { Icon(Icons.Default.School, contentDescription = null) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = section,
                    onValueChange = { section = it },
                    label = { Text("Section") },
                    modifier = Modifier.weight(0.8f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // Roll No and Mobile Number
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = rollNo,
                    onValueChange = { rollNo = it },
                    label = { Text("Roll No") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(0.8f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = mobile,
                    onValueChange = {
                        mobile = it
                        mobileError = false
                    },
                    label = { Text("Mobile (WhatsApp) *") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    isError = mobileError,
                    supportingText = if (mobileError) { { Text("Enter a valid mobile number") } } else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.weight(1.2f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // Monthly Fee Amount
            OutlinedTextField(
                value = monthlyFeeText,
                onValueChange = { monthlyFeeText = it },
                label = { Text("Monthly Fee (₹)") },
                leadingIcon = { Icon(Icons.Default.CurrencyRupee, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Address
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Address") },
                leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                        return@Button
                    }
                    if (fatherName.isBlank()) {
                        fatherError = true
                        return@Button
                    }
                    if (mobile.isBlank() || mobile.trim().length < 8) {
                        mobileError = true
                        return@Button
                    }

                    val fee = monthlyFeeText.toDoubleOrNull() ?: 1500.0
                    val updated = Student(
                        id = student?.id ?: 0,
                        name = name.trim(),
                        fatherName = fatherName.trim(),
                        className = className.trim(),
                        section = section.trim().uppercase(),
                        rollNo = rollNo.trim(),
                        mobile = mobile.trim(),
                        address = address.trim(),
                        photoUri = photoUri,
                        avatarPreset = selectedAvatarPreset,
                        monthlyFee = fee
                    )
                    onSave(updated)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (student == null) "Save Student" else "Update Student")
            }
        }
    }
}

private fun String?.isNull_or_blank(): Boolean {
    return this.isNullOrBlank() || this == "null"
}
