package com.qassioun.android.sdknative;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.os.Environment;
import java.io.File;

public class QappsNative {
    private static String TAG = "Qapps";
    private static String qappsNativeCrashFolderPath;

    static boolean loadBreakpadSuccess = false;

    static {
        try {
            System.loadLibrary("qapps_native");
            loadBreakpadSuccess = true;
            Log.d(TAG, "qapps_native library loaded.");
        } catch (Exception e) {
            loadBreakpadSuccess = false;
            Log.e(TAG, "fail to load qapps_native library");
        }
    }

    /**
     * init breakpad
     * @return true: init success  false: init fail
     */
    public static boolean initNative(Context cxt){
        // String basePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        String basePath = cxt.getCacheDir().getAbsolutePath();
        String qappsFolderName = "Qapps";
        String qappsNativeCrashFolderName = "CrashDumps";
        qappsNativeCrashFolderPath = basePath + File.separator + qappsFolderName + File.separator + qappsNativeCrashFolderName;

        File folder = new File(qappsNativeCrashFolderPath);
        if (!folder.exists()) {
            boolean res = folder.mkdirs();
        }
        if (loadBreakpadSuccess) {
            return init(qappsNativeCrashFolderPath) > 0 ;
        }
        return false;
    }

    public static void crash() {
        testCrash();
    }
    private static native int init(String dumpFileDir);
    private static native int testCrash();
}
