package admin;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pujamandal.R;
import com.example.pujamandal.UserAdapter;
import com.example.pujamandal.UserModel;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminUserListActivity extends AppCompatActivity {

    RecyclerView userRecyclerView;
    UserAdapter userAdapter;
    List<UserModel> userList = new ArrayList<>();
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_list);

        userRecyclerView = findViewById(R.id.userRecyclerView);
        userRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();

        fetchUsers();
    }

    private void fetchUsers() {
        db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot snapshots = task.getResult();
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            UserModel user = doc.toObject(UserModel.class);
                            userList.add(user);
                        }
                        userAdapter = new UserAdapter(userList);
                        userRecyclerView.setAdapter(userAdapter);
                    } else {
                        Log.e("USER_FETCH", "Error fetching users", task.getException());
                    }
                });
    }
}

