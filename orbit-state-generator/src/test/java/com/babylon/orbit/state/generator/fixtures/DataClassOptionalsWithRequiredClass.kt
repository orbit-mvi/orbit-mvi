package com.babylon.orbit.state.generator.fixtures

data class DataClassOptionalsWithRequiredClass(
    val name: String? = null,
    val age: Int? = null,
    val address: String,
    val email: String? = null,
    val date: Long,
    val weight: Float?,
    val height: Double?,
    val student: Student
)

data class Student(
    val gender: String? = null,
    val classRoom: String
)