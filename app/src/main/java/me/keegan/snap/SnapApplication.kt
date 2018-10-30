package me.keegan.snap

import android.app.Application

import com.parse.Parse

class SnapApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Parse.initialize(Parse.Configuration.Builder(this)
                .applicationId("APPLICATION_ID")
                .clientKey("YOUR_CLIENT_KEY")
                .server("http://localhost:1337/parse/")
                .build()
        )
    }
}
