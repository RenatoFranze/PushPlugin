package com.comprovei.plugins.push;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.iid.InstanceID;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

public class PushPlugin extends CordovaPlugin implements PushConstants{
    public static final String TAG = PushPlugin.class.getSimpleName();

    private static CallbackContext pushContext;
    private static CordovaWebView gWebView;
    private static Bundle gCachedExtras = null;
    private static boolean gForeground = false;

    /**
     * Gets application context from cordova
     * @return App context
     */
    private Context getApplicationContext(){
        return this.cordova.getActivity().getApplicationContext();
    }

    @Override
    public boolean execute(final String action, final JSONArray data, final CallbackContext callbackContext){
        Log.i(TAG, "Execute action: " + action);
        gWebView = this.webView;

        if(INIT.equals(action)){
            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    pushContext = callbackContext;
                    JSONObject json = null;

                    Log.i(TAG, "Execute data: " + data);
                    SharedPreferences sharedPrefs = getApplicationContext().getSharedPreferences(COM_COMPROVEI_PLUGINS_PUSH, Context.MODE_PRIVATE);
                    String token = null;
                    String senderID = null;

                    try{
                        json = data.getJSONObject(0).getJSONObject(ANDROID);
                        Log.i(TAG, "Execute parsed json: " + json.toString());
                        senderID = json.getString(SENDER_ID);

                        String saveSenderID = sharedPrefs.getString(SENDER_ID, "");
                        String saveRegID = sharedPrefs.getString(REGISTRATION_ID, "");

                        if(saveRegID.equals("")){
                            Log.i(TAG, "First time run");
                            token = InstanceID.getInstance(getApplicationContext()).getToken(senderID, GCM);
                        }
                        else if(!saveSenderID.equals(senderID)){
                            Log.i(TAG, "New sender ID");
                            token = InstanceID.getInstance(getApplicationContext()).getToken(senderID, GCM);
                        }
                        else{
                            Log.i(TAG, "Using previous token");
                            token = sharedPrefs.getString(REGISTRATION_ID, "");
                        }

                        if(!token.equals("")){
                            JSONObject jo = new JSONObject().put(REGISTRATION_ID, token);
                            Log.i(TAG, "onRegistered: " + jo.toString());
                            PushPlugin.sendEvent( jo );
                        }else{
                            callbackContext.error("Empty registration ID received from GCM");
                            return;
                        }

                        if(json != null){
                            SharedPreferences.Editor editor = sharedPrefs.edit();
                            editor.putBoolean(SOUND, json.optBoolean(SOUND, true));
                            editor.putBoolean(VIBRATE, json.optBoolean(VIBRATE, true));
                            editor.putBoolean(CLEAR_NOTIFICATIONS, json.optBoolean(CLEAR_NOTIFICATIONS, true));
                            editor.putString(SENDER_ID, senderID);
                            editor.putString(REGISTRATION_ID, token);
                            editor.commit();
                        }

                        if (gCachedExtras != null) {
                            Log.v(TAG, "Sending cached extras");
                            sendExtras(gCachedExtras);
                            gCachedExtras = null;
                        }
                    }catch(JSONException e){
                        Log.e(TAG, "Execute JSON exception: " + e.getMessage());
                        callbackContext.error(e.getMessage());

                    }catch(IOException e){
                        Log.e(TAG, "Execute IO exception: " + e.getMessage());
                        callbackContext.error(e.getMessage());
                    }
                }
            });
        }else if (UNREGISTER.equals(action)){
            cordova.getThreadPool().execute(new Runnable() {
                public void run(){
                    try{
                        SharedPreferences sharedPrefs = getApplicationContext().getSharedPreferences(COM_COMPROVEI_PLUGINS_PUSH, Context.MODE_PRIVATE);
                        String token = sharedPrefs.getString(REGISTRATION_ID, "");
                        InstanceID.getInstance(getApplicationContext()).deleteInstanceID();
                        Log.e(TAG, "UNREGISTER");

                        SharedPreferences.Editor editor = sharedPrefs.edit();
                        editor.remove(SENDER_ID);
                        editor.remove(REGISTRATION_ID);
                        editor.commit();

                        callbackContext.success();
                    }catch (IOException e){
                        Log.e(TAG, "Execute IO exception: " + e.getMessage());
                        callbackContext.error(e.getMessage());
                    }
                }
            });
        }

        return true;
    }

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        gForeground = true;
    }

    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);
        gForeground = true;
    }

    @Override
    public void onPause(boolean multitasking) {
        super.onPause(multitasking);
        gForeground = false;

        SharedPreferences prefs = getApplicationContext().getSharedPreferences(COM_COMPROVEI_PLUGINS_PUSH, Context.MODE_PRIVATE);
        if (prefs.getBoolean(CLEAR_NOTIFICATIONS, true)) {
            final NotificationManager notificationManager = (NotificationManager) cordova.getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancelAll();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        gForeground = false;
        gWebView = null;
    }

    public static void sendEvent(JSONObject _json) {
        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, _json);
        pluginResult.setKeepCallback(true);
        if (pushContext != null) {
            pushContext.sendPluginResult(pluginResult);
        }
    }

    private static JSONObject convertBundleToJson(Bundle extras) {
        Log.i(TAG, "convert extras to json");
        try {
            JSONObject json = new JSONObject();
            JSONObject additionalData = new JSONObject();

            // Add any keys that need to be in top level json to this set
            HashSet<String> jsonKeySet = new HashSet();
            Collections.addAll(jsonKeySet, TITLE, MESSAGE, INTERNAL);

            Iterator<String> it = extras.keySet().iterator();
            while (it.hasNext()) {
                String key = it.next();
                Object value = extras.get(key);

                if (jsonKeySet.contains(key)) {
                    json.put(key, value);
                }
            } // while

            Log.i(TAG, "extrasToJSON: " + json.toString());
            return json;
        }
        catch( JSONException e) {
            Log.e(TAG, "extrasToJSON: JSON exception " + e.getMessage());
        }
        return null;
    }

    public static void sendExtras(Bundle extras) {
        if (extras != null) {
            if (gWebView != null) {
                sendEvent(convertBundleToJson(extras));
            } else {
                Log.i(TAG, "sendExtras: caching extras to send at a later time.");
                gCachedExtras = extras;
            }
        }
    }

    public static boolean isInForeground() {
        return gForeground;
    }

    public static boolean isActive() {
        return gWebView != null;
    }
}