package pandit;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.pujamandal.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment {

    private RecyclerView recyclerView;
    private ChatUserAdapter adapter;
    private List<ChatUser> userList;
    private DatabaseReference chatUsersRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_chat2, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        userList = new ArrayList<>();
        adapter = new ChatUserAdapter(userList, getContext());
        recyclerView.setAdapter(adapter);

        fetchChatUsers();

        return view;
    }

    private void fetchChatUsers() {
        String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        chatUsersRef = FirebaseDatabase.getInstance().getReference("ChatList").child(currentUid);

        chatUsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot userSnap : snapshot.getChildren()) {
                    ChatUser user = userSnap.getValue(ChatUser.class);
                    if (user != null && user.getUid() != null && !user.getUid().equals(currentUid)) {
                        userList.add(user);
                    }
                }
                adapter.notifyDataSetChanged();
            }


            @Override
            public void onCancelled(DatabaseError error) {
                // Log error
            }
        });
    }
}
