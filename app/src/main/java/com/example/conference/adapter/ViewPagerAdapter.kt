package com.example.conference.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.conference.fragment.ConferencesFragment
import com.example.conference.fragment.DialoguesFragment
import com.example.conference.fragment.ProfileFragment

class ViewPagerAdapter(fm: FragmentManager, behavior: Int = BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT)
    : FragmentPagerAdapter(fm, behavior) {

    private val fragmentList: List<Fragment> =
        listOf(ConferencesFragment(), DialoguesFragment(), ProfileFragment())

    override fun getCount(): Int = fragmentList.size

    override fun getItem(position: Int): Fragment = fragmentList[position]
}
