package com.example.pujamandal;

import android.content.Context;
import android.icu.util.Calendar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;


public class UserBookingAdapter extends RecyclerView.Adapter<UserBookingAdapter.BookingViewHolder> {

    private Context context;
    private List<DocumentSnapshot> bookingList;

    public UserBookingAdapter(Context context, List<DocumentSnapshot> bookingList) {
        this.context = context;
        this.bookingList = bookingList;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        DocumentSnapshot doc = bookingList.get(position);

        String puja = doc.getString("pujaType");
        String date = doc.getString("date");
        String time = doc.getString("time");
        Long price = doc.getLong("price");
        String address = doc.getString("address");
        String status = doc.getString("status");


        holder.textPujaType.setText("Puja: " + puja);
        holder.textDateTime.setText("DateTime: " + date + " " + time);
        holder.textPrice.setText("Price: ₹" + price);
        holder.textAddress.setText("Address: " + address);
        holder.textStatus.setText("Status: " + status);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String combinedDateTime = date + " " + time;

        try {
            Date bookingDateTime = sdf.parse(combinedDateTime);
            Date now = new Date();

            if (now.after(bookingDateTime) && !"completed".equalsIgnoreCase(status)) {
                holder.btnMarkComplete.setVisibility(View.VISIBLE);
                holder.btnRequestCancel.setVisibility(View.GONE);
                holder.btnRequestUpdate.setVisibility(View.GONE);
            } else {
                holder.btnMarkComplete.setVisibility(View.GONE);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // 👇 Hide all buttons if status is already "completed"
        if ("completed".equalsIgnoreCase(status)) {
            holder.btnRequestCancel.setVisibility(View.GONE);
            holder.btnRequestUpdate.setVisibility(View.GONE);
            holder.btnMarkComplete.setVisibility(View.GONE);
        }


        if ("cancel_requested".equalsIgnoreCase(status)) {
            holder.textStatus.setText("Status: Cancel Requested");
            holder.textStatus.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
        }
        if ("cancelled".equalsIgnoreCase(status)) {
            holder.textStatus.setText("Status: Cancelled");
            holder.textStatus.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
        }
        String rejectReason = doc.getString("cancelRejectReason");
        if (rejectReason != null && !rejectReason.isEmpty()) {
            holder.txtCancelRejectReason.setText("Cancel Rejected Reason: " + rejectReason);
            holder.txtCancelRejectReason.setVisibility(View.VISIBLE);
        } else {
            holder.txtCancelRejectReason.setVisibility(View.GONE);
        }


        if ("completed".equalsIgnoreCase(status) || "cancel_requested".equalsIgnoreCase(status) || "cancelled".equalsIgnoreCase(status)) {
            // Hide all buttons if completed or cancelled
            holder.btnRequestUpdate.setVisibility(View.GONE);
            holder.btnRequestCancel.setVisibility(View.GONE);
            holder.btnMarkComplete.setVisibility(View.GONE);
        } else {
            // Normal case: allow update/cancel
            holder.btnRequestUpdate.setVisibility(View.VISIBLE);
            holder.btnRequestCancel.setVisibility(View.VISIBLE);

            // Only show complete button if time passed
            try {
                Date bookingDateTime = sdf.parse(date + " " + time);
                if (new Date().after(bookingDateTime)) {
                    holder.btnMarkComplete.setVisibility(View.VISIBLE);
                    // Hide update/cancel when complete button is shown
                    holder.btnRequestUpdate.setVisibility(View.GONE);
                    holder.btnRequestCancel.setVisibility(View.GONE);
                } else {
                    holder.btnMarkComplete.setVisibility(View.GONE);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }




        holder.btnRequestCancel.setOnClickListener(v -> {
            // Step 1: Check if already cancelled
            String currentStatus = doc.getString("status");
            if ("cancel_requested".equalsIgnoreCase(currentStatus) || "cancelled_by_user".equalsIgnoreCase(currentStatus)) {
                Toast.makeText(context, "Already requested for cancellation", Toast.LENGTH_SHORT).show();
                return;
            }

            // Step 2: Show Reason Dialog
            EditText input = new EditText(context);
            input.setHint("Enter reason for cancellation");

            // Inside btnRequestCancel.setOnClickListener()
            View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_cancel_reason, null);
            EditText reasonInput = dialogView.findViewById(R.id.editCancelReason);

            new AlertDialog.Builder(context)
                    .setTitle("Cancel Booking")
                    .setView(dialogView)
                    .setPositiveButton("Confirm Cancel", (dialog, which) -> {
                        String reason = reasonInput.getText().toString().trim();
                        if (reason.isEmpty()) {
                            Toast.makeText(context, "Please provide a reason", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Map<String, Object> updates = new HashMap<>();
                        updates.put("status", "cancel_requested");
                        updates.put("cancelReason", reason);
                        updates.put("cancelTime", com.google.firebase.Timestamp.now());

                        FirebaseFirestore.getInstance()
                                .collection("Bookings")
                                .document(doc.getId())
                                .update(updates)
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(context, "Cancellation Requested", Toast.LENGTH_SHORT).show();
                                    notifyItemChanged(position);
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    })
                    .setNegativeButton("Dismiss", null)
                    .show();

        });
        holder.btnRequestUpdate.setOnClickListener(v -> {
            showUpdateDialog(doc);
        });
        holder.btnMarkComplete.setOnClickListener(v -> {
            View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_rating, null);
            RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);
            EditText editFeedback = dialogView.findViewById(R.id.editFeedback);

            new AlertDialog.Builder(context)
                    .setTitle("Rate your Pandit")
                    .setView(dialogView)
                    .setPositiveButton("Submit", (dialog, which) -> {
                        float rating = ratingBar.getRating();
                        String feedback = editFeedback.getText().toString().trim();

                        if (rating == 0) {
                            Toast.makeText(context, "Please give a rating", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Map<String, Object> updates = new HashMap<>();
                        updates.put("status", "completed");

                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("Bookings").document(doc.getId())
                                .update(updates)
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(context, "Marked as Completed", Toast.LENGTH_SHORT).show();
                                    notifyItemChanged(position);

                                    // 🔁 Now update Pandit avg rating
                                    String panditId = doc.getString("panditId");
                                    String userId = doc.getString("userId");
                                    if (panditId != null) {
                                        Map<String, Object> ratingEntry = new HashMap<>();
                                        ratingEntry.put("userId", userId);
                                        ratingEntry.put("rating", rating);
                                        ratingEntry.put("feedback", feedback);
                                        ratingEntry.put("timestamp", com.google.firebase.Timestamp.now());

                                        db.collection("Pandits").document(panditId)
                                                .update("rating", FieldValue.arrayUnion(ratingEntry))
                                                .addOnSuccessListener(aVoid -> {
                                                    // 🔁 After pushing rating, calculate new avg
                                                    db.collection("Pandits").document(panditId).get()
                                                            .addOnSuccessListener(panditDoc -> {
                                                                List<Map<String, Object>> allRatings =
                                                                        (List<Map<String, Object>>) panditDoc.get("rating");

                                                                float total = 0;
                                                                int count = 0;
                                                                if (allRatings != null) {
                                                                    for (Map<String, Object> r : allRatings) {
                                                                        if (r.containsKey("rating")) {
                                                                            total += ((Number) r.get("rating")).floatValue();
                                                                            count++;
                                                                        }
                                                                    }
                                                                }

                                                                float avg = (count > 0) ? total / count : 0;
                                                                Map<String, Object> ratingSummary = new HashMap<>();
                                                                ratingSummary.put("avgRating", avg);
                                                                ratingSummary.put("totalRatings", count);

                                                                db.collection("Pandits").document(panditId)
                                                                        .update(ratingSummary);
                                                            });
                                                });
                                    }
                                });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });


    }

    private void showUpdateDialog(DocumentSnapshot booking) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_update_booking, null);

        Spinner spinnerDialogPujaType = dialogView.findViewById(R.id.spinnerDialogPujaType);
        RadioGroup radioGroupDialogOptions = dialogView.findViewById(R.id.radioGroupDialogOptions);
        RadioButton radioDialogPanditOnly = dialogView.findViewById(R.id.radioDialogPanditOnly);
        RadioButton radioDialogWithSamaan = dialogView.findViewById(R.id.radioDialogWithSamaan);
        EditText editDialogDate = dialogView.findViewById(R.id.editDialogDate);
        EditText editDialogTime = dialogView.findViewById(R.id.editDialogTime);
        EditText editDialogAddress = dialogView.findViewById(R.id.editDialogAddress);
        EditText editDialogNote = dialogView.findViewById(R.id.editDialogNote);
        TextView textDialogPrice = dialogView.findViewById(R.id.textDialogPrice);
        Button btnSubmitUpdate = dialogView.findViewById(R.id.btnSubmitUpdate);

        FirebaseFirestore.getInstance().collection("puja").get()
                .addOnSuccessListener(snapshot -> {
                    List<String> pujaList = new ArrayList<>();
                    Map<String, Long> panditOnlyMap = new HashMap<>();
                    Map<String, Long> withSamaanMap = new HashMap<>();

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String name = doc.getString("name");
                        Long price1 = doc.getLong("pricePanditOnly");
                        Long price2 = doc.getLong("priceWithSamaan");

                        if (name != null) {
                            pujaList.add(name);
                            panditOnlyMap.put(name, price1 != null ? price1 : 0);
                            withSamaanMap.put(name, price2 != null ? price2 : 0);
                        }
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                            android.R.layout.simple_spinner_item, pujaList);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerDialogPujaType.setAdapter(adapter);

                    // Set existing values
                    spinnerDialogPujaType.setSelection(pujaList.indexOf(booking.getString("pujaType")));
                    editDialogDate.setText(booking.getString("date"));
                    editDialogTime.setText(booking.getString("time"));
                    editDialogAddress.setText(booking.getString("address"));
                    editDialogNote.setText(booking.getString("note"));

                    String option = booking.getString("option");
                    if ("With Samaan".equals(option)) {
                        radioDialogWithSamaan.setChecked(true);
                    } else {
                        radioDialogPanditOnly.setChecked(true);
                    }

                    Runnable updatePrice = () -> {
                        String puja = spinnerDialogPujaType.getSelectedItem().toString();
                        boolean isWithSamaan = radioDialogWithSamaan.isChecked();
                        long price = isWithSamaan ? withSamaanMap.get(puja) : panditOnlyMap.get(puja);
                        textDialogPrice.setText("Price: ₹" + price);
                    };

                    spinnerDialogPujaType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                            updatePrice.run();
                        }
                        public void onNothingSelected(AdapterView<?> parent) {}
                    });
                    radioGroupDialogOptions.setOnCheckedChangeListener((g, i) -> updatePrice.run());
                    updatePrice.run();

                    editDialogDate.setOnClickListener(v -> {
                        Calendar cal = Calendar.getInstance();
                        new DatePickerDialog(context, (view, year, month, day) ->
                                editDialogDate.setText(day + "/" + (month+1) + "/" + year),
                                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
                        ).show();
                    });

                    editDialogTime.setOnClickListener(v -> {
                        Calendar cal = Calendar.getInstance();
                        new TimePickerDialog(context, (view, hour, min) ->
                                editDialogTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, min)),
                                cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true
                        ).show();
                    });

