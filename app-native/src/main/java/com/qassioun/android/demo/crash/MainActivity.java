package com.qassioun.android.demo.crash;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.qassioun.android.sdk.Qapps;
import com.qassioun.android.sdknative.QappsNative;

public class MainActivity extends AppCompatActivity {

    private static String TAG = "QappsDemoNative";

    final String QAPPS_SERVER_URL = "https://try.qassioun.com";
    final String QAPPS_APP_KEY = "xxxxxxx";

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private boolean initCrashReporting() {
        return QappsNative.initNative(getApplicationContext());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //init qapps
        Context appC = getApplicationContext();

        Qapps.sharedInstance().setLoggingEnabled(true);
        Qapps.sharedInstance().enableCrashReporting();
        Qapps.sharedInstance().setViewTracking(false);
        Qapps.sharedInstance().setRequiresConsent(false);
        Qapps.sharedInstance().init(appC, QAPPS_SERVER_URL, QAPPS_APP_KEY, "4432");

        Qapps.onCreate(this);

        // Example of a call to a native method
        TextView tv = findViewById(R.id.sampleText);
        tv.setText(stringFromJNI());
        initCrashReporting();
        final Button button = findViewById(R.id.crashButton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG,"crash button clicked");
                // QappsNative.crash();
                testCrash();
            }
        });
    }

    @Override
    public void onStart()
    {
        super.onStart();
        Qapps.sharedInstance().onStart(this);
    }

    @Override
    public void onStop()
    {
        Qapps.sharedInstance().onStop();
        super.onStop();
    }

    // defined in native-lib.cpp
    public native String stringFromJNI();
    public native int testCrash();
}
