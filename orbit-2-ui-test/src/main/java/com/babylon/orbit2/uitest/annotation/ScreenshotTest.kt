package com.babylon.orbit2.uitest.annotation

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ScreenshotTest(val description: String)
