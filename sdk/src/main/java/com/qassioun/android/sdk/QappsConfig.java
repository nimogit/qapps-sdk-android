package com.qassioun.android.sdk;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

public class QappsConfig {

    /**
     * Internal fields for testing
     */

    protected QappsStore qappsStore = null;

    protected boolean checkForNativeCrashDumps = true;

    /**
     * Android context.
     * Mandatory field.
     */
    protected Context context = null;

    /**
     * URL of the Qapps server to submit data to.
     * Mandatory field.
     */
    protected String serverURL = null;

    /**
     * app key for the application being tracked; find in the Qapps Dashboard under Management &gt; Applications.
     * Mandatory field.
     */
    protected String appKey = null;

    /**
     * unique ID for the device the app is running on; note that null in deviceID means that Qapps will fall back to OpenUDID, then, if it's not available, to Google Advertising ID.
     */
    protected String deviceID = null;

    /**
     * enum value specifying which device ID generation strategy Qapps should use: OpenUDID or Google Advertising ID.
     */
    protected DeviceId.Type idMode = null;

    /**
     * sets the limit after how many sessions, for each apps version, the automatic star rating dialog is shown.
     */
    protected int starRatingLimit = 5;

    /**
     * the callback function that will be called from the automatic star rating dialog.
     */
    protected QappsStarRating.RatingCallback starRatingCallback = null;

    /**
     * the shown title text for the star rating dialogs.
     */
    protected String starRatingTextTitle = null;

    /**
     * the shown message text for the star rating dialogs.
     */
    protected String starRatingTextMessage = null;

    /**
     * the shown dismiss button text for the shown star rating dialogs.
     */
    protected String starRatingTextDismiss = null;

    protected boolean loggingEnabled = false;

    protected boolean enableUnhandledCrashReporting = false;

    protected boolean enableViewTracking = false;

    protected boolean autoTrackingUseShortName = false;

    protected Class[] autoTrackingExceptions = null;

    protected Map<String, Object> automaticViewSegmentation = null;

    protected Map<String, String> customNetworkRequestHeaders = null;

    protected boolean pushIntentAddMetadata = false;

    protected boolean enableRemoteConfigAutomaticDownload = false;
    RemoteConfig.RemoteConfigCallback remoteConfigCallback = null;

    protected boolean shouldRequireConsent = false;
    protected String[] enabledFeatureNames = null;

    boolean httpPostForced = false;

    protected boolean temporaryDeviceIdEnabled = false;

    protected String[] crashRegexFilters = null;

    protected String tamperingProtectionSalt = null;

    protected boolean trackOrientationChange = false;

    public QappsConfig(){ }

    public QappsConfig(Context context, String appKey, String serverURL){
        setContext(context);
        setAppKey(appKey);
        setServerURL(serverURL);
    }

    /**
     * Android context.
     * Mandatory field.
     */
    public QappsConfig setContext(Context context){
        this.context = context;
        return this;
    }

    /**
     * URL of the Qapps server to submit data to.
     * Mandatory field.
     */
    public QappsConfig setServerURL(String serverURL){
        this.serverURL = serverURL;
        return this;
    }

    /**
     * app key for the application being tracked; find in the Qapps Dashboard under Management &gt; Applications.
     * Mandatory field.
     */
    public QappsConfig setAppKey(String appKey){
        this.appKey = appKey;
        return this;
    }

    /**
     * unique ID for the device the app is running on; note that null in deviceID means that Qapps will fall back to OpenUDID, then, if it's not available, to Google Advertising ID.
     */
    public QappsConfig setDeviceId(String deviceID){
        this.deviceID = deviceID;
        return this;
    }

    /**
     * enum value specifying which device ID generation strategy Qapps should use: OpenUDID or Google Advertising ID.
     */
    public QappsConfig setIdMode(DeviceId.Type idMode){
        this.idMode = idMode;
        return this;
    }

    /**
     * sets the limit after how many sessions, for each apps version, the automatic star rating dialog is shown.
     */
    public QappsConfig setStarRatingLimit(int starRatingLimit){
        this.starRatingLimit = starRatingLimit;
        return this;
    }

