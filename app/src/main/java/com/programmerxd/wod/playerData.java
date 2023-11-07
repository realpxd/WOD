package com.programmerxd.wod;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Button;
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

public class playerData extends AppCompatActivity {

    private EditText usernameInput;
    private Button saveUsernameButton;
    private DatabaseReference usersRef;
    private FirebaseAuth firebaseAuth;

//    private static final int PERMISSION_REQ_ID = 22;
    private static final String[] REQUESTED_PERMISSIONS =
            {
                    Manifest.permission.INTERNET
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_data);



        // If the permissions are granted
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_LONG).show();
        }


        // Initialize Firebase database reference
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("users");
        firebaseAuth = FirebaseAuth.getInstance();

        usernameInput = findViewById(R.id.usernameEditText);
        saveUsernameButton = findViewById(R.id.saveUsernameButton);

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(playerData.this, home.class));
            finish(); // Finish this activity if a user is already logged in
        }

        saveUsernameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (!isNetworkAvailable()) {
                    showToast("No Internet Connection");
                    return;
                }
                MediaPlayer mediaPlayer;
                mediaPlayer = MediaPlayer.create(playerData.this, R.raw.button_clicked);
                mediaPlayer.setLooping(false); // Set looping to continuously play the audio
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    public void onCompletion(MediaPlayer mp) {
                        mediaPlayer.stop();
                        mediaPlayer.release();

                    }
                });


                final String username = usernameInput.getText().toString().trim();

                if (!username.isEmpty()) {
                    usersRef.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                showToast("Username already exists, please choose a different one.");
                            } else {
                                // Anonymous sign-up with username
                                firebaseAuth.signInAnonymously().addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                                        if (currentUser != null) {
                                            String uniqueUserId = currentUser.getUid();

                                            // Store each username separately using push()
                                            usersRef.child(uniqueUserId).child("username").setValue(username);

                                            showToast("User signed up with the username: " + username);
                                            startActivity(new Intent(playerData.this, home.class));
                                            finish(); // Finish this activity after sign-up
                                        } else {
                                            showToast("Failed to sign up. Please try again.");
                                        }
                                    } else {
                                        showToast("Failed to sign up. Please try again.");
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            showToast("Database error: " + databaseError.getMessage());
                        }
                    });
                } else {
                    showToast("Please enter a username");
                }
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    public void showToast(String message) {
        Toast.makeText(playerData.this, message, Toast.LENGTH_SHORT).show();
    }
}
