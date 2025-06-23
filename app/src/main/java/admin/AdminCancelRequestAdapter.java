package admin;

import android.app.AlertDialog;
import android.content.Context;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pujamandal.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.*;

import java.util.*;

public class AdminCancelRequestAdapter extends RecyclerView.Adapter<AdminCancelRequestAdapter.ViewHolder> {

    private Context context;
    private List<DocumentSnapshot> cancelList;

    public AdminCancelRequestAdapter(Context context, List<DocumentSnapshot> cancelList) {
        this.context = context;
        this.cancelList = cancelList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cancel_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DocumentSnapshot doc = cancelList.get(position);

        String puja = doc.getString("pujaType");
        String userId = doc.getString("userId");
        String date = doc.getString("date");
        String time = doc.getString("time");
        String reason = doc.getString("cancelReason");
        String status = doc.getString("status");
        Long price = doc.getLong("price");

        holder.textPujaType.setText("Puja: " + puja);
        holder.textDateTime.setText("Date: " + date + "   Time: " + time);
        holder.textCancelReason.setText("Reason: " + reason);
        holder.textStatus.setText("Status: " + status);
        holder.textPrice.setText("Price: ₹" + (price != null ? price : 0));

        // 🔍 Fetch user info
        FirebaseFirestore.getInstance()
                .collection("Users")
                .document(userId)
                .get()
                .addOnSuccessListener(userDoc -> {
                    if (userDoc.exists()) {
                        String name = userDoc.getString("name");
                        String phone = userDoc.getString("phone");

                        holder.textUserName.setText("User: " + name);
                        holder.textUserPhone.setText("Phone: " + phone);
                    } else {
                        holder.textUserName.setText("User: Unknown");
                        holder.textUserPhone.setText("Phone: -");
                    }
                });

        // 🔘 Show buttons only if cancel requested
        if ("cancel_requested".equals(status)) {
            holder.btnApproveCancel.setVisibility(View.VISIBLE);
            holder.btnRejectCancel.setVisibility(View.VISIBLE);
        } else {
            holder.btnApproveCancel.setVisibility(View.GONE);
            holder.btnRejectCancel.setVisibility(View.GONE);
        }

        // ✅ Accept → Cancel confirm
        holder.btnApproveCancel.setOnClickListener(v -> {
            FirebaseFirestore.getInstance()
                    .collection("Bookings")
                    .document(doc.getId())
                    .update("status", "cancelled")
                    .addOnSuccessListener(unused -> {

                        // ✅ 1. Realtime DB se chat delete karo

                        String panditId = doc.getString("panditId");

                        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats");

                        dbRef.child(userId).child(panditId).removeValue();
                        dbRef.child(panditId).child(userId).removeValue();

                        // ✅ 2. Toast & UI update
                        Toast.makeText(context, "Booking Cancelled", Toast.LENGTH_SHORT).show();

                        // ✅ 3. UI remove
                        cancelList.remove(position);
                        notifyItemRemoved(position);
                        Toast.makeText(context, "Booking Cancelled", Toast.LENGTH_SHORT).show();
                        holder.textStatus.setText("Status: cancelled");
                        holder.btnApproveCancel.setVisibility(View.GONE);
                        holder.btnRejectCancel.setVisibility(View.GONE);
                    });
        });

        // ❌ Reject → Reason lena hoga
        holder.btnRejectCancel.setOnClickListener(v -> {
            EditText input = new EditText(context);
            input.setHint("Enter rejection reason");

            new AlertDialog.Builder(context)
                    .setTitle("Reject Cancel Request")
                    .setMessage("Are you sure you want to reject this cancel request?")
                    .setView(input)
                    .setPositiveButton("Reject", (dialog, which) -> {
                        String rejectReason = input.getText().toString().trim();

                        if (!rejectReason.isEmpty()) {
                            Map<String, Object> update = new HashMap<>();
                            update.put("status", "accepted");
                            update.put("cancelRejectReason", rejectReason);

                            FirebaseFirestore.getInstance()
                                    .collection("Bookings")
                                    .document(doc.getId())
                                    .update(update)
                                    .addOnSuccessListener(unused -> {
                                        Toast.makeText(context, "Request Rejected", Toast.LENGTH_SHORT).show();
                                        holder.textStatus.setText("Status: accepted");
                                        holder.btnApproveCancel.setVisibility(View.GONE);
                                        holder.btnRejectCancel.setVisibility(View.GONE);
                                    });
                        } else {
                            Toast.makeText(context, "Reason required", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

}

    @Override
    public int getItemCount() {
        return cancelList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textUserName, textUserPhone, textPujaType, textDateTime, textCancelReason, textStatus, textPrice;
        Button btnApproveCancel, btnRejectCancel;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textUserName = itemView.findViewById(R.id.textUserName);
            textUserPhone = itemView.findViewById(R.id.textUserPhone);
            textPujaType = itemView.findViewById(R.id.textPujaType);
            textDateTime = itemView.findViewById(R.id.textDateTime);
            textCancelReason = itemView.findViewById(R.id.textCancelReason);
            textStatus = itemView.findViewById(R.id.textStatus);
            textPrice = itemView.findViewById(R.id.textPrice);
            btnApproveCancel = itemView.findViewById(R.id.btnApproveCancel);
            btnRejectCancel = itemView.findViewById(R.id.btnRejectCancel);

        }
    }
}
