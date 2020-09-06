package com.babylon.orbit2.uitest.plugin.modifier

import android.view.View
import androidx.test.espresso.IdlingRegistry
import com.airbnb.lottie.LottieAnimationView
import com.babylon.orbit2.uitest.engine.idling.LottieIdlingAnimationResource
import com.babylon.orbit2.uitest.plugin.captor.interaction.hierarchySequence

internal class IdlingResourceRegistrator : ViewModifier {
    override fun modify(view: View) {
        view.hierarchySequence()
            .filterIsInstance<LottieAnimationView>()
            .forEachIndexed { index, lottieView ->
                val idlingResource = LottieIdlingAnimationResource(lottieView, "Lottie Animation $index")
                IdlingRegistry.getInstance().register(idlingResource)
            }
    }
}
