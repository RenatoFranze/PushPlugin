package com.comprovei.plugins.push;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

public class PushHandlerActivity extends Activity implements PushConstants {
    private static String TAG = PushHandlerActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        GCMIService gcm = new GCMIService();
        int notId = getIntent().getExtras().getInt(NOT_ID, 0);
        gcm.setNotification(notId, "");
        super.onCreate(savedInstanceState);

        boolean foreground = getIntent().getExtras().getBoolean("foreground", true);
        boolean isPushPluginActive = PushPlugin.isActive();
        processPushBundle(isPushPluginActive);
        finish();

        if (!isPushPluginActive && foreground) {
            Log.d(TAG, "Reloading main activity");
            forceMainActivityReload();
        } else {
            Log.d(TAG, "Don't want main acitivity");
        }
    }

    private void processPushBundle(boolean isPushPluginActive) {
        Bundle extras = getIntent().getExtras();

        if (extras != null)	{
            Bundle originalExtras = extras.getBundle(PUSH_BUNDLE);
            boolean processed = originalExtras.getBoolean(PROCESSED, false);
            if(!processed) {
                PushPlugin.sendExtras(originalExtras);
            }else{
                Log.d(TAG, "Notification already processed!");
            }
        }
    }

    private void forceMainActivityReload() {
        PackageManager pm = getPackageManager();
        Intent launchIntent = pm.getLaunchIntentForPackage(getApplicationContext().getPackageName());
        startActivity(launchIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        final NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }
}