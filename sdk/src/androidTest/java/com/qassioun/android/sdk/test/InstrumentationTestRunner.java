/*
Copyright (c) Kassioun 2014-"$(date +%Y)" Walid Elhadi - All rights reserved.

*/
package com.qassioun.android.sdk.test;

import android.os.Bundle;
import androidx.test.runner.AndroidJUnitRunner;

public class InstrumentationTestRunner extends AndroidJUnitRunner {
    // TODO: since Android 4.3 dexmaker requires this workaround, can be removed once dexmaker fixes this issue http://code.google.com/p/dexmaker/issues/detail?id=2
    @Override
    public void onCreate(Bundle arguments) {
        super.onCreate(arguments);
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
    }
}
