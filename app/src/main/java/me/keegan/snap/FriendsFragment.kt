package me.keegan.snap

import android.app.AlertDialog
import android.os.Bundle
import android.support.v4.app.ListFragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter

import com.parse.FindCallback
import com.parse.ParseException
import com.parse.ParseQuery
import com.parse.ParseRelation
import com.parse.ParseUser

class FriendsFragment : ListFragment() {

    protected lateinit var mFriendsRelation: ParseRelation<ParseUser>
    protected lateinit var mCurrentUser: ParseUser
    protected lateinit var mFriends: List<ParseUser>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_friends,
                container, false)
    }

    override fun onResume() {
        super.onResume()

        mCurrentUser = ParseUser.getCurrentUser()
        mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION)

        activity!!.setProgressBarIndeterminateVisibility(true)

        val query = mFriendsRelation.query
        query.addAscendingOrder(ParseConstants.KEY_USERNAME)
        query.findInBackground { friends, e ->
            activity!!.setProgressBarIndeterminateVisibility(false)

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
                        android.R.layout.simple_list_item_1,
                        usernames)
                listAdapter = adapter
            } else {
                Log.e(TAG, e.message)
                val builder = AlertDialog.Builder(listView.context)
                builder.setMessage(e.message)
                        .setTitle(R.string.error_title)
                        .setPositiveButton(android.R.string.ok, null)
                val dialog = builder.create()
                dialog.show()
            }
        }
    }

    companion object {
        val TAG = FriendsFragment::class.java.simpleName
    }

}
