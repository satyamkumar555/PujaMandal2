package com.example.pujamandal;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatUserAdapter extends RecyclerView.Adapter<ChatUserAdapter.ViewHolder> {
    private List<chatuser> userList;
    private Context context;

    public ChatUserAdapter(List<chatuser> userList, Context context) {
        this.userList = userList;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_user_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        chatuser user = userList.get(position);
        holder.name.setText(user.getName());
        holder.phone.setText(user.getPhone());

        holder.itemView.setOnClickListener(v -> {
            SingleChatFragment chatFragment = SingleChatFragment.newInstance(
                    user.getPanditUid(),
                    user.getName()
            );

            // Context se FragmentManager lene ke liye typecast
            FragmentActivity activity = (FragmentActivity) context;
            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, chatFragment) // 🔁 Replace your current container ID
                    .addToBackStack(null)
                    .commit();
        });
    }




    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, phone;

        public ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.nameText);
            phone = itemView.findViewById(R.id.phoneText);
        }
    }

}
