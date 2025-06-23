package pandit;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pujamandal.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private final Context context;
    private final List<com.example.pujamandal.Message> messageList;
    private final String currentUserId;

    public MessageAdapter(List<com.example.pujamandal.Message> messageList, String currentUserId, Context context) {
        this.messageList = messageList;
        this.currentUserId = currentUserId;
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (messageList.get(position).getSender().equals(currentUid)) {
            return 1; // sender
        } else {
            return 0; // receiver
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 1) {
            // Sender message - right align
            view = LayoutInflater.from(context).inflate(R.layout.item_chat_sent, parent, false);
        } else {
            // Receiver message - left align
            view = LayoutInflater.from(context).inflate(R.layout.item_chat_received, parent, false);
        }
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.msgText.setText(messageList.get(position).getMessage());
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView msgText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            msgText = itemView.findViewById(R.id.msgText);
        }
    }
}
