package pandit;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pujamandal.Message;
import com.example.pujamandal.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class PanditChatFragment extends Fragment {

    private String userUid, userName;
    private String panditUid;

    private EditText inputMessage;
    private ImageButton sendBtn;
    private RecyclerView recyclerView;

    private MessageAdapter adapter;
    private List<Message> messageList;

    public static PanditChatFragment newInstance(String userUid, String userName) {
        PanditChatFragment fragment = new PanditChatFragment();
        Bundle args = new Bundle();
        args.putString("userUid", userUid);
        args.putString("userName", userName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pandit_chat, container, false);

        // Get arguments
        if (getArguments() != null) {
            userUid = getArguments().getString("userUid");
            userName = getArguments().getString("userName");
        }

        panditUid = FirebaseAuth.getInstance().getUid();

        inputMessage = view.findViewById(R.id.inputMessage);
        sendBtn = view.findViewById(R.id.sendBtn);
        recyclerView = view.findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        messageList = new ArrayList<>();
        adapter = new MessageAdapter(messageList, panditUid, getContext());
        recyclerView.setAdapter(adapter);


        sendBtn.setOnClickListener(v -> sendMessage());

        loadMessages();

        return view;
    }

    private void sendMessage() {
        String msg = inputMessage.getText().toString().trim();
        if (TextUtils.isEmpty(msg)) return;

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Chats");
        String messageId = ref.push().getKey();

        Message message = new Message(panditUid, userUid, msg);
        ref.child(messageId).setValue(message);

        DatabaseReference chatListRef = FirebaseDatabase.getInstance().getReference("ChatList");

        // ✅ Step 1: Get user's full info (name + phone) from Firestore before setting
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("Users").document(userUid)
                .get()
                .addOnSuccessListener(userDoc -> {
                    if (userDoc.exists()) {
                        String userName = userDoc.getString("name");
                        String userPhone = userDoc.getString("phone");

                        ChatUser userChatUser = new ChatUser();
                        userChatUser.setUid(userUid);
                        userChatUser.setName(userName);
                        userChatUser.setPhone(userPhone);

                        chatListRef.child(panditUid).child(userUid).setValue(userChatUser); // ✅ full user info
                    }
                });

        // ✅ Step 2: Get Pandit info and set it in user's chat list
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("Pandits").document(panditUid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String panditName = doc.getString("name");
                        String panditPhone = doc.getString("phone");

                        ChatUser panditChatUser = new ChatUser();
                        panditChatUser.setUid(panditUid);
                        panditChatUser.setName(panditName);
                        panditChatUser.setPhone(panditPhone);

                        chatListRef.child(userUid).child(panditUid).setValue(panditChatUser);
                    }
                });

        inputMessage.setText("");
    }


    private void loadMessages() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Chats");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Message msg = ds.getValue(Message.class);
                    if (msg != null &&
                            ((msg.getSender().equals(panditUid) && msg.getReceiver().equals(userUid)) ||
                                    (msg.getSender().equals(userUid) && msg.getReceiver().equals(panditUid)))) {
                        messageList.add(msg);
                    }
                }
                adapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(messageList.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
}
