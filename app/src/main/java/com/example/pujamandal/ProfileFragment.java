package com.example.pujamandal;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private TextView tvName, tvEmail, tvPhone, tvCity, tvGender;
    private ImageView profileImage;
    private ImageButton editProfileBtn;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Uri selectedImageUri;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ImageView editDialogImageView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // UI components
        tvName = view.findViewById(R.id.tv_name);
        tvEmail = view.findViewById(R.id.tv_email);
        tvPhone = view.findViewById(R.id.tv_phone);
        tvCity = view.findViewById(R.id.tv_city);
        tvGender = view.findViewById(R.id.tv_gender);
        profileImage = view.findViewById(R.id.profileImage);
        editProfileBtn = view.findViewById(R.id.editProfileBtn);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loadUserProfile();

        editProfileBtn.setOnClickListener(v -> showEditDialog());

        // Image Picker
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        if (editDialogImageView != null) {
                            editDialogImageView.setImageURI(selectedImageUri);
                        }
                    }
                });

        return view;
    }

    private void loadUserProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String uid = currentUser.getUid();
        tvEmail.setText("Email: " + currentUser.getEmail());

        db.collection("users").document(uid).get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                tvName.setText("Name: " + snapshot.getString("name"));
                tvPhone.setText("Phone: " + snapshot.getString("phone"));
                tvCity.setText("City: " + snapshot.getString("city"));
                tvGender.setText("Gender: " + snapshot.getString("gender"));

                String base64 = snapshot.getString("imageBase64");
                if (base64 != null) {
                    byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
                    Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    profileImage.setImageBitmap(bmp);
                }
            }
        });
    }

    private void showEditDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_profile_user, null);

        EditText nameET = dialogView.findViewById(R.id.editName);
        EditText phoneET = dialogView.findViewById(R.id.editPhone);
        EditText cityET = dialogView.findViewById(R.id.editCity);
        RadioGroup genderGroup = dialogView.findViewById(R.id.editGender);
        RadioButton radioMale = dialogView.findViewById(R.id.radioMale);
        RadioButton radioFemale = dialogView.findViewById(R.id.radioFemale);
        RadioButton radioOther = dialogView.findViewById(R.id.radioOther);
        editDialogImageView = dialogView.findViewById(R.id.editProfileImage);

        // Set existing values
        nameET.setText(tvName.getText().toString().replace("Name: ", ""));
        phoneET.setText(tvPhone.getText().toString().replace("Phone: ", ""));
        cityET.setText(tvCity.getText().toString().replace("City: ", ""));
        String currentGender = tvGender.getText().toString().replace("Gender: ", "");

        if (currentGender.equalsIgnoreCase("Male")) {
            radioMale.setChecked(true);
        } else if (currentGender.equalsIgnoreCase("Female")) {
            radioFemale.setChecked(true);
        } else {
            radioOther.setChecked(true);
        }

        // Pick new image
        editDialogImageView.setOnClickListener(v -> {
            Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(pickIntent);
        });

        new AlertDialog.Builder(getContext())
                .setTitle("Edit Profile")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = nameET.getText().toString();
                    String phone = phoneET.getText().toString();
                    String city = cityET.getText().toString();

                    int selectedGenderId = genderGroup.getCheckedRadioButtonId();
                    String gender = "";
                    if (selectedGenderId != -1) {
                        RadioButton selectedRadio = dialogView.findViewById(selectedGenderId);
                        gender = selectedRadio.getText().toString();
                    }

                    String base64Image = selectedImageUri != null ? encodeImageToBase64(selectedImageUri) : null;
                    updateUserProfile(name, phone, city, gender, base64Image);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private String encodeImageToBase64(Uri uri) {
        try {
            InputStream stream = getActivity().getContentResolver().openInputStream(uri);
            Bitmap bmp = BitmapFactory.decodeStream(stream);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void updateUserProfile(String name, String phone, String city, String gender, String base64Image) {
        String uid = mAuth.getCurrentUser().getUid();
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("phone", phone);
        updates.put("city", city);
        updates.put("gender", gender);
        if (base64Image != null) updates.put("imageBase64", base64Image);

        db.collection("users").document(uid).update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getActivity(), "Profile Updated!", Toast.LENGTH_SHORT).show();
                    selectedImageUri = null;
                    loadUserProfile();
                })
                .addOnFailureListener(e -> Toast.makeText(getActivity(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
