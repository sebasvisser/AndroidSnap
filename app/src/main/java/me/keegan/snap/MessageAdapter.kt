package me.keegan.snap

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

import com.parse.ParseObject

class MessageAdapter(protected var mContext: Context, protected var mMessages: MutableList<ParseObject>) : ArrayAdapter<ParseObject>(mContext, R.layout.message_item, mMessages) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val holder: ViewHolder

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.message_item, null)
            holder = ViewHolder()
            holder.iconImageView = convertView!!.findViewById<View>(R.id.messageIcon) as ImageView
            holder.nameLabel = convertView.findViewById<View>(R.id.senderLabel) as TextView
            convertView.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }

        val message = mMessages[position]

        if (message.getString(ParseConstants.KEY_FILE_TYPE) == ParseConstants.TYPE_IMAGE) {
            holder.iconImageView!!.setImageResource(R.drawable.ic_action_picture)
        } else {
            holder.iconImageView!!.setImageResource(R.drawable.ic_action_play_over_video)
        }
        holder.nameLabel!!.text = message.getString(ParseConstants.KEY_SENDER_NAME)

        return convertView
    }

    fun refill(messages: List<ParseObject>) {
        mMessages.clear()
        mMessages.addAll(messages)
        notifyDataSetChanged()
    }

    private class ViewHolder {
        internal var iconImageView: ImageView? = null
        internal var nameLabel: TextView? = null
    }
}
