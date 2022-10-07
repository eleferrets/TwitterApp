package com.codepath.apps.restclienttemplate

import android.app.Activity.RESULT_OK
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.codepath.apps.restclienttemplate.models.Tweet
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import com.google.android.material.snackbar.Snackbar
import okhttp3.Headers
import org.json.JSONException
import java.util.*

class TimelineActivity : AppCompatActivity() {

    lateinit var client: TwitterClient
    lateinit var rvTweets: RecyclerView
    lateinit var adapter: TweetsAdapter
    lateinit var swipeContainer: SwipeRefreshLayout
    val tweets = ArrayList<Tweet>()
    lateinit var scrollListener: EndlessRecyclerViewScrollListener
    lateinit var fab: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timeline)

        client = TwitterApplication.getRestClient(this)

        fab = findViewById(R.id.compose)
fab.setOnClickListener{
        val intent = Intent(this, ComposeActivity::class.java)
    startActivityForResult(intent, REQUEST_CODE)
}

        swipeContainer = findViewById(R.id.swipeContainer)
        swipeContainer.setOnRefreshListener {
            Log.i(TAG, "Refreshing timeline")
            populateHomeTimeline()
        }
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        )


        rvTweets = findViewById(R.id.rvTweets)
        adapter = TweetsAdapter(tweets)
        var linearLayoutManager = LinearLayoutManager(this)
        rvTweets.layoutManager = linearLayoutManager
        scrollListener = object : EndlessRecyclerViewScrollListener(linearLayoutManager) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                loadMoreData()
                Log.i(TAG, "onLoadMore: $page")
            }
        }
        rvTweets.addOnScrollListener(scrollListener)



        rvTweets.adapter = adapter

        populateHomeTimeline()
    }

    // Override and inflate the menu we created for the compose icon
//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.menu_main, menu)
//        return true
//    }

    // Handles clicks for menu item
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        if (item.itemId == R.id.compose) {
////        Toast.makeText(this, "Ready to compose tweet!", Toast.LENGTH_SHORT).show()
////        Navigate to compose screen
//            val intent = Intent(this, ComposeActivity::class.java)
//            startActivityForResult(intent, REQUEST_CODE)
//        }
//        return super.onOptionsItemSelected(item)
//    }


    // Called when we come back from ComposeActivity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
            // Get data from tweet intent
            val tweet = data?.getParcelableExtra("tweet") as Tweet

            // Update timeline
            // Modify data source of tweets
            tweets.add(0, tweet)
            // Update adapter
            adapter.notifyItemInserted(0)
            // Prevents the scroll up yourself
            rvTweets.smoothScrollToPosition(0)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun loadMoreData() {
        // 1. Send an API request to retrieve appropriate paginated data
        // 2. Deserialize and construct new model objects from the API response
        // 3. Append the new data objects to the existing set of items inside the array of items
        // 4. Notify the adapter of the new items made with `notifyItemRangeInserted()`
        client.getNextPageOfTweets(object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Headers, json: JSON) {
                Log.i(TAG, "onSuccess for loadMoreData!")
                val jsonArray = json.jsonArray
                try {
                    val currentSize = tweets.size - 1
                    val listOfNewTweetsRetrieved = Tweet.fromJsonArray(jsonArray)
                    tweets.addAll(listOfNewTweetsRetrieved)
                    adapter.notifyItemRangeInserted(currentSize, listOfNewTweetsRetrieved.size)

                } catch (e: JSONException) {
                    Log.e(TAG, "JSON Exception $e")
                }
            }

            override fun onFailure(
                statusCode: Int,
                headers: Headers?,
                response: String?,
                throwable: Throwable?
            ) {
                Log.i(TAG, "onFailure loadMoreData $response $throwable")

            }
        }, tweets.get(tweets.size - 1).id)
    }

    fun populateHomeTimeline() {
        client.getHomeTimeline(object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Headers, json: JSON) {
                Log.i(TAG, "onSuccess!")
                val jsonArray = json.jsonArray
                try {
                    // Clear out our currently fetched tweets
                    adapter.clear()
                    val listOfNewTweetsRetrieved = Tweet.fromJsonArray(jsonArray)
                    tweets.addAll(listOfNewTweetsRetrieved)
                    adapter.notifyDataSetChanged()
                    // Now we call setRefreshing(false) to signal refresh has finished
                    swipeContainer.setRefreshing(false)

                } catch (e: JSONException) {
                    Log.e(TAG, "JSON Exception $e")
                }
            }

            override fun onFailure(
                statusCode: Int,
                headers: Headers?,
                response: String?,
                throwable: Throwable?
            ) {
                Log.i(TAG, "onFailure $response $statusCode")

            }
        })
    }

    companion object {
        val TAG = "TimelineActivity"
        val REQUEST_CODE = 10
    }
}
