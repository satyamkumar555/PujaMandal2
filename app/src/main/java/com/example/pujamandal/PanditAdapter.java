package com.example.pujamandal;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class PanditAdapter extends RecyclerView.Adapter<PanditAdapter.ViewHolder> {
    private List<PanditModel> panditList;
    private Context context;
    private boolean showPhoneNumber; // ✅ Admin ke liye true, user ke liye false

    public PanditAdapter(List<PanditModel> panditList, Context context, boolean showPhoneNumber) {
        this.panditList = panditList;
        this.context = context;
        this.showPhoneNumber = showPhoneNumber;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pandit, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PanditModel pandit = panditList.get(position);

        holder.panditName.setText(pandit.getName());
        holder.panditCity.setText(pandit.getCity());
        holder.panditExperience.setText("Experience: " + pandit.getExperience() + " years");
        holder.panditRating.setText("⭐ " + pandit.getRating());

        if (showPhoneNumber && pandit.getPhone() != null && !pandit.getPhone().isEmpty()) {
            holder.panditPhone.setVisibility(View.VISIBLE);
            holder.panditPhone.setText("Phone: " + pandit.getPhone());
        } else {
            holder.panditPhone.setVisibility(View.GONE);
        }

        // Image load
        Glide.with(context).load(pandit.getImageUrl()).into(holder.panditImage);
    }

    @Override
    public int getItemCount() {
        return panditList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView panditName, panditCity, panditExperience, panditRating, panditPhone;
        ImageView panditImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            panditName = itemView.findViewById(R.id.panditName);
            panditCity = itemView.findViewById(R.id.panditCity);
            panditExperience = itemView.findViewById(R.id.panditExperience);
            panditRating = itemView.findViewById(R.id.panditRating);
            panditPhone = itemView.findViewById(R.id.panditPhone); // ✅ New phone TextView
            panditImage = itemView.findViewById(R.id.panditImage);
        }
    }
}
