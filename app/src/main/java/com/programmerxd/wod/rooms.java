/*
    Activity class handling the room setup and player collection process.
*/

package com.programmerxd.wod;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

public class rooms extends AppCompatActivity {

    private final int MAX_WAIT_TIME = 120; // Maximum wait time in seconds
    // Initialize image views for room selection
    ImageView imageView6;
    ImageView imageView7;
    ImageView imageView8;
    ImageView imageView9;
    ImageView imageView10;
    ImageView imageView11;
    private DatabaseReference room1Ref; // Reference to 'room1' in Firebase Database
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private Handler handler;
    private int elapsedTime = 0;
    private boolean gameStarted = false; // Flag to track whether the game has started
//    private boolean roomStarted = false;
    private boolean intentClosed = false;
    private boolean isHost = false;
    private String[] usernames;
    private String[] uids;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set the status bar color
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.purple));

        // Set the system UI visibility to light status bar
        View decor = getWindow().getDecorView();
        decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rooms);

        // Initialize Firebase Database reference and Firebase Auth
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        room1Ref = database.getReference("room1");
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        // Initialize image views for room selection
        imageView6 = findViewById(R.id.imageView6);
        imageView7 = findViewById(R.id.imageView7);
        imageView8 = findViewById(R.id.imageView8);
        imageView9 = findViewById(R.id.imageView9);
        imageView10 = findViewById(R.id.imageView10);
        imageView11 = findViewById(R.id.imageView11);

        // Initialize Firebase reference for room verification
        DatabaseReference verifyRoomsRef = FirebaseDatabase.getInstance().getReference("verifyAvailableRooms");
        DatabaseReference room1VerificationRef = verifyRoomsRef.child("room1");

        // Check if the current user is assigned to room1
        room1VerificationRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getValue().equals(currentUser.getUid())) {
                    isHost = true;
                    collectPlayers();
                    handler = new Handler();
                    startCheckingPlayers();
                } else if (dataSnapshot.exists()) {

                } else {

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                showToast("Error: " + databaseError.getMessage());
            }
        });

        // Set up click listeners for room selection
        setRoomClickListener(imageView6);
        nullRoomClickListener(imageView7);
        nullRoomClickListener(imageView8);
        nullRoomClickListener(imageView9);
        nullRoomClickListener(imageView10);
        nullRoomClickListener(imageView11);

    }

    private void nullRoomClickListener(ImageView imageView) {
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playButtonClickSound();
                showToast("Room not found.");
            }
        });
    }

    // Method to set up click listener for room selection
    private void setRoomClickListener(ImageView imageView) {
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Play button click sound
                playButtonClickSound();

                // Check if the card view is not visible
                CardView cardView = findViewById(R.id.cardView);
                if (!(cardView.getVisibility() == View.VISIBLE)) {
                    // Check if the current user is not null
                    if (currentUser != null) {

                        DatabaseReference verifyRoomsRef = FirebaseDatabase.getInstance().getReference("verifyAvailableRooms");

                        verifyRoomsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists() && snapshot.child("isRoomStarted").getValue().equals(true)) {
                                    showToast("Room is already started");
                                } else if (snapshot.child("room1").getValue() != null && snapshot.child("isRoomStarted").getValue().equals(false)) {

//                                        showToast("Starting Room");
                                    collectPlayers();
                                    handler = new Handler();
                                    startCheckingPlayers();

                                } else if (snapshot.child("room1").getValue() == null){
                                    showToast("No room available, Please host one.");
                                } else {
                                    showToast("Error: " + snapshot.getValue());
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
//                        checkIfRoomIsAlreadyStarted(new RoomCheckCallback() {
//                            @Override
//                            public void onResult(boolean roomStarted) {
//                                if (roomStarted) {
//                                    showToast("Room is already started");
//                                } else {
//                                    if ()
//                                        showToast("Starting Room");
//                                    collectPlayers();
//                                    handler = new Handler();
//                                    startCheckingPlayers();
//                                }
//                            }
//                        });
                    }
                }
            }
        });
    }

