package com.cory.hourcalculator.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.cory.hourcalculator.R
import com.cory.hourcalculator.classes.PerformanceModeData

class SplashScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawable(null)
        setContentView(R.layout.activity_splash_screen)

        load()
    }

    fun load() {
        if(!PerformanceModeData(this).loadPerformanceMode()) {
            val cardView: CardView = findViewById(R.id.cardView)
            val slideAnimation = AnimationUtils.loadAnimation(this, R.anim.side_slide)
            cardView.startAnimation(slideAnimation)
            val textView: TextView = findViewById(R.id.hour_calculator)
            textView.startAnimation(slideAnimation)
        }

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            if(!PerformanceModeData(this).loadPerformanceMode()) {
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            }
            else {
                overridePendingTransition(R.anim.no_animation, R.anim.no_animation)
            }
            finish()
        }, 3000)
    }

    override fun onResume() {
        super.onResume()
        load()
    }
}