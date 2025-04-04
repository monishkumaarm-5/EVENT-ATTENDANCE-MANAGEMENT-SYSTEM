package com.example.cyclerallyapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.example.cyclerallyapp.ui.theme.CycleRallyAppTheme

class AttendanceActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CycleRallyAppTheme {
                AttendanceScreen()
            }
        }
    }
}

@Composable
fun AttendanceScreen() {
    val context = LocalContext.current
    val db = Firebase.firestore
    var registerNumber by remember { mutableStateOf("") }
    var resultMessage by remember { mutableStateOf("") }

    // Initialize the barcode scanner
    val options = GmsBarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
        .build()
    val scanner = GmsBarcodeScanning.getClient(context, options)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Manual Entry Field
        TextField(
            value = registerNumber,
            onValueChange = { registerNumber = it },
            label = { Text("Enter Register Number") },
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Manual Entry Button
        Button(
            onClick = {
                if (registerNumber.isNotEmpty()) {
                    fetchStudentData(registerNumber) { student ->
                        if (student != null) {
                            markAttendance(student)
                            resultMessage = "Attendance marked for ${student.name}"
                        } else {
                            resultMessage = "Student not found!"
                        }
                    }
                }
            },
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text(text = "Submit Manual Entry")
        }

        // Scan Barcode Button
        Button(
            onClick = {
                scanner.startScan()
                    .addOnSuccessListener { barcode ->
                        val scannedValue = barcode.rawValue ?: ""
                        registerNumber = scannedValue
                        fetchStudentData(scannedValue) { student ->
                            if (student != null) {
                                markAttendance(student)
                                resultMessage = "Attendance marked for ${student.name}"
                            } else {
                                resultMessage = "Student not found!"
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        resultMessage = "Scan failed: ${e.message}"
                    }
            },
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text(text = "Scan Barcode")
        }

        // Result Message
        Text(text = resultMessage)
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

// Mark attendance
private fun markAttendance(student: Student) {
    val db = Firebase.firestore
    db.collection("students")
        .document(student.id)
        .update("attendance", true)
        .addOnSuccessListener {
            // Successfully updated
        }
        .addOnFailureListener {
            // Handle error
        }
}