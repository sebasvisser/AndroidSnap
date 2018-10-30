package me.keegan.snap

import android.app.AlertDialog
import android.app.ListActivity
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.NavUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast

import com.parse.FindCallback
import com.parse.ParseException
import com.parse.ParseFile
import com.parse.ParseObject
import com.parse.ParseQuery
import com.parse.ParseRelation
import com.parse.ParseUser
import com.parse.SaveCallback

import java.util.ArrayList

class RecipientsActivity : ListActivity() {

    protected lateinit var mFriendsRelation: ParseRelation<ParseUser>
    protected lateinit var mCurrentUser: ParseUser
    protected lateinit var mFriends: List<ParseUser>
    protected lateinit var mSendMenuItem: MenuItem
    protected var mMediaUri: Uri? = null
    protected var mFileType: String? = null

    protected val recipientIds: ArrayList<String>
        get() {
            val recipientIds = ArrayList<String>()
            for (i in 0 until listView.count) {
                if (listView.isItemChecked(i)) {
                    recipientIds.add(mFriends[i].objectId)
                }
            }
            return recipientIds
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS)
        setContentView(R.layout.activity_recipients)
        // Show the Up button in the action bar.
        setupActionBar()

        listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE

        mMediaUri = intent.data
        mFileType = intent.extras!!.getString(ParseConstants.KEY_FILE_TYPE)
    }

    public override fun onResume() {
        super.onResume()

        mCurrentUser = ParseUser.getCurrentUser()
        mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION)

        setProgressBarIndeterminateVisibility(true)

        val query = mFriendsRelation.query
        query.addAscendingOrder(ParseConstants.KEY_USERNAME)
        query.findInBackground { friends, e ->
            setProgressBarIndeterminateVisibility(false)

            if (e == null) {
                mFriends = friends

                val usernames = arrayOfNulls<String>(mFriends.size)
                var i = 0
                for (user in mFriends) {
                    usernames[i] = user.username
                    i++
                }
                val adapter = ArrayAdapter<String>(
                        listView.context,
                        android.R.layout.simple_list_item_checked,
                        usernames)
                listAdapter = adapter
            } else {
                Log.e(TAG, e.message)
                val builder = AlertDialog.Builder(this@RecipientsActivity)
                builder.setMessage(e.message)
                        .setTitle(R.string.error_title)
                        .setPositiveButton(android.R.string.ok, null)
                val dialog = builder.create()
                dialog.show()
            }
        }
    }

    /**
     * Set up the [android.app.ActionBar].
     */
    private fun setupActionBar() {

        actionBar!!.setDisplayHomeAsUpEnabled(true)

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.recipients, menu)
        mSendMenuItem = menu.getItem(0)
        return true
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
            R.id.action_send -> {
                val message = createMessage()
                if (message == null) {
                    // error
                    val builder = AlertDialog.Builder(this)
                    builder.setMessage(R.string.error_selecting_file)
                            .setTitle(R.string.error_selecting_file_title)
                            .setPositiveButton(android.R.string.ok, null)
                    val dialog = builder.create()
                    dialog.show()
                } else {
                    send(message)
                    finish()
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
        super.onListItemClick(l, v, position, id)

        if (l.checkedItemCount > 0) {
            mSendMenuItem.isVisible = true
        } else {
            mSendMenuItem.isVisible = false
        }
    }

    protected fun createMessage(): ParseObject? {
        val message = ParseObject(ParseConstants.CLASS_MESSAGES)
        message.put(ParseConstants.KEY_SENDER_ID, ParseUser.getCurrentUser().objectId)
        message.put(ParseConstants.KEY_SENDER_NAME, ParseUser.getCurrentUser().username)
        message.put(ParseConstants.KEY_RECIPIENT_IDS, recipientIds)
        message.put(ParseConstants.KEY_FILE_TYPE, mFileType!!)

        var fileBytes = FileHelper.getByteArrayFromFile(this, mMediaUri!!)

        if (fileBytes == null) {
            return null
        } else {
            if (mFileType == ParseConstants.TYPE_IMAGE) {
                fileBytes = FileHelper.reduceImageForUpload(fileBytes)
            }

            val fileName = FileHelper.getFileName(this, mMediaUri!!, mFileType!!)
            val file = ParseFile(fileName, fileBytes)
            message.put(ParseConstants.KEY_FILE, file)

            return message
        }
    }

    protected fun send(message: ParseObject?) {
        message!!.saveInBackground { e ->
            if (e == null) {
                // success!
                Toast.makeText(this@RecipientsActivity, R.string.success_message, Toast.LENGTH_LONG).show()
            } else {
                val builder = AlertDialog.Builder(this@RecipientsActivity)
                builder.setMessage(R.string.error_sending_message)
                        .setTitle(R.string.error_selecting_file_title)
                        .setPositiveButton(android.R.string.ok, null)
                val dialog = builder.create()
                dialog.show()
            }
        }
    }

    companion object {

        val TAG = RecipientsActivity::class.java.simpleName
    }
}