    /**
     * the callback function that will be called from the automatic star rating dialog.
     */
    public QappsConfig setStarRatingCallback(QappsStarRating.RatingCallback starRatingCallback){
        this.starRatingCallback = starRatingCallback;
        return this;
    }

    /**
     * the shown title text for the star rating dialogs.
     */
    public QappsConfig setStarRatingTextTitle(String starRatingTextTitle){
        this.starRatingTextTitle = starRatingTextTitle;
        return this;
    }

    /**
     * the shown message text for the star rating dialogs.
     */
    public QappsConfig setStarRatingTextMessage(String starRatingTextMessage){
        this.starRatingTextMessage = starRatingTextMessage;
        return this;
    }

    /**
     * the shown dismiss button text for the shown star rating dialogs.
     */
    public QappsConfig setStarRatingTextDismiss(String starRatingTextDismiss){
        this.starRatingTextDismiss = starRatingTextDismiss;
        return this;
    }

    /**
     * Set to true of you want to enable qapps internal debugging logs
     * @param enabled
     */
    public QappsConfig setLoggingEnabled(boolean enabled){
        this.loggingEnabled = enabled;
        return this;
    }

    public QappsConfig enableCrashReporting(){
        this.enableUnhandledCrashReporting = true;
        return this;
    }

    public QappsConfig setViewTracking(boolean enable){
        this.enableViewTracking = enable;
        return this;
    }

    public QappsConfig setAutoTrackingUseShortName(boolean enable){
        this.autoTrackingUseShortName = enable;
        return this;
    }

    public QappsConfig setAutomaticViewSegmentation(Map<String, Object> segmentation){
        automaticViewSegmentation = segmentation;
        return this;
    }

    /**
     * Set which activities should be excluded from automatic view tracking
     * @param exceptions activities which should be ignored
     * @return
     */
    public QappsConfig setAutoTrackingExceptions(Class[] exceptions){
        if(exceptions != null){
            for(int a = 0 ; a< exceptions.length ; a++){
                if(exceptions[a] == null){
                    throw new IllegalArgumentException("setAutoTrackingExceptions() does not accept 'null' activities");
                }
            }
        }

        autoTrackingExceptions = exceptions;
        return this;
    }

    public QappsConfig addCustomNetworkRequestHeaders(Map<String, String> customHeaderValues){
        this.customNetworkRequestHeaders = customHeaderValues;
        return this;
    }

    public QappsConfig setPushIntentAddMetadata(boolean enable){
        pushIntentAddMetadata = enable;
        return this;
    }

    public QappsConfig setRemoteConfigAutomaticDownload(boolean enabled, RemoteConfig.RemoteConfigCallback callback){
        enableRemoteConfigAutomaticDownload = enabled;
        remoteConfigCallback = callback;
        return this;
    }

    /**
     * Set if consent should be required
     * @param shouldRequireConsent
     * @return
     */
    public QappsConfig setRequiresConsent(boolean shouldRequireConsent){
        this.shouldRequireConsent = shouldRequireConsent;
        return this;
    }

    /**
     * Sets which features are enabled in case consent is required
     * @param featureNames
     * @return
     */
    public QappsConfig setConsentEnabled(String[] featureNames){
        enabledFeatureNames = featureNames;
        return this;
    }

    public QappsConfig setHttpPostForced(boolean isForced){
        httpPostForced = isForced;
        return this;
    }

    public QappsConfig enableTemporaryDeviceIdMode(){
        temporaryDeviceIdEnabled = true;
        return this;
    }

    public QappsConfig setCrashFilters(String [] regexFilters){
        crashRegexFilters = regexFilters;
        return this;
    }

    public QappsConfig setParameterTamperingProtectionSalt(String salt){
        tamperingProtectionSalt = salt;
        return this;
    }

    public QappsConfig setTrackOrientationChanges(boolean shouldTrackOrientation){
        trackOrientationChange = shouldTrackOrientation;
        return this;
    }
    protected QappsConfig checkForNativeCrashDumps(boolean checkForDumps){
        checkForNativeCrashDumps = checkForDumps;
        return this;
    }

    protected QappsConfig setQappsStore(QappsStore store){
        qappsStore = store;
        return this;
    }
}
