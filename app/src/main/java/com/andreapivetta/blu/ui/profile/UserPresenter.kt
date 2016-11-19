package com.andreapivetta.blu.ui.profile

import com.andreapivetta.blu.data.model.Tweet
import com.andreapivetta.blu.data.twitter.TwitterAPI
import com.andreapivetta.blu.ui.base.BasePresenter
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import timber.log.Timber
import twitter4j.Paging
import twitter4j.User

/**
 * Created by andrea on 14/11/16.
 */
class UserPresenter(val user: User) : BasePresenter<UserMvpView>() {

    var page: Int = 1
    private var subscription: Subscription? = null

    fun loadUserData() {
        checkViewAttached()

        subscription = TwitterAPI.getUserTimeline(user.id, Paging(page, 50))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    mvpView?.showTweets(it.map(::Tweet).toMutableList())
                }, { Timber.e(it?.message) })
    }

}