package com.programmerxd.wod;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

    private static final int PERMISSION_REQ_ID = 22;
    private static final String[] REQUESTED_PERMISSIONS =
            {
                    Manifest.permission.RECORD_AUDIO
            };
    private final String[] currentRadioServer = new String[5]; // Array to hold the 5 channels tokens
    private final String[] currentRadioServerFrequency = {"72.34", "24.57", "92.64", "39.23", "58.79"}; // Array to hold the 5 channels frequency
    // Fill the App ID of your project generated on Agora Console.
    private final String appId = "3f393ce1fa6b4c6b80495f09c07f5d34";
    // Fill the channel name.
    private final String[] channelNames = {"rs1", "rs2", "rs3", "rs4", "rs5"};
    // Fill the temp token generated on Agora Console.
//    private String token = "007eJxTYFBv+fRMOkFLUWzddkHZ85z2lwQDn/Em60XoCwc4iZpqblFgME4ztjROTjVMSzRLMkk2S7IwMLE0TTOwTDYwTzNNMTbZ3OOc2hDIyOByYwkLIwMEgvgsDCWpxSUMDADxAhs0";
//    private String token = tokens[1];
    // An integer that identifies the local user.
    private final int uid = 0;
    public String channelName = channelNames[0];
    ImageView roleIndicator;
    boolean isPlayerRoleContainerClicked = true;
    //    private TextView infoText;
    private DatabaseReference room1rsRef;
    // Track the status of your connection
    private boolean isJoined = false;
    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        // Listen for the remote user joining the channel.
        public void onUserJoined(int uid, int elapsed) {
//            runOnUiThread(()->infoText.setText("Remote user joined: " + uid));
            runOnUiThread(() -> Toast.makeText(Playground.this, "Remote user joined: ", Toast.LENGTH_SHORT).show());
//            showToast("Remote user joined: " );
        }

        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            // Successfully joined a channel
            isJoined = true;
