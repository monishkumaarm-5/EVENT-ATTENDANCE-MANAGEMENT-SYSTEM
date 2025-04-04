package com.example.cyclerallyapp

data class Student(
    var id: String = "",
    var name: String = "",
    var regNumber: String = "",
    var dept: String = "",  // New field
    var year: String = "",  // New field
    var attendance: Boolean = false,
    var refreshmentCollected: Boolean = false,
    var certificateCollected: Boolean = false,
    var medalCollected: Boolean = false
)