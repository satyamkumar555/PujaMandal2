package com.example.pujamandal;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashScreen extends AppCompatActivity {

    private static final int SPLASH_TIME = 2000; // 2 seconds

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        new Handler().postDelayed(() -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                // 🔵 User is already logged in, go to Main Page
                startActivity(new Intent(SplashScreen.this, Main_Page.class));
            } else {
                // 🔴 User not logged in, go to Login Page
                startActivity(new Intent(SplashScreen.this, LoginActivity.class));
            }
            finish();
        }, SPLASH_TIME);
    }
}
