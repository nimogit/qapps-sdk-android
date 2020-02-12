package com.qassioun.android.demo;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;

import java.util.HashMap;
import java.util.Map;

import com.qassioun.android.sdk.Qapps;

@SuppressWarnings({"UnusedParameters", "unused"})
public class ActivityExampleViewTracking extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_example_view_tracking);
        Qapps.onCreate(this);

    }

    public void onClickViewTrackingDisableAuto(View v) {
        Qapps.sharedInstance().setViewTracking(false);
    }

    public void onClickViewTrackingEnableAuto(View v) {
        Qapps.sharedInstance().setViewTracking(true);
    }

    public void onClickViewTracking03(View v) {

    }

    public void onClickViewTracking04(View v) {

    }

    public void onClickViewTracking05(View v) {

    }

    public void onClickViewTracking06(View v) {

    }

    public void onClickViewTrackingRecordView(View v) {
        Qapps.sharedInstance().recordView("Awesome view", null);
    }

    public void onClickViewTrackingRecordViewWithSegmentation(View v) {
        Map<String, Object> viewSegmentation = new HashMap<>();

        viewSegmentation.put("Cats", 123);
        viewSegmentation.put("Moons", 9.98d);
        viewSegmentation.put("Moose", "Deer");

        Qapps.sharedInstance().recordView("Better view", viewSegmentation);
    }

    public void onClickViewSetAutomaticSegmentation(View v) {
        Map<String, Object> viewSegmentation = new HashMap<>();

        viewSegmentation.put("Early", 987);
        viewSegmentation.put("Bird", 11.77d);
        viewSegmentation.put("Catches", "Bush");

        Qapps.sharedInstance().setAutomaticViewSegmentation(viewSegmentation);
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

    @Override
    public void onConfigurationChanged (Configuration newConfig){
        super.onConfigurationChanged(newConfig);
        Qapps.sharedInstance().onConfigurationChanged(newConfig);
    }
}
