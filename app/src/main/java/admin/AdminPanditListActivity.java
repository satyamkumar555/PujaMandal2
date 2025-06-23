package admin;

import android.os.Bundle;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pujamandal.PanditAdapter;
import com.example.pujamandal.PanditModel;
import com.example.pujamandal.R;
import com.google.firebase.firestore.*;
import java.util.ArrayList;
import java.util.List;

public class AdminPanditListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PanditAdapter adapter;
    private List<PanditModel> panditList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.pujamandal.R.layout.activity_admin_pandit_list);

        recyclerView = findViewById(R.id.recycler_admin_pandits);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        panditList = new ArrayList<>();
        adapter = new PanditAdapter(panditList, this, true);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        fetchPandits();
    }

    private void fetchPandits() {
        db.collection("Pandits")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    panditList.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        PanditModel pandit = new PanditModel();
                        pandit.setName(document.getString("name"));
                        pandit.setCity(document.getString("city"));
                        pandit.setExperience(document.getString("experience"));
                        pandit.setPhone(document.getString("phone"));
                        pandit.setImageUrl(document.getString("imageBase64"));

                        Double rating = document.getDouble("avgRating");
                        Long total = document.getLong("totalRatings");
                        pandit.setAvgRating(rating != null ? rating : 0.0);
                        pandit.setTotalRatings(total != null ? total : 0);

                        pandit.setId(document.getId());  // ✅ Important

                        panditList.add(pandit);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("AdminFirestoreError", "Error fetching pandits", e));
    }

}
