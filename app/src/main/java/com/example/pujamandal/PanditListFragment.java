package com.example.pujamandal;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.*;
import java.util.ArrayList;
import java.util.List;

public class PanditListFragment extends Fragment {

    private RecyclerView recyclerView;
    private PanditAdapter adapter;
    private List<PanditModel> panditList;
    private FirebaseFirestore db;

    public PanditListFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pandit_list, container, false);

        recyclerView = view.findViewById(R.id.recycler_pandits);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        panditList = new ArrayList<>();
        adapter = new PanditAdapter(panditList, getContext(), false); // user side: phone number hidden
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        fetchPandits();

        return view;
    }

    private void fetchPandits() {
        db.collection("Pandits")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    panditList.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        PanditModel pandit = document.toObject(PanditModel.class);
                        if (pandit != null) {
                            panditList.add(pandit);
                            Log.d("FirestoreData", "Fetched: " + pandit.getName()); // ✅ Log data
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Error fetching data", e); // ✅ Log error
                });
    }
}
