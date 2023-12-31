package com.programmerxd.wod;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;

public class Playground extends AppCompatActivity {
    // Unique identifier for requesting runtime permissions
    private static final int PERMISSION_REQ_ID = 22;
    private static final String[] REQUESTED_PERMISSIONS = {Manifest.permission.RECORD_AUDIO};

    // Array to hold the 5 channels tokens
    private final String[] currentRadioServer = new String[5];

    // Array to hold the 5 channels frequency
    private final String[] currentRadioServerFrequency = {"72.34", "24.57", "92.64", "39.23", "58.79"};
    // Agora Console App ID
    private final String appId = "3f393ce1fa6b4c6b80495f09c07f5d34";
    // Array containing channel names
    private final String[] channelNames = {"rs1", "rs2", "rs3", "rs4", "rs5"};
    // An integer that identifies the local user
    private final int uid = 0;
    private final String currentRoom = "room1";
    private final String[] playerNames = new String[10];
    //    private String[] allPlayerRoles;
    private final String[] allPlayerRoles = new String[10]; // Initialize with appropriate size
    private final boolean isPlayerDead = false;
    private final String[] deadPlayers = new String[10];
    // Default channel name
    public String channelName = channelNames[0];
    // ImageView for role indication
    ImageView roleIndicator;
    // Flag to track if the player role container is clicked
    boolean isPlayerRoleContainerClicked = true;
    TextView notificationToast;
    TextView textCurrentRadioServerFrequency;
    TextView infoText;
    Boolean didImposterLeft = false;
    Boolean isEmergencyMeetingTriggered = false;
    DatabaseReference currentRoomData;
    private String frequency;
    // DatabaseReference for room1rs
    private DatabaseReference currentRoomRsRef;
    private DatabaseReference currentRoomRef;
    private DatabaseReference verifyAvailableRoomsRef;
    // Flag to check if the local user has joined a channel
    private boolean isJoined = false;
    // Agora RTC engine event handler to handle real-time communication events
    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        // Listen for the remote user joining the channel.
        public void onUserJoined(int uid, int elapsed) {
//            runOnUiThread(() -> Toast.makeText(Playground.this, "Remote user joined: " + uid, Toast.LENGTH_SHORT).show());
        }

        @Override
        // Triggered when the local user successfully joins the channel
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            isJoined = true;
//            runOnUiThread(() -> Toast.makeText(Playground.this, "Joined Channel", Toast.LENGTH_SHORT).show());
        }

        @Override
        // Triggered when a remote user goes offline
        public void onUserOffline(int uid, int reason) {
//            runOnUiThread(() -> Toast.makeText(Playground.this, "Remote user offline", Toast.LENGTH_SHORT).show());
        }

        @Override
        // Triggered when the local user leaves the channel
        public void onLeaveChannel(RtcStats stats) {
            isJoined = false;
        }
    };
    // HorizontalScrollView to contain player views
    private HorizontalScrollView playersContainer;
    // Database reference for user information
    private DatabaseReference users;
    // Agora Voice SDK engine for real-time communication
    private RtcEngine agoraEngine;
    // Index to select a random radio server token
    private int randomIndex = 0;
    // Countdown timer for specific game actions
    private int time_countdown = 10;
    // Role assigned to the current user (e.g., imposter or crewmate)
    private String role;
    // Countdown timer for certain game actions
    private CountDownTimer countdownTimer;
    // Flag to indicate the microphone state (on or off)
    private boolean isMicOn = false;
    // Launcher for handling speech recognition results
    private ActivityResultLauncher<Intent> speechRecognitionLauncher;
    // Flag to indicate whether speech recognition is in progress
    private boolean isSpeechRecognitionInProgress = false;
    // Username of the current user
    private String username;
    // Firebase authentication instance
    private FirebaseAuth firebaseAuth;
    // Firebase user representing the current user
    private FirebaseUser currentUser;
    private MediaPlayer gameStartVoicePlayer;
    private ImageView microphoneIcon;
    private DatabaseReference broadcastingRef;
    private RelativeLayout emergencyMeetingWrapper;
    private RelativeLayout emergencyMeetingContainerSpliter1, emergencyMeetingContainerSpliter2;
    private LinearLayout emmpc1, emmpc2, emmpc3, emmpc4;
    private TextView meetingCooldown;
    private int eccount = 0;
    private int eccount2 = 0;
    private boolean isPlayerSelectedOnEmergencyMeeting = false;
    private String selectedPlayerOnEmergencyMeeting = "";
    private boolean hasVoted = false;
    private int highestVotes = 0;
    private String playerToBeKicked = "";
    private View previousSelectedViewInEmergencyMeeting = null;
    private TextView displayGameResultText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the status bar color
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.red));

        // Set the system UI visibility to light status bar
        View decor = getWindow().getDecorView();
        decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        // Set the content view to the activity_playground layout
        setContentView(R.layout.activity_playground);

        // Initialize roleIndicator
        roleIndicator = findViewById(R.id.roleIndicator);
        notificationToast = findViewById(R.id.notificationToast);
        infoText = findViewById(R.id.infoText);
        // Find the TextView responsible for displaying the current radio server frequency
        textCurrentRadioServerFrequency = findViewById(R.id.currentRadioServerFrequency);
        // Get reference to the microphone icon ImageView
        microphoneIcon = findViewById(R.id.microphoneIcon);
        verifyAvailableRoomsRef = FirebaseDatabase.getInstance().getReference("verifyAvailableRooms");

        emergencyMeetingWrapper = findViewById(R.id.emergencyMeetingWrapper);
        emergencyMeetingContainerSpliter1 = findViewById(R.id.emergencyMeetingContainerSpliter1);
        emergencyMeetingContainerSpliter2 = findViewById(R.id.emergencyMeetingContainerSpliter2);
        emmpc1 = findViewById(R.id.EmergencyMeetingMaxPlayersContainer1);
        emmpc2 = findViewById(R.id.EmergencyMeetingMaxPlayersContainer2);
        emmpc3 = findViewById(R.id.EmergencyMeetingMaxPlayersContainer3);
        emmpc4 = findViewById(R.id.EmergencyMeetingMaxPlayersContainer4);
        meetingCooldown = findViewById(R.id.meetingCooldown);

        displayGameResultText = findViewById(R.id.displayGameResultText);

        currentRoomData = FirebaseDatabase.getInstance().getReference("verifyAvailableRooms");

        // Request necessary permissions if not granted
        if (!checkSelfPermission()) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, PERMISSION_REQ_ID);
        }

        // Setup the Agora Voice SDK engine
        setupVoiceSDKEngine();

        // Play game start voice
        gameStartVoicePlayer = MediaPlayer.create(this, R.raw.game_start_voice);
        gameStartVoicePlayer.setLooping(false);
        gameStartVoicePlayer.start();
        gameStartVoicePlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                gameStartVoicePlayer.stop();
                gameStartVoicePlayer.release();
            }
        });

        // Initialize playersContainer
        playersContainer = findViewById(R.id.playersContainer);

        // Initialize Firebase Authentication and get the current user
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        // Listen for changes in the "users" node in Firebase
        users = FirebaseDatabase.getInstance().getReference("users");
        users.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Iterate through the players in the "users" node
