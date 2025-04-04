package com.example.cyclerallyapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.example.cyclerallyapp.ui.theme.CycleRallyAppTheme

class ScannerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CycleRallyAppTheme {
                ScannerScreen()
            }
        }
    }
}

@Composable
fun ScannerScreen() {
    val context = LocalContext.current
    val db = Firebase.firestore
    var registerNumber by remember { mutableStateOf("") }
    var resultMessage by remember { mutableStateOf("") }

    // State for student details
    var student by remember { mutableStateOf<Student?>(null) }

    // State for toggle buttons
    var attendance by remember { mutableStateOf(false) }
    var refreshmentCollected by remember { mutableStateOf(false) }
    var certificateCollected by remember { mutableStateOf(false) }
    var medalCollected by remember { mutableStateOf(false) }

    // Initialize the barcode scanner
    val options = GmsBarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
        .build()
    val scanner = GmsBarcodeScanning.getClient(context, options)

    // Make the screen scrollable
    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // Add vertical scrolling
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Title
            Text(
                text = "Start Check-In",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Manual Entry Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Manual Entry",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = registerNumber,
                        onValueChange = { registerNumber = it },
                        label = { Text("Enter Register Number") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Button(
                        onClick = {
                            if (registerNumber.isNotEmpty()) {
                                fetchStudentData(registerNumber) { fetchedStudent ->
                                    if (fetchedStudent != null) {
                                        student = fetchedStudent
                                        attendance = fetchedStudent.attendance
                                        refreshmentCollected = fetchedStudent.refreshmentCollected
                                        certificateCollected = fetchedStudent.certificateCollected
                                        medalCollected = fetchedStudent.medalCollected
                                        resultMessage = "Student details loaded: ${fetchedStudent.name}"
                                    } else {
                                        resultMessage = "Student not found!"
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "Submit", fontSize = 16.sp)
                    }
                }
            }

            // Scan Barcode Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Scan Barcode",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Button(
                        onClick = {
                            scanner.startScan()
                                .addOnSuccessListener { barcode ->
                                    val scannedValue = barcode.rawValue ?: ""
                                    registerNumber = scannedValue
                                    fetchStudentData(scannedValue) { fetchedStudent ->
                                        if (fetchedStudent != null) {
                                            student = fetchedStudent
                                            attendance = fetchedStudent.attendance
                                            refreshmentCollected = fetchedStudent.refreshmentCollected
                                            certificateCollected = fetchedStudent.certificateCollected
                                            medalCollected = fetchedStudent.medalCollected
                                            resultMessage = "Student details loaded: ${fetchedStudent.name}"
                                        } else {
                                            resultMessage = "Student not found!"
                                        }
                                    }
                                }
                                .addOnFailureListener { e ->
                                    resultMessage = "Scan failed: ${e.message}"
                                }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "Scan", fontSize = 16.sp)
                    }
                }
            }

            // Display Student Details
            if (student != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Student Details",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(text = "Name: ${student!!.name}", modifier = Modifier.padding(bottom = 4.dp))
                        Text(text = "Reg Number: ${student!!.regNumber}", modifier = Modifier.padding(bottom = 4.dp))
                        Text(text = "Department: ${student!!.dept}", modifier = Modifier.padding(bottom = 4.dp))  // New field
                        Text(text = "Year: ${student!!.year}", modifier = Modifier.padding(bottom = 16.dp))  // New field

                        // Toggle Buttons
                        ToggleButton(
                            label = "Attendance",
                            checked = attendance,
                            onCheckedChange = { isChecked ->
                                attendance = isChecked
                                updateStudentStatus(student!!.id, "attendance", isChecked)
                            }
                        )
                        ToggleButton(
                            label = "Refreshment Collected",
                            checked = refreshmentCollected,
                            onCheckedChange = { isChecked ->
                                refreshmentCollected = isChecked
                                updateStudentStatus(student!!.id, "refreshmentCollected", isChecked)
                            }
                        )
                        ToggleButton(
                            label = "Certificate Collected",
                            checked = certificateCollected,
                            onCheckedChange = { isChecked ->
                                certificateCollected = isChecked
                                updateStudentStatus(student!!.id, "certificateCollected", isChecked)
                            }
                        )
                        ToggleButton(
                            label = "T-SHIRT Collected",
                            checked = medalCollected,
                            onCheckedChange = { isChecked ->
                                medalCollected = isChecked
                                updateStudentStatus(student!!.id, "T-SHIRT Collected", isChecked)
                            }
                        )
                    }
                }
            }

            // Result Message
            if (resultMessage.isNotEmpty()) {
                Text(
                    text = resultMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}

@Composable
fun ToggleButton(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Text(text = label, modifier = Modifier.padding(start = 8.dp))
    }
}

// Fetch student data from Firestore
private fun fetchStudentData(regNumber: String, onResult: (Student?) -> Unit) {
    val db = Firebase.firestore
    db.collection("students")
        .whereEqualTo("regNumber", regNumber)
        .get()
        .addOnSuccessListener { result ->
            if (!result.isEmpty) {
                val student = result.documents[0].toObject(Student::class.java)
                onResult(student)
            } else {
                onResult(null)
            }
        }
        .addOnFailureListener { e ->
            onResult(null)
        }
}

// Update student status in Firestore
private fun updateStudentStatus(studentId: String, field: String, value: Boolean) {
    val db = Firebase.firestore
    db.collection("students")
        .document(studentId)
        .update(field, value)
        .addOnSuccessListener {
            // Successfully updated
        }
        .addOnFailureListener {
            // Handle error
        }
}