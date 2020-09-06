package com.babylon.orbit2.uitest.engine.rules

/**
 * Rule which can be used to launch a screen with a particular state.
 * Takes a screenshot after a test completes successfully.
 */
/*class ScreenshotWithLauncherRule<STATE : Any>(private val screenClass: KClass<out MockConsumer<STATE>>) : TestRule {
    private val activityTestRule = ActivityTestRule(ScreenLauncherActivity::class.java, false, false)
    private val delegate = RuleChain.outerRule(activityTestRule).around(ScreenshotRule(screenClass))

    override fun apply(base: Statement, description: Description): Statement {
        return delegate.apply(base, description)
    }

    fun launch(state: STATE, navigationAction: Serializable? = null) {
        val intent = ScreenLauncherActivity.intent(
            context = ApplicationProvider.getApplicationContext(),
            screenClass = screenClass.java,
            state = state,
            extras = navigationAction?.let {
                Bundle().apply {
                    putSerializable("com.babylon.v2.base.navigation.NavigationAction.EXTRAS_KEY", it)
                }
            }
        )
        activityTestRule.launchActivity(intent)
    }
}
*/
