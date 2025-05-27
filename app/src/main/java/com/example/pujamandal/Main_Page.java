package com.example.pujamandal;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class Main_Page extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNavigationView;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        // ✅ Initialize Views
        drawerLayout = findViewById(R.id.drawer_layout);
        bottomNavigationView = findViewById(R.id.bottom_nav);
        navigationView = findViewById(R.id.nav_view);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


        // ✅ Setup Toolbar & Navigation Drawer Toggle
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // ✅ Default Fragment Load Karein
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new DashboardFragment()).commit();
            bottomNavigationView.setSelectedItemId(R.id.nav_dashboard);
        }

    // ✅ Bottom Navigation Click Listener
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_dashboard) {
                selectedFragment = new DashboardFragment();
            } else if (itemId == R.id.nav_bookings) {
                selectedFragment = new BookingsFragment();
            }else if (itemId == R.id.nav_chat){
                selectedFragment = new ChatFragment();
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            } else if (itemId == R.id.nav_pandits) {
                selectedFragment = new PanditListFragment();  //
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
            }

            return true;
        });
        Menu menu = navigationView.getMenu();
        MenuItem loginLogoutItem = menu.findItem(R.id.menu_login);
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            loginLogoutItem.setTitle("Logout");
        } else {
            loginLogoutItem.setTitle("Login");
        }

        // ✅ Navigation Drawer Click Listener
        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Fragment selectedFragment = null;

            if (itemId == R.id.menu_home) {
                selectedFragment = new DashboardFragment();
            } else if (itemId == R.id.menu_bookings) {
                selectedFragment = new BookingsFragment();
            } else if (itemId == R.id.menu_profile) {
                selectedFragment = new ProfileFragment();
            } else if (itemId == R.id.menu_pandits) {
                selectedFragment = new PanditListFragment();
            } else if (itemId == R.id.menu_login) {
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    // 🔐 Logout
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(Main_Page.this, LoginActivity.class));
                    finish();
                } else {
                    // 🔑 Go to Login
                    startActivity(new Intent(Main_Page.this, LoginActivity.class));
                    finish();
                }
                return true;
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
            }

            drawerLayout.closeDrawers();
            return true;
        });
        updateHeader();

    }
    private void logoutUser() {
        mAuth.signOut(); // Sign out from Firebase
        Toast.makeText(this, "Logged Out!", Toast.LENGTH_SHORT).show();
        recreate(); // Refresh the activity to update UI
    }

    // Update the navigation drawer header with user details
    private void updateHeader() {
        // Get the header layout view
        View headerView = navigationView.getHeaderView(0);
        TextView userNameTextView = headerView.findViewById(R.id.UserName); // Username TextView
        TextView emailTextView = headerView.findViewById(R.id.gmail); // Email TextView

        FirebaseUser user = mAuth.getCurrentUser(); // Get the current Firebase user
        if (user != null) {
            // Display email in the header
            String email = user.getEmail();
            emailTextView.setText(email != null ? email : "Email not available");

            // Fetch user data from Firestore using UID
            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
            firebaseFirestore.collection("users") // Replace "Users" with your Firestore collection name
                    .document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name"); // Fetch 'Name' field from Firestore
                            if (name != null && !name.isEmpty()) {
                                userNameTextView.setText(name); // Set fetched name
                            } else {
                                userNameTextView.setText("User"); // Fallback name
                            }
                        } else {
                            userNameTextView.setText("User"); // Document does not exist
                        }
                    })
                    .addOnFailureListener(e -> {
                        userNameTextView.setText("User"); // Fallback name on error
                    });
        } else {
            // Default text if no user is logged in
            userNameTextView.setText("UserName");
            emailTextView.setText("guest@example.com");
        }
    }
}
