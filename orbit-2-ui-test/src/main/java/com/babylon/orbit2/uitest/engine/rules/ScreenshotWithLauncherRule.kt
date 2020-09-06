package com.babylon.orbit2.uitest.engine.rules

import android.os.Bundle
import androidx.test.core.app.ApplicationProvider
import androidx.test.rule.ActivityTestRule
import com.babylon.screen.launcher.contract.MockConsumer
import com.babylon.screen.launcher.screenlist.ui.ScreenLauncherActivity
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import java.io.Serializable
import kotlin.reflect.KClass

/**
 * Rule which can be used to launch a screen with a particular state.
 * Takes a screenshot after a test completes successfully.
 */
class ScreenshotWithLauncherRule<STATE : Any>(private val screenClass: KClass<out MockConsumer<STATE>>) : TestRule {
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
