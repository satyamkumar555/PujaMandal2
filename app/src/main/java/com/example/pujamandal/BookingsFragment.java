package com.example.pujamandal;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.*;

public class BookingsFragment extends Fragment {

    private Spinner spinnerCity, spinnerPujaType;
    private EditText editDate, editTime, editNote, editAddress;
    private TextView textPujaPrice;
    private Button btnBookNow;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private RadioGroup radioGroupOptions;
    private RadioButton radioPanditOnly, radioWithSamaan;
    private Map<String, Long> pricePanditOnlyMap = new HashMap<>();
    private Map<String, Long> priceWithSamaanMap = new HashMap<>();


    private Map<String, Long> pujaPrices = new HashMap<>();

    public BookingsFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bookings, container, false);

        spinnerCity = view.findViewById(R.id.spinnerCity);
        spinnerPujaType = view.findViewById(R.id.spinnerPujaType);
        editDate = view.findViewById(R.id.editDate);
        editTime = view.findViewById(R.id.editTime);
        editNote = view.findViewById(R.id.editNote);
        editAddress = view.findViewById(R.id.editAddress);
        textPujaPrice = view.findViewById(R.id.textPujaPrice);
        btnBookNow = view.findViewById(R.id.btnBookNow);
        radioGroupOptions = view.findViewById(R.id.radioGroupOptions);
        radioPanditOnly = view.findViewById(R.id.radioPanditOnly);
        radioWithSamaan = view.findViewById(R.id.radioWithSamaan);


        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        EditText editAddress = view.findViewById(R.id.editAddress);

        SharedPreferences prefs = requireContext().getSharedPreferences("location_prefs", Context.MODE_PRIVATE);
        String savedAddress = prefs.getString("current_address", "");

        if (!savedAddress.isEmpty()) {
            editAddress.setText(savedAddress);
        }




        loadCities();
        loadPujaTypes();

        editDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePicker = new DatePickerDialog(getContext(),
                    (view1, year, month, day) -> editDate.setText(day + "/" + (month + 1) + "/" + year),
                    calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datePicker.show();
        });

        editTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            TimePickerDialog timePicker = new TimePickerDialog(getContext(),
                    (view12, hourOfDay, minute) -> editTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)),
                    calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
            timePicker.show();
        });

        editAddress.setOnClickListener(v -> {
            // TODO: Implement map picker here
            Toast.makeText(getContext(), "Map Picker not implemented yet", Toast.LENGTH_SHORT).show();
        });


        spinnerPujaType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                updatePrice();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        radioGroupOptions.setOnCheckedChangeListener((group, checkedId) -> updatePrice());


        btnBookNow.setOnClickListener(v -> bookPuja());

        return view;
    }

    private void loadCities() {
        List<String> cities = Arrays.asList("Select City", "Delhi", "Mumbai", "Varanasi", "Hyderabad");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, cities);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCity.setAdapter(adapter);
    }

    private void loadPujaTypes() {
        db.collection("puja").get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<String> pujaNames = new ArrayList<>();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                String name = doc.getString("name");
                Long pricePanditOnly = doc.getLong("pricePanditOnly");
                Long priceWithSamaan = doc.getLong("priceWithSamaan");

                if (name != null) {
                    pujaNames.add(name);
                    pricePanditOnlyMap.put(name, pricePanditOnly != null ? pricePanditOnly : 0);
                    priceWithSamaanMap.put(name, priceWithSamaan != null ? priceWithSamaan : 0);
                }
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                    android.R.layout.simple_spinner_item, pujaNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerPujaType.setAdapter(adapter);
        });
    }


    private void bookPuja() {
        String city = spinnerCity.getSelectedItem().toString();
        String pujaType = spinnerPujaType.getSelectedItem().toString();
        String date = editDate.getText().toString();
        String time = editTime.getText().toString();
        String note = editNote.getText().toString();
        String address = editAddress.getText().toString();
        String userId = auth.getCurrentUser().getUid();
        boolean isWithSamaan = radioWithSamaan.isChecked();
        String option = isWithSamaan ? "With Samaan" : "Only Pandit";

        Long price = isWithSamaan ?
                priceWithSamaanMap.get(pujaType) :
                pricePanditOnlyMap.get(pujaType);

        if (city.equals("Select City") || pujaType.isEmpty() || date.isEmpty() || time.isEmpty() || address.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> booking = new HashMap<>();
        booking.put("userId", userId);
        booking.put("city", city);
        booking.put("pujaType", pujaType);
        booking.put("option", option);
        booking.put("price", price != null ? price : 0);
        booking.put("date", date);
        booking.put("time", time);
        booking.put("note", note);
        booking.put("address", address);
        booking.put("status", "pending");
        booking.put("timestamp", FieldValue.serverTimestamp());

        db.collection("Bookings")
                .add(booking)
                .addOnSuccessListener(documentReference ->
                        Toast.makeText(getContext(), "Booking placed!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updatePrice() {
        String pujaType = spinnerPujaType.getSelectedItem().toString();
        boolean isWithSamaan = radioWithSamaan.isChecked();

        Long price = isWithSamaan ?
                priceWithSamaanMap.get(pujaType) :
                pricePanditOnlyMap.get(pujaType);

        textPujaPrice.setText("Price: ₹" + (price != null ? price : 0));
    }


}
