package admin;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.pujamandal.R;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;
import android.util.Base64;
import java.io.InputStream;


public class AddPanditActivity extends AppCompatActivity {

    private EditText editName, editCity, editPhone, editRating, editExperience, editDescription,editEmail, editPassword;
    private Button btnAddPandit, btnSelectImage;
    private ImageView imagePreview;
    private ProgressBar uploadProgress;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseAuth secondaryAuth;


    private static final int IMAGE_PICK_CODE = 1000;
    private static final int PERMISSION_CODE = 101;
    private Uri imageUri;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pandit);

        FirebaseApp.initializeApp(this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance());




        // Initialize views
        editName = findViewById(R.id.editName);
        editCity = findViewById(R.id.editCity);
        editPhone = findViewById(R.id.editPhone);
        editRating = findViewById(R.id.editRating);
        editExperience = findViewById(R.id.editExperience);
        editDescription = findViewById(R.id.editDescription);
        editEmail = findViewById(R.id.edit_Email);
        editPassword = findViewById(R.id.editPassword);
        btnAddPandit = findViewById(R.id.btnAddPandit);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        imagePreview = findViewById(R.id.imagePreview);
        uploadProgress = findViewById(R.id.uploadProgress);
        uploadProgress.setVisibility(ProgressBar.INVISIBLE);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        btnSelectImage.setOnClickListener(v -> checkPermissionAndPickImage());
        btnAddPandit.setOnClickListener(v -> addPandit());
    }

    private void checkPermissionAndPickImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ specific permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES}, PERMISSION_CODE);
            } else {
                pickImageFromGallery();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_CODE);
            } else {
                pickImageFromGallery();
            }
        }
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            Log.d("IMAGE_URI", "Selected URI: " + imageUri);
            imagePreview.setImageURI(imageUri);
        }
    }

    private void addPandit() {
        String name = editName.getText().toString().trim();
        String city = editCity.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();
        String rating = editRating.getText().toString().trim();
        String experience = editExperience.getText().toString().trim();
        String description = editDescription.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(city) || TextUtils.isEmpty(phone)
                || TextUtils.isEmpty(rating) || TextUtils.isEmpty(experience)
                || TextUtils.isEmpty(description) || TextUtils.isEmpty(password)
                || TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_LONG).show();
            return;
        }


        uploadProgress.setVisibility(ProgressBar.VISIBLE);

        // Save current admin email & password (hardcoded or from session)
        String adminEmail = "satyambth311@gmail.com";  // 🔁 Replace with real admin email
        String adminPassword = "Satyam@311";   // 🔁 Replace with real admin password

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String panditUid = task.getResult().getUser().getUid();
                        // ✅ Sign out the newly created Pandit user
                        auth.signOut();

                        // ✅ Sign back in as admin
                        auth.signInWithEmailAndPassword(adminEmail, adminPassword)
                                .addOnSuccessListener(authResult -> {
                                    if (imageUri != null) {
                                        uploadImageAndSaveData(name, city, phone, rating, experience, description, email, panditUid);
                                    } else {
                                        // No image selected, save without image
                                        savePanditToFirestore(name, city, phone, rating, experience, description, email, null,panditUid);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    uploadProgress.setVisibility(ProgressBar.INVISIBLE);
                                    Toast.makeText(this, "Admin re-login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });

                    } else {
                        uploadProgress.setVisibility(ProgressBar.INVISIBLE);
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            Toast.makeText(this, "Pandit already exists!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Failed to create account: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    private void uploadImageAndSaveData(String name, String city, String phone, String rating,
                                        String experience, String description, String email, String panditUid) {

        if (imageUri == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        uploadProgress.setVisibility(ProgressBar.VISIBLE);

        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            byte[] imageBytes = new byte[inputStream.available()];
            inputStream.read(imageBytes);

            String base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT);

            JSONObject payload = new JSONObject();
            payload.put("file", "data:image/jpeg;base64," + base64Image);
            payload.put("upload_preset", "puja mandal");  // ✅ Your Cloudinary preset
            payload.put("cloud_name", "dsbmvmdaa");       // ✅ Your Cloudinary cloud name

            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(payload.toString(), JSON);

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("https://api.cloudinary.com/v1_1/dsbmvmdaa/image/upload")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(() -> {
                        uploadProgress.setVisibility(ProgressBar.INVISIBLE);
                        Toast.makeText(AddPanditActivity.this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {
                            String responseStr = response.body().string();
                            JSONObject json = new JSONObject(responseStr);
                            String imageUrl = json.getString("secure_url");

                            runOnUiThread(() -> {
                                savePanditToFirestore(name, city, phone, rating, experience, description, email, imageUrl, panditUid);
                            });
                        } catch (JSONException e) {
                            runOnUiThread(() -> {
                                uploadProgress.setVisibility(ProgressBar.INVISIBLE);
                                Toast.makeText(AddPanditActivity.this, "Parse error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        }
                    } else {
                        runOnUiThread(() -> {
                            uploadProgress.setVisibility(ProgressBar.INVISIBLE);
                            Toast.makeText(AddPanditActivity.this, "Upload failed: " + response.message(), Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            });

        } catch (Exception e) {
            uploadProgress.setVisibility(ProgressBar.INVISIBLE);
            Toast.makeText(this, "Error reading image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    private void savePanditToFirestore(String name, String city, String phone, String rating,
                                       String experience, String description, String email, String imageUrl, String panditUid) {
        Map<String, Object> pandit = new HashMap<>();
        pandit.put("name", name);
        pandit.put("city", city);
        pandit.put("phone", phone);
        pandit.put("rating", rating);
        pandit.put("experience", experience);
        pandit.put("description", description);
        pandit.put("email", email);
        pandit.put("panditUid",panditUid);
        if (imageUrl != null) {
            pandit.put("imageUrl", imageUrl);
        }
        else {
            pandit.put("imageUrl","");
        }
        pandit.put("role","pandit");

        // Use a single write call instead of two redundant calls:
        db.collection("Pandits")
                .document(panditUid)
                .set(pandit)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(AddPanditActivity.this, "Pandit added successfully!", Toast.LENGTH_SHORT).show();
                    clearFields();
                    uploadProgress.setVisibility(ProgressBar.INVISIBLE);
                    imagePreview.setImageResource(R.drawable.ic_launcher_background);
                })
                .addOnFailureListener(e -> {
                    uploadProgress.setVisibility(ProgressBar.INVISIBLE);
                    Toast.makeText(AddPanditActivity.this, "Error saving data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private void clearFields() {
        editName.setText("");
        editCity.setText("");
        editPhone.setText("");
        editRating.setText("");
        editExperience.setText("");
        editDescription.setText("");
        editPassword.setText("");
        editEmail.setText("");
        imageUri = null;
    }
}
