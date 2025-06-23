package pandit;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pujamandal.R;
import java.util.List;

public class ChatUserAdapter extends RecyclerView.Adapter<ChatUserAdapter.ViewHolder> {
    private List<ChatUser> userList;
    private Context context;

    public ChatUserAdapter(List<ChatUser> userList, Context context) {
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
        ChatUser user = userList.get(position);
        holder.name.setText(user.getName());
        holder.phone.setText(user.getPhone());

        holder.itemView.setOnClickListener(v -> {
            PanditChatFragment chatFragment = PanditChatFragment.newInstance(
                    user.getUid(),
                    user.getName()
            );

            FragmentActivity activity = (FragmentActivity) context;
            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, chatFragment)
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
