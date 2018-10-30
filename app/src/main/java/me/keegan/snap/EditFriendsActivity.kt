package me.keegan.snap

import android.app.AlertDialog
import android.app.ListActivity
import android.os.Bundle
import android.support.v4.app.NavUtils
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.ListView

import com.parse.FindCallback
import com.parse.ParseException
import com.parse.ParseQuery
import com.parse.ParseRelation
import com.parse.ParseUser
import com.parse.SaveCallback

class EditFriendsActivity : ListActivity() {
    protected lateinit var mFriendsRelation: ParseRelation<ParseUser>
    protected lateinit var mCurrentUser: ParseUser
    protected lateinit var mUsers: List<ParseUser>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS)
        setContentView(R.layout.activity_edit_friends)
        // Show the Up button in the action bar.
        setupActionBar()

        listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE
    }

    override fun onResume() {
        super.onResume()

        mCurrentUser = ParseUser.getCurrentUser()
        mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION)

        setProgressBarIndeterminateVisibility(true)

        val query = ParseUser.getQuery()
        query.orderByAscending(ParseConstants.KEY_USERNAME)
        query.limit = 1000
        query.findInBackground { users, e ->
            setProgressBarIndeterminateVisibility(false)

            if (e == null) {
                // Success
                mUsers = users
                val usernames = arrayOfNulls<String>(mUsers.size)
                var i = 0
                for (user in mUsers) {
                    usernames[i] = user.username
                    i++
                }
                val adapter = ArrayAdapter<String>(
                        this@EditFriendsActivity,
                        android.R.layout.simple_list_item_checked,
                        usernames)
                listAdapter = adapter

                addFriendCheckmarks()
            } else {
                Log.e(TAG, e.message)
                val builder = AlertDialog.Builder(this@EditFriendsActivity)
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

    override fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
        super.onListItemClick(l, v, position, id)

        if (listView.isItemChecked(position)) {
            // add the friend
            mFriendsRelation.add(mUsers[position])
        } else {
            // remove the friend
            mFriendsRelation.remove(mUsers[position])
        }

        mCurrentUser.saveInBackground { e ->
            if (e != null) {
                Log.e(TAG, e.message)
            }
        }
    }

    private fun addFriendCheckmarks() {
        mFriendsRelation.query.findInBackground { friends, e ->
            if (e == null) {
                // list returned - look for a match
                for (i in mUsers.indices) {
                    val user = mUsers[i]

                    for (friend in friends) {
                        if (friend.objectId == user.objectId) {
                            listView.setItemChecked(i, true)
                        }
                    }
                }
            } else {
                Log.e(TAG, e.message)
            }
        }
    }

    companion object {

        val TAG = EditFriendsActivity::class.java.simpleName
    }
}
