package com.example.cyclerallyapp

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.example.cyclerallyapp.ui.theme.CycleRallyAppTheme
import java.io.File
import java.io.FileOutputStream

class DistributionActivity : ComponentActivity() {
    private val REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Request permissions for Android 9 and below
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            requestPermissions()
        }
        setContent {
            CycleRallyAppTheme {
                AttendanceTable()
            }
        }
    }

    private fun requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CODE_WRITE_EXTERNAL_STORAGE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
            } else {
                // Permission denied
                Toast.makeText(this, "Permission denied. Cannot export CSV.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
fun AttendanceTable() {
    val db = Firebase.firestore
    var students by remember { mutableStateOf<List<Student>>(emptyList()) }
    var sortBy by remember { mutableStateOf("name") } // Default sort by name
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Fetch student data from Firestore
    LaunchedEffect(Unit) {
        db.collection("students")
            .get()
            .addOnSuccessListener { result ->
                val studentList = result.map { document ->
                    document.toObject(Student::class.java)
                }
                students = studentList
            }
    }

    // Sort and filter students
    val sortedStudents = when (sortBy) {
        "name" -> students.sortedBy { it.name }
        "regNumber" -> students.sortedBy { it.regNumber }
        "attendance" -> students.sortedBy { it.attendance }
        else -> students
    }
    val filteredStudents = sortedStudents.filter { student ->
        student.name.contains(searchQuery, ignoreCase = true) ||
                student.regNumber.contains(searchQuery, ignoreCase = true)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Title
            Text(
                text = "View Attendance",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search by name or reg number") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp)
            )

            // Sorting Options
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FilterChip(
                    selected = sortBy == "name",
                    onClick = { sortBy = "name" },
                    label = { Text("Sort by Name") }
                )
                FilterChip(
                    selected = sortBy == "regNumber",
                    onClick = { sortBy = "regNumber" },
                    label = { Text("Sort by Reg Number") }
                )
                FilterChip(
                    selected = sortBy == "attendance",
                    onClick = { sortBy = "attendance" },
                    label = { Text("Sort by Attendance") }
                )
            }

            // Student List
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                itemsIndexed(filteredStudents) { index, student ->
                    StudentCard(
                        serialNumber = index + 1,
                        name = student.name,
                        regNumber = student.regNumber,
                        dept = student.dept,
                        year = student.year,
                        attendance = student.attendance,
                        refreshmentCollected = student.refreshmentCollected,
                        certificateCollected = student.certificateCollected,
                        medalCollected = student.medalCollected
                    )
                }
            }
        }

        // Export Button
        Button(
            onClick = {
                exportToCsv(filteredStudents, context)
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(top = 16.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = "Export to CSV", fontSize = 16.sp)
        }
    }
}

@Composable
fun StudentCard(
    serialNumber: Int,
    name: String,
    regNumber: String,
    dept: String,
    year: String,
    attendance: Boolean,
    refreshmentCollected: Boolean,
    certificateCollected: Boolean,
    medalCollected: Boolean
) {
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
            // Student Details
            Text(
                text = "Student #$serialNumber",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(text = "Name: $name", modifier = Modifier.padding(bottom = 4.dp))
            Text(text = "Reg Number: $regNumber", modifier = Modifier.padding(bottom = 4.dp))
            Text(text = "Department: $dept", modifier = Modifier.padding(bottom = 4.dp))
            Text(text = "Year: $year", modifier = Modifier.padding(bottom = 4.dp))

            // Attendance Status
            Text(
                text = "Attendance: ${if (attendance) "Present" else "Absent"}",
                color = if (attendance) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // Refreshment Status
            Text(
                text = "Refreshment: ${if (refreshmentCollected) "Collected" else "Not Collected"}",
                color = if (refreshmentCollected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // Certificate Status
            Text(
                text = "Certificate: ${if (certificateCollected) "Collected" else "Not Collected"}",
                color = if (certificateCollected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // Medal Status
            Text(
                text = "T-SHIRT: ${if (medalCollected) "Collected" else "Not Collected"}",
                color = if (medalCollected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 4.dp)
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

