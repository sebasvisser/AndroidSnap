package me.keegan.snap

import android.app.ActionBar
import android.app.Activity
import android.app.AlertDialog
import android.app.FragmentTransaction
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.FragmentActivity
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.Toast

import com.parse.ParseAnalytics
import com.parse.ParseUser

import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : FragmentActivity(), ActionBar.TabListener {

    protected var mMediaUri: Uri? = null

    protected var mDialogListener: DialogInterface.OnClickListener = object : DialogInterface.OnClickListener {

        private val isExternalStorageAvailable: Boolean
            get() {
                val state = Environment.getExternalStorageState()

                return if (state == Environment.MEDIA_MOUNTED) {
                    true
                } else {
                    false
                }
            }

        override fun onClick(dialog: DialogInterface, which: Int) {
            when (which) {
                0 // Take picture
                -> {
                    val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    mMediaUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE)
                    if (mMediaUri == null) {
                        // display an error
                        Toast.makeText(this@MainActivity, R.string.error_external_storage,
                                Toast.LENGTH_LONG).show()
                    } else {
                        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri)
                        startActivityForResult(takePhotoIntent, TAKE_PHOTO_REQUEST)
                    }
                }
                1 // Take video
                -> {
                    val videoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
                    mMediaUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO)
                    if (mMediaUri == null) {
                        // display an error
                        Toast.makeText(this@MainActivity, R.string.error_external_storage,
                                Toast.LENGTH_LONG).show()
                    } else {
                        videoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri)
                        videoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10)
                        videoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0) // 0 = lowest res
                        startActivityForResult(videoIntent, TAKE_VIDEO_REQUEST)
                    }
                }
                2 // Choose picture
                -> {
                    val choosePhotoIntent = Intent(Intent.ACTION_GET_CONTENT)
                    choosePhotoIntent.type = "image/*"
                    startActivityForResult(choosePhotoIntent, PICK_PHOTO_REQUEST)
                }
                3 // Choose video
                -> {
                    val chooseVideoIntent = Intent(Intent.ACTION_GET_CONTENT)
                    chooseVideoIntent.type = "video/*"
                    Toast.makeText(this@MainActivity, R.string.video_file_size_warning, Toast.LENGTH_LONG).show()
                    startActivityForResult(chooseVideoIntent, PICK_VIDEO_REQUEST)
                }
            }
        }

        private fun getOutputMediaFileUri(mediaType: Int): Uri? {
            // To be safe, you should check that the SDCard is mounted
            // using Environment.getExternalStorageState() before doing this.
            if (isExternalStorageAvailable) {
                // get the URI

                // 1. Get the external storage directory
                val appName = this@MainActivity.getString(R.string.app_name)
                val mediaStorageDir = File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                        appName)

                // 2. Create our subdirectory
                if (!mediaStorageDir.exists()) {
                    if (!mediaStorageDir.mkdirs()) {
                        Log.e(TAG, "Failed to create directory.")
                        return null
                    }
                }

                // 3. Create a file name
                // 4. Create the file
                val mediaFile: File
                val now = Date()
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(now)

                val path = mediaStorageDir.path + File.separator
                if (mediaType == MEDIA_TYPE_IMAGE) {
                    mediaFile = File(path + "IMG_" + timestamp + ".jpg")
                } else if (mediaType == MEDIA_TYPE_VIDEO) {
                    mediaFile = File(path + "VID_" + timestamp + ".mp4")
                } else {
                    return null
                }

                Log.d(TAG, "File: " + Uri.fromFile(mediaFile))

                // 5. Return the file's URI
                return Uri.fromFile(mediaFile)
            } else {
                return null
            }
        }
    }

    /**
     * The [android.support.v4.view.PagerAdapter] that will provide
     * fragments for each of the sections. We use a
     * [android.support.v4.app.FragmentPagerAdapter] derivative, which
     * will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a
     * [android.support.v4.app.FragmentStatePagerAdapter].
     */
    internal lateinit var mSectionsPagerAdapter: SectionsPagerAdapter

    /**
     * The [ViewPager] that will host the section contents.
     */
    internal lateinit var mViewPager: ViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS)
        setContentView(R.layout.activity_main)

        //        ParseAnalytics.trackAppOpened(getIntent());

        val currentUser = ParseUser.getCurrentUser()
        if (currentUser == null) {
            navigateToLogin()
        } else {
            Log.i(TAG, currentUser.username)
        }

        // Set up the action bar.
        val actionBar = actionBar
        actionBar!!.navigationMode = ActionBar.NAVIGATION_MODE_TABS

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the app.
        mSectionsPagerAdapter = SectionsPagerAdapter(this,
                supportFragmentManager)

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById<View>(R.id.pager) as ViewPager
        mViewPager.adapter = mSectionsPagerAdapter

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager
                .setOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
                    override fun onPageSelected(position: Int) {
                        actionBar.setSelectedNavigationItem(position)
                    }
                })

        // For each of the sections in the app, add a tab to the action bar.
        for (i in 0 until mSectionsPagerAdapter.count) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(actionBar.newTab()
                    .setText(mSectionsPagerAdapter.getPageTitle(i))
                    .setTabListener(this))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_PHOTO_REQUEST || requestCode == PICK_VIDEO_REQUEST) {
                if (data == null) {
                    Toast.makeText(this, getString(R.string.general_error), Toast.LENGTH_LONG).show()
                } else {
                    mMediaUri = data.data
                }

                Log.i(TAG, "Media URI: " + mMediaUri!!)
                if (requestCode == PICK_VIDEO_REQUEST) {
                    // make sure the file is less than 10 MB
                    var fileSize = 0
                    var inputStream: InputStream? = null

                    try {
                        inputStream = contentResolver.openInputStream(mMediaUri!!)
                        fileSize = inputStream!!.available()
                    } catch (e: FileNotFoundException) {
                        Toast.makeText(this, R.string.error_opening_file, Toast.LENGTH_LONG).show()
                        return
                    } catch (e: IOException) {
                        Toast.makeText(this, R.string.error_opening_file, Toast.LENGTH_LONG).show()
                        return
                    } finally {
                        try {
                            inputStream!!.close()
                        } catch (e: IOException) { /* Intentionally blank */
                        }

                    }

                    if (fileSize >= FILE_SIZE_LIMIT) {
                        Toast.makeText(this, R.string.error_file_size_too_large, Toast.LENGTH_LONG).show()
                        return
                    }
                }
            } else {
                val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                mediaScanIntent.data = mMediaUri
                sendBroadcast(mediaScanIntent)
            }

            val recipientsIntent = Intent(this, RecipientsActivity::class.java)
            recipientsIntent.data = mMediaUri

            val fileType: String
            if (requestCode == PICK_PHOTO_REQUEST || requestCode == TAKE_PHOTO_REQUEST) {
                fileType = ParseConstants.TYPE_IMAGE
            } else {
                fileType = ParseConstants.TYPE_VIDEO
            }

            recipientsIntent.putExtra(ParseConstants.KEY_FILE_TYPE, fileType)
            startActivity(recipientsIntent)
        } else if (resultCode != Activity.RESULT_CANCELED) {
            Toast.makeText(this, R.string.general_error, Toast.LENGTH_LONG).show()
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId

        when (itemId) {
            R.id.action_logout -> {
                ParseUser.logOut()
                navigateToLogin()
            }
            R.id.action_edit_friends -> {
                val intent = Intent(this, EditFriendsActivity::class.java)
                startActivity(intent)
            }
            R.id.action_camera -> {
                val builder = AlertDialog.Builder(this)
                builder.setItems(R.array.camera_choices, mDialogListener)
                val dialog = builder.create()
                dialog.show()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onTabSelected(tab: ActionBar.Tab,
                               fragmentTransaction: FragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.currentItem = tab.position
    }

    override fun onTabUnselected(tab: ActionBar.Tab,
                                 fragmentTransaction: FragmentTransaction) {
    }

    override fun onTabReselected(tab: ActionBar.Tab,
                                 fragmentTransaction: FragmentTransaction) {
    }

    companion object {

        val TAG = MainActivity::class.java.simpleName

        val TAKE_PHOTO_REQUEST = 0
        val TAKE_VIDEO_REQUEST = 1
        val PICK_PHOTO_REQUEST = 2
        val PICK_VIDEO_REQUEST = 3

        val MEDIA_TYPE_IMAGE = 4
        val MEDIA_TYPE_VIDEO = 5

        val FILE_SIZE_LIMIT = 1024 * 1024 * 10 // 10 MB
    }
}