//    private boolean checkIfRoomIsAlreadyStarted(RoomCheckCallback callback) {
//        DatabaseReference verifyRoomsRef = FirebaseDatabase.getInstance().getReference("verifyAvailableRooms");
////        verifyRoomsRef.child("isRoomStarted").equals(true));
//        verifyRoomsRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                boolean roomStarted = false;
//                for (DataSnapshot roomSnapshot : snapshot.getChildren()) {
//                    if (roomSnapshot.getKey().equals("isRoomStarted") && roomSnapshot.getValue().equals(true)) {
//                        roomStarted = true;
//                        break;
//                    } else if (roomSnapshot.getKey().equals("isRoomStarted") && roomSnapshot.getValue().equals(false)) {
//                        roomStarted = false;
//                    }
//                }
//                callback.onResult(roomStarted);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                callback.onResult(false); // Handle errors gracefully
//
//            }
//        });
//        return roomStarted;
//    }

    // Method to collect players in the room
    private void collectPlayers() {
        // Set the player in the room1 database
        room1Ref.child("player_" + currentUser.getUid()).setValue(true);

        CardView cardView = findViewById(R.id.cardView);
        cardView.setVisibility(View.VISIBLE);

        // Set up an empty click listener for the card view
        findViewById(R.id.cardView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Do nothing on card view click
            }
        });

        // Check the number of players in the room
        room1Ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Update player count
                TextView textView = findViewById(R.id.textView6);
                textView.setText(dataSnapshot.getChildrenCount() + " / 10");

                if (dataSnapshot.getChildrenCount() >= 4) {
                    // Start the playground if there are enough players
                    startPlayground();
                    handler.removeCallbacksAndMessages(null); // Stop the checking mechanism
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                showToast("Database error: " + databaseError.getMessage());
            }
        });
    }

    // Method to start the playground and assign roles
    private void startPlayground() {
        Intent serviceIntent = new Intent(this, AudioService.class);
        stopService(serviceIntent); // Stop the audio service

        DatabaseReference room1Ref = FirebaseDatabase.getInstance().getReference("room1");
        DatabaseReference users = FirebaseDatabase.getInstance().getReference("users");

        users.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // store the usernames in an string array
                    usernames = new String[(int) snapshot.getChildrenCount()];
                    uids = new String[(int) snapshot.getChildrenCount()];
                    int index = 0;
                    for (DataSnapshot usernameSnapshot : snapshot.getChildren()) {
                        usernames[index] = usernameSnapshot.child("username").getValue().toString();
                        uids[index] = usernameSnapshot.getKey();
                        index++;
                    }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        room1Ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    int playerCount = (int) dataSnapshot.getChildrenCount();
                    String[] playerUIDs = new String[playerCount];
                    int index = 0;
                    for (DataSnapshot playerSnapshot : dataSnapshot.getChildren()) {
                        playerUIDs[index] = playerSnapshot.getKey();
                        index++;
                    }
                    Random random = new Random();
                    int imposterIndex = random.nextInt(playerCount);

                    for (int i = 0; i < playerCount; i++) {
                        String role = (i == imposterIndex) ? "imposter" : "crewmate";
                        DatabaseReference playerRef = FirebaseDatabase.getInstance().getReference("room1/" + playerUIDs[i]);
                        playerRef.child("role").setValue("imposter");

                        // Assign username if player UID matches user UID
                        for (int j = 0; j < uids.length; j++) {
                            if (playerUIDs[i].equals("player_" + uids[j])) {
                                playerRef.child("username").setValue(usernames[j]);
                                break;
                            }
                        }

                    }

                    if (!gameStarted) {
                        gameStarted = true;
                        int timer = 10;
                        showToast("Starting the game in 10 seconds");
                        CountDownTimer countDownTimer = new CountDownTimer(timer * 1000, 1000) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                            }

                            @Override
                            public void onFinish() {
                                startGame();
                            }
                        }.start();
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                showToast("Error: " + databaseError.getMessage());
            }
        });
    }

    // Method to start the game after assigning roles
    private void startGame() {
        if (intentClosed) {
            return;
        }
        DatabaseReference verifyRoomsRef = FirebaseDatabase.getInstance().getReference("verifyAvailableRooms");
        verifyRoomsRef.child("isRoomStarted").setValue(true);

        Intent intent = new Intent(this, Playground.class);
        startActivity(intent);
//        overridePendingTransition(R.anim.fadein, R.anim.scaledown);
        finish(); // Finish this activity
    }

    // Method to show a toast message
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // Method to start checking players with a delay
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
                    elapsedTime = 0;
                }
            }
        }, 300); // Initial delay for the first check
    }

    // Method to play the button click sound
    private void playButtonClickSound() {
        MediaPlayer mediaPlayer;
        mediaPlayer = MediaPlayer.create(rooms.this, R.raw.button_clicked);
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
        intentClosed = true;
        playButtonClickSound();
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        room1Ref.child("player_" + currentUser.getUid()).removeValue();
        super.onBackPressed();
        finish();

    }

    @Override
    public void onDestroy() {
//        intentClosed = true;
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler.removeCallbacks(null);
            handler = null;
        }

        super.onDestroy();

    }

//    public interface RoomCheckCallback {
//        void onResult(boolean roomStarted);
//    }

}
