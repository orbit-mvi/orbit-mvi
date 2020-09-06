package com.babylon.orbit2.uitest.plugin.modifier.resize

import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.core.view.children
import androidx.test.platform.app.InstrumentationRegistry
import com.babylon.orbit2.uitest.plugin.modifier.ViewModifier
import java.util.concurrent.CountDownLatch

/**
 * This class is resizing an existing screen that the content is bigger than the
 * viewport, in order to be able to capture the content of the full screen.
 */
internal class ViewResizer<T : ViewGroup>(
    private val scrollingViewFinder: ScrollingViewFinder<T>
) : ViewModifier {

    override fun modify(view: View) {
        if (view !is ViewGroup) return

        val scrollingView = scrollingViewFinder.findScrollingView(view)
            ?: return

        view.waitForMeasure {
            val originalScrollingViewHeight = scrollingView.height
            val remainingElementsHeight = view.height - originalScrollingViewHeight
            val originalRootViewHeight = view.height
            val rootViewGroupNewHeight = view.height * ROOT_HEIGHT_MULTIPLIER
            resizeView(view, rootViewGroupNewHeight)

            val contentHeight = calculateScrollingViewContent(scrollingView)
            val actualScreenSize = contentHeight + remainingElementsHeight

            if (contentHeight == originalScrollingViewHeight) {
                resizeView(view, originalRootViewHeight)
            } else {
                if (actualScreenSize <= 0) {
                    throw IllegalArgumentException("something when wrong here....")
                }

                resizeView(view, actualScreenSize)
            }
        }
    }

    private fun calculateScrollingViewContent(scrollingView: T): Int {
        return scrollingView.children.sumBy {
            it.height + extraViewMarginHeight(it)
        }
    }

    private fun extraViewMarginHeight(view: View): Int = when (val layoutParams = view.layoutParams) {
        is ViewGroup.MarginLayoutParams -> layoutParams.topMargin + layoutParams.bottomMargin
        else -> 0
    }

    private fun resizeView(view: View, newHeight: Int) {
        runOnMainSync {
            view.updateHeight(newHeight)
        }
        waitForUiToUpdate(view, newHeight)
    }

    private fun waitForUiToUpdate(viewGroup: View, expectedHeight: Int) {
        val countDownLatch = CountDownLatch(1)

        viewGroup.waitForMeasure(expectedHeight) {
            countDownLatch.countDown()
        }

        countDownLatch.await()
    }

    private companion object {
        const val ROOT_HEIGHT_MULTIPLIER = 4
    }
}

private fun View.updateHeight(height: Int) {
    val originalLayoutParams = layoutParams
    originalLayoutParams.height = height
    layoutParams = originalLayoutParams
}

private fun View.waitForMeasure(expectedHeight: Int = 0, cb: () -> Unit) {
    if (height > 0 && height >= expectedHeight && width > 0) {
        cb()
    } else {
        viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                if (height > 0 && height >= expectedHeight && width > 0) {
                    viewTreeObserver.removeOnPreDrawListener(this)
                    cb()
                }

                return true
            }
        })
    }
}

private fun runOnMainSync(action: () -> Unit) {
    if (Looper.myLooper() == Looper.getMainLooper()) {
        action()
    } else {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(action)
    }
}
