package com.babylon.orbit.state.generator

import com.babylon.orbit.state.generator.fixtures.DataClassAllOptionals
import com.babylon.orbit.state.generator.fixtures.DataClassOptionalsWithRequiredClass
import com.babylon.orbit.state.generator.fixtures.DataClassWithListProperties
import com.babylon.orbit.state.generator.fixtures.DataSealedClassWithInnerClasses
import com.babylon.orbit.state.generator.fixtures.DataSealedClassWithoutInnerClasses
import com.babylon.orbit.state.generator.fixtures.Example
import com.babylon.orbit.state.generator.fixtures.ExampleEnum
import com.babylon.orbit.state.generator.fixtures.Other
import com.babylon.orbit.state.generator.fixtures.SealedClassWithInnerClasses
import com.babylon.orbit.state.generator.fixtures.Student
import com.babylon.orbit.state.generator.fixtures.StudentForList
import org.junit.Assert.assertEquals
import org.junit.Test

class OrbitStateGeneratorTest {

    private val orbitStateGenerator = OrbitStateGenerator()

    @Test
    fun `data class all optionals with class as property`() {
        val fixture = DataClassOptionalsWithRequiredClass(
            email = "Asdads",
            address = "London",
            date = 123L,
            weight = 65.0f,
            height = 1.65,
            student = Student("male", "aaa")
        )

        val fixtureText =
            """
                |DataClassOptionalsWithRequiredClass(
                    |address = "London",
                    |email = "Asdads",
                    |date = 123,
                    |weight = 65.0f,
                    |height = 1.65,
                    |student = Student(
                        |gender = "male",
                        |classRoom = "aaa")
                |)
            """.trimMargin()

        val result = orbitStateGenerator.generateFixture(fixture).trim()
        assertEquals(result, fixtureText)
    }

    @Test
    fun `data class all optionals without class as property`() {
        val fixture = DataClassAllOptionals(
            email = "Asdads",
            address = "London",
            date = 123L,
            weight = 65.0f,
            height = 1.65,
            exampleEnum = ExampleEnum.SECOND
        )

        val fixtureText =
            """
                |DataClassAllOptionals(
                    |address = "London",
                    |email = "Asdads",
                    |date = 123,
                    |weight = 65.0f,
                    |height = 1.65,
                    |exampleEnum = ExampleEnum.SECOND
                |)
            """.trimMargin()

        val result = orbitStateGenerator.generateFixture(fixture).trim()
        assertEquals(result, fixtureText)
    }

    @Test
    fun `data class with list properties`() {
        val fixture = DataClassWithListProperties(
            ids = listOf("id_1", "id_2", "id_3"),
            names = listOf("London", "Orbit", "Babylon"),
            students = listOf(
                StudentForList("Male", "classA"),
                StudentForList("Female", "classB")
            )
        )

        val fixtureText =
            """
                |DataClassWithListProperties(
                    |ids = listOf("id_1","id_2","id_3"),
                    |names = listOf("London","Orbit","Babylon"),
                    |students = listOf(
                        |StudentForList(
                            |gender = "Male",
                            |classRoom = "classA"),
                        |StudentForList(
                            |gender = "Female",
                            |classRoom = "classB"))
                |)
            """.trimMargin()

        val result = orbitStateGenerator.generateFixture(fixture)
        println(result)
        assertEquals(result, fixtureText)
    }

    @Test
    fun `sealed class with inner classes`() {
        val fixture = SealedClassWithInnerClasses.Example(
            a = "a",
            b = 2
        )

        val fixtureText =
            """
                |SealedClassWithInnerClasses.Example(
                    |a = "a",
                    |b = 2
                |)
            """.trimMargin()

        val result = orbitStateGenerator.generateFixture(fixture)
        println(result)
        assertEquals(result, fixtureText)
    }

    @Test
    fun `data sealed class with inner classes without object`() {
        val fixture = DataSealedClassWithInnerClasses(
            first = SealedClassWithInnerClasses.Example(
                a = "a",
                b = 2
            ),
            second = SealedClassWithInnerClasses.Example(
                a = "a",
                b = 2
            )
        )

        val fixtureText =
            """
                |DataSealedClassWithInnerClasses(
                    |first = SealedClassWithInnerClasses.Example(
                        |a = "a",
                        |b = 2),
                    |second = SealedClassWithInnerClasses.Example(
                        |a = "a",
                        |b = 2)
                |)
            """.trimMargin()

        val result = orbitStateGenerator.generateFixture(fixture)
        println(result)
        assertEquals(result, fixtureText)
    }

    @Test
    fun `data sealed class without inner classes without object`() {
        val fixture = DataSealedClassWithoutInnerClasses(
            first = Example(
                a = "a",
                b = 2
            ),
            second = Example(
                a = "a",
                b = 2
            )
        )

        val fixtureText =
            """
                |DataSealedClassWithoutInnerClasses(
                    |first = Example(
                        |a = "a",
                        |b = 2),
                    |second = Example(
                        |a = "a",
                        |b = 2)
                |)
            """.trimMargin()

        val result = orbitStateGenerator.generateFixture(fixture)
        println(result)
        assertEquals(result, fixtureText)
    }

    @Test
    fun `data sealed class with inner classes with object`() {
        val fixture = DataSealedClassWithInnerClasses(
            first = SealedClassWithInnerClasses.Example(
                a = "a",
                b = 2
            ),
            second = SealedClassWithInnerClasses.Other
        )

        val fixtureText =
            """
                |DataSealedClassWithInnerClasses(
                    |first = SealedClassWithInnerClasses.Example(
                        |a = "a",
                        |b = 2),
                    |second = SealedClassWithInnerClasses.Other
                |)
            """.trimMargin()

        val result = orbitStateGenerator.generateFixture(fixture)
        println(result)
        assertEquals(result, fixtureText)
    }

    @Test
    fun `data sealed class without inner classes with object`() {
        val fixture = DataSealedClassWithoutInnerClasses(
            first = Example(
                a = "a",
                b = 2
            ),
            second = Other
        )

        val fixtureText =
            """
                |DataSealedClassWithoutInnerClasses(
                    |first = Example(
                        |a = "a",
                        |b = 2),
                    |second = Other
                |)
            """.trimMargin()

        val result = orbitStateGenerator.generateFixture(fixture)
        println(result)
        assertEquals(result, fixtureText)
    }
}