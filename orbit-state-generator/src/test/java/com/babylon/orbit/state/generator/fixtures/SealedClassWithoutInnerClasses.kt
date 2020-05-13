package com.babylon.orbit.state.generator.fixtures

sealed class SealedClassWithoutInnerClasses

data class Example(val a: String, val b: Int) : SealedClassWithoutInnerClasses()

object Other : SealedClassWithoutInnerClasses()

data class DataSealedClassWithoutInnerClasses(
    val first: SealedClassWithoutInnerClasses,
    val second: SealedClassWithoutInnerClasses
)