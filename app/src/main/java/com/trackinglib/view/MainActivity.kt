package com.trackinglib.view

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.trackinglib.R
import com.trackinglib.untils.ContextUtils
import com.trackinglibrary.services.NewMessageNotification
import kotlinx.android.synthetic.main.activity_main.*
import refactored.sdk.TrackingApi


class MainActivity : AppCompatActivity() {

    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null

    lateinit var receiver: BroadcastReceiver
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        container.offscreenPageLimit = 4
        container.adapter = mSectionsPagerAdapter

        container.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(container))
        ContextUtils.askForLocationPermission(this)
//        ContextUtils.askForStoragePermission(this)
TrackingApi
//        val filter = IntentFilter("SUPER_ACTION")
//        receiver = object : BroadcastReceiver() {
//            override fun onReceive(context: Context?, intent: Intent?) {
//                Log.e("SUPER!!!!", "AAAA")
//            }
//        }
//        registerReceiver(receiver, filter)
//        NewMessageNotification.createNotification(this, "aaa", "sss")
    }


    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return when (position) {
                0 -> StartTrackerFragment()
                1 -> RecognitionFragment()
                2 -> TracksListFragment()
                3 -> MapFragment()
                else -> throw Exception("no such position=$position")
            }
        }

        override fun getCount(): Int {
            return 4
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        ContextUtils.askForLocationPermission(this)
        ContextUtils.askForStoragePermission(this)
    }
}