//            showToast("Joined Channel " + channel);
//            runOnUiThread(()->infoText.setText("Waiting for a remote user to join"));
//            runOnUiThread(()->infoText.setText("Waiting for a remote user to join"));
            runOnUiThread(() -> Toast.makeText(Playground.this, "Joined Channel ", Toast.LENGTH_SHORT).show());
        }

        @Override
        public void onUserOffline(int uid, int reason) {
            // Listen for remote users leaving the channel
//            showToast("Remote user offline " + uid + " " + reason);
            runOnUiThread(() -> Toast.makeText(Playground.this, "Remote user offline ", Toast.LENGTH_SHORT).show());
//            if (isJoined) runOnUiThread(()->infoText.setText("Waiting for a remote user to join"));
        }

        @Override
        public void onLeaveChannel(RtcStats stats) {
            // Listen for the local user leaving the channel
//            runOnUiThread(()->infoText.setText("Press the button to join a channel"));
            isJoined = false;
        }
    };
    private HorizontalScrollView playersContainer;
    private DatabaseReference room1Ref;
    private DatabaseReference users;
    private String[] playerNames = new String[10];
    // Agora engine instance
    private RtcEngine agoraEngine;
    private int randomIndex;
    private int time_countdown = 10;
    private String role;
    private CountDownTimer countdownTimer;
    private boolean isMicOn = false; // Flag to track the microphone state
    private AudioRecord audioRecorder;
    private AudioTrack audioTrack;

    public void pushToTalk(View view) {
        ImageView microphoneIcon = findViewById(R.id.microphoneIcon);

        if (isMicOn) {
            // If mic is on, turn it off
            // Your logic to turn off the microphone
            // For example:
            stopMicrophone(); // Your method to stop microphone

            // Change the icon to mic off
            microphoneIcon.setImageResource(R.drawable.mic_mute);
            isMicOn = false;
        } else {
            // If mic is off, turn it on
            // Your logic to turn on the microphone
            // For example:
            startMicrophone(); // Your method to start the microphone


            // Change the icon to mic on
            microphoneIcon.setImageResource(R.drawable.mic_unmute);
            isMicOn = true;
        }
    }

    private void startMicrophone() {
        int audioSource = MediaRecorder.AudioSource.MIC;
        int sampleRate = 44100;
        int channelConfig = AudioFormat.CHANNEL_IN_MONO;
        int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
        int bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        audioRecorder = new AudioRecord(audioSource, sampleRate, channelConfig, audioFormat, bufferSize);
        audioRecorder.startRecording();

        // You may want to process or store the audio data received from the microphone
        // For example: read the audio data from audioRecorder and do something with it
        // byte[] audioBuffer = new byte[bufferSize];
        // audioRecorder.read(audioBuffer, 0, bufferSize);

        // In a production environment, you would typically handle this data differently
        // For example: send it to a network or store it in a file
    }

    private void stopMicrophone() {
        if (audioRecorder != null) {
            audioRecorder.stop();
            audioRecorder.release();
            audioRecorder = null;
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Hide the navigation bar
//        int flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                | View.SYSTEM_UI_FLAG_FULLSCREEN;
//        getWindow().getDecorView().setSystemUiVisibility(flags);

        setContentView(R.layout.activity_playground);
//        infoText = findViewById(R.id.infoText);
        roleIndicator = findViewById(R.id.roleIndicator);

        // If all the permissions are granted, initialize the RtcEngine object and join a channel.
        if (!checkSelfPermission()) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, PERMISSION_REQ_ID);
        }

        setupVoiceSDKEngine();

        // Play game sounds
        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.game_start_voice);
        mediaPlayer.setLooping(false);
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                mediaPlayer.stop();
                mediaPlayer.release();

            }

        });


        playersContainer = findViewById(R.id.playersContainer);


        users = FirebaseDatabase.getInstance().getReference("users");

        users.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> names = new ArrayList<>(); // Use a dynamic list to store player names

                for (DataSnapshot playerSnapshot : dataSnapshot.getChildren()) {
                    String playerName = playerSnapshot.child("username").getValue(String.class); // Get player name

                    if (playerName != null) {
                        names.add(playerName); // Add names to the list
                    }
                }

                playerNames = names.toArray(new String[0]); // Convert list to array
                createPlayerView(); // Call method to create player views
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors
            }
        });


        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference room1Ref = FirebaseDatabase.getInstance().getReference("room1");

        room1Ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean roleFound = false;

                for (DataSnapshot playerSnapshot : dataSnapshot.getChildren()) {
                    String playerUID = playerSnapshot.getKey();
//                    showToast("Finding your role");
//                    showToast(currentUser.getUid());
                    if (playerUID != null && playerUID.equals("player_" + currentUser.getUid())) {
                        role = playerSnapshot.getValue(String.class);
                        if (role != null) {
                            if (role.equals("imposter")) {
                                showToast("You are an imposter!");
                                roleIndicator.setImageResource(R.drawable.imposter_toast);
                                roleFound = true;

//                                    playerRole("imposter");
                                break;
                            } else {
                                showToast("You are a crewmate!");
                                roleIndicator.setImageResource(R.drawable.crew_toast);
                                roleFound = true;

//                                playerRole("crewmate");
                                break;
                            }
                        }
                    }
                }
