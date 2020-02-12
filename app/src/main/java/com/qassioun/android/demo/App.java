package com.qassioun.android.demo;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.HashMap;
import java.util.Map;

import com.qassioun.android.sdk.Qapps;
import com.qassioun.android.sdk.QappsConfig;
import com.qassioun.android.sdk.DeviceId;
import com.qassioun.android.sdk.RemoteConfig;
import com.qassioun.android.sdk.messaging.QappsPush;

import static com.qassioun.android.sdk.Qapps.TAG;

public class App extends Application {
    /** You should use try.qassioun.com instead of YOUR_SERVER for the line below if you are using Qapps trial service */
    final String QAPPS_SERVER_URL = "YOUR_SERVER";
    final String QAPPS_APP_KEY = "YOUR_APP_KEY";

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                // Create the NotificationChannel
                NotificationChannel channel = new NotificationChannel(QappsPush.CHANNEL_ID, getString(R.string.qapps_hannel_name), NotificationManager.IMPORTANCE_DEFAULT);
                channel.setDescription(getString(R.string.qapps_channel_description));

                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build();

                Uri soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"+ getApplicationContext().getPackageName() + "/" + R.raw.notif_sample);

                channel.setSound(soundUri, audioAttributes);
                notificationManager.createNotificationChannel(channel);
            }
        }

        Context appC = getApplicationContext();

        HashMap<String, String> customHeaderValues = new HashMap<>();
        customHeaderValues.put("foo", "bar");

        Map<String, Object> automaticViewSegmentation = new HashMap<>();

        automaticViewSegmentation.put("One", 2);
        automaticViewSegmentation.put("Three", 4.44d);
        automaticViewSegmentation.put("Five", "Six");

        //Qapps.sharedInstance().setConsent(new String[]{Qapps.QappsFeatureNames.push, Qapps.QappsFeatureNames.sessions, Qapps.QappsFeatureNames.location, Qapps.QappsFeatureNames.attribution, Qapps.QappsFeatureNames.crashes, Qapps.QappsFeatureNames.events, Qapps.QappsFeatureNames.starRating, Qapps.QappsFeatureNames.users, Qapps.QappsFeatureNames.views}, false);
        //Log.i(demoTag, "Before calling init. This should return 'false', the value is:" + Qapps.sharedInstance().isInitialized());
        QappsConfig config = (new QappsConfig(appC, QAPPS_APP_KEY, QAPPS_SERVER_URL)).setIdMode(DeviceId.Type.OPEN_UDID)
                //.enableTemporaryDeviceIdMode()
                .enableCrashReporting().setLoggingEnabled(true).enableCrashReporting().setViewTracking(true).setAutoTrackingUseShortName(true)
                .setRequiresConsent(true).setConsentEnabled(new String[]{Qapps.QappsFeatureNames.push, Qapps.QappsFeatureNames.sessions, Qapps.QappsFeatureNames.location, Qapps.QappsFeatureNames.attribution, Qapps.QappsFeatureNames.crashes, Qapps.QappsFeatureNames.events, Qapps.QappsFeatureNames.starRating, Qapps.QappsFeatureNames.users, Qapps.QappsFeatureNames.views})
                .addCustomNetworkRequestHeaders(customHeaderValues).setPushIntentAddMetadata(true).setRemoteConfigAutomaticDownload(true, new RemoteConfig.RemoteConfigCallback() {
                    @Override
                    public void callback(String error) {
                        if(error == null) {
                            Log.d(Qapps.TAG, "Automatic remote config download has completed");
                        } else {
                            Log.d(Qapps.TAG, "Automatic remote config download encountered a problem, " + error);
                        }
                    }
                })
                .setCrashFilters(new String[]{".*secret.*"})
                .setParameterTamperingProtectionSalt("SampleSalt")
                .setAutomaticViewSegmentation(automaticViewSegmentation)
                .setAutoTrackingExceptions(new Class[]{ActivityExampleCustomEvents.class})
                .setTrackOrientationChanges(true)
                ;
        Qapps.sharedInstance().init(config);
        //Log.i(demoTag, "After calling init. This should return 'true', the value is:" + Qapps.sharedInstance().isInitialized());


        QappsPush.init(this, Qapps.QappsMessagingMode.PRODUCTION);



        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();
                        QappsPush.onTokenRefresh(token);
                    }
                });

    }
}
