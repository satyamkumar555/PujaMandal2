package pandit;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.pujamandal.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.*;

public class OrderFragment extends Fragment {

    private RecyclerView recyclerPendingOrders, recyclerAcceptedOrders;
    private PanditOrderAdapter pendingAdapter, acceptedAdapter;
    private List<DocumentSnapshot> pendingList = new ArrayList<>();
    private List<DocumentSnapshot> acceptedList = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String currentPanditId = FirebaseAuth.getInstance().getCurrentUser().getUid();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order, container, false);

        recyclerPendingOrders = view.findViewById(R.id.recyclerPendingOrders);
        recyclerAcceptedOrders = view.findViewById(R.id.recyclerAcceptedOrders);

        recyclerPendingOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerAcceptedOrders.setLayoutManager(new LinearLayoutManager(getContext()));

        pendingAdapter = new PanditOrderAdapter(getContext(), pendingList, false);
        acceptedAdapter = new PanditOrderAdapter(getContext(), acceptedList, true);


        recyclerPendingOrders.setAdapter(pendingAdapter);
        recyclerAcceptedOrders.setAdapter(acceptedAdapter);

        loadAllOrders();

        return view;
    }

    private void loadAllOrders() {
        db.collection("Bookings")
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        pendingList.clear();
                        acceptedList.clear();

                        for (DocumentSnapshot doc : value.getDocuments()) {
                            String status = doc.getString("status");
                            String panditId = doc.getString("panditId");

                            if ("pending".equals(status)) {
                                pendingList.add(doc);
                            } else if (("accepted".equals(status) || "update_requested".equals(status) || "cancel_requested".equals(status) || "cancelled".equals(status))
                                    && currentPanditId.equals(panditId)) {
                                acceptedList.add(doc);
                            }
                        }

                        pendingAdapter.notifyDataSetChanged();
                        acceptedAdapter.notifyDataSetChanged();
                    } else if (error != null) {
                        Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
