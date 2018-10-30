package me.keegan.snap;

import android.app.Application;

import com.parse.Parse;

public class SnapApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Parse.initialize(new Parse.Configuration.Builder(this)
            .applicationId("APPLICATION_ID")
            .clientKey("YOUR_CLIENT_KEY")
            .server("http://localhost:1337/parse/")
            .build()
        );
    }
}
