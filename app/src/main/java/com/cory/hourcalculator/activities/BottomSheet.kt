package com.cory.hourcalculator.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cory.hourcalculator.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
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

        textView6.setOnClickListener {
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_ID, "tip_calculator_link")
                param(FirebaseAnalytics.Param.ITEM_NAME, "tip_calculator_link_clicked")
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "text_view")
            }
            textView6.movementMethod = LinkMovementMethod.getInstance()
        }
        textView7.setOnClickListener {
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_ID, "hour_calculator_link")
                param(FirebaseAnalytics.Param.ITEM_NAME, "hour_calculator_link_clicked")
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "text_view")
            }
            textView7.movementMethod = LinkMovementMethod.getInstance()
        }
        textView8.setOnClickListener {
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_ID, "tic_tac_toe_link")
                param(FirebaseAnalytics.Param.ITEM_NAME, "tic_tac_toe_link_clicked")
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "text_view")
            }
            textView8.movementMethod = LinkMovementMethod.getInstance()
        }
        textView9.setOnClickListener {
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_ID, "book_finder_link")
                param(FirebaseAnalytics.Param.ITEM_NAME, "book_finder_link_link_clicked")
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "text_view")
            }
            textView9.movementMethod = LinkMovementMethod.getInstance()
        }
        textView10.setOnClickListener {
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_ID, "bible_link")
                param(FirebaseAnalytics.Param.ITEM_NAME, "bible_link_clicked")
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "text_view")
            }
            textView10.movementMethod = LinkMovementMethod.getInstance()
        }
        textView11.setOnClickListener {
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_ID, "hour_converter_link")
                param(FirebaseAnalytics.Param.ITEM_NAME, "hour_converter_link_clicked")
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "text_view")
            }
            textView11.movementMethod = LinkMovementMethod.getInstance()
        }
        textView12.setOnClickListener {
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_ID, "memorable_places_link")
                param(FirebaseAnalytics.Param.ITEM_NAME, "memorable_places_link_clicked")
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "text_view")
            }
            textView12.movementMethod = LinkMovementMethod.getInstance()
        }
        textView13.setOnClickListener {
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_ID, "texarkana_college_link")
                param(FirebaseAnalytics.Param.ITEM_NAME, "texarkana_college_link_clicked")
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "text_view")
            }
            textView13.movementMethod = LinkMovementMethod.getInstance()
        }
        imageView.setOnClickListener {
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_ID, "tip_calculator_image")
                param(FirebaseAnalytics.Param.ITEM_NAME, "tip_calculator_image_clicked")
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "image_view")
            }
            val intent = Intent(Intent.ACTION_VIEW).setData(Uri.parse(getString(R.string.tip_calculator_link)))
            this.startActivity(intent)
        }
        imageView3.setOnClickListener {
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_ID, "hour_calculator_image")
                param(FirebaseAnalytics.Param.ITEM_NAME, "hour_calculator_image_clicked")
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "image_view")
            }
            val intent = Intent(Intent.ACTION_VIEW).setData(Uri.parse(getString(R.string.hour_calculator_link)))
            this.startActivity(intent)
        }
        imageView4.setOnClickListener {
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_ID, "tic_tac_toe_image")
                param(FirebaseAnalytics.Param.ITEM_NAME, "tic_tac_toe_image_clicked")
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "image_view")
            }
            val intent = Intent(Intent.ACTION_VIEW).setData(Uri.parse(getString(R.string.tic_tac_toe_link)))
            this.startActivity(intent)
        }
        imageView5.setOnClickListener {
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_ID, "book_finder_image")
                param(FirebaseAnalytics.Param.ITEM_NAME, "book_finder_image_clicked")
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "image_view")
            }
            val intent = Intent(Intent.ACTION_VIEW).setData(Uri.parse(getString(R.string.book_finder_link)))
            this.startActivity(intent)
        }
        imageView6.setOnClickListener {
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_ID, "bible_image")
                param(FirebaseAnalytics.Param.ITEM_NAME, "bible_image_clicked")
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "image_view")
            }
            val intent = Intent(Intent.ACTION_VIEW).setData(Uri.parse(getString(R.string.bibleify_link)))
            this.startActivity(intent)
        }
        imageView7.setOnClickListener {
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_ID, "hour_converter_image")
                param(FirebaseAnalytics.Param.ITEM_NAME, "hour_converter_image_clicked")
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "image_view")
            }
            val intent = Intent(Intent.ACTION_VIEW).setData(Uri.parse(getString(R.string.hour_converter_link)))
            this.startActivity(intent)
        }
        imageView8.setOnClickListener {
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_ID, "memorable_places_image")
                param(FirebaseAnalytics.Param.ITEM_NAME, "memorable_places_image_clicked")
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "image_view")
            }
            val intent = Intent(Intent.ACTION_VIEW).setData(Uri.parse(getString(R.string.memorable_places_link)))
            this.startActivity(intent)
        }
        imageView9.setOnClickListener {
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
                param(FirebaseAnalytics.Param.ITEM_ID, "texarkana_college_image")
                param(FirebaseAnalytics.Param.ITEM_NAME, "texarkana_college_image_clicked")
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "image_view")
            }
            val intent = Intent(Intent.ACTION_VIEW).setData(Uri.parse(getString(R.string.texarkana_college_link)))
            this.startActivity(intent)
        }
    }
}