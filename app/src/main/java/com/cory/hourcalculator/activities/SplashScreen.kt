package com.cory.hourcalculator.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.cory.hourcalculator.R
import com.cory.hourcalculator.classes.AccentColor

class SplashScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawable(null)
        setContentView(R.layout.activity_splash_screen)

        load()

        val accentColor = AccentColor(this)

        val imageView = findViewById<ImageView>(R.id.SplashScreenImage)
        if (accentColor.loadAccent() == 0) {
            imageView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.hourcalculatorlogo))
        }
        else if (accentColor.loadAccent() == 1) {
            imageView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.pinklogo))
        }
        else if (accentColor.loadAccent() == 2) {
            imageView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.orange_logo))
        }
        else if (accentColor.loadAccent() == 3) {
            imageView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.red_logo))
        }
    }

    fun load() {

        val cardView: CardView = findViewById(R.id.cardView)
        val slideAnimation = AnimationUtils.loadAnimation(this, R.anim.side_slide)
        cardView.startAnimation(slideAnimation)
        val textView: TextView = findViewById(R.id.hour_calculator)
        textView.startAnimation(slideAnimation)
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            finish()
        }, 3000)
    }

    override fun onResume() {
        super.onResume()
        load()
    }
}