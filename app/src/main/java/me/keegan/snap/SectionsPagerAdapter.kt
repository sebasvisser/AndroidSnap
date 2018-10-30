package me.keegan.snap

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

import java.util.Locale

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class SectionsPagerAdapter(protected var mContext: Context, fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment? {
        // getItem is called to instantiate the fragment for the given page.
        // Return a DummySectionFragment (defined as a static inner class
        // below) with the page number as its lone argument.

        when (position) {
            0 -> return InboxFragment()
            1 -> return FriendsFragment()
        }

        return null
    }

    override fun getCount(): Int {
        return 2
    }

    override fun getPageTitle(position: Int): CharSequence? {
        val l = Locale.getDefault()
        when (position) {
            0 -> return mContext.getString(R.string.title_section1).toUpperCase(l)
            1 -> return mContext.getString(R.string.title_section2).toUpperCase(l)
        }
        return null
    }
}
