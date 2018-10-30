package me.keegan.snap

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ListFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView

import com.parse.FindCallback
import com.parse.ParseException
import com.parse.ParseFile
import com.parse.ParseObject
import com.parse.ParseQuery
import com.parse.ParseUser

import java.util.ArrayList

class InboxFragment : ListFragment() {

    protected lateinit var mMessages: MutableList<ParseObject>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_inbox,
                container, false)
    }

    override fun onResume() {
        super.onResume()

        activity!!.setProgressBarIndeterminateVisibility(true)

        val query = ParseQuery<ParseObject>(ParseConstants.CLASS_MESSAGES)
        query.whereEqualTo(ParseConstants.KEY_RECIPIENT_IDS, ParseUser.getCurrentUser().objectId)
        query.addDescendingOrder(ParseConstants.KEY_CREATED_AT)
        query.findInBackground { messages, e ->
            activity!!.setProgressBarIndeterminateVisibility(false)

            if (e == null) {
                // We found messages!
                mMessages = messages

                val usernames = arrayOfNulls<String>(mMessages.size)
                var i = 0
                for (message in mMessages) {
                    usernames[i] = message.getString(ParseConstants.KEY_SENDER_NAME)
                    i++
                }
                if (listView.adapter == null) {
                    val adapter = MessageAdapter(
                            listView.context,
                            mMessages)
                    listAdapter = adapter
                } else {
                    // refill the adapter!
                    (listView.adapter as MessageAdapter).refill(mMessages)
                }
            }
        }
    }

    override fun onListItemClick(l: ListView?, v: View?, position: Int, id: Long) {
        super.onListItemClick(l, v, position, id)

        val message = mMessages[position]
        val messageType = message.getString(ParseConstants.KEY_FILE_TYPE)
        val file = message.getParseFile(ParseConstants.KEY_FILE)
        val fileUri = Uri.parse(file!!.url)

        if (messageType == ParseConstants.TYPE_IMAGE) {
            // view the image
            val intent = Intent(activity, ViewImageActivity::class.java)
            intent.data = fileUri
            startActivity(intent)
        } else {
            // view the video
            val intent = Intent(Intent.ACTION_VIEW, fileUri)
            intent.setDataAndType(fileUri, "video/*")
            startActivity(intent)
        }

        // Delete it!
        val ids = message.getList<String>(ParseConstants.KEY_RECIPIENT_IDS)

        if (ids!!.size == 1) {
            // last recipient - delete the whole thing!
            message.deleteInBackground()
        } else {
            // remove the recipient and save
            ids.remove(ParseUser.getCurrentUser().objectId)

            val idsToRemove = ArrayList<String>()
            idsToRemove.add(ParseUser.getCurrentUser().objectId)

            message.removeAll(ParseConstants.KEY_RECIPIENT_IDS, idsToRemove)
            message.saveInBackground()
        }
    }
}
