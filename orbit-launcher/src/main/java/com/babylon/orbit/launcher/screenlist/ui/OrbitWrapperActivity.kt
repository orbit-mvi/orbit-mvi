package com.babylon.orbit.launcher.screenlist.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.babylon.orbit.launcher.R
import com.babylon.orbit.launcher.view.OrbitFragment
import com.babylon.orbit.launcher.view.OrbitView

internal class OrbitWrapperActivity : AppCompatActivity() {

    @Suppress("UNCHECKED_CAST")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.orbit_wrapper_activity)

        val viewClazz = intent.extras?.getSerializable(VIEW_CLASS) as Class<out OrbitView<*>>
        val fragment = OrbitFragment.createAsMockConsumer(viewClazz)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.content, fragment)
            .commit()
    }

    companion object {

        internal fun start(
            context: Context,
            viewClass: Class<out OrbitView<*>>
        ) = context.startActivity(
            Intent(context, OrbitWrapperActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra(VIEW_CLASS, viewClass)
        )

        private const val VIEW_CLASS =
            "com.babylon.orbit.launcher.OrbitWrapperActivity_VIEW_CLAZZ"
    }
}
