package com.programmerxd.wod;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;


public class home extends AppCompatActivity {

    private static final int PERMISSION_REQ_ID = 22;
    private static final String[] REQUESTED_PERMISSIONS =
            {
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.INTERNET
            };

    private FirebaseAuth firebaseAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        // If all the permissions are granted
        if (!checkSelfPermission() || !isNetworkAvailable()) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, PERMISSION_REQ_ID);
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_LONG).show();
            TextView textView5 = findViewById(R.id.textView5);
            textView5.setText("NO INTERNET CONNECTION !");
            textView5.setTextColor(getResources().getColor(R.color.red));
        }


        firebaseAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            fetchAndSetUsername(currentUser.getUid());
        }
    }


    private boolean checkSelfPermission()
    {
        return ContextCompat.checkSelfPermission(this, REQUESTED_PERMISSIONS[0]) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    // Fetches username from database and sets it in the TextView
    private void fetchAndSetUsername(String userId) {
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                        String username = dataSnapshot.child("username").getValue(String.class);
                        TextView textView5 = findViewById(R.id.textView5);
                        textView5.setText(username + "'s Home ");

                } catch (Exception e) {
                    e.printStackTrace();
                    // Handle exceptions here
                    Toast.makeText(home.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle any errors while fetching data
            }
        });
    }

    // Method to start the game
    public void startMusic(View view) {

        Intent serviceIntent = new Intent(this, AudioService.class);


        startService(serviceIntent); // Start the audio service
    }
    public void startGame(View view) {

        if (!isNetworkAvailable()) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, PERMISSION_REQ_ID);
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show();
            TextView textView5 = findViewById(R.id.textView5);
            textView5.setText("NO INTERNET CONNECTION !");
            textView5.setTextColor(getResources().getColor(R.color.red));
            return;
        }

        Intent serviceIntent = new Intent(this, AudioService.class);


        startService(serviceIntent); // Start the audio service

        MediaPlayer mediaPlayer;
        mediaPlayer = MediaPlayer.create(home.this, R.raw.button_clicked);
        mediaPlayer.setLooping(false); // Set looping to continuously play the audio
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                mediaPlayer.stop();
                mediaPlayer.release();

            }
        });

        Intent intent = new Intent(this, rooms.class);

        startActivity(intent);
    }
    public void hostGame(View view) {

        if (!isNetworkAvailable()) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, PERMISSION_REQ_ID);
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show();
            TextView textView5 = findViewById(R.id.textView5);
            textView5.setText("NO INTERNET CONNECTION !");
            textView5.setTextColor(getResources().getColor(R.color.red));

            return;
        }

        Intent serviceIntent = new Intent(this, AudioService.class);


        startService(serviceIntent); // Start the audio service

        MediaPlayer mediaPlayer;
        mediaPlayer = MediaPlayer.create(home.this, R.raw.button_clicked);
        mediaPlayer.setLooping(false); // Set looping to continuously play the audio
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                mediaPlayer.stop();
                mediaPlayer.release();

            }
        });
        int numberOfTokens = 5; // Define the number of tokens needed

        // Call the generateTokens method to get multiple tokens
        String[] tokens = App.generateTokens(this, numberOfTokens);

        DatabaseReference roomRef = FirebaseDatabase.getInstance().getReference("room1rs");
        DatabaseReference verifyRoomsRef = FirebaseDatabase.getInstance().getReference("verifyAvailableRooms");

        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i] != null) {
                // Store the generated tokens in "room1rs" reference
                roomRef.child("rs" + (i + 1)).setValue(tokens[i]);
            }
        }

        // Retrieve the current user's UID
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            // Set the user's UID as a value in "verifyAvailableRooms" reference
            verifyRoomsRef.child("room1").setValue(uid);

        }

        // Proceed to the next activity or implement any other logic here
        Intent intent = new Intent(this, rooms.class);
        startActivity(intent);
    }



    @Override
    public void onBackPressed() {
        // Do nothing to disable the back button
        MediaPlayer mediaPlayer;
        mediaPlayer = MediaPlayer.create(home.this, R.raw.button_clicked);
        mediaPlayer.setLooping(false); // Set looping to continuously play the audio
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                mediaPlayer.stop();
                mediaPlayer.release();

            }

        });
    }
}
