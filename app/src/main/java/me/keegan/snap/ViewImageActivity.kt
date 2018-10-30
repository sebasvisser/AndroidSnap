package me.keegan.snap

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.NavUtils
import android.view.MenuItem
import android.view.View
import android.widget.ImageView

import com.squareup.picasso.Picasso

import java.util.Timer
import java.util.TimerTask

class ViewImageActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_image)
        // Show the Up button in the action bar.
        setupActionBar()

        val imageView = findViewById<View>(R.id.imageView) as ImageView

        val imageUri = intent.data

        Picasso.with(this).load(imageUri!!.toString()).into(imageView)

        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                finish()
            }
        }, (10 * 1000).toLong())
    }

    /**
     * Set up the [android.app.ActionBar].
     */
    private fun setupActionBar() {

        actionBar!!.setDisplayHomeAsUpEnabled(true)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                // This ID represents the Home or Up button. In the case of this
                // activity, the Up button is shown. Use NavUtils to allow users
                // to navigate up one level in the application structure. For
                // more details, see the Navigation pattern on Android Design:
                //
                // http://developer.android.com/design/patterns/navigation.html#up-vs-back
                //
                NavUtils.navigateUpFromSameTask(this)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}
