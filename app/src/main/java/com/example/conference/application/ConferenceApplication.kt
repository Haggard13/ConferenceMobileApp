package com.example.conference.application

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.example.conference.activity.ConferenceActivity
import com.example.conference.activity.DialogueActivity

class ConferenceApplication: Application(), Application.ActivityLifecycleCallbacks {
    var conferenceID = 0
    var dialogueID = 0

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
    }

    override fun onActivityStarted(activity: Activity) {
    }

    override fun onActivityResumed(activity: Activity) {
        if (activity is DialogueActivity) {
            dialogueID = activity.viewModel.dialogueID
        }
        else if (activity is ConferenceActivity) {
            conferenceID = activity.viewModel.conferenceID
        }
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityStopped(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {
    }

}