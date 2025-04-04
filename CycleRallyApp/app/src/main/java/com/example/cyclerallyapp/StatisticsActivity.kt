package com.example.cyclerallyapp

import android.content.ContentValues
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.example.cyclerallyapp.ui.theme.CycleRallyAppTheme
import java.io.File
import java.io.FileOutputStream

class StatisticsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CycleRallyAppTheme {
                StatisticsScreen()
            }
        }
    }
}

@Composable
fun StatisticsScreen() {
    val db = Firebase.firestore
    var totalStudents by remember { mutableStateOf(0) }
    var totalAttendance by remember { mutableStateOf(0) }
    var totalRefreshments by remember { mutableStateOf(0) }
    var totalCertificates by remember { mutableStateOf(0) }
    var totalMedals by remember { mutableStateOf(0) }

    // Fetch student data from Firestore
    LaunchedEffect(Unit) {
        db.collection("students")
            .get()
            .addOnSuccessListener { result ->
                totalStudents = result.size()
                totalAttendance = result.count { it.getBoolean("attendance") == true }
                totalRefreshments = result.count { it.getBoolean("refreshmentCollected") == true }
                totalCertificates = result.count { it.getBoolean("certificateCollected") == true }
                totalMedals = result.count { it.getBoolean("medalCollected") == true }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Title
        Text(
            text = "Statistics",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Total Students Card
        StatisticCard(title = "Total Students", value = totalStudents.toString())

        // Total Attendance Card
        StatisticCard(title = "Total Attendance", value = totalAttendance.toString())

        // Total Refreshments Collected Card
        StatisticCard(title = "Refreshments Collected", value = totalRefreshments.toString())

        // Total Certificates Collected Card
        StatisticCard(title = "Certificates Collected", value = totalCertificates.toString())

        // Total Medals Collected Card
        StatisticCard(title = "T-SHIRT Collected", value = totalMedals.toString())
    }
}

@Composable
fun StatisticCard(title: String, value: String) {
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
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

        }
    }
}

// Function to export data to CSV
private fun exportToCsv(students: List<Student>, context: android.content.Context) {
    val csvHeader = "S.No,Name,Reg Number,Department,Year,Attendance,Refreshment Collected,Certificate Collected,T-SHIRT Collected\n"
    val csvRows = students.joinToString("\n") { student ->
        "${students.indexOf(student) + 1},${student.name},${student.regNumber},${student.dept},${student.year},${if (student.attendance) "Present" else "Absent"},${if (student.refreshmentCollected) "Yes" else "No"},${if (student.certificateCollected) "Yes" else "No"},${if (student.medalCollected) "Yes" else "No"}"
    }
    val csvContent = csvHeader + csvRows

    // File name
    val fileName = "attendance_report.csv"

    // Save the CSV file to the Downloads folder
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        // For Android 10 (API 29) and above
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "text/csv")
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        uri?.let {
            resolver.openOutputStream(it)?.use { outputStream ->
                outputStream.write(csvContent.toByteArray())
            }
        }
    } else {
        // For Android 9 (API 28) and below
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, fileName)
        FileOutputStream(file).use { outputStream ->
            outputStream.write(csvContent.toByteArray())
        }
    }

    // Notify the user
    Toast.makeText(context, "Exported to $fileName in Downloads", Toast.LENGTH_SHORT).show()
}
