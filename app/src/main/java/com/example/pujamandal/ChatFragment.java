package com.example.pujamandal;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pujamandal.Message;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;


public class ChatFragment extends Fragment {

    private RecyclerView recyclerView;
    private EditText etMessage;
    private ImageButton btnSend;
    private ChatAdapter chatAdapter;
    private List<Message> chatList;

    private DatabaseReference chatRef;
    private String currentUserId, panditId = "PANDIT_ID_HARDCODE"; // Replace later

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewChat);
        etMessage = view.findViewById(R.id.etMessage);
        btnSend = view.findViewById(R.id.btnSend);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatList = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatList, currentUserId);
        recyclerView.setAdapter(chatAdapter);

        chatRef = FirebaseDatabase.getInstance().getReference("Chats").child(currentUserId + "_" + panditId);

        // Load messages
        chatRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
                Message msg = snapshot.getValue(Message.class);
                chatList.add(msg);
                chatAdapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(chatList.size() - 1);
            }

            public void onChildChanged(DataSnapshot snapshot, String previousChildName) {}
            public void onChildRemoved(DataSnapshot snapshot) {}
            public void onChildMoved(DataSnapshot snapshot, String previousChildName) {}
            public void onCancelled(DatabaseError error) {}
        });

        btnSend.setOnClickListener(v -> {
            String msg = etMessage.getText().toString().trim();
            if (!msg.isEmpty()) {
                Message m = new Message(currentUserId, panditId, msg, System.currentTimeMillis());
                chatRef.push().setValue(m);
                etMessage.setText("");
            }
        });

        return view;
    }
}