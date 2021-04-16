package com.example.conference.util

import java.text.SimpleDateFormat
import java.util.*

object LongUtils {
    fun Long.toTime(): String =
        if (this == 0L)
            ""
        else
            SimpleDateFormat("dd MMM HH:mm").format(Date(this))
}