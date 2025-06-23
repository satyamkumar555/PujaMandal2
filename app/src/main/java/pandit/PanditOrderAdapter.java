package pandit;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pujamandal.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.*;

public class PanditOrderAdapter extends RecyclerView.Adapter<PanditOrderAdapter.OrderViewHolder> {

    private Context context;
    private List<DocumentSnapshot> bookingList;

    private String currentPanditId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private boolean isAcceptedList;

    public PanditOrderAdapter(Context context, List<DocumentSnapshot> bookingList, boolean isAcceptedList) {
        this.context = context;
        this.bookingList = bookingList;
        this.isAcceptedList = isAcceptedList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        DocumentSnapshot doc = bookingList.get(position);

        String puja = doc.getString("pujaType");
        String date = doc.getString("date");
        String time = doc.getString("time");
        String address = doc.getString("address");
        Long price = doc.getLong("price");
        String status = doc.getString("status");
        String userId = doc.getString("userId");
        String cancelReason = doc.getString("cancelReason");

        holder.txtPujaType.setText("Puja: " + puja);
        holder.txtDateTime.setText("DateTime: " + date + " " + time);
        holder.txtAddress.setText("Address: " + address);
        holder.txtPrice.setText("Price: Rs. " + price);
        holder.txtCancelReason.setVisibility(View.GONE); // default

        // 🔽 Handle by status
        if ("cancel_requested".equals(status)) {
            holder.txtStatus.setText("Status: Cancel Requested");
            holder.txtStatus.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));

            if (cancelReason != null && !cancelReason.isEmpty()) {
                holder.txtCancelReason.setVisibility(View.VISIBLE);
                holder.txtCancelReason.setText("Reason: " + cancelReason);
            }

            holder.btnAccept.setVisibility(View.GONE);
            holder.btnCancel.setVisibility(View.GONE);

        } else if ("cancelled".equals(status)) {
            holder.txtStatus.setText("Status: Cancelled");
            holder.txtStatus.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));

            if (cancelReason != null && !cancelReason.isEmpty()) {
                holder.txtCancelReason.setVisibility(View.VISIBLE);
                holder.txtCancelReason.setText("Reason: " + cancelReason);
            }

            holder.btnAccept.setVisibility(View.GONE);
            holder.btnCancel.setVisibility(View.GONE);

        } else if ("update_requested".equals(status)) {
            holder.txtStatus.setText("Status: Update Requested");
            holder.txtStatus.setTextColor(context.getResources().getColor(android.R.color.holo_orange_dark));

            holder.btnAccept.setVisibility(View.VISIBLE);
            holder.btnCancel.setVisibility(View.VISIBLE);
            holder.txtCancelReason.setVisibility(View.GONE);

            holder.btnAccept.setOnClickListener(v -> {
                doc.getReference().update("status", "accepted")
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(context, "Update Accepted", Toast.LENGTH_SHORT).show();
                            holder.btnAccept.setVisibility(View.GONE);
                            holder.btnCancel.setVisibility(View.GONE);
                            holder.txtStatus.setText("Status: accepted");
                            holder.txtStatus.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
                        });
            });

            holder.btnCancel.setOnClickListener(v -> {
                new AlertDialog.Builder(context)
                        .setTitle("Cancel Order")
                        .setMessage("Are you sure you want to cancel this order?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            doc.getReference().update("status", "pending", "panditId", null)
                                    .addOnSuccessListener(unused -> {
                                        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("ChatList");
                                        dbRef.child(currentPanditId).child(userId).removeValue();
                                        dbRef.child(userId).child(currentPanditId).removeValue();

                                        Toast.makeText(context, "Order Cancelled", Toast.LENGTH_SHORT).show();
                                    });
                        })
                        .setNegativeButton("No", null)
                        .show();
            });

        } else if ("accepted".equals(status)) {
            holder.txtStatus.setText("Status: accepted");
            holder.txtStatus.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
            holder.btnAccept.setVisibility(View.GONE);
            holder.btnCancel.setVisibility(View.GONE);
            holder.txtCancelReason.setVisibility(View.GONE);

        } else {
            // For pending orders
            holder.btnAccept.setVisibility(View.VISIBLE);
            holder.btnCancel.setVisibility(View.GONE);
            holder.txtCancelReason.setVisibility(View.GONE);

            holder.btnAccept.setOnClickListener(v -> {
                doc.getReference().update("status", "accepted", "panditId", currentPanditId)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(context, "Accepted", Toast.LENGTH_SHORT).show();
                            holder.txtStatus.setText("Status: accepted");
                            holder.btnAccept.setVisibility(View.GONE);

                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("ChatList");

                            db.collection("users").document(userId)
                                    .get()
                                    .addOnSuccessListener(userDoc -> {
                                        if (userDoc.exists()) {
                                            String userName = userDoc.getString("name");
                                            String userPhone = userDoc.getString("phone");

                                            db.collection("Pandits").document(currentPanditId)
                                                    .get()
                                                    .addOnSuccessListener(panditDoc -> {
                                                        if (panditDoc.exists()) {
                                                            String panditName = panditDoc.getString("name");
                                                            String panditPhone = panditDoc.getString("phone");

                                                            Map<String, Object> userInfo = new HashMap<>();
                                                            userInfo.put("name", userName);
                                                            userInfo.put("phone", userPhone);
                                                            userInfo.put("uid", userId);

                                                            Map<String, Object> panditInfo = new HashMap<>();
                                                            panditInfo.put("name", panditName);
                                                            panditInfo.put("phone", panditPhone);
                                                            panditInfo.put("PanditUid", currentPanditId);

                                                            dbRef.child(currentPanditId).child(userId).setValue(userInfo);
                                                            dbRef.child(userId).child(currentPanditId).setValue(panditInfo);
                                                        }
                                                    });
                                        }
                                    });
                        });
            });
        }
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView txtPujaType, txtDateTime, txtAddress, txtPrice, txtStatus, txtCancelReason;
        Button btnAccept, btnCancel;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            txtPujaType = itemView.findViewById(R.id.txtPujaType);
            txtDateTime = itemView.findViewById(R.id.txtDateTime);
            txtAddress = itemView.findViewById(R.id.txtAddress);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            txtCancelReason = itemView.findViewById(R.id.txtCancelReason);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnCancel = itemView.findViewById(R.id.btnCancel);
        }
    }
}
