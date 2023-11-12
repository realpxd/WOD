/*
    Service class responsible for managing audio playback in the background.
*/

package com.programmerxd.wod;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

public class AudioService extends Service {
    // MediaPlayer for handling audio playback
    private MediaPlayer mediaPlayer;

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize MediaPlayer with the audio resource
        mediaPlayer = MediaPlayer.create(this, R.raw.ship_wrek_zookeepers_ark_ncs);
        mediaPlayer.setLooping(true); // Set looping to continuously play the audio
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mediaPlayer.start(); // Start the audio playback
        return START_STICKY; // Service will be restarted if it gets terminated
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mediaPlayer.stop(); // Stop the audio playback
        mediaPlayer.release(); // Release resources associated with the MediaPlayer
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // Binding is not supported in this service
    }
}
