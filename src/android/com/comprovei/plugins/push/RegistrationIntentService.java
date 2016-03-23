package com.comprovei.plugins.push;

import android.content.Context;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

public class RegistrationIntentService extends IntentService implements PushConstants {
    public static final String TAG = RegistrationIntentService.class.getSimpleName();

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(COM_COMPROVEI_PLUGINS_PUSH, Context.MODE_PRIVATE);

        try {
            InstanceID instanceID = InstanceID.getInstance(this);
            String senderID = sharedPreferences.getString(SENDER_ID, "");
            String token = instanceID.getToken(senderID,
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            Log.i(TAG, "Nem GCM Registration Token: " + token);

            // save new token
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(REGISTRATION_ID, token);
            editor.commit();

        } catch (Exception e) {
            Log.d(TAG, "Failed to complete token refresh", e);
        }
    }
}