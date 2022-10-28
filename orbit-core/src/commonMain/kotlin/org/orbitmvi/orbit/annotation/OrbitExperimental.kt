package org.orbitmvi.orbit.annotation

@RequiresOptIn(message = "This in an experimental API and may be subject to change.", level = RequiresOptIn.Level.WARNING)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
public annotation class OrbitExperimental
