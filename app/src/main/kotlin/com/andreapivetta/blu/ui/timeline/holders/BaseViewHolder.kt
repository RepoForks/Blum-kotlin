package com.andreapivetta.blu.ui.timeline.holders

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.andreapivetta.blu.R
import com.andreapivetta.blu.data.twitter.model.Tweet
import com.andreapivetta.blu.ui.timeline.InteractionListener
import java.util.*
import java.util.concurrent.TimeUnit

abstract class BaseViewHolder(val container: View, val listener: InteractionListener) :
        RecyclerView.ViewHolder(container) {

    protected var userNameTextView: TextView
    protected var userScreenNameTextView: TextView
    protected var statusTextView: TextView
    protected var timeTextView: TextView
    protected val retweetsStatsTextView: TextView
    protected val favouritesStatsTextView: TextView
    protected var userProfilePicImageView: ImageView
    protected var favouriteImageButton: ImageButton
    protected var retweetImageButton: ImageButton
    protected var respondImageButton: ImageButton

    init {
        userNameTextView = container.findViewById(R.id.userNameTextView) as TextView
        userScreenNameTextView = container.findViewById(R.id.userScreenName_TextView) as TextView
        statusTextView = container.findViewById(R.id.statusTextView) as TextView
        userProfilePicImageView = container.findViewById(R.id.userProfilePicImageView) as ImageView
        timeTextView = container.findViewById(R.id.timeTextView) as TextView
        retweetsStatsTextView = container.findViewById(R.id.retweetsStatsTextView) as TextView
        favouritesStatsTextView = container.findViewById(R.id.favouritesStatsTextView) as TextView
        favouriteImageButton = container.findViewById(R.id.favouriteImageButton) as ImageButton
        retweetImageButton = container.findViewById(R.id.retweetImageButton) as ImageButton
        respondImageButton = container.findViewById(R.id.respondImageButton) as ImageButton
    }

    abstract fun setup(tweet: Tweet)

    protected fun formatDate(timeStamp: Long, context: Context): String {
        val c = Calendar.getInstance()
        val c2 = Calendar.getInstance()
        c2.timeInMillis = timeStamp

        val diff = c.timeInMillis - timeStamp
        val seconds = TimeUnit.MILLISECONDS.toSeconds(diff)
        if (seconds > 60) {
            val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
            if (minutes > 60) {
                val hours = TimeUnit.MILLISECONDS.toHours(diff)
                if (hours > 24) {
                    if (c.get(Calendar.YEAR) == c2.get(Calendar.YEAR))
                        return java.text.SimpleDateFormat("MMM dd", Locale.getDefault()).format(c2.time)
                    else
                        return java.text.SimpleDateFormat("MMM dd yyyy", Locale.getDefault()).format(c2.time)
                } else
                    return context.getString(R.string.mini_hours, hours.toInt())
            } else
                return context.getString(R.string.mini_minutes, minutes.toInt())
        } else
            return context.getString(R.string.mini_seconds, seconds.toInt())
    }

}