//                int i = 0;
                for (DataSnapshot playerSnapshot : dataSnapshot.getChildren()) {
                    String playerName = playerSnapshot.child("username").getValue(String.class);
                    if (playerName != null) {
//                        playerNames[i] = playerName;
//                        i++;
                        // Identify the current user's username
                        if (playerSnapshot.getKey().equals(currentUser.getUid())) {
                            username = playerName;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors
            }
        });

        // Get the user's role from the "room1" node in Firebase
        currentRoomRef = verifyAvailableRoomsRef.child(currentRoom + "/players");
        currentRoomRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
//                boolean roleFound = false;
                int i = 0;

                // Iterate through the players in the "room1" node
                for (DataSnapshot playerSnapshot : dataSnapshot.getChildren()) {
                    String playerUID = playerSnapshot.getKey();
                    if (playerUID != null) {

                        String playerName = playerSnapshot.child("username").getValue(String.class);
                        playerNames[i] = playerName;
//                        showToast(playerName);

                        String allPlayerRolesRef = playerSnapshot.child("role").getValue(String.class);
                        allPlayerRoles[i] = allPlayerRolesRef;
                        i++;


                        if (playerUID.equals("player_" + currentUser.getUid())) {
                            role = playerSnapshot.child("role").getValue(String.class);
                            if (role != null) {
//                                roleFound = true;
                                // Display the role indicator based on the user's role
                                if (role.equals("imposter")) {
//                                    showToast("You are the imposter!");
                                    roleIndicator.setImageResource(R.drawable.imposter_toast);
                                } else if (role.equals("crewmate")) {
//                                    showToast("You are a crewmate!");
                                    roleIndicator.setImageResource(R.drawable.crew_toast);
                                } else {
//                                    showToast("You are dead!");
                                    roleIndicator.setImageResource(R.drawable.dead_toast);
                                    // Disable the push-to-talk functionality for dead players
                                    agoraEngine.muteLocalAudioStream(true);
                                    // Update microphone icon to muted state
                                    microphoneIcon.setImageResource(R.drawable.mic_mute);
                                }
                            }
                        }

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                showToast("Error: " + databaseError.getMessage());
            }
        });

        // Get available radio server tokens from the "room1rs" node in Firebase
        currentRoomRsRef = verifyAvailableRoomsRef.child(currentRoom + "/rs");
        currentRoomRsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    int i = 0;
                    // Iterate through the radio server tokens in the "room1rs" node
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        currentRadioServer[i] = snapshot.child("key").getValue(String.class);

                        if (randomIndex != -1) {
                            // Update the player's role in the current radio server
                            if (channelNames[randomIndex].equals(snapshot.getKey())) {
                                DatabaseReference curPlayerRef = snapshot.getRef().child("players").child(currentUser.getUid()).getRef().child(username);
                                curPlayerRef.setValue(role);
                            }
                        } else {
                        }
                        i++;
                    }
                    // Assign a token and connect to the radio channel
                    assignToken();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                showToast("Error: " + databaseError.getMessage());
            }
        });

        // Register the speechRecognitionLauncher for speech recognition result handling
        speechRecognitionLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                handleSpeechRecognitionResult(result.getData());
                agoraEngine.enableAudio();
            } else {
                showToast("Speech recognition error");
                displayToast("Can't hear you :(");
                agoraEngine.enableAudio();
            }
        });

        startRoomCountdown();


        broadcastingRef = FirebaseDatabase.getInstance().getReference("broadcastingInformation");
