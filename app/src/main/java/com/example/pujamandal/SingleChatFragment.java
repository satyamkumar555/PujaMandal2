package com.example.pujamandal;

import android.os.Bundle;
import android.view.*;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.*;

public class SingleChatFragment extends Fragment {

    private RecyclerView recyclerView;
    private EditText messageInput;
    private ImageButton sendButton;
    private List<Message> messageList;
    private MessageAdapter messageAdapter;

    private String currentUserId, panditUid, panditName;
    private DatabaseReference chatRef;

    public SingleChatFragment() {}

    public static SingleChatFragment newInstance(String panditUid, String panditName) {
        SingleChatFragment fragment = new SingleChatFragment();
        Bundle args = new Bundle();
        args.putString("panditUid", panditUid);
        args.putString("panditName", panditName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_single_chat, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        messageInput = view.findViewById(R.id.messageInput);
        sendButton = view.findViewById(R.id.sendButton);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(getContext(), messageList);
        recyclerView.setAdapter(messageAdapter);

        if (getArguments() != null) {
            panditUid = getArguments().getString("panditUid");
            panditName = getArguments().getString("panditName");
        }

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        chatRef = FirebaseDatabase.getInstance().getReference("Chats");

        loadMessages();

        sendButton.setOnClickListener(v -> sendMessage());

        return view;
    }

    private void sendMessage() {
        String msg = messageInput.getText().toString().trim();
        if (!msg.isEmpty()) {
            HashMap<String, String> map = new HashMap<>();
            map.put("sender", currentUserId);
            map.put("receiver", panditUid);
            map.put("message", msg);

            chatRef.push().setValue(map);
            messageInput.setText("");
        }
    }

    private void loadMessages() {
        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Message msg = snap.getValue(Message.class);
                    if ((msg.getSender().equals(currentUserId) && msg.getReceiver().equals(panditUid)) ||
                            (msg.getSender().equals(panditUid) && msg.getReceiver().equals(currentUserId))) {
                        messageList.add(msg);
                    }
                }
                messageAdapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(messageList.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
