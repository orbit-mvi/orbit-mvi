package com.babylon.orbit.state.generator.fixtures

data class DataClassAllOptionals(
    val name: String? = null,
    val age: Int? = null,
    val address: String,
    val email: String? = null,
    val date: Long? = null,
    val weight: Float?= null,
    val height: Double? = null,
    val exampleEnum: ExampleEnum? = null
)

enum class ExampleEnum(val id: Int) {
    FIRST(1),
    SECOND(1),
    THIRD(1),
}