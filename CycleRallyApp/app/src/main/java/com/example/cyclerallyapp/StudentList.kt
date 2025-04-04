package com.example.cyclerallyapp

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun StudentList(students: List<Student>, onRefreshmentCollected: (Student, Boolean) -> Unit) {
    LazyColumn {
        items(students) { student ->
            StudentItem(
                student = student,
                onRefreshmentCollected = { isChecked ->
                    onRefreshmentCollected(student, isChecked)
                }
            )
        }
    }
}

@Composable
fun StudentItem(student: Student, onRefreshmentCollected: (Boolean) -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Name: ${student.name}")
        Text(text = "Reg Number: ${student.regNumber}")
        Checkbox(
            checked = student.refreshmentCollected,
            onCheckedChange = { isChecked ->
                student.refreshmentCollected = isChecked
                onRefreshmentCollected(isChecked)
            }
        )
        Text(text = "Refreshment Collected: ${student.refreshmentCollected}")
        // Add similar Checkbox and Text for Certificate and Medal
    }
}