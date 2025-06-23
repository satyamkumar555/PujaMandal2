package admin;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;

import com.example.pujamandal.R;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class ItemAdminCancelRequestActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AdminCancelRequestAdapter adapter;
    private List<DocumentSnapshot> cancelList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_admin_cancel_request);

        recyclerView = findViewById(R.id.recyclerViewCancelRequest);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        cancelList = new ArrayList<>();
        adapter = new AdminCancelRequestAdapter(this, cancelList);
        recyclerView.setAdapter(adapter);

        fetchCancelRequests();
    }

    private void fetchCancelRequests() {
        FirebaseFirestore.getInstance()
                .collection("Bookings")
                .whereEqualTo("status", "cancel_requested")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    cancelList.clear();
                    cancelList.addAll(queryDocumentSnapshots.getDocuments());
                    adapter.notifyDataSetChanged();

                    if (cancelList.isEmpty()) {
                        Toast.makeText(this, "No cancel requests found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
