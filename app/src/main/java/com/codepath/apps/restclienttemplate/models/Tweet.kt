package com.codepath.apps.restclienttemplate.models

import com.codepath.apps.restclienttemplate.TimeFormatter
import org.json.JSONArray
import org.json.JSONObject

class Tweet {
    var body: String = ""
    var createdAt: String = ""
    var id: Long = 0
    var user: User? = null
    fun getFormattedTimestamp(): String {
        return TimeFormatter.getTimeDifference(createdAt)
    }

    companion object {
        // Create one tweet
        fun fromJson(jsonObject: JSONObject): Tweet {
            val tweet = Tweet()
            tweet.body = jsonObject.getString("text")
            tweet.createdAt = jsonObject.getString("created_at")
            tweet.id = jsonObject.getLong("id")
            tweet.user = User.fromJson(jsonObject.getJSONObject("user"))
            return tweet
        }

        // Take a list of tweets
        fun fromJsonArray(jsonArray: JSONArray): List<Tweet> {
            val tweets = ArrayList<Tweet>()
            // until is exclusive - .. is inclusive
            for (i in 0 until jsonArray.length()) {
                tweets.add(fromJson(jsonArray.getJSONObject(i)))
            }
            return tweets
        }
    }
}