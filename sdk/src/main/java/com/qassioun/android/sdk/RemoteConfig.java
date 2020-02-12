package com.qassioun.android.sdk;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLConnection;
import java.util.Iterator;

public class RemoteConfig {

    public interface RemoteConfigCallback {
        /**
         * Called after receiving remote config update result
         * @param error if is null, it means that no errors were encountered
         */
        void callback(String error);
    }

    /**
     * Internal call for updating remote config keys
     * @param keysOnly set if these are the only keys to update
     * @param keysExcept set if these keys should be ignored from the update
     * @param requestShouldBeDelayed this is set to true in case of update after a deviceId change
     * @param callback called after the update is done
     */
    protected static void updateRemoteConfigValues(final Context context, final String[] keysOnly, final String[] keysExcept, final ConnectionQueue connectionQueue_, final boolean requestShouldBeDelayed, final RemoteConfigCallback callback){
        if (Qapps.sharedInstance().isLoggingEnabled()) {
            Log.d(Qapps.TAG, "Updating remote config values, requestShouldBeDelayed:[" + requestShouldBeDelayed + "]");
        }
        String keysInclude = null;
        String keysExclude = null;

        if(keysOnly != null && keysOnly.length > 0){
            //include list takes precedence
            //if there is at least one item, use it
            JSONArray includeArray = new JSONArray();
            for (String key:keysOnly) {
                includeArray.put(key);
            }
            keysInclude = includeArray.toString();
        } else if(keysExcept != null && keysExcept.length > 0){
            //include list was not used, use the exclude list
            JSONArray excludeArray = new JSONArray();
            for(String key:keysExcept){
                excludeArray.put(key);
            }
            keysExclude = excludeArray.toString();
        }

        if(connectionQueue_.getDeviceId().getId() == null){
            //device ID is null, abort
            if (Qapps.sharedInstance().isLoggingEnabled()) {
                Log.d(Qapps.TAG, "RemoteConfig value update was aborted, deviceID is null");
            }

            if(callback != null){
                callback.callback("Can't complete call, device ID is null");
            }

            return;
        }

        if(connectionQueue_.getDeviceId().temporaryIdModeEnabled() || connectionQueue_.queueContainsTemporaryIdItems()){
            //temporary id mode enabled, abort
            if (Qapps.sharedInstance().isLoggingEnabled()) {
                Log.d(Qapps.TAG, "RemoteConfig value update was aborted, temporary device ID mode is set");
            }

            if(callback != null){
                callback.callback("Can't complete call, temporary device ID is set");
            }

            return;
        }

        ConnectionProcessor cp = connectionQueue_.createConnectionProcessor();
        URLConnection urlConnection;
        String requestData = connectionQueue_.prepareRemoteConfigRequest(keysInclude, keysExclude);
        if (Qapps.sharedInstance().isLoggingEnabled()) {
            Log.d(Qapps.TAG, "RemoteConfig requestData:[" + requestData + "]");
        }

        try {
            urlConnection = cp.urlConnectionForServerRequest(requestData, "/o/sdk?");
        } catch (IOException e) {
            if (Qapps.sharedInstance().isLoggingEnabled()) {
                Log.e(Qapps.TAG, "IOException while preparing remote config update request :[" + e.toString() + "]");
            }

            if(callback != null){
                callback.callback("Encountered problem while trying to reach the server");
            }

            return;
        }

        (new QappsStarRating.ImmediateRequestMaker()).execute(urlConnection, requestShouldBeDelayed, new QappsStarRating.InternalFeedbackRatingCallback() {
            @Override
            public void callback(JSONObject checkResponse) {
                if (Qapps.sharedInstance().isLoggingEnabled()) {
                    Log.d(Qapps.TAG, "Processing remote config received response, received response is null:[" + (checkResponse == null) + "]");
                }
                if(checkResponse == null) {
                    if(callback != null){
                        callback.callback("Encountered problem while trying to reach the server, possibly no internet connection");
                    }
                    return;
                }

                //merge the new values into the current ones
                RemoteConfigValueStore rcvs = loadConfig(context);
                if(keysExcept == null && keysOnly == null){
                    //in case of full updates, clear old values
                    rcvs.values = new JSONObject();
                }
                rcvs.mergeValues(checkResponse);

                if (Qapps.sharedInstance().isLoggingEnabled()) {
                    Log.d(Qapps.TAG, "Finished remote config processing, starting saving");
                }

                saveConfig(context, rcvs);

                if (Qapps.sharedInstance().isLoggingEnabled()) {
                    Log.d(Qapps.TAG, "Finished remote config saving");
                }

                if(callback != null){
                    callback.callback(null);
                }
            }
        });
    }

    protected static Object getValue(String key, Context context){
        RemoteConfigValueStore rcvs = loadConfig(context);
        return rcvs.getValue(key);
    }


    protected static void saveConfig(Context context, RemoteConfigValueStore rcvs){
        QappsStore cs = new QappsStore(context);
        cs.setRemoteConfigValues(rcvs.dataToString());
    }

    protected static RemoteConfigValueStore loadConfig(Context context){
        QappsStore cs = new QappsStore(context);
        String rcvsString = cs.getRemoteConfigValues();
        //noinspection UnnecessaryLocalVariable
        RemoteConfigValueStore rcvs = RemoteConfigValueStore.dataFromString(rcvsString);
        return rcvs;
    }

    protected static void clearValueStore(Context context){
        QappsStore cs = new QappsStore(context);
        cs.setRemoteConfigValues("");
    }

    protected static class RemoteConfigValueStore {
        public JSONObject values = new JSONObject();

        //add new values to the current storage
        public void mergeValues(JSONObject newValues){
            if(newValues == null) {return;}

            Iterator<String> iter = newValues.keys();
            while (iter.hasNext()) {
                String key = iter.next();
                try {
                    Object value = newValues.get(key);
                    values.put(key, value);
                } catch (Exception e) {
                    if (Qapps.sharedInstance().isLoggingEnabled()) {
                        Log.e(Qapps.TAG, "Failed merging new remote config values");
                    }
                }
            }
        }

        private RemoteConfigValueStore(JSONObject values){
            this.values = values;
        }

        public Object getValue(String key){
            return values.opt(key);
        }

        public static RemoteConfigValueStore dataFromString(String storageString){
            if(storageString == null || storageString.isEmpty()){
                return new RemoteConfigValueStore(new JSONObject());
            }

            JSONObject values;
            try {
                values = new JSONObject(storageString);
            } catch (JSONException e) {
                if (Qapps.sharedInstance().isLoggingEnabled()) {
                    Log.e(Qapps.TAG, "Couldn't decode RemoteConfigValueStore successfully: " + e.toString());
                }
                values = new JSONObject();
            }
            return new RemoteConfigValueStore(values);
        }

        public String dataToString(){
            return values.toString();
        }
    }
}