                    AlertDialog dialog = new AlertDialog.Builder(context)
                            .setView(dialogView)
                            .create();

                    btnSubmitUpdate.setOnClickListener(v -> {
                        String updatedPuja = spinnerDialogPujaType.getSelectedItem().toString();
                        String updatedDate = editDialogDate.getText().toString();
                        String updatedTime = editDialogTime.getText().toString();
                        String updatedAddress = editDialogAddress.getText().toString();
                        String updatedNote = editDialogNote.getText().toString();
                        String updatedOption = radioDialogWithSamaan.isChecked() ? "With Samaan" : "Only Pandit";
                        long updatedPrice = radioDialogWithSamaan.isChecked() ? withSamaanMap.get(updatedPuja) : panditOnlyMap.get(updatedPuja);

                        Map<String, Object> updates = new HashMap<>();
                        updates.put("pujaType", updatedPuja);
                        updates.put("option", updatedOption);
                        updates.put("date", updatedDate);
                        updates.put("time", updatedTime);
                        updates.put("address", updatedAddress);
                        updates.put("note", updatedNote);
                        updates.put("price", updatedPrice);
                        updates.put("status", "update_requested");

                        FirebaseFirestore.getInstance()
                                .collection("Bookings")
                                .document(booking.getId())
                                .update(updates)
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(context, "Update Requested", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    });

                    dialog.show();
                });
    }


    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    public static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView textPujaType, textDateTime, textPrice, textStatus, textAddress, txtCancelRejectReason;
        Button btnRequestUpdate, btnRequestCancel,btnMarkComplete;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            textPujaType = itemView.findViewById(R.id.textPujaType);
            textDateTime = itemView.findViewById(R.id.textDateTime);
            textPrice = itemView.findViewById(R.id.textPrice);
            textAddress = itemView.findViewById(R.id.textAddress);
            textStatus = itemView.findViewById(R.id.textStatus);
            btnRequestUpdate = itemView.findViewById(R.id.btnRequestUpdate);
            btnRequestCancel = itemView.findViewById(R.id.btnRequestCancel);
            btnMarkComplete = itemView.findViewById(R.id.btnMarkComplete);
            txtCancelRejectReason = itemView.findViewById(R.id.txtCancelRejectReason);

        }
    }
}
