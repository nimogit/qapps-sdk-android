package com.qassioun.android.demo;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;

import java.util.Random;

import com.qassioun.android.sdk.Qapps;
import com.qassioun.android.sdk.DeviceId;

public class ActivityExampleDeviceId extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_example_device_id);
        Qapps.onCreate(this);

    }

    public void onClickDeviceId01(View v) {
        //set device id without merge
        Qapps.sharedInstance().changeDeviceId(DeviceId.Type.DEVELOPER_SUPPLIED, "New Device ID" + (new Random().nextInt()));
    }

    public void onClickDeviceId02(View v) {
        //set device id with merge
        Qapps.sharedInstance().changeDeviceId("New Device ID!" + (new Random().nextInt()));
    }

    public void onClickDeviceId03(View v) {
        //Entering temporary id mode
        Qapps.sharedInstance().enableTemporaryIdMode();
    }

    public void onClickDeviceId04(View v) {
        //set device id without merge
        Qapps.sharedInstance().changeDeviceId(DeviceId.Type.OPEN_UDID, null);
    }

    public void onClickDeviceId05(View v) {
        //set device id witho merge
        Qapps.sharedInstance().changeDeviceId(null);
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
