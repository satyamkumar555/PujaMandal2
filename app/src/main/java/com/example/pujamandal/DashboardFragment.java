package com.example.pujamandal;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class DashboardFragment extends Fragment {

    private GoogleMap mMap;
    private TextInputEditText searchLocation;
    private TextView locationText; // Map ke niche address dikhane ke liye
    private FusedLocationProviderClient fusedLocationClient;
    private Marker currentMarker;
    private TextView statusTextView;  // TextView to show status
    private TextView panditNameTextView;  // TextView to show Pandit name
    private FirebaseFirestore db;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        db = FirebaseFirestore.getInstance();
        statusTextView = view.findViewById(R.id.statusTextView);
        panditNameTextView = view.findViewById(R.id.panditNameTextView);

        String orderId = "your_order_id";

        fetchOrderStatus(orderId);


        // Initialize Google Places API
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), getString(R.string.google_maps_key));
        }

        // Reference UI Elements
        searchLocation = view.findViewById(R.id.searchLocation);
        locationText = view.findViewById(R.id.locationText); // Address ke liye TextView
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        // Load Map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_container);
        if (mapFragment == null) {
            mapFragment = new SupportMapFragment();
            getChildFragmentManager().beginTransaction().replace(R.id.map_container, mapFragment).commit();
        }

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap) {
                mMap = googleMap;
                getCurrentLocation(); // GPS se location fetch karega
                mMap.setOnMapClickListener(latLng -> updateLocation(latLng));
            }
        });

        // Search Button Click Event
        searchLocation.setOnEditorActionListener((v, actionId, event) -> {
            String location = searchLocation.getText().toString();
            if (!location.isEmpty()) {
                searchPlace(location);
            } else {
                Toast.makeText(getContext(), "Enter a location", Toast.LENGTH_SHORT).show();
            }
            return true;
        });

        return view;
    }

    // Fetch User's Current Location
    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                updateLocation(userLocation);
            }
        });
    }

    // Update Map, Search Bar & TextView
    private void updateLocation(LatLng latLng) {
        if (currentMarker != null) {
            currentMarker.remove();
        }
        currentMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Selected Location"));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

        // Address Fetch & Show in Search Bar & TextView
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (!addresses.isEmpty()) {
                String addressText = addresses.get(0).getAddressLine(0);
                searchLocation.setText(addressText);
                locationText.setText("Selected Location: " + addressText);

                // Save address in SharedPreferences
                SharedPreferences prefs = requireContext().getSharedPreferences("location_prefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("current_address", addressText);
                editor.apply();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Search Location Function
    private void searchPlace(String location) {
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(location, 1);
            if (!addresses.isEmpty()) {
                Address address = addresses.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                updateLocation(latLng);
            } else {
                Toast.makeText(getContext(), "Location not found", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error finding location", Toast.LENGTH_SHORT).show();
        }
    }
    // Method to fetch order status from Firestore
    private void fetchOrderStatus(String orderId) {
        db.collection("orders").document(orderId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String status = documentSnapshot.getString("status");
                        String panditName = documentSnapshot.getString("panditName");

                        // Update UI based on order status
                        if ("pending".equals(status)) {
                            statusTextView.setText("Order Status: Pending");
                            panditNameTextView.setVisibility(View.GONE);  // Hide Pandit name if still pending
                        } else if ("accepted".equals(status) && panditName != null) {
                            statusTextView.setText("Order Accepted");
                            panditNameTextView.setVisibility(View.VISIBLE);
                            panditNameTextView.setText("Accepted by: " + panditName);  // Show Pandit name
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error fetching order status", Toast.LENGTH_SHORT).show();

                });
    }

}
