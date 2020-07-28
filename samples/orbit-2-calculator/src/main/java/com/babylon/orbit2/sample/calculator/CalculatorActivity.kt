package com.babylon.orbit2.sample.calculator

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.babylon.orbit2.sample.calculator.databinding.ActivityMainBinding
import org.koin.androidx.viewmodel.ext.android.getStateViewModel

class CalculatorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main).apply {
            viewmodel = getStateViewModel()
            lifecycleOwner = this@CalculatorActivity
        }
    }
}
