package pandit;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.pujamandal.LoginActivity;
import com.example.pujamandal.R;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class PanditMainPage extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pandit_main_page);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        // ✅ Setup Toolbar & Drawer Toggle
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // ✅ Default fragment on launch
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new DashboardFragment())
                    .commit();
            navigationView.setCheckedItem(R.id.nav_dashboardd);
        }

        // ✅ Menu Item Click Listener
        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Fragment selectedFragment = null;

            if (itemId == R.id.nav_orderr) {
                selectedFragment = new OrderFragment();
            } else if (itemId == R.id.nav_chatt) {
                selectedFragment = new ChatFragment();
            } else if (itemId == R.id.nav_dashboardd) {
                selectedFragment = new DashboardFragment();
            } else if (itemId == R.id.nav_profilee) {
                selectedFragment = new ProfileFragment();
            } else if (itemId == R.id.nav_logout) {
                // 🔐 Logout
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(PanditMainPage.this, LoginActivity.class));
                finish();
                return true;
            }

            // ✅ Load selected fragment
            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }

            drawerLayout.closeDrawers();
            return true;
        });
    }
}
