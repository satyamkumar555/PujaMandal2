package com.example.pujamandal;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.SpannableString;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


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
        String ratingText = String.format("Rating⭐ %.1f ", pandit.getAvgRating());
        String countText = String.format("(%d)", pandit.getTotalRatings());

        SpannableString spannable = new SpannableString(ratingText + countText);
        spannable.setSpan(
                new android.text.style.ForegroundColorSpan(android.graphics.Color.GRAY),
                ratingText.length(),
                spannable.length(),
                android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        holder.panditRating.setText(spannable);


        if (showPhoneNumber && pandit.getPhone() != null && !pandit.getPhone().isEmpty()) {
            holder.panditPhone.setVisibility(View.VISIBLE);
            holder.panditPhone.setText("Phone: " + pandit.getPhone());
        } else {
            holder.panditPhone.setVisibility(View.GONE);
        }

        // Image load
        // Image load from base64
        if (pandit.getImageUrl() != null && !pandit.getImageUrl().isEmpty()) {
            try {
                byte[] decodedBytes = Base64.decode(pandit.getImageUrl(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                holder.panditImage.setImageBitmap(bitmap);
            } catch (Exception e) {
                holder.panditImage.setImageResource(R.drawable.account_box); // fallback image
            }
        } else {
            holder.panditImage.setImageResource(R.drawable.account_box); // default image
        }

        holder.itemView.setOnClickListener(v -> {
            if (pandit.getId() != null) {
                showFeedbackDialog(pandit.getId());
            }
        });

    }
    private void showFeedbackDialog(String panditId) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_feedback_list, null);
        RecyclerView recyclerView = dialogView.findViewById(R.id.recyclerFeedback);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        List<String> feedbackList = new ArrayList<>();
        FeedbackAdapter feedbackAdapter = new FeedbackAdapter(feedbackList);
        recyclerView.setAdapter(feedbackAdapter);

        FirebaseFirestore.getInstance()
                .collection("Pandits")
                .document(panditId)
                .get()
                .addOnSuccessListener(doc -> {
                    List<Map<String, Object>> ratings = (List<Map<String, Object>>) doc.get("rating");
                    if (ratings != null) {
                        for (Map<String, Object> rating : ratings) {
                            String fb = (String) rating.get("feedback");
                            if (fb != null && !fb.trim().isEmpty()) {
                                feedbackList.add(fb);
                            }
                        }
                        feedbackAdapter.notifyDataSetChanged();
                    }
                });

        new AlertDialog.Builder(context)
                .setTitle("User Feedback")
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .show();
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
