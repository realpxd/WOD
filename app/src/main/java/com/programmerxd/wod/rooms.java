package com.programmerxd.wod;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

public class rooms extends AppCompatActivity {

    private final int MAX_WAIT_TIME = 120; // Maximum wait time in seconds
    private DatabaseReference room1Ref; // Reference to 'room1' in Firebase Database
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private Handler handler;
    private int elapsedTime = 0;
    private boolean gameStarted = false; // Flag to track whether the game has started

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rooms);

        // Initialize Firebase Database reference and Firebase Auth
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        room1Ref = database.getReference("room1");
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        ImageView imageView6 = findViewById(R.id.imageView6);

        ImageView imageView7 = findViewById(R.id.imageView7);
        ImageView imageView8 = findViewById(R.id.imageView8);
        ImageView imageView9 = findViewById(R.id.imageView9);
        ImageView imageView10 = findViewById(R.id.imageView10);
        ImageView imageView11 = findViewById(R.id.imageView11);

        DatabaseReference verifyRoomsRef = FirebaseDatabase.getInstance().getReference("verifyAvailableRooms");


        DatabaseReference room1VerificationRef = verifyRoomsRef.child("room1");
        room1VerificationRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getValue() == currentUser.getUid()) {
                    // Current user's UID exists in the room1 reference
//                        room1Ref.child("player_" + currentUser.getUid()).setValue(true);
                    collectPlayers();
                    handler = new Handler();
                    startCheckingPlayers();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle any errors here
                showToast("Error: " + databaseError.getMessage());
            }
        });


        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CardView cardView = findViewById(R.id.cardView);
                if (!(cardView.getVisibility() == View.VISIBLE)) {

                    MediaPlayer mediaPlayer;
                    mediaPlayer = MediaPlayer.create(rooms.this, R.raw.button_clicked);
                    mediaPlayer.setLooping(false); // Set looping to continuously play the audio
                    mediaPlayer.start();
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        public void onCompletion(MediaPlayer mp) {
                            mediaPlayer.stop();
                            mediaPlayer.release();

                        }


                    });

                    if (v.getId() == R.id.imageView6) {
                        if (currentUser != null) {
                            room1Ref.child("player_" + currentUser.getUid()).setValue(true);
                            collectPlayers();

                            handler = new Handler();
                            startCheckingPlayers();
                        }
                    } else {
                        showToast("Room not found");
                    }
                }
            }
        };

        imageView6.setOnClickListener(onClickListener);
        imageView7.setOnClickListener(onClickListener);
        imageView8.setOnClickListener(onClickListener);
        imageView9.setOnClickListener(onClickListener);
        imageView10.setOnClickListener(onClickListener);
        imageView11.setOnClickListener(onClickListener);

    }

    private void collectPlayers() {

        CardView cardView = findViewById(R.id.cardView);
        cardView.setVisibility(View.VISIBLE);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        };

        room1Ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() >= 4) {
                    startPlayground();
                    handler.removeCallbacksAndMessages(null); // Stop the checking mechanism
                } else {
                    TextView textView = findViewById(R.id.textView6);
                    textView.setText(dataSnapshot.getChildrenCount() + " / 10");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                showToast("Database error: " + databaseError.getMessage());
            }
        });
    }

    private void startPlayground() {

        Intent serviceIntent = new Intent(this, AudioService.class);
        stopService(serviceIntent); // Stop the audio service

        // Get a reference to the Firebase Database
        DatabaseReference room1Ref = FirebaseDatabase.getInstance().getReference("room1");

        room1Ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
//                    for (DataSnapshot playerSnapshot : dataSnapshot.getChildren()) {
//                        // Randomly decide if the player is an imposter or a crewmate
//                        String role = (new Random().nextInt(2) == 0) ? "imposter" : "crewmate";
//
//                        // Update the role of the player in the database
//                        playerSnapshot.getRef().setValue(role);
//                    }

                    // Fetch the total count of players
                    int playerCount = (int) dataSnapshot.getChildrenCount();

                    // Store the UIDs of all players
                    String[] playerUIDs = new String[playerCount];
                    int index = 0;
                    for (DataSnapshot playerSnapshot : dataSnapshot.getChildren()) {
                        playerUIDs[index] = playerSnapshot.getKey();
                        index++;
                    }

                    // Randomly select an imposter
                    Random random = new Random();
                    int imposterIndex = random.nextInt(playerCount);

                    for (int i = 0; i < playerCount; i++) {
                        String role = (i == imposterIndex) ? "imposter" : "crewmate";

                        // Set the role for the corresponding UID
                        DatabaseReference playerRef = FirebaseDatabase.getInstance().getReference("room1/" + playerUIDs[i]);
                        playerRef.setValue(role);
                    }


                    if (!gameStarted) {
                        gameStarted = true; // Set the flag to true
                        // Proceed with the rest of the game after assigning roles
                        startGame();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                showToast("Error: " + databaseError.getMessage());
            }
        });
    }

    private void startGame() {
        // The rest of your logic to start the game after assigning roles
        // This might include playing game sounds, initiating UI changes, etc.
        // For example:

        // Start a new activity or update UI elements for the game
        Intent intent = new Intent(this, Playground.class);
        startActivity(intent);

        // Finish this activity
        finish();
    }


    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void startCheckingPlayers() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                elapsedTime++;
                if (elapsedTime <= MAX_WAIT_TIME) {
                    collectPlayers();
                    handler.postDelayed(this, 1000); // Check every 1 second
                } else {
                    showToast("Maximum wait time reached. Exiting the room.");
                    CardView cardView = findViewById(R.id.cardView);
                    cardView.setVisibility(View.GONE);
//                    startPlayground();
                    elapsedTime = 0;
                }
            }
        }, 300); // Initial delay for the first check
    }

    @Override
    public void onBackPressed() {
        MediaPlayer mediaPlayer;
        mediaPlayer = MediaPlayer.create(rooms.this, R.raw.button_clicked);
        mediaPlayer.setLooping(false); // Set looping to continuously play the audio
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                mediaPlayer.stop();
                mediaPlayer.release();

            }

        });
        super.onBackPressed();
    }
}
