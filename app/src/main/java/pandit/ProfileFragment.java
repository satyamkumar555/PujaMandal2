package pandit;


import com.example.pujamandal.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pujamandal.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private TextView nameTV, emailTV, phoneTV, cityTV, expTV, ratingTV, aboutTV;
    private ImageButton editBtn;
    private ImageView profileImage, editDialogImageView;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private Uri selectedImageUri;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile2, container, false);

        // Bind Views
        nameTV = view.findViewById(R.id.panditName);
        emailTV = view.findViewById(R.id.panditEmail);
        phoneTV = view.findViewById(R.id.phoneText);
        cityTV = view.findViewById(R.id.cityText);
        expTV = view.findViewById(R.id.expText);
        ratingTV = view.findViewById(R.id.ratingText);
        aboutTV = view.findViewById(R.id.aboutText);
        editBtn = view.findViewById(R.id.editProfileBtn);
        profileImage = view.findViewById(R.id.profileImage);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loadPanditData();

        editBtn.setOnClickListener(v -> showEditDialog());

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        if (editDialogImageView != null && selectedImageUri != null) {
                            editDialogImageView.setImageURI(selectedImageUri);
                        }
                    }
                });

        return view;
    }

    private void showEditDialog() {
        View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_edit_profile, null);

        EditText nameET = dialogView.findViewById(R.id.editName);
        EditText phoneET = dialogView.findViewById(R.id.editPhone);
        EditText cityET = dialogView.findViewById(R.id.editCity);
        EditText expET = dialogView.findViewById(R.id.editExperience);
        EditText aboutET = dialogView.findViewById(R.id.editAbout);
        editDialogImageView = dialogView.findViewById(R.id.editProfileImage);

        // Pre-fill fields
        nameET.setText(nameTV.getText().toString());
        phoneET.setText(phoneTV.getText().toString().replace("Phone: ", ""));
        cityET.setText(cityTV.getText().toString().replace("City: ", ""));
        expET.setText(expTV.getText().toString().replace("Experience: ", ""));
        aboutET.setText(aboutTV.getText().toString());

        // Pick image
        editDialogImageView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        new AlertDialog.Builder(requireContext())
                .setTitle("Edit Profile")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = nameET.getText().toString();
                    String phone = phoneET.getText().toString();
                    String city = cityET.getText().toString();
                    String exp = expET.getText().toString();
                    String about = aboutET.getText().toString();

                    if (selectedImageUri != null) {
                        // Convert image to Base64 and save
                        String base64Image = encodeImageToBase64(selectedImageUri);
                        updateProfileInFirestore(name, phone, city, exp, about, base64Image);
                    } else {
                        updateProfileInFirestore(name, phone, city, exp, about, null);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private String encodeImageToBase64(Uri imageUri) {
        try {
            InputStream inputStream = getActivity().getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] imageBytes = baos.toByteArray();

            return Base64.encodeToString(imageBytes, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void loadPanditData() {
        String uid = mAuth.getCurrentUser().getUid();

        db.collection("Pandits").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        nameTV.setText(documentSnapshot.getString("name"));
                        emailTV.setText(documentSnapshot.getString("email"));
                        phoneTV.setText("Phone: " + documentSnapshot.getString("phone"));
                        cityTV.setText("City: " + documentSnapshot.getString("city"));
                        expTV.setText("Experience: " + documentSnapshot.getString("experience"));
                        ratingTV.setText(documentSnapshot.getString("rating") + " ★");
                        aboutTV.setText(documentSnapshot.getString("description"));

                        String base64 = documentSnapshot.getString("imageBase64");
                        if (base64 != null) {
                            byte[] decodedBytes = Base64.decode(base64, Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                            profileImage.setImageBitmap(bitmap);
                        } else {
                            profileImage.setImageResource(R.drawable.account_box); // default image
                            Toast.makeText(getActivity(), "No profile image found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getActivity(), "Profile not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getActivity(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateProfileInFirestore(String name, String phone, String city, String exp, String about, String base64Image) {
        String uid = mAuth.getCurrentUser().getUid();

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("phone", phone);
        updates.put("city", city);
        updates.put("experience", exp);
        updates.put("description", about);
        if (base64Image != null) {
            updates.put("imageBase64", base64Image);
        }

        db.collection("Pandits").document(uid)
                .update(updates)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(getActivity(), "Profile updated!", Toast.LENGTH_SHORT).show();
                    loadPanditData();
                    selectedImageUri = null; // reset
                })
                .addOnFailureListener(e -> Toast.makeText(getActivity(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
