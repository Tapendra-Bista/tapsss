

package com.tapsss;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;

import android.os.Build;
import android.os.IBinder;
import android.util.Log;


import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;


public class RecordingService extends Service {

    private static final String CHANNEL_ID = "RecordingServiceChannel";
    private static final int NOTIFICATION_ID = 1; // Use a constant for the notification ID

    private static final String TAG = "RecordingService";
    private boolean isRecording = false;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        Log.d(TAG, "Service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (action != null) {
            switch (action) {
                case "ACTION_START_RECORDING":
                    startRecording();
                    break;
                case "ACTION_STOP_RECORDING":
                    stopRecording();
                    break;
            }
        }
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRecording();
        Log.d(TAG, "Service destroyed");
    }

    private void startRecording() {
        Log.d(TAG, "startRecording: Initiating video recording from recording service.");
        if (!isRecording) {
            isRecording = true;
            Log.d(TAG, "Recording started from recording service.");

            // Start foreground service
            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Recording in progress")
                    .setContentText("Recording video")
                    .setSmallIcon(R.drawable.unknown_icon3)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .build();
            startForeground(NOTIFICATION_ID, notification);
            Log.d(TAG, "Foreground service started");

            Intent intent = new Intent("RECORDING_STATE_CHANGED");
            intent.putExtra("isRecording", true);
            sendBroadcast(intent);
        } else {
            Log.d(TAG, "Recording already in progress");
        }
    }

    private void stopRecording() {
        Log.d(TAG, "stopRecording: Attempting to stop recording from recording service.");
        if (isRecording) {
            isRecording = false;
            Log.d(TAG, "Recording stopped from recording service");

            Intent intent = new Intent("RECORDING_STATE_CHANGED");
            intent.putExtra("isRecording", false);
            sendBroadcast(intent);

            stopForeground(STOP_FOREGROUND_REMOVE); // Remove notification
            Log.d(TAG, "Foreground service stopped, notification should be removed");

            // Cancel the notification explicitly
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.cancel(NOTIFICATION_ID);
                Log.d(TAG, "Notification with ID " + NOTIFICATION_ID + " canceled");
            } else {
                Log.e(TAG, "NotificationManager is null, unable to cancel notification");
            }

            stopSelf();  // Stop the service
            Log.d(TAG, "Service stopped");
        } else {
            Log.d(TAG, "No recording to stop from recording service");
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Recording Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
                Log.d(TAG, "Notification channel created");
            } else {
                Log.e(TAG, "NotificationManager is null, unable to create notification channel");
            }
        }
    }
}