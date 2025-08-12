package com.androidvip.sysctlgui.ui.start

import android.content.Intent
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.androidvip.sysctlgui.R
import com.androidvip.sysctlgui.databinding.ActivityStartErrorBinding

class StartErrorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStartErrorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartErrorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val avd = ContextCompat.getDrawable(this, R.drawable.avd_no_root)
        if (avd is AnimatedVectorDrawable) {
            val handler = Handler(mainLooper)
            val runnable = object : Runnable {
                override fun run() {
                    if (!isFinishing) {
                        avd.start()
                    }
                    handler.postDelayed(this, DELAY)
                }
            }
            handler.postDelayed(runnable, DELAY)
            binding.animation.setImageDrawable(avd)
            avd.start()
        }

        binding.tryAgain.setOnClickListener {
            startActivity(Intent(this, StartActivity::class.java))
            finish()
        }
    }

    companion object {
        private const val DELAY = 3000L
    }
}
