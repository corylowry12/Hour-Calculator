package com.cory.hourcalculator.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cory.hourcalculator.R
import com.cory.hourcalculator.classes.VibrationData
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.bottom_sheet_layout.*

class BottomSheet: BottomSheetDialogFragment() {

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    companion object {
        fun newInstance() : BottomSheet = BottomSheet().apply {
            arguments = Bundle().apply {
                putInt("my_bs", 0)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetTheme)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.bottom_sheet_layout, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAnalytics = Firebase.analytics
        val vibrationData = VibrationData(requireContext())

        textView6.setOnClickListener {
            vibration(vibrationData)
            textView6.movementMethod = LinkMovementMethod.getInstance()
        }
        textView7.setOnClickListener {
            vibration(vibrationData)
            textView7.movementMethod = LinkMovementMethod.getInstance()
        }
        textView8.setOnClickListener {
            vibration(vibrationData)
            textView8.movementMethod = LinkMovementMethod.getInstance()
        }
        textView9.setOnClickListener {
            vibration(vibrationData)
            textView9.movementMethod = LinkMovementMethod.getInstance()
        }
        textView10.setOnClickListener {
            vibration(vibrationData)
            textView10.movementMethod = LinkMovementMethod.getInstance()
        }
        textView11.setOnClickListener {
            vibration(vibrationData)
            textView11.movementMethod = LinkMovementMethod.getInstance()
        }
        textView12.setOnClickListener {
            vibration(vibrationData)
            textView12.movementMethod = LinkMovementMethod.getInstance()
        }
        textView13.setOnClickListener {
            vibration(vibrationData)
            textView13.movementMethod = LinkMovementMethod.getInstance()
        }
        imageView.setOnClickListener {
            vibration(vibrationData)
            val intent = Intent(Intent.ACTION_VIEW).setData(Uri.parse(getString(R.string.tip_calculator_link)))
            this.startActivity(intent)
        }
        imageView3.setOnClickListener {
            vibration(vibrationData)
            val intent = Intent(Intent.ACTION_VIEW).setData(Uri.parse(getString(R.string.hour_calculator_link)))
            this.startActivity(intent)
        }
        imageView4.setOnClickListener {
            vibration(vibrationData)
            val intent = Intent(Intent.ACTION_VIEW).setData(Uri.parse(getString(R.string.tic_tac_toe_link)))
            this.startActivity(intent)
        }
        imageView5.setOnClickListener {
            vibration(vibrationData)
            val intent = Intent(Intent.ACTION_VIEW).setData(Uri.parse(getString(R.string.book_finder_link)))
            this.startActivity(intent)
        }
        imageView6.setOnClickListener {
            vibration(vibrationData)
            val intent = Intent(Intent.ACTION_VIEW).setData(Uri.parse(getString(R.string.bibleify_link)))
            this.startActivity(intent)
        }
        imageView7.setOnClickListener {
            vibration(vibrationData)
            val intent = Intent(Intent.ACTION_VIEW).setData(Uri.parse(getString(R.string.hour_converter_link)))
            this.startActivity(intent)
        }
        imageView8.setOnClickListener {
            vibration(vibrationData)
            val intent = Intent(Intent.ACTION_VIEW).setData(Uri.parse(getString(R.string.memorable_places_link)))
            this.startActivity(intent)
        }
        imageView9.setOnClickListener {
            vibration(vibrationData)
            val intent = Intent(Intent.ACTION_VIEW).setData(Uri.parse(getString(R.string.texarkana_college_link)))
            this.startActivity(intent)
        }
        closeImageButton.setOnClickListener {
            vibration(vibrationData)
            dismiss()
        }
    }
    fun vibration(vibrationData: VibrationData) {
        if (vibrationData.loadVibrationState()) {
            val vibrator = context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(5, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }
}