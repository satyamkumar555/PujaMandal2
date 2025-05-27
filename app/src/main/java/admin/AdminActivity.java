package admin;

import static com.example.pujamandal.R.*;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pujamandal.LoginActivity;
import com.example.pujamandal.Main_Page;
import com.example.pujamandal.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Button btnLogout, panditList,userList,managePandits, Addpujas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnLogout = findViewById(R.id.admin_logout);
        userList = findViewById(R.id.viewUserList);
        panditList = findViewById(R.id.viewPanditList);
        managePandits=findViewById(R.id.managePandits);
        Addpujas = findViewById(R.id.AddPuja);

        panditList.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, AdminPanditListActivity.class);
            startActivity(intent);
        });

        userList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdminActivity.this, AdminUserListActivity.class);
                startActivity(intent);
            }
        });
        managePandits.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdminActivity.this, AddPanditActivity.class);
                startActivity(intent);
            }
        });

        Addpujas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdminActivity.this, AddPujaActivity.class);
                startActivity(intent);
            }
        });

        // ✅ Logout Button Click => Logout & Redirect to Login
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(AdminActivity.this, LoginActivity.class));
            finish();
        });

        checkAdminAccess(); // ✅ Check Admin Access
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkAdminAccess();
    }


    private void checkAdminAccess() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            startActivity(new Intent(AdminActivity.this, LoginActivity.class));
            finish();
            return;
        }

        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");

                        if (!"admin".equals(role)) {
                            Toast.makeText(AdminActivity.this, "Access Denied! Not an Admin.", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(AdminActivity.this, Main_Page.class));
                            finish();
                        }
                    } else {
                        Toast.makeText(AdminActivity.this, "User role not found!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(AdminActivity.this, LoginActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AdminActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(AdminActivity.this, LoginActivity.class));
                    finish();
                });
    }
}