//        ChildEventListener childEventListener = new ChildEventListener() {
//            @Override
//            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//                if (snapshot.getKey().equals("didImposterLeft")) {
//                    if (snapshot.getValue().equals(true)) {
//                        didImposterLeft = true;
//                        showToast("Imposter left the game");
////                        gameOver("crewmate");
//                    }
//                }
//            }
//
//            @Override
//            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//
//            }
//
//            @Override
//            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
//
//            }
//
//            @Override
//            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        };
//        childEventListener = broadcastingRef.addChildEventListener(childEventListener);

        broadcastingRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot playerSnapshot : snapshot.getChildren()) {
                    if (playerSnapshot != null) {
                        if (playerSnapshot.getKey().equals("didImposterLeft")) {
                            if (playerSnapshot.getValue().equals(true)) {
                                didImposterLeft = true;
                                showToast("Imposter left the game");
                                gameOver("crewmate");
                            }
                        } else if (playerSnapshot.getKey().equals("isEmergencyMeetingTriggered")) {
                            if (playerSnapshot.getValue().equals(true)) {
                                isEmergencyMeetingTriggered = true;
                                showToast("Emergency meeting called");
                                emergencyMeeting();
                            }
                        } else {
                            isEmergencyMeetingTriggered = false;
                            didImposterLeft = false;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void gameOver(String winner) {
        RelativeLayout gameResultWrapper = findViewById(R.id.gameResultWrapper);
        gameResultWrapper.setVisibility(View.VISIBLE);
        if (role.equals("crewmate")) {
            showToast("Game Over " + winner + "s won the game");
            displayGameResult("Game Over!  You Won :3");
//            finish();
        } else if (role.equals("imposter")) {
            showToast("Game Over " + winner + " won the game");
            displayGameResult("Game Over!  You Lost :(");

        } else {

        }
    }

//    private void callEmergencyMeeting() {
//        showToast("Emergency meeting called");
//    }

    private void startRoomCountdown() {
        // Set the countdown time to 10 minutes (600 seconds)
        int countdownTimeInSeconds = 600;

        // Create a CountDownTimer with the specified countdown time
        CountDownTimer countDownTimer = new CountDownTimer(countdownTimeInSeconds * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // This method will be called every second (1000 milliseconds) until the countdown is finished
                // You can update UI elements or perform actions during the countdown here
                // For example, you might want to display the remaining time in a TextView
                // textViewTimer.setText("Time remaining: " + millisUntilFinished / 1000 + " seconds");
            }

            @Override
            public void onFinish() {
                // This method will be called when the countdown is finished (after 10 minutes)
                // You can add any actions you want to perform when the countdown reaches zero
//                DatabaseReference currentRoom = FirebaseDatabase.getInstance().getReference("verifyAvailableRooms");
//                currentRoom.child("room1").removeValue();
//                currentRoom.child("isRoomStarted").setValue(false);
//                gameOver(); // win the imposter
//                finish(); // Finish the activity
                gameOver("imposter");
            }
        }.start();
    }

    /**
     * Dynamically creates player views in the UI based on the provided player names.
     *
     * @param playerNames Array of player names to be displayed.
     */
    private void createPlayerView(String[] playerNames, String[] playerRoles) {
        // Remove all existing views from the playersContainer
        playersContainer.removeAllViews();

        // Create a horizontal layout to hold player views
        LinearLayout horizontalLayout = new LinearLayout(this);
        horizontalLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);

        // Iterate through each player name
        for (int i = 0; i < playerNames.length; i++) {
            String playerName = playerNames[i];
            if (playerName != null) {
                // Create a vertical layout for each player
                LinearLayout playerLayout = new LinearLayout(this);

                // Set layout parameters
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                playerLayout.setOrientation(LinearLayout.VERTICAL);
                playerLayout.setPadding(dpToPx(5), dpToPx(5), dpToPx(5), dpToPx(5));
                params.setMarginEnd(dpToPx(20));
                params.setMarginStart(dpToPx(20));
                playerLayout.setLayoutParams(params);

                // Create an ImageView for the player image
                ImageView playerImage = new ImageView(this);
                LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(dpToPx(50), dpToPx(50));
                playerImage.setLayoutParams(imageParams);

                // Set the appropriate image resource based on player role
                if (playerRoles[i].equals("dead")) {
                    playerImage.setImageResource(R.drawable.dead_toast);
                } else {
                    playerImage.setImageResource(R.drawable.player_pfp);
                }

                // Create a TextView for the player name
                TextView playerNameTextView = new TextView(this);
                playerNameTextView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                playerNameTextView.setText(playerName);
                playerNameTextView.setGravity(Gravity.CENTER);
                playerNameTextView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                playerNameTextView.setTextColor(getResources().getColor(R.color.white));
                playerNameTextView.setTypeface(getResources().getFont(R.font.vertigo_flf_bold));

                // Add the player image and name TextView to the player layout
                playerLayout.addView(playerImage);
                playerLayout.addView(playerNameTextView);

                // Add the player layout to the horizontal layout
                horizontalLayout.addView(playerLayout);
            }
        }

        // Add the horizontal layout to the playersContainer
        playersContainer.addView(horizontalLayout);
    }

    // Helper method to convert dp to pixels
    private int dpToPx(int dp) {
        // Get the screen density from the resources
        float density = getResources().getDisplayMetrics().density;
        // Convert dp to pixels using the formula: pixels = dp * density
        return Math.round(dp * density);
    }

    /**
     * Initiates the speech recognition process, allowing the user to speak commands.
     */
    private void startSpeechRecognition() {
        // Set the flag indicating that speech recognition is in progress
        isSpeechRecognitionInProgress = true;

        // Create an intent for speech recognition
        Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        // Specify the language model for free-form speech recognition
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        // Set the language to the default locale
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        // Set a prompt for the user to say 'kill' followed by the player's name
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say 'kill' followed by the player's name");

        try {
            // Disable Agora audio temporarily during speech recognition
            agoraEngine.disableAudio();
//            agoraEngine.muteLocalAudioStream(true);
//            agoraEngine.muteAllRemoteAudioStreams(true);
//            agoraEngine.leaveChannel();
            // Launch the speech recognition activity using the registered launcher
            speechRecognitionLauncher.launch(speechRecognizerIntent);
        } catch (ActivityNotFoundException e) {
            // If speech recognition is not supported, show a toast and re-enable Agora audio
            Toast.makeText(this, "Speech recognition not supported", Toast.LENGTH_SHORT).show();
            agoraEngine.enableAudio();
        }
    }

    /**
     * Handles the result of the speech recognition process.
     *
     * @param data The Intent containing the speech recognition result.
     */
    private void handleSpeechRecognitionResult(Intent data) {
        // Check if the Intent contains data
        if (data != null) {
            // Retrieve the list of recognized speech results
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            // Check if the result list is not empty
            if (result != null && !result.isEmpty()) {
                // Get the first recognized command from the result list
                String commandSpoken = result.get(0);
                // Process the recognized command
                processRecognizedCommand(commandSpoken);
            }
        }

        // Set the flag indicating that speech recognition is no longer in progress
        isSpeechRecognitionInProgress = false;
    }

    /**
     * Overrides the method to handle activity results.
     *
     * @param requestCode The request code passed to startActivityForResult().
     * @param resultCode  The result code returned by the child activity.
     * @param data        An Intent that carries the result data.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Call the superclass method to handle the default behavior
        super.onActivityResult(requestCode, resultCode, data);

        // Check if the request code matches the speech recognition permission request
        if (requestCode == PERMISSION_REQ_ID) {
            // Check if the result code indicates a successful result
            if (resultCode == RESULT_OK) {
                // Handle the speech recognition result
                handleSpeechRecognitionResult(data);
            } else {
                // Set the flag indicating that speech recognition is no longer in progress
                isSpeechRecognitionInProgress = false;
            }
        }
    }

    /**
     * Processes the recognized speech command, specifically handling 'kill' commands.
     *
     * @param commandSpoken The recognized speech command.
     */
    private void processRecognizedCommand(String commandSpoken) {

//        agoraEngine.muteLocalAudioStream(false);
//        agoraEngine.enableLocalAudio(true);
        // Check if the recognized command starts with "kill"
        if (commandSpoken.toLowerCase().startsWith("kill")) {
            // Split the recognized command into words
            String[] spokenWords = commandSpoken.split(" ");

            // Check if there are at least two words in the command
            if (spokenWords.length >= 2) {
                // Extract the player name from the command
                String saidPlayerName = spokenWords[1];
                boolean nameMatched = false;
                for (String playerName : playerNames) {
                    if (saidPlayerName.equals(playerName) || saidPlayerName.equalsIgnoreCase(playerName)) {
                        nameMatched = true;
//                        room1Ref = FirebaseDatabase.getInstance().getReference("room1");
                        currentRoomRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot playerSnapshot : snapshot.getChildren()) {
                                    String playerUID = playerSnapshot.getKey();
                                    if (playerUID != null) {
                                        String playerUserName = playerSnapshot.child("username").getValue(String.class);
                                        if (playerUserName != null) {
                                            if (playerUserName.equalsIgnoreCase(saidPlayerName)) {
                                                String playerRole = playerSnapshot.child("role").getValue(String.class);
                                                if (playerRole != null) {
                                                    if (playerRole.equals("imposter")) {
                                                        showToast("You cannot kill yourself");
                                                        displayToast("Trying to Suicide ? :)");
                                                    } else if (playerRole.equals("crewmate")) {
                                                        showToast("Killing " + playerName);
                                                        DatabaseReference curPlayerRef = playerSnapshot.getRef().child("role");
                                                        curPlayerRef.setValue("dead");
                                                        startPlayground("update");
                                                        displayToast("killing " + playerUserName);
                                                    } else if (playerRole.equals("dead")) {
                                                        showToast("Player is already dead");
                                                        displayToast("RIP Already!");
                                                    } else {
                                                        showToast("Player not found , No-one was killed");
                                                        displayToast("No such player");
                                                    }
                                                }
                                            } else {
                                            }
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });


                        currentRoomRsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                List<String> names = new ArrayList<>(); // Use a dynamic list to store player names
                                List<String> allPlayersRoles = new ArrayList<>(); // Use a dynamic list to store player names

                                // Check if the data snapshot exists
                                if (dataSnapshot.exists()) {
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        // Check if a valid randomIndex is available
                                        if (randomIndex != -1) {
                                            if (channelNames[randomIndex].equals(snapshot.getKey())) {


                                                DatabaseReference allPlayerRef = snapshot.getRef().child("players");
                                                DatabaseReference curPlayerRef = snapshot.getRef().child("players").child(currentUser.getUid());

                                                // Set the current player's role in the database
                                                curPlayerRef.child(username).setValue(role);

                                                // Listen for a single value event on the allPlayerRef node
                                                allPlayerRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {


                                                        int i = 0;

                                                        // Iterate through playerSnapshot in the snapshot
                                                        for (DataSnapshot playerSnapshot : snapshot.getChildren()) {


                                                            // Get player name from the iterators next key
                                                            String playerNameTwo = String.valueOf(playerSnapshot.getChildren().iterator().next().getKey());
                                                            String playerRole = String.valueOf(playerSnapshot.getChildren().iterator().next().getValue());


                                                            if (playerNameTwo != null) {
                                                                if (playerNameTwo.equalsIgnoreCase(saidPlayerName)) {
                                                                    if (playerRole != null) {
                                                                        if (playerRole.equals("imposter")) {
                                                                            showToast("You cannot kill yourself");
                                                                            displayToast("Trying to Suicide ? :)");
                                                                        } else if (playerRole.equals("crewmate")) {
                                                                            showToast("Killing " + playerName);
                                                                            DatabaseReference curPlayerRef = playerSnapshot.getRef().child(playerNameTwo);
                                                                            curPlayerRef.setValue("dead");
                                                                            startPlayground("update");
                                                                            displayToast("killing " + playerNameTwo);
                                                                        } else if (playerRole.equals("dead")) {
                                                                            showToast("Player is already dead");
                                                                            displayToast("RIP Already!");
                                                                        } else {
                                                                            showToast("Player not found , No-one was killed");
                                                                            displayToast("No such player");
                                                                        }
                                                                    }
                                                                } else {
                                                                }
                                                            }


                                                            // Check if the playerNameTwo is not null
                                                            if (playerNameTwo != null) {
                                                                names.add(playerNameTwo); // Add names to the list
                                                            }
                                                            if (playerRole != null) {
                                                                allPlayersRoles.add(playerRole);
                                                            }
                                                            i++;
                                                        }

                                                        // Convert the list to an array
                                                        String[] playerNamesTwo = names.toArray(new String[0]);
                                                        String[] playersRoles = allPlayersRoles.toArray(new String[0]);

                                                        // Call method to create player views
                                                        createPlayerView(playerNamesTwo, playersRoles);
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {
                                                        // Handle errors during data retrieval
                                                    }
                                                });
                                            } else {
                                                // Remove the current player's data if not in the correct channel
                                                DatabaseReference curPlayerRef = snapshot.getRef().child("players").child(currentUser.getUid());
                                                curPlayerRef.removeValue();
                                            }
                                        } else {
                                            // Handle the case when randomIndex is not valid
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                // Display a toast in case of an error during data retrieval
                                showToast("Error: " + databaseError.getMessage());
                            }
                        });
                        break;
                    }


                    // Display a toast indicating the intention to kill the specified player
//                showToast("Killing " + playerName);
                }

                if (!nameMatched) {
//                    showToast("Player not found , No-one was killed");
                    displayToast("No such player");
                }

            } else {
//                showToast("Invalid command");
                displayToast("Invalid command");
            }
        } else {
//            showToast("Invalid command");
            displayToast("Invalid command");
        }

    }

    private void displayToast(String message) {

        infoText.setVisibility(View.GONE);
        textCurrentRadioServerFrequency.setVisibility(View.GONE);
        notificationToast.setVisibility(View.VISIBLE);
        notificationToast.setText(message);
        CountDownTimer countDownTimer = new CountDownTimer(2500, 500) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {

                infoText.setVisibility(View.VISIBLE);
                textCurrentRadioServerFrequency.setVisibility(View.VISIBLE);
                notificationToast.setVisibility(View.GONE);
            }
        }.start();
    }

    private void displayGameResult(String message) {
        displayGameResultText.setText(message);
        CountDownTimer countDownTimer = new CountDownTimer(2500, 500) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {

            }
        }.start();
    }


    /**
     * Handles the click event on the player role container, managing actions based on the player's role.
     *
     * @param view The clicked view representing the player role container.
     */
    public void playerRole(View view) {
        playButtonClickSound();
        // Cast the clicked view to a LinearLayout representing the player role container
        LinearLayout roleIndicatorContainer = (LinearLayout) view;

        // Find the TextView responsible for displaying the kill countdown within the roleIndicatorContainer
        TextView killCountdown = roleIndicatorContainer.findViewById(R.id.killCountdown);

        // Check if the player role container can be clicked
        if (isPlayerRoleContainerClicked) {
            isPlayerRoleContainerClicked = false;

            // Check the player's role
            if (role.equals("imposter")) {
                // If the player is an imposter, update UI elements for kill countdown
                roleIndicator.setVisibility(View.GONE);
                killCountdown.setVisibility(View.VISIBLE);
                killCountdown.setText(String.valueOf(time_countdown));

                // Start speech recognition for command input
                startSpeechRecognition();

                // Start a countdown timer for a specified duration
                countdownTimer = new CountDownTimer(10000, 1000) {
                    public void onTick(long millisUntilFinished) {
                        // Update the kill countdown display during each tick
                        time_countdown--;
                        killCountdown.setText(String.valueOf(time_countdown));
                    }

                    public void onFinish() {
                        // Restore the role indicator and hide the kill countdown after the countdown finishes
                        roleIndicator.setVisibility(View.VISIBLE);
                        killCountdown.setVisibility(View.GONE);
                        time_countdown = 10;
                        isPlayerRoleContainerClicked = true;
                    }
                }.start();
            } else if (role.equals("crewmate")) {
                // If the player is not an imposter, show a toast indicating the player's tasks
                isPlayerRoleContainerClicked = true;
                emergencyMeetingWrapper.setVisibility(View.VISIBLE);
                emergencyMeetingContainerSpliter1.setVisibility(View.VISIBLE);
            } else if (role.equals("dead")) {
                displayToast("You are dead");
                isPlayerRoleContainerClicked = true;
            } else {
//                showToast("Error: Invalid role");
            }
        }
    }

    /**
     * Checks whether the app has the required permissions, specifically for audio recording.
     *
     * @return True if the permission is granted, false otherwise.
     */
    private boolean checkSelfPermission() {
        // Check if the app has the required audio recording permission
        return ContextCompat.checkSelfPermission(this, REQUESTED_PERMISSIONS[0]) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Sets up the Agora Voice SDK engine for audio communication.
     */
    private void setupVoiceSDKEngine() {
        // Check if speech recognition is in progress and return if true
        if (isSpeechRecognitionInProgress) {
            return;
        }

        try {
            // Create a configuration for the Agora Voice SDK engine
            RtcEngineConfig config = new RtcEngineConfig();
            config.mContext = getBaseContext();
            config.mAppId = appId;
            config.mEventHandler = mRtcEventHandler;

            // Create an instance of the Agora Voice SDK engine and mute the local audio stream
            agoraEngine = RtcEngine.create(config);
            agoraEngine.muteLocalAudioStream(true);
        } catch (Exception e) {
            // Print the stack trace and throw a runtime exception with an error message
            e.printStackTrace();
            throw new RuntimeException("Check the error: " + e.getMessage());
        }
    }

    /**
     * Assigns a token for the user to join a communication channel.
     */
    private void assignToken() {
        // Check if there are available channel tokens
        if (currentRadioServer != null) {
            // Trigger the startPlayground method with the action "leaving"
            startPlayground("leaving");

            // Generate a random index within the range of available tokens
            randomIndex = new Random().nextInt(currentRadioServer.length);

            channelName = channelNames[randomIndex];

            // Get the token at the randomly generated index and display the corresponding frequency
            String userToken = currentRadioServer[randomIndex];
            frequency = currentRadioServerFrequency[randomIndex];
            setTextCurrentRadioServerFrequency(frequency);

            // Connect to the communication channel using the assigned token
            connectToChannel(userToken);
        } else {
            // Display a toast indicating that no channel tokens are available
            showToast("No channel tokens available");
        }
    }

    /**
     * Connects to a communication channel using the provided user token.
     *
     * @param userToken The token required for joining the communication channel.
     */
    private void connectToChannel(String userToken) {
        // Create options for the communication channel
        ChannelMediaOptions options = new ChannelMediaOptions();
        options.autoSubscribeAudio = true;
        options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;
        options.channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING;

        // Log Agora channel connection details (consider proper logging)
        Log.d("Agora", "Connecting to channel: " + channelName + " with token: " + userToken + " and UID: " + uid);

        // Check if the Agora engine instance is not null
        if (agoraEngine != null) {
            // Check if the user is already in a channel; if so, leave before joining a new one
            if (agoraEngine.getConnectionState() == Constants.CONNECTION_STATE_CONNECTED) {
                agoraEngine.leaveChannel();
                // showToast("Leaving current channel to join a new one..."); // Uncomment this line if needed
            }
        }

        // Join the communication channel with the Agora engine
        agoraEngine.joinChannel(userToken, channelName, uid, options);

        // Trigger the startPlayground method with the action "joining"
        startPlayground("joining");

        if (role.equals("dead")) {
            agoraEngine.muteLocalAudioStream(true);
        }

        // Log a message indicating an attempt to join the channel
        Log.d("Agora", "Attempted to join channel");
    }

    /**
     * Initiates the playground based on the provided action, such as "joining" or "leaving."
     *
     * @param action The action to be performed in the playground.
     */
    private void startPlayground(String action) {
        // Listen for a single value event on the currentRoomRsRef node
        currentRoomRsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> names = new ArrayList<>(); // Use a dynamic list to store player names
                List<String> allPlayersRoles = new ArrayList<>(); // Use a dynamic list to store player names

                // Check if the data snapshot exists
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        // Check if a valid randomIndex is available
                        if (randomIndex != -1) {
                            if (channelNames[randomIndex].equals(snapshot.getKey())) {
                                DatabaseReference allPlayerRef = snapshot.getRef().child("players");
                                DatabaseReference curPlayerRef = snapshot.getRef().child("players").child(currentUser.getUid());

                                // Set the current player's role in the database
                                curPlayerRef.child(username).setValue(role);

                                // Listen for a single value event on the allPlayerRef node
                                allPlayerRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        int i = 0;

                                        // Iterate through playerSnapshot in the snapshot
                                        for (DataSnapshot playerSnapshot : snapshot.getChildren()) {
                                            // Get player name from the iterators next key
                                            String playerNameTwo = String.valueOf(playerSnapshot.getChildren().iterator().next().getKey());
                                            String playerRole = String.valueOf(playerSnapshot.getChildren().iterator().next().getValue());

                                            // Check if the playerNameTwo is not null
                                            if (playerNameTwo != null) {
                                                names.add(playerNameTwo); // Add names to the list
                                            }
                                            if (playerRole != null) {
                                                allPlayersRoles.add(playerRole);
                                            }
                                            i++;
                                        }

                                        // Convert the list to an array
                                        String[] playerNamesTwo = names.toArray(new String[0]);
                                        String[] playersRoles = allPlayersRoles.toArray(new String[0]);

                                        // Call method to create player views
                                        createPlayerView(playerNamesTwo, playersRoles);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        // Handle errors during data retrieval
                                    }
                                });
                            } else {
                                // Remove the current player's data if not in the correct channel
                                DatabaseReference curPlayerRef = snapshot.getRef().child("players").child(currentUser.getUid());
                                curPlayerRef.removeValue();
                            }
                        } else {
                            // Handle the case when randomIndex is not valid
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Display a toast in case of an error during data retrieval
                showToast("Error: " + databaseError.getMessage());
            }
        });
    }

    /**
     * Shuffles the radio server by generating a random token and connecting to the corresponding channel.
     *
     * @param view The view triggering the shuffle action.
     */
    public void shuffleRadioServer(View view) {
        playButtonClickSound();
        // Call the method to assign a random token and connect to the corresponding channel
        assignToken();
    }

    /**
     * Switches to the previous radio server channel.
     *
     * @param view The view triggering the switch action.
     */
    public void previousRadioServer(View view) {
        playButtonClickSound();
        // Decrease the random index and ensure it stays within the valid range
        randomIndex--;
        if (randomIndex < 0) randomIndex = 4;

        // Update the UI with the frequency and channel name of the new server
        textCurrentRadioServerFrequency.setText(currentRadioServerFrequency[randomIndex]);
        channelName = channelNames[randomIndex];

        // Leave the current channel and connect to the new channel
        agoraEngine.leaveChannel();
        connectToChannel(currentRadioServer[randomIndex]);
    }

    /**
     * Switches to the next radio server channel.
     *
     * @param view The view triggering the switch action.
     */
    public void nextRadioServer(View view) {
        playButtonClickSound();
        // Increase the random index and ensure it stays within the valid range
        randomIndex++;
        if (randomIndex > 4) randomIndex = 0;

        // Update the UI with the frequency and channel name of the new server
        textCurrentRadioServerFrequency.setText(currentRadioServerFrequency[randomIndex]);
        channelName = channelNames[randomIndex];

        // Leave the current channel and connect to the new channel
        agoraEngine.leaveChannel();
        connectToChannel(currentRadioServer[randomIndex]);
    }

    private void setTextCurrentRadioServerFrequency(String frequency) {
        CountDownTimer countDownTimer = new CountDownTimer(500, 10) {
            @Override
            public void onTick(long millisUntilFinished) {
                randomIndex = new Random().nextInt(currentRadioServer.length);
                textCurrentRadioServerFrequency.setText(currentRadioServerFrequency[randomIndex]);

            }

            @Override
            public void onFinish() {
                textCurrentRadioServerFrequency.setText(frequency);

            }
        }.start();

    }

    /**
     * Handles the push-to-talk functionality.
     *
     * @param view The View that triggers the method, in this case, the microphone icon.
     */
    public void pushToTalk(View view) {
        playButtonClickSound();

        if (role.equals("dead")) {
            displayToast("You are dead");
            return;
        }

        // Toggle the microphone state
        if (isMicOn) {
            // If microphone is on, mute local audio stream
            agoraEngine.muteLocalAudioStream(true);
            // Update microphone icon to muted state
            microphoneIcon.setImageResource(R.drawable.mic_mute);
            // Update microphone state flag
            isMicOn = false;
        } else {
            // If microphone is off, unmute local audio stream
            agoraEngine.muteLocalAudioStream(false);
            // Update microphone icon to unmuted state
            microphoneIcon.setImageResource(R.drawable.mic_unmute);
            // Update microphone state flag
            isMicOn = true;
        }
    }


    public void disableOtherClickEvents(View view) {
    }

    public void callEmergencyMeetingButton(View view) {
        callEmergencyMeeting();
    }

    private void callEmergencyMeeting() {
        playButtonClickSound();

        DatabaseReference isEmergencyMeetingTriggered = FirebaseDatabase.getInstance().getReference("broadcastingInformation");
        isEmergencyMeetingTriggered.child("isEmergencyMeetingTriggered").setValue(true);
    }

    private void emergencyMeeting() {
        emergencyMeetingWrapper.setVisibility(View.VISIBLE);
        emergencyMeetingContainerSpliter1.setVisibility(View.GONE);
        emergencyMeetingContainerSpliter2.setVisibility(View.VISIBLE);
        CountDownTimer cooldown = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                meetingCooldown.setText("Cooldown : " + millisUntilFinished / 1000 + "s");
            }

            @Override
            public void onFinish() {
                showToast("Cooldown finished");
                DatabaseReference isEmergencyMeetingTriggered = FirebaseDatabase.getInstance().getReference("broadcastingInformation");
                isEmergencyMeetingTriggered.child("isEmergencyMeetingTriggered").setValue(false);

                emmpc1.removeAllViews();
                emmpc2.removeAllViews();
                emmpc3.removeAllViews();
                emmpc4.removeAllViews();

                eccount = 0;
                eccount2 = 0;

                kickPlayer();

            }
        }.start();


        LinearLayout EmergencyMeetingPlayerContainerWrapper = findViewById(R.id.EmergencyMeetingPlayerContainerWrapper);
