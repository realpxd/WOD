package com.programmerxd.wod;


import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;


public class AudioService extends Service {
    private MediaPlayer mediaPlayer;

    @Override
    public void onCreate() {
        super.onCreate();

//        Intent intent = getIntentOld(resId);
//        if (intent != null && intent.hasExtra("resIds")) {
//            resId = intent.getStringExtra("resIds");
//        }

        mediaPlayer = MediaPlayer.create(this, R.raw.ship_wrek_zookeepers_ark_ncs);
        mediaPlayer.setLooping(true); // Set looping to continuously play the audio
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mediaPlayer.start(); // Start the audio
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop(); // Stop the audio
        mediaPlayer.release();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
