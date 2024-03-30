package com.mudasobwatv;

import android.app.Application;
import com.onesignal.OneSignal;

public class ApplicationClass extends Application {

//    private static final String ONESIGNAL_APP_ID = "0e35d491-ab92-4276-884c-937fa65cdb84";
    private static final String ONESIGNAL_APP_ID = "588eab76-fc99-4616-9fbc-787852a1f5e1";

    @Override
    public void onCreate() {
        super.onCreate();
        // initialise here..

        // Enable verbose OneSignal logging to debug issues if needed.
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);

        // OneSignal Initialization
        OneSignal.initWithContext(this);
        OneSignal.setAppId(ONESIGNAL_APP_ID);

        // promptForPushNotifications will show the native Android notification permission prompt.
        // We recommend removing the following code and instead using an In-App Message to prompt for notification permission (See step 7)
        OneSignal.promptForPushNotifications();

    }
}