package com.example.pujamandal;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

public class SplashScreen extends AppCompatActivity {

    private static final int SPLASH_TIME = 2000; // 2 seconds
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        db = FirebaseFirestore.getInstance();

        new Handler().postDelayed(() -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

            if (currentUser == null) {
                startActivity(new Intent(SplashScreen.this, LoginActivity.class));
                finish();
                return;
            }

            // ✅ Check user's role from Firestore
            db.collection("users").document(currentUser.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String role = documentSnapshot.getString("role");

                            if (role == null) role = "user"; // default fallback

                            switch (role.toLowerCase()) {
                                case "admin":
                                    startActivity(new Intent(SplashScreen.this, admin.AdminActivity.class));
                                    break;
                                case "pandit":
                                    startActivity(new Intent(SplashScreen.this, pandit.PanditMainPage.class));
                                    break;
                                default:
                                    startActivity(new Intent(SplashScreen.this, Main_Page.class)); // user
                                    break;
                            }
                        } else {
                            Toast.makeText(this, "User data not found!", Toast.LENGTH_SHORT).show();
                            FirebaseAuth.getInstance().signOut();
                            startActivity(new Intent(this, LoginActivity.class));
                        }
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to fetch user role", Toast.LENGTH_SHORT).show();
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(this, LoginActivity.class));
                        finish();
                    });

        }, SPLASH_TIME);
    }
}
