package com.codepath.apps.restclienttemplate

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable

class ReceiveIntentDataActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receive_intent_data)
        val intent = intent
        val action = intent.action
        val type = intent.type
        val data: Uri? = intent.data

        if (Intent.ACTION_SEND == action && type != null) {
            if ("text/plain" == type) {

                // Make sure to check whether returned data will be null.
                val titleOfPage = intent.getStringExtra(Intent.EXTRA_SUBJECT)
                val urlOfPage = intent.getStringExtra(Intent.EXTRA_TEXT)
                val imageUriOfPage: Uri? =
                    intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as Uri?
            }
        }
    }
}