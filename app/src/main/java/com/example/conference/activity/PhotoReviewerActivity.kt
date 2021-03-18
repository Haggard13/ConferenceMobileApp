package com.example.conference.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.conference.R
import com.example.conference.service.Server
import kotlinx.android.synthetic.main.activity_photo_reviewer.*

class PhotoReviewerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_reviewer)

        val photoID = intent.getIntExtra("photo_id", -1)
        val isConference = intent.getBooleanExtra("is_conference", true)

        with(photoReviewerWV) {
            setBackgroundColor(resources.getColor(R.color.colorText))
            settings.setSupportZoom(true)
            settings.builtInZoomControls = true
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            setPadding(0, 0, 0, 0)
            isScrollbarFadingEnabled = true
            scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
            loadUrl(
                "${Server.baseURL}/${if (isConference) "conference" else "dialogue"}/getPhotography/?id=$photoID")
        }
    }
}