package org.orbitmvi.orbit.annotation

@RequiresOptIn(message = "This is an internal API designed for Orbit extensions.")
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
public annotation class OrbitInternal