//                if (!roleFound) {
//                    showToast("You are not in the room or role not set.");
//                }
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {
                showToast("Error: " + databaseError.getMessage());
            }
        });


        room1rsRef = FirebaseDatabase.getInstance().getReference("room1rs");

        // Fetch channel tokens from the "room1rs" reference
        room1rsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    int i = 0;
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        currentRadioServer[i] = snapshot.getValue(String.class);
                        i++;
                    }

                    // Randomly assign a token to the current user
                    assignToken();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                showToast("Error: " + databaseError.getMessage());
            }
        });
    }

    private void createPlayerView() {
        playersContainer.removeAllViews(); // Clear the container before updating

        LinearLayout horizontalLayout = new LinearLayout(this);
        horizontalLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);

        for (String playerName : playerNames) {
            if (playerName != null) {
                LinearLayout playerLayout = new LinearLayout(this);


                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );


                playerLayout.setOrientation(LinearLayout.VERTICAL);
                playerLayout.setPadding(dpToPx(5), dpToPx(5), dpToPx(5), dpToPx(5)); // Add padding
                params.weight = 1;
                playerLayout.setLayoutParams(params);


                ImageView playerImage = new ImageView(this);
                LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                        dpToPx(50), // Convert dp to pixels
                        dpToPx(50)
                );
                playerImage.setLayoutParams(imageParams);
                playerImage.setImageResource(R.drawable.player_temp);

                TextView playerNameTextView = new TextView(this);
                playerNameTextView.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));
                playerNameTextView.setText(playerName);
                playerNameTextView.setGravity(Gravity.CENTER);
                playerNameTextView.setTextColor(getResources().getColor(R.color.white));
                playerNameTextView.setTypeface(getResources().getFont(R.font.vertigo_flf_bold));

                // Add the image and text view to the player's layout
                playerLayout.addView(playerImage);
                playerLayout.addView(playerNameTextView);

                // Add the player's layout to the horizontal layout
                horizontalLayout.addView(playerLayout);
            }
        }

        // Add the horizontal layout to the playersContainer (HorizontalScrollView)
        playersContainer.addView(horizontalLayout);
    }


    // Helper method to convert dp to pixels
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    // Add this method in your Playground activity
    private void startSpeechRecognition() {
        // Use Intent to start the speech recognition activity
        Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say 'kill' followed by the player's name");

        try {
            startActivityForResult(speechRecognizerIntent, PERMISSION_REQ_ID);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Speech recognition not supported", Toast.LENGTH_SHORT).show();
        }
    }

    // Override the onActivityResult method to handle the speech recognition result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PERMISSION_REQ_ID && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            if (result != null && !result.isEmpty()) {
                String commandSpoken = result.get(0); // Get the first recognized phrase

                // Call the logic to process the recognized command
                processRecognizedCommand(commandSpoken);
            }
        }
    }

    private void processRecognizedCommand(String commandSpoken) {
        if (commandSpoken.toLowerCase().startsWith("kill")) {
            // Extract the player name from the recognized command
            String[] spokenWords = commandSpoken.split(" ");
            if (spokenWords.length >= 2) {
                String playerName = spokenWords[1]; // Extract the player name

                // Process the kill command and show toast
                showToast("Killing " + playerName);

                // Perform actions based on the recognized command, such as starting the kill countdown
                // Example: startKillCountdown(playerName);
            }
        }
    }

    public void playerRole(View view) {
        LinearLayout roleIndicatorContainer = (LinearLayout) view; // Get the clicked layout
        TextView killCountdown = roleIndicatorContainer.findViewById(R.id.killCountdown);
        if (isPlayerRoleContainerClicked) {
            isPlayerRoleContainerClicked = false;
            if (role.equals("imposter")) {
                roleIndicator.setVisibility(View.GONE);
                killCountdown.setVisibility(View.VISIBLE);
                killCountdown.setText(String.valueOf(time_countdown)); // Set initial countdown value
                startSpeechRecognition();

                countdownTimer = new CountDownTimer(10000, 1000) { // 10 seconds countdown (10,000 milliseconds)
                    public void onTick(long millisUntilFinished) {
                        time_countdown--;
                        killCountdown.setText(String.valueOf(time_countdown)); // Update countdown text
                    }

                    public void onFinish() {

                        roleIndicator.setVisibility(View.VISIBLE);
                        killCountdown.setVisibility(View.GONE);
                        time_countdown = 10;
                        isPlayerRoleContainerClicked = true;
                        // Perform actions when the countdown finishes
                        // For example, hide the countdown or trigger an action
                    }
                }.start();
            } else {
                showToast("Your tasks");
            }
        }
    }

    private boolean checkSelfPermission() {
        return ContextCompat.checkSelfPermission(this, REQUESTED_PERMISSIONS[0]) == PackageManager.PERMISSION_GRANTED;
    }

    private void setupVoiceSDKEngine() {
        try {
            // Initialize Agora RtcEngineConfig
            RtcEngineConfig config = new RtcEngineConfig();
            config.mContext = getBaseContext();
            config.mAppId = appId;
            config.mEventHandler = mRtcEventHandler;
            // Create Agora RtcEngine
            agoraEngine = RtcEngine.create(config);
        } catch (Exception e) {
            e.printStackTrace(); // Handle errors more explicitly
            throw new RuntimeException("Check the error: " + e.getMessage());
        }
    }

    private void assignToken() {
        if (currentRadioServer != null) {
            // Generate a random index within the range of available tokens
            randomIndex = new Random().nextInt(currentRadioServer.length);
            TextView textCurrentRadioServerFrequency = findViewById(R.id.currentRadioServerFrequency);
            channelName = channelNames[randomIndex];

            // Get the token at the randomly generated index
            String userToken = currentRadioServer[randomIndex];
            textCurrentRadioServerFrequency.setText(currentRadioServerFrequency[randomIndex]);


            // Proceed to connect to the channel with the assigned token using the Agora SDK
            connectToChannel(userToken);
        } else {
            showToast("No channel tokens available");
        }
    }

    private void connectToChannel(String userToken) {
        ChannelMediaOptions options = new ChannelMediaOptions();
        options.autoSubscribeAudio = true;
        options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;
        options.channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING;

        // Log Agora channel connection details (consider proper logging)
        Log.d("Agora", "Connecting to channel: " + channelName + " with token: " + userToken + " and UID: " + uid);

        if (agoraEngine != null) {
            // Check if the user is already in a channel, then leave before joining a new one
            if (agoraEngine.getConnectionState() == Constants.CONNECTION_STATE_CONNECTED) {
                agoraEngine.leaveChannel();
//                showToast("Leaving current channel to join a new one...");
            }
        }

        // Join the channel with the Agora engine
        agoraEngine.joinChannel(userToken, channelName, uid, options);

        // Use logs for debugging, but not excessively
        Log.d("Agora", "Attempted to join channel");

//        showToast("Connecting to channel with channel name: " + channelName + " and token: " + userToken);

        // Start Agora channel with user token
        startPlayground(userToken);
    }

    private void startPlayground(String userToken) {
        // This method is a mockup and should contain your Agora SDK logic to join a channel using the 'userToken'.
        // You might want to instantiate the Agora engine, set up event listeners, and join a channel.
        // Below is a simple toast to indicate the selected channel token (userToken).
//
//        showToast("Connecting to channel with channel name : "  + channelName + " and  token : " + userToken);
        // Perform Agora SDK logic to join the channel using 'userToken'
    }

    public void shuffleRadioServer(View view) {
        assignToken();
    }

    public void previousRadioServer(View view) {
        randomIndex--;
        if (randomIndex < 0) randomIndex = 4;
        TextView textCurrentRadioServerFrequency = findViewById(R.id.currentRadioServerFrequency);
        textCurrentRadioServerFrequency.setText(currentRadioServerFrequency[randomIndex]);
        channelName = channelNames[randomIndex];


        agoraEngine.leaveChannel();
        connectToChannel(currentRadioServer[randomIndex]);

    }

    public void nextRadioServer(View view) {
        randomIndex++;
        if (randomIndex > 4) randomIndex = 0;
        TextView textCurrentRadioServerFrequency = findViewById(R.id.currentRadioServerFrequency);
        textCurrentRadioServerFrequency.setText(currentRadioServerFrequency[randomIndex]);
        channelName = channelNames[randomIndex];


        agoraEngine.leaveChannel();
        connectToChannel(currentRadioServer[randomIndex]);
    }

    private void showToast(String message) {
        Toast.makeText(Playground.this, message, Toast.LENGTH_SHORT).show();
    }

    public void exitGame(View view){
        finish();
    }
@Override
public void onBackPressed() {
    MediaPlayer mediaPlayer;
    mediaPlayer = MediaPlayer.create(Playground.this, R.raw.button_clicked);
    mediaPlayer.setLooping(false); // Set looping to continuously play the audio
    mediaPlayer.start();
    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer mp) {
            mediaPlayer.stop();
            mediaPlayer.release();

        }

    });
}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (agoraEngine != null) {
            new Thread(() -> {
                agoraEngine.leaveChannel();
                RtcEngine.destroy();
                agoraEngine = null;
            }).start();
        }
    }

}