//        EmergencyMeetingPlayerContainerWrapper.removeAllViews();

//        horizontalContainer.setId(R.id.EmergencyMeetingHorizontalPlayerContainer);

//        for (int i = 0; i < 3; i++) {
//            LinearLayout verticalContainer = createVerticalContainer(this);
//            ImageView imageView = createImageView(this);
//            TextView textView = createTextView(this, playersNames[i]);

//            verticalContainer.addView(imageView);
//            verticalContainer.addView(textView);

//            horizontalContainer.addView(verticalContainer);
//        }

        eccount = 0;
        currentRoomRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot playerSnapshot : snapshot.getChildren()) {
                    String playerUID = playerSnapshot.getKey();
                    if (playerUID != null) {
                        String playerUserName = playerSnapshot.child("username").getValue(String.class);
                        if (playerUserName != null) {
                            String playerRole = playerSnapshot.child("role").getValue(String.class);
                            if (playerRole != null && !playerRole.equals("dead")) {
                                LinearLayout verticalContainer = new LinearLayout(Playground.this);

                                verticalContainer.setOrientation(LinearLayout.VERTICAL);
                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                params.setMargins(10, 0, 10, 0);
                                params.weight = 1;
                                verticalContainer.setLayoutParams(params);
                                verticalContainer.setOnClickListener(v -> playerSelectedOnEmergencyMeeting(playerUserName, v));

                                ImageView imageView = new ImageView(Playground.this);
                                imageView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                                imageView.setImageResource(R.drawable.player_pfp); // Set default image resource

                                LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(dpToPx(50), dpToPx(50));
                                imageView.setLayoutParams(imageParams);


                                TextView textView = new TextView(Playground.this);
                                textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                                textView.setText(playerUserName);
                                textView.setTextSize(20);
                                textView.setTextColor(getResources().getColor(R.color.white));
                                textView.setGravity(Gravity.CENTER);
                                textView.setTypeface(ResourcesCompat.getFont(Playground.this, R.font.vertigo_flf_bold));

                                verticalContainer.addView(imageView);
                                verticalContainer.addView(textView);
                                eccount++;
                                if (eccount2 == 0) {
                                    emmpc1.addView(verticalContainer);
                                } else if (eccount2 == 1) {
                                    emmpc2.addView(verticalContainer);
                                } else if (eccount2 == 2) {
                                    emmpc3.addView(verticalContainer);
                                } else {
                                    emmpc4.addView(verticalContainer);
                                }

                                if (eccount == 3) {
                                    eccount = 0;
                                    eccount2++;
//                                    showToast("eccount2 : " + eccount2);
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void playerSelectedOnEmergencyMeeting(String playerName, View view) {
        playButtonClickSound();
        if (hasVoted) {
            showToast("You have already voted");
            return;
        }
        isPlayerSelectedOnEmergencyMeeting = true;
        selectedPlayerOnEmergencyMeeting = playerName;
        if (previousSelectedViewInEmergencyMeeting != null) {
            previousSelectedViewInEmergencyMeeting.setAlpha(1f);
        }
        previousSelectedViewInEmergencyMeeting = view;
        view.setAlpha(0.5f);

//        view.setBackground(getResources().getDrawable(R.drawable.selected_player_border));
    }

    public void submitVote(View view) {
        playButtonClickSound();
        if (hasVoted) {
            showToast("You have already voted");
            return;
        }
        if (isPlayerSelectedOnEmergencyMeeting) {
            showToast("Vote submitted");
            //set text as voted in this view
            Button voteBtn = findViewById(R.id.button3);
            voteBtn.setText("Voted");
            voteBtn.setBackgroundColor(getResources().getColor(R.color.purple));
            hasVoted = true;

            currentRoomRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot playerSnapshot : snapshot.getChildren()) {
                        String playerUID = playerSnapshot.getKey();
                        if (playerUID != null) {
                            String playerUserName = playerSnapshot.child("username").getValue(String.class);
                            if (playerUserName != null) {
                                if (playerUserName.equalsIgnoreCase(selectedPlayerOnEmergencyMeeting)) {
                                    Integer currentVotes = playerSnapshot.child("votes").getValue(Integer.class);
                                    if (currentVotes != null) {
                                        int incrementedVotes = currentVotes + 1;
                                        playerSnapshot.child("votes").getRef().setValue(incrementedVotes);
                                    } else {
                                        playerSnapshot.child("votes").getRef().setValue(1);
                                    }
                                }
                            }
                        }
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } else {
            showToast("No player selected");
        }

    }

    private void kickPlayer() {
        if (isPlayerSelectedOnEmergencyMeeting) {
            isPlayerSelectedOnEmergencyMeeting = false;
            selectedPlayerOnEmergencyMeeting = "";
            hasVoted = false;

            Button voteBtn = findViewById(R.id.button3);
            voteBtn.setText("Vote");
            voteBtn.setBackgroundColor(getResources().getColor(R.color.red));

            currentRoomRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot playerSnapshot : snapshot.getChildren()) {
                        String playerUID = playerSnapshot.getKey();
                        if (playerUID != null) {
                            String playerUserName = playerSnapshot.child("username").getValue(String.class);
                            if (playerUserName != null) {
//                                if (playerUserName.equalsIgnoreCase(selectedPlayerOnEmergencyMeeting)) {
//                                    playerSnapshot.getRef().removeValue();
//                                }

                                Integer currentVotes = playerSnapshot.child("votes").getValue(Integer.class);
                                //the player with the highest votes should be removed in this reference loop
                                if (currentVotes != null) {
                                    if (currentVotes > highestVotes) {
                                        highestVotes = currentVotes;
                                        playerToBeKicked = playerUserName;
                                    }
                                }
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


            currentRoomRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot playerSnapshot : snapshot.getChildren()) {
                        String playerUID = playerSnapshot.getKey();
                        if (playerUID != null) {
                            String playerUserName = playerSnapshot.child("username").getValue(String.class);
                            if (playerUserName != null) {
                                if (playerUserName.equalsIgnoreCase(playerToBeKicked)) {
                                    String playerRole = playerSnapshot.child("role").getValue(String.class);
                                    if (playerRole != null) {
                                        if (playerRole.equals("imposter")) {
                                            playerSnapshot.getRef().child("role").setValue("dead");

                                        } else {
                                            playerSnapshot.getRef().child("role").setValue("dead");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


            currentRoomRsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    List<String> names = new ArrayList<>(); // Use a dynamic list to store player names
                    List<String> allPlayersRoles = new ArrayList<>(); // Use a dynamic list to store player names

                    // Check if the data snapshot exists
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            // Check if a valid randomIndex is available
                            if (randomIndex != -1) {
                                if (channelNames[randomIndex].equals(snapshot.getKey())) {


                                    DatabaseReference allPlayerRef = snapshot.getRef().child("players");
                                    DatabaseReference curPlayerRef = snapshot.getRef().child("players").child(currentUser.getUid());

                                    // Set the current player's role in the database
                                    curPlayerRef.child(username).setValue(role);

                                    // Listen for a single value event on the allPlayerRef node
                                    allPlayerRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {


                                            int i = 0;

                                            // Iterate through playerSnapshot in the snapshot
                                            for (DataSnapshot playerSnapshot : snapshot.getChildren()) {


                                                // Get player name from the iterators next key
                                                String playerNameTwo = String.valueOf(playerSnapshot.getChildren().iterator().next().getKey());
                                                String playerRole = String.valueOf(playerSnapshot.getChildren().iterator().next().getValue());
                                                DatabaseReference curPlayerRef = playerSnapshot.getRef().child(playerNameTwo);


                                                if (playerNameTwo != null) {
                                                    if (playerNameTwo.equalsIgnoreCase(playerToBeKicked)) {
                                                        if (playerRole != null) {
                                                            if (playerRole.equals("imposter")) {
                                                                showToast("Imposter was kicked " + playerNameTwo);
                                                                displayToast("Imposter " + playerNameTwo);
                                                                curPlayerRef.setValue("dead");

                                                                DatabaseReference setImposterLeft = FirebaseDatabase.getInstance().getReference("broadcastingInformation");
                                                                setImposterLeft.child("didImposterLeft").setValue(true);
                                                            } else {
                                                                showToast("Crewmate was kicked " + playerNameTwo);
                                                                displayToast("Try Again :)");
                                                                curPlayerRef.setValue("dead");
                                                            }
                                                            startPlayground("update");
                                                        }
                                                    }
                                                }


                                                // Check if the playerNameTwo is not null
                                                if (playerNameTwo != null) {
                                                    names.add(playerNameTwo); // Add names to the list
                                                }
                                                if (playerRole != null) {
                                                    allPlayersRoles.add(playerRole);
                                                }
                                                i++;
                                            }

                                            // Convert the list to an array
                                            String[] playerNamesTwo = names.toArray(new String[0]);
                                            String[] playersRoles = allPlayersRoles.toArray(new String[0]);

                                            // Call method to create player views
                                            createPlayerView(playerNamesTwo, playersRoles);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            // Handle errors during data retrieval
                                        }
                                    });
                                } else {
                                    // Remove the current player's data if not in the correct channel
                                    DatabaseReference curPlayerRef = snapshot.getRef().child("players").child(currentUser.getUid());
                                    curPlayerRef.removeValue();
                                }
                            } else {
                                // Handle the case when randomIndex is not valid
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Display a toast in case of an error during data retrieval
                    showToast("Error: " + databaseError.getMessage());
                }
            });
        } else {
            showToast("No one was kicked");
        }


        emergencyMeetingWrapper.setVisibility(View.GONE);
        emergencyMeetingContainerSpliter1.setVisibility(View.GONE);
        emergencyMeetingContainerSpliter2.setVisibility(View.GONE);
    }

    public void disableEmergencyMeeting(View view) {
        playButtonClickSound();
        emergencyMeetingWrapper.setVisibility(View.GONE);
        emergencyMeetingContainerSpliter1.setVisibility(View.GONE);
        emergencyMeetingContainerSpliter2.setVisibility(View.GONE);
    }

    /**
     * Displays a short-duration toast message with the provided message text.
     *
     * @param message The text to be displayed in the toast.
     */
    private void showToast(String message) {
        Toast.makeText(Playground.this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Finishes the current activity, effectively exiting the game.
     *
     * @param view The view triggering the exit action.
     */
    public void exitGame(View view) {
        playButtonClickSound();
        // Finish the current activity to exit the game
        finish();
//        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    }

    private void removePlayer() {
        // Remove the current player's data from the database
//        DatabaseReference room1Ref = FirebaseDatabase.getInstance().getReference("room1");

        currentRoomRef.child("player_" + currentUser.getUid()).removeValue();

//        currentRoomRsRef.child(channelName).child("players").child(currentUser.getUid()).removeValue();
        if (role.equals("imposter")) {
            DatabaseReference setImposterLeft = FirebaseDatabase.getInstance().getReference("broadcastingInformation");
            setImposterLeft.child("didImposterLeft").setValue(true);

//            showToast("Crewmates won the game!");

            currentRoomData.child(currentRoom).removeValue();
//            currentRoomData.child("isRoomStarted").setValue(false);
        }
        broadcastingRef.removeValue();

//            finish();

    }

    private void playButtonClickSound() {
        // Create and play a button click sound using MediaPlayer
        MediaPlayer mediaPlayer;
        mediaPlayer = MediaPlayer.create(Playground.this, R.raw.button_clicked);
        mediaPlayer.setLooping(false);
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(mp -> {
            mediaPlayer.stop();
            mediaPlayer.release();
        });
    }

    /**
     * Overrides the back button press behavior to play a button click sound.
     */
    @Override
    public void onBackPressed() {
        playButtonClickSound();

    }

    /**
     * Overrides the onDestroy method to ensure proper cleanup when the activity is destroyed.
     */
    @Override
    protected void onDestroy() {

//        // Memory cleanup
//        if (gameStartVoicePlayer != null && gameStartVoicePlayer.isPlaying()) {
//            gameStartVoicePlayer.stop();
//            gameStartVoicePlayer.release();
//        }

        // Remove player from the database
        removePlayer();

        // Check if the Agora engine instance is not null
        if (agoraEngine != null) {
            // Leave the channel and destroy the Agora engine instance
            agoraEngine.leaveChannel();
            RtcEngine.destroy();
            agoraEngine = null;
        }

        //destroy
        super.onDestroy();
    }
}
