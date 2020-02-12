package com.qassioun.android.demo;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;

import com.qassioun.android.sdk.Qapps;

@SuppressWarnings("UnusedParameters")
public class ActivityExampleCrashReporting extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_example_crash_reporting);
        Qapps.onCreate(this);

    }

    @SuppressWarnings("unused")
    void EmptyFunction_1(){
        //keep this here, it's for proguard testing
    }

    @SuppressWarnings("unused")
    void EmptyFunction_2(){
        //keep this here, it's for proguard testing
    }
    @SuppressWarnings("unused")
    void EmptyFunction_3(){
        //keep this here, it's for proguard testing
    }


    public void onClickCrashReporting01(View v) {
        Qapps.sharedInstance().addCrashBreadcrumb("Unrecognized selector crash");
    }

    public void onClickCrashReporting02(View v) {
        Qapps.sharedInstance().addCrashBreadcrumb("Out of bounds crash");
        //noinspection MismatchedReadAndWriteOfArray
        int[] data = new int[]{};
        data[0] = 9;
    }

    public void onClickCrashReporting03(View v) {
        Qapps.sharedInstance().addCrashBreadcrumb("Null pointer crash");
        Qapps.sharedInstance().crashTest(3);
    }

    public void onClickCrashReporting04(View v) {
        Qapps.sharedInstance().addCrashBreadcrumb("Invalid Geometry crash");
    }

    public void onClickCrashReporting05(View v) {
        Qapps.sharedInstance().addCrashBreadcrumb("Assert fail crash");
        //Assert.assertEquals(1, 0);
    }

    public void onClickCrashReporting06(View v) {
        Qapps.sharedInstance().addCrashBreadcrumb("Kill process crash");
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public void onClickCrashReporting07(View v) {
        Qapps.sharedInstance().addCrashBreadcrumb("Custom crash log crash");
        Qapps.sharedInstance().addCrashBreadcrumb("Adding some custom crash log");
        Qapps.sharedInstance().crashTest(2);
    }

    public void onClickCrashReporting08(View v) {
        Qapps.sharedInstance().addCrashBreadcrumb("Recording handled exception 1");
        Qapps.sharedInstance().recordHandledException(new Exception("A custom error text"));
        Qapps.sharedInstance().addCrashBreadcrumb("Recording handled exception 3");
    }

    public void onClickCrashReporting09(View v) throws Exception {
        Qapps.sharedInstance().addCrashBreadcrumb("Unhandled exception info");
        throw new Exception("A unhandled exception");
    }

    public void onClickCrashReporting13(View v){
        String largeCrumb = "dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd";
        Qapps.sharedInstance().addCrashBreadcrumb(largeCrumb);
    }

    public void onClickCrashReporting10(View v) throws Exception {
        deepFunctionCall_1();
    }

    public void onClickCrashReporting11(View v) throws Exception {
        recursiveDeepCall(3);
    }

    void deepFunctionCall_1() throws Exception{
        deepFunctionCall_2();
    }

    void deepFunctionCall_2() throws Exception{
        deepFunctionCall_3();
    }

    void deepFunctionCall_3() throws Exception{
        Utility.DeepCall_a();
    }

    void recursiveDeepCall(int depthLeft) {
        if(depthLeft > 0){
            recursiveDeepCall(depthLeft - 1);
        } else {
            Utility.AnotherRecursiveCall(3);
        }
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
