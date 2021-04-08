package com.example.conference.application

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.example.conference.activity.ConferenceActivity
import com.example.conference.activity.DialogueActivity

class ConferenceApplication: Application(), Application.ActivityLifecycleCallbacks {
    var conference_id = 0
    var dialogue_id = 0

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
    }

    override fun onActivityStarted(activity: Activity) {
    }

    override fun onActivityResumed(activity: Activity) {
        if (activity is DialogueActivity) {
            dialogue_id = activity.vm.dialogueID
        }
        else if (activity is ConferenceActivity) {
            conference_id = activity.viewModel.conferenceID
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