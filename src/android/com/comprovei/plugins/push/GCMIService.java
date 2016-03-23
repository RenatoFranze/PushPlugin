package com.comprovei.plugins.push;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.Boolean;
import java.lang.Integer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

public class GCMIService extends GcmListenerService implements PushConstants{
    private static final String TAG = GCMIService.class.getSimpleName();
    private static HashMap<Integer, ArrayList<String>> messageMap = new HashMap<Integer, ArrayList<String>>();

    @Override
    public void onMessageReceived(String from, Bundle data) {
        Log.i(TAG, "Message from: " + from);
        if(data != null){
            SharedPreferences prefs = getApplicationContext().getSharedPreferences(COM_COMPROVEI_PLUGINS_PUSH, Context.MODE_PRIVATE);

            if(PushPlugin.isInForeground()){
                Log.i(TAG, "App is in foreground");
                data.putBoolean(FOREGROUND, true);
                PushPlugin.sendExtras(data);
            }else{
                Log.i(TAG, "App is in background");
                data.putBoolean(FOREGROUND, false);

                boolean bgProcess = Boolean.parseBoolean(data.getString(BACKGROUND_PROCESS));
                if(bgProcess) {
                    Log.i(TAG, "Background process: " + bgProcess);
                    data.putBoolean(PROCESSED, true);
                    PushPlugin.sendExtras(data);
                }

                sendNotification(getApplicationContext(), data);
            }
        }
    }

    public void sendNotification(Context context, Bundle extras) {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String appName = getAppName(this);
        String packageName = context.getPackageName();
        Resources resources = context.getResources();
        String message = extras.getString(MESSAGE);
        String title = extras.getString(TITLE);

        int notId = parseInt(NOT_ID, extras);
        Log.i(TAG, "Notification ID:" + notId);
        setNotification(notId, message);

        Intent notificationIntent = new Intent(this, PushHandlerActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        notificationIntent.putExtra(PUSH_BUNDLE, extras);
        notificationIntent.putExtra(NOT_ID, notId);

        int requestCode = new Random().nextInt();
        PendingIntent contentIntent = PendingIntent.getActivity(this, requestCode, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        ArrayList<String> messageList = messageMap.get(notId);
        Integer sizeList = messageList.size();

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setWhen(System.currentTimeMillis())
                        .setContentTitle(title)
                        .setContentText(getUnexpandedContentText(sizeList, message))
                        .setNumber(sizeList)
                        .setStyle(getExpandedNotificationStyle(messageList, sizeList))
                        .setContentIntent(contentIntent)
                        .setAutoCancel(true);

        SharedPreferences prefs = context.getSharedPreferences(PushPlugin.COM_COMPROVEI_PLUGINS_PUSH, Context.MODE_PRIVATE);
        boolean soundOption = prefs.getBoolean(SOUND, true);
        boolean vibrateOption = prefs.getBoolean(VIBRATE, true);
        boolean ongoing = Boolean.parseBoolean(extras.getString(ONGOING, ""));

        if(vibrateOption) setNotificationVibration(mBuilder);

        setNotificationSmallIcon(context, mBuilder);

        if (soundOption) setNotificationSound(context, extras, mBuilder);
        if(ongoing) setNotificationOngoing(mBuilder);

        mNotificationManager.notify(appName, notId, mBuilder.build());
    }

    public void setNotification(int notId, String message){
        ArrayList<String> messageList = messageMap.get(notId);
        if(messageList == null) {
            messageList = new ArrayList<String>();
            messageMap.put(notId, messageList);
        }

        if(message.isEmpty()){
            messageList.clear();
        }else{
            messageList.add(message);
        }
    }

    private String getUnexpandedContentText(Integer numOfNotifications, String message){
        switch (numOfNotifications) {
            case 0:
                return "0 mensagens";
            case 1:
                return message;
            default:
                return numOfNotifications + " mensagens";
        }
    }

    private NotificationCompat.Style getExpandedNotificationStyle(ArrayList<String> messageList, Integer sizeList){
        NotificationCompat.InboxStyle expandedNotificationStyle = new NotificationCompat.InboxStyle();
        expandedNotificationStyle.setSummaryText(getUnexpandedContentText(sizeList, ""));

        for (int i = sizeList - 1; i >= 0; i--) {
            expandedNotificationStyle.addLine(messageList.get(i));
        }
        return expandedNotificationStyle;
    }

    private void setNotificationVibration(NotificationCompat.Builder mBuilder) {
        mBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
    }

    private void setNotificationSound(Context context, Bundle extras, NotificationCompat.Builder mBuilder) {
        mBuilder.setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI);
    }

    private void setNotificationOngoing(NotificationCompat.Builder mBuilder) {
        mBuilder.setOngoing(true);
    }

    private void setNotificationSmallIcon(Context context, NotificationCompat.Builder mBuilder) {
        Integer iconId = context.getApplicationInfo().icon;
        mBuilder.setSmallIcon(iconId);
    }

    public Bitmap getBitmapFromURL(String strURL) {
        try {
            URL url = new URL(strURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getAppName(Context context) {
        CharSequence appName =  context.getPackageManager().getApplicationLabel(context.getApplicationInfo());
        return (String)appName;
    }

    private int parseInt(String value, Bundle extras) {
        int retval = 0;

        try {
            String intValue = extras.getString(value);
            if(value != null) retval = Integer.parseInt(intValue);
        }
        catch(NumberFormatException e) {
            Log.e(TAG, "Number format exception - Error parsing " + value + ": " + e.getMessage());
        }
        catch(Exception e) {
            Log.e(TAG, "Number format exception - Error parsing " + value + ": " + e.getMessage());
        }

        return retval;
    }
}