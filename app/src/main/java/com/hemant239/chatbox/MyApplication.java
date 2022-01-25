package com.hemant239.chatbox;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;
import com.onesignal.OneSignal;

public class MyApplication extends Application {

    private static final String ONE_SIGNAL_APP_ID = "c01d8a82-28eb-4d70-8242-4a6c33a4bdd4";

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);


        // Enable verbose OneSignal logging to debug issues if needed.
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);

        // OneSignal Initialization
        OneSignal.initWithContext(this);
        OneSignal.setAppId(ONE_SIGNAL_APP_ID);

        OneSignal.unsubscribeWhenNotificationsAreDisabled(false);

    }
}
