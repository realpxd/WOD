package com.programmerxd.wod;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * This activity allows the user to enter a username and sign up for the application.
 */
public class playerData extends AppCompatActivity {

    private static final String[] REQUESTED_PERMISSIONS = {Manifest.permission.INTERNET};

    private EditText usernameInput;
    private Button saveUsernameButton;
    private DatabaseReference usersRef;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Set the status bar color
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.red));

        // Set the system UI visibility to light status bar
        View decor = getWindow().getDecorView();
        decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_data);

        // Check for internet connectivity
        if (!isNetworkAvailable()) {
            showToast("No Internet Connection");
        }

        // Initialize Firebase database reference and authentication
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("users");
        firebaseAuth = FirebaseAuth.getInstance();

        // Check if the user is already logged in
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            // If logged in, proceed to the home activity
            startActivity(new Intent(playerData.this, home.class));
            overridePendingTransition(R.anim.fadein, R.anim.fadeout);
            finish(); // Finish this activity to prevent going back
        }

        // Initialize UI components
        usernameInput = findViewById(R.id.usernameEditText);
        saveUsernameButton = findViewById(R.id.saveUsernameButton);


        // Add a TextWatcher to enforce restrictions on the input
        usernameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (!isValidInput(charSequence.toString())) {
                    // If input is invalid, show a toast and remove the last entered character
                    showToast("Invalid input. Only letters are allowed.");
                    usernameInput.setText(charSequence.subSequence(0, charSequence.length() - 1));
                    usernameInput.setSelection(usernameInput.length());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });


        // Set click listener for the save username button
        saveUsernameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check for internet connectivity before proceeding
                if (!isNetworkAvailable()) {
                    showToast("No Internet Connection");
                    return;
                }

                // Play button click sound
                playButtonClickSound();

                // Get username from the input field
                final String username = usernameInput.getText().toString().trim();

                // Validate username
                if (!username.isEmpty()) {

                    if (username.length() < 4) {
                        showToast("Username must be at least 4 characters long");
                        return;
                    } else if (username.length() > 10) {
                        showToast("Username must be at most 10 characters long");
                        return;

                    }
                    // Check if the username already exists in the database
                    usersRef.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                // Username already exists, notify the user
                                showToast("Username already exists, please choose a different one.");
                            } else {
                                // Perform anonymous sign-up with the entered username
                                firebaseAuth.signInAnonymously().addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        // Successfully signed up
                                        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                                        if (currentUser != null) {
                                            // Get the unique user ID
                                            String uniqueUserId = currentUser.getUid();

                                            // Store the username in the database
                                            usersRef.child(uniqueUserId).child("username").setValue(username);

                                            // Notify the user about successful sign-up
                                            showToast("User signed up with the username: " + username);

                                            // Proceed to the home activity
                                            startActivity(new Intent(playerData.this, home.class));
                                            overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                                            finish(); // Finish this activity after sign-up
                                        } else {
                                            // Failed to get the current user
                                            showToast("Failed to sign up. Please try again.");
                                        }
                                    } else {
                                        // Failed to sign up
                                        showToast("Failed to sign up. Please try again.");
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle database error
                            showToast("Database error: " + databaseError.getMessage());
                        }
                    });
                } else {
                    // Username is empty, notify the user
                    showToast("Please enter a username");
                }
            }
        });
    }


    private boolean isValidInput(String input) {
        // Validate that the input contains only letters
        if (input.isEmpty()) return true;
        return input.matches("[a-zA-Z]+");
    }


    /**
     * Checks if there is an active internet connection.
     *
     * @return True if there is an active internet connection, false otherwise.
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * Displays a toast message with the given text.
     *
     * @param message The message to be displayed.
     */
    public void showToast(String message) {
        Toast.makeText(playerData.this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Plays the button click sound.
     */
    private void playButtonClickSound() {
        MediaPlayer mediaPlayer = MediaPlayer.create(playerData.this, R.raw.button_clicked);
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
    public void onDestroy() {
        super.onDestroy();
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    }
}
