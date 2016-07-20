package com.andreapivetta.blu.ui.timeline

import com.andreapivetta.blu.arch.BasePresenter
import com.andreapivetta.blu.data.twitter.TwitterAPI
import com.andreapivetta.blu.data.twitter.model.Tweet
import rx.SingleSubscriber
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import timber.log.Timber
import twitter4j.Paging
import twitter4j.Status
import twitter4j.User

/**
 * Created by andrea on 17/05/16.
 */
open class TimelinePresenter : BasePresenter<TimelineMvpView>() {

    var page: Int = 1
    protected var isLoading: Boolean = false
    protected var mSubscriber: Subscription? = null
    protected var mRefreshSubscriber: Subscription? = null
    private var mFavoriteSubscriber: Subscription? = null
    private var mRetweetSubscriber: Subscription? = null
    private var mUnfavoriteSubscriber: Subscription? = null
    private var mUnretweetSubscriber: Subscription? = null

    override fun detachView() {
        super.detachView()
        mSubscriber?.unsubscribe()
        mRefreshSubscriber?.unsubscribe()
        mFavoriteSubscriber?.unsubscribe()
        mRefreshSubscriber?.unsubscribe()
        mUnfavoriteSubscriber?.unsubscribe()
        mUnretweetSubscriber?.unsubscribe()
    }

    open fun getTweets() {
        checkViewAttached()
        mvpView?.showLoading()
        isLoading = true

        mSubscriber = TwitterAPI.getHomeTimeline(Paging(page, 50))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(object : SingleSubscriber<MutableList<Status>>() {
                    override fun onSuccess(list: MutableList<Status>?) {
                        mvpView?.hideLoading()

                        when {
                            list == null -> mvpView?.showError()
                            list.isEmpty() -> mvpView?.showEmpty()
                            else -> {
                                mvpView?.showTweets(list.map { status -> Tweet(status) }.toMutableList())
                                page++
                            }
                        }

                        isLoading = false
                    }

                    override fun onError(error: Throwable?) {
                        Timber.e(error?.message)
                        mvpView?.hideLoading()
                        mvpView?.showError()
                        isLoading = false
                    }
                })
    }

    open fun getMoreTweets() {
        if (isLoading)
            return

        checkViewAttached()
        isLoading = true

        mSubscriber = TwitterAPI.getHomeTimeline(Paging(page, 50))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(object : SingleSubscriber<MutableList<Status>>() {
                    override fun onSuccess(list: MutableList<Status>?) {
                        if (list != null) {
                            if (list.isNotEmpty())
                                mvpView?.showMoreTweets(list.map { status -> Tweet(status) }.toMutableList())
                            page++
                        }
                        isLoading = false
                    }

                    override fun onError(error: Throwable?) {
                        Timber.e(error?.message)
                        isLoading = false
                    }
                })
    }

    open fun onRefresh() {
        checkViewAttached()

        val page = Paging(1, 200)
        page.sinceId = mvpView!!.getLastTweetId()

        mRefreshSubscriber = TwitterAPI.refreshTimeLine(page)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(object : SingleSubscriber<MutableList<Status>>() {
                    override fun onSuccess(list: MutableList<Status>?) {
                        mvpView?.stopRefresh()
                        list?.reversed()?.forEach { status -> mvpView?.showTweet(Tweet(status)) }
                    }

                    override fun onError(error: Throwable?) {
                        Timber.e(error?.message)
                        mvpView?.stopRefresh()
                    }
                })
    }

    fun favorite(tweet: Tweet) {
        checkViewAttached()

        mFavoriteSubscriber = TwitterAPI.favorite(tweet.id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .map { status -> Tweet(status) }
                .subscribe(object : SingleSubscriber<Tweet>() {
                    override fun onError(error: Throwable?) {
                        Timber.e(error?.message)
                    }

                    override fun onSuccess(tweetResult: Tweet?) {
                        tweet.favorited = true
                        tweet.favoriteCount++
                        mvpView?.updateRecyclerViewView()
                    }
                })
    }

    fun retweet(tweet: Tweet) {
        checkViewAttached()

        mRetweetSubscriber = TwitterAPI.retweet(tweet.id)
                .map { status -> Tweet(status) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(object : SingleSubscriber<Tweet>() {
                    override fun onError(error: Throwable?) {
                        Timber.e(error?.message)
                    }

                    override fun onSuccess(value: Tweet?) {
                        tweet.retweeted = true
                        tweet.retweetCount++
                        mvpView?.updateRecyclerViewView()
                    }
                })
    }

    fun unfavorite(tweet: Tweet) {
        checkViewAttached()

        mUnfavoriteSubscriber = TwitterAPI.unfavorite(tweet.id)
                .map { status -> Tweet(status) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(object : SingleSubscriber<Tweet>() {
                    override fun onError(error: Throwable?) {
                        Timber.e(error?.message)
                    }

                    override fun onSuccess(value: Tweet?) {
                        tweet.favorited = false
                        tweet.favoriteCount--
                        mvpView?.updateRecyclerViewView()
                    }
                })
    }

    fun reply(tweet: Tweet, user: User) {
        mvpView?.showNewTweet(tweet, user)
    }

}