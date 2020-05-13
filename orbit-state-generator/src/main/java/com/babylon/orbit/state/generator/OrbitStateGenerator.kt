package com.babylon.orbit.state.generator

class OrbitStateGenerator {

    fun generateFixture(obj: Any): String {

        // TODO throw for Java class or find what to do there...

        val kotlinClassDescriptor = FixtureAnalyser.of(obj).analiseClass()
        println("kotlinClassDescriptor $kotlinClassDescriptor")

//        kotlinClassDescriptor.requiredProperties.forEach {
//            println("mutable propert ${it.name}")
//        }
//
//        kotlinClassDescriptor.requiredProperties.forEach {
//            println("required propert ${it.name}")
//        }


        val content = ClassGenerator.of(obj).generateClass()
        return content
    }
}