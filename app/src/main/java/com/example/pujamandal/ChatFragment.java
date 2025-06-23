package com.example.pujamandal;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment {

    private RecyclerView recyclerView;
    private ChatUserAdapter adapter;
    private List<chatuser> userList;
    private DatabaseReference chatListRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        userList = new ArrayList<>();
        adapter = new ChatUserAdapter(userList, getContext());
        recyclerView.setAdapter(adapter);

        fetchChatList();

        return view;
    }

    private void fetchChatList() {
        chatListRef = FirebaseDatabase.getInstance().getReference("ChatList");

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // ChatList/currentUserUid ke under sirf wahi log hain jinke sath user ne baat ki
        chatListRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();

                for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
                    String otherUserId = chatSnapshot.getKey();

                    // 👇 Sirf dusre user ka data lena (Pandit ka)
                    String name = chatSnapshot.child("name").getValue(String.class);
                    String phone = chatSnapshot.child("phone").getValue(String.class);

                    if (name != null && phone != null) {
                        chatuser user = new chatuser();
                        user.setUid(currentUserId);         // Logged-in user
                        user.setPanditUid(otherUserId);     // Pandit UID
                        user.setName(name);                 // Pandit name
                        user.setPhone(phone);               // Pandit phone
                        userList.add(user);
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ChatFragment", "Database Error: " + error.getMessage());
            }
        });
    }

}