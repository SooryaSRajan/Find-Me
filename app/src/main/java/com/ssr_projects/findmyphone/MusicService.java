package com.ssr_projects.findmyphone;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;
import java.io.IOException;
import java.util.Objects;

public class MusicService extends Service {
    MediaPlayer mp;

    int mediaStreamVolume;

    private static final String CHANNEL_ID = "23219FindMyPhone";
    private static final String CHANNEL_NAME = "com.ssr.FindMyPhone";

    CameraManager cameraManager;
    String mCameraId;
    boolean turnOnFlash = true;

    private Handler handler = new Handler();

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            turnFlashOn(turnOnFlash);

            turnOnFlash = !turnOnFlash;
            handler.postDelayed(runnable, 1000);
        }
    };


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(cameraManager == null)
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        try {
            mCameraId = cameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }


        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        startForeground(1, startForeground());
        if (Objects.equals(intent.getAction(), "ACTION_PLAY_AUDIO")) {
            if(sharedPreferences.getBoolean("audio_enable_disable", true))
                enableAudio(this);
            if(sharedPreferences.getBoolean("torch", false))
                controlFlashLightLoop(true);
        }

        else if(Objects.equals(intent.getAction(), "ACTION_STOP_AUDIO")){
            ConstantClass.isServiceRunning = false;
            stopForeground(true);

            if(sharedPreferences.getBoolean("audio_enable_disable", true))
                disableAudio(this);

            controlFlashLightLoop(false);
            stopSelf();
        }

        return START_NOT_STICKY;
    }

    private Notification startForeground() {

        Intent stopIntent = new Intent(this, MusicService.class);
        stopIntent.setAction("ACTION_STOP_AUDIO");
        PendingIntent stopPendingIntent = PendingIntent.getService(this, 0, stopIntent, 0);

        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(channel);

        final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                getApplicationContext(), CHANNEL_ID);

        Notification notification;
        notification = mBuilder.setTicker(getString(R.string.app_name)).setWhen(0)
                .setOngoing(true)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Find my phone protocol running in background, click \"Stop Protocol\" to dismiss")
                .setSmallIcon(R.mipmap.ic_launcher)
                .addAction(R.mipmap.ic_launcher, "Stop Protocol", stopPendingIntent)
                .setShowWhen(true)
                .build();

        return notification;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mp != null){
            if(mp.isPlaying()){
                mp.release();
            }
        }
    }

    public void enableAudio(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        mediaStreamVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, sharedPreferences.getInt("volume", max), 0); //Change to max later

            Log.e(context.getClass().getName(), "adjustAudio: enabled");

            String TAG = getClass().getName();

            if(sharedPreferences.getBoolean("custom_ringtone", false)){
                Log.d(TAG, "enableAudio: Enabled" );
                String uri = sharedPreferences.getString("audio_url", null);
                Log.d(getClass().getName(), "enableAudio: " + uri);
                if(uri!= null){
                    try {
                        Log.d(TAG, "enableAudio: Prepared " + uri );
                        mp = new MediaPlayer();
                        mp.setDataSource(uri);
                        mp.prepare();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.d(TAG, "enableAudio: Failed " + e );
                        mp = MediaPlayer.create(context, R.raw.standard_5);
                    }
                    Log.d(TAG, "enableAudio: Playing " );
                    mp.start();
                    mp.setLooping(true);
                }
                else{
                    Log.d(TAG, "enableAudio: Uri Empty" );
                    mp = MediaPlayer.create(context, R.raw.standard_5);
                    mp.start();
                    mp.setLooping(true);
                }
            }
            else{
                Log.d(TAG, "enableAudio: Disabled" );
                mp = MediaPlayer.create(context, R.raw.standard_5);
                mp.start();
                mp.setLooping(true);
            }

    }

    public void disableAudio(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
             audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mediaStreamVolume, 0);

        Log.e(context.getClass().getName(), "adjustAudio: disabled");
    }

    void turnFlashOn(boolean turnOnFlash) {
        if (getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            try {
                cameraManager.setTorchMode(mCameraId, turnOnFlash);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private void controlFlashLightLoop(boolean turnOnFlash) {

        if (turnOnFlash) {
            handler.post(runnable);
        } else {
            handler.removeCallbacks(runnable);
            turnFlashOn(false);
        }

    }
}