package com.programmerxd.wod;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private static final long ANIMATION_DURATION = 1700; // 1 second
    private static final float FROM_X = 0.8f;
    private static final float TO_X = 1.0f;
    private static final float FROM_Y = 0.8f;
    private static final float TO_Y = 1.0f;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        final RelativeLayout rootLayout = findViewById(R.id.rootLayout);

        // Scale Animation
        ScaleAnimation anim = new ScaleAnimation(
                FROM_X, TO_X, FROM_Y, TO_Y,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        anim.setDuration(ANIMATION_DURATION);
        rootLayout.startAnimation(anim);

        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // Animation started
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // Animation ended, start the new activity


                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                if (currentUser == null) {
                    startActivity(new Intent(MainActivity.this, playerData.class));
                    overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                } else {
                    startActivity(new Intent(MainActivity.this, home.class));
                    overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                }
                finish(); // Finish this activity to prevent going back
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // Animation repeated
                anim.cancel();
            }
        });
    }
}
