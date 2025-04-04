package com.example.cyclerallyapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.example.cyclerallyapp.ui.theme.CycleRallyAppTheme

class StudentDetailsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CycleRallyAppTheme {
                StudentDetailsScreen()
            }
        }
    }
}

@Composable
fun StudentDetailsScreen() {
    val db = Firebase.firestore
    var students by remember { mutableStateOf<List<Student>>(emptyList()) }

    // Fetch student data from Firestore
    LaunchedEffect(Unit) {
        db.collection("students")
            .get()
            .addOnSuccessListener { result ->
                students = result.map { document ->
                    document.toObject(Student::class.java)
                }
            }
    }

    // Display student details
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(students) { student ->
            StudentItem(student = student)
        }
    }
}

@Composable
fun StudentItem(student: Student) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(text = "Name: ${student.name}")
        Text(text = "Reg Number: ${student.regNumber}")
        Text(text = "Attendance: ${student.attendance}")
        Text(text = "Refreshment Collected: ${student.refreshmentCollected}")
        Text(text = "Certificate Collected: ${student.certificateCollected}")
        Text(text = "T-SHIRT Collected: ${student.medalCollected}")
        Spacer(modifier = Modifier.height(16.dp))
    }
}