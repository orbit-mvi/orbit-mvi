package com.babylon.orbit.state.generator.fixtures

sealed class SealedClassWithInnerClasses {

    data class Example(val a: String, val b: Int) : SealedClassWithInnerClasses()

    object Other : SealedClassWithInnerClasses()

}

data class DataSealedClassWithInnerClasses(
    val first: SealedClassWithInnerClasses,
    val second: SealedClassWithInnerClasses
)