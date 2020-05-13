package com.babylon.orbit.state.generator.fixtures

data class DataClassWithListProperties(
    val emails: List<String>? = null,
    val ids: List<String>?,
    val names: List<String>,
    val students: List<StudentForList>
)

data class StudentForList(
    val gender: String? = null,
    val classRoom: String
)