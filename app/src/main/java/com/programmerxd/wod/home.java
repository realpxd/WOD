package com.programmerxd.wod;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class home extends AppCompatActivity {

    private static final int PERMISSION_REQ_ID = 22;
    private static final String[] REQUESTED_PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.INTERNET
    };

    private FirebaseAuth firebaseAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set the status bar color
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.red));

        // Set the system UI visibility to light status bar
        View decor = getWindow().getDecorView();
        decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if (!checkSelfPermission()) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, PERMISSION_REQ_ID);
        } else if (!isNetworkAvailable()) {
            showToast("No Internet Connection");
            setNoInternetConnection();
        }

        firebaseAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            fetchAndSetUsername(currentUser.getUid());
        }
    }

    private boolean checkSelfPermission() {
        return ContextCompat.checkSelfPermission(this, REQUESTED_PERMISSIONS[0]) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void fetchAndSetUsername(String userId) {
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    String username = dataSnapshot.child("username").getValue(String.class);
                    setTextView5(username);

                } catch (Exception e) {
                    e.printStackTrace();
                    showToast("Error: " + e.getMessage());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle any errors while fetching data
            }
        });
    }

    private void setTextView5(String username) {
        TextView textView5 = findViewById(R.id.textView5);
        textView5.setText(username + "'s Home ");
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void setNoInternetConnection() {
        TextView textView5 = findViewById(R.id.textView5);
        textView5.setText("NO INTERNET CONNECTION !");
        textView5.setTextColor(getResources().getColor(R.color.red));
    }

    public void startMusic(View view) {
        Intent serviceIntent = new Intent(this, AudioService.class);
        startService(serviceIntent);
    }

    public void startGame(View view) {
        if (!isNetworkAvailable()) {
            showToast("No Internet Connection");
            setNoInternetConnection();
            return;
        } else if (!checkSelfPermission()) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, PERMISSION_REQ_ID);
            showToast("In order to play the game , you must grant the mic permission.");
            return;
        }

        Intent serviceIntent = new Intent(this, AudioService.class);
        startService(serviceIntent);

        playButtonClickSound();

        Intent intent = new Intent(this, rooms.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    }

    public void hostGame(View view) {
        if (!isNetworkAvailable()) {
            showToast("No Internet Connection");
            setNoInternetConnection();
            return;
        } else if (!checkSelfPermission()) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, PERMISSION_REQ_ID);
            showToast("In order to play the game , you must grant the mic permission.");
            return;
        }

        Intent serviceIntent = new Intent(this, AudioService.class);
        startService(serviceIntent);

        playButtonClickSound();

        int numberOfTokens = 5;
        String[] tokens = App.generateTokens(this, numberOfTokens);

        DatabaseReference roomRef = FirebaseDatabase.getInstance().getReference("room1rs");
        DatabaseReference verifyRoomsRef = FirebaseDatabase.getInstance().getReference("verifyAvailableRooms");

        storeTokensInDatabase(tokens, roomRef);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            storeUserUidInDatabase(currentUser, verifyRoomsRef);
        }

        verifyRoomsRef.child("isRoomStarted").setValue(false);

        Intent intent = new Intent(this, rooms.class);
        startActivity(intent);
    }

    private void storeTokensInDatabase(String[] tokens, DatabaseReference roomRef) {
        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i] != null) {
                roomRef.child("rs" + (i + 1)).child("key").setValue(tokens[i]);
            }
        }
    }

    private void storeUserUidInDatabase(FirebaseUser currentUser, DatabaseReference verifyRoomsRef) {
        String uid = currentUser.getUid();
        verifyRoomsRef.child("room1").setValue(uid);
    }

    private void playButtonClickSound() {
        MediaPlayer mediaPlayer = MediaPlayer.create(home.this, R.raw.button_clicked);
        mediaPlayer.setLooping(false);
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                mediaPlayer.stop();
                mediaPlayer.release();
            }
        });
    }

    @Override
    public void onBackPressed() {
        playButtonClickSound();
        // Do nothing to disable the back button
    }
}
