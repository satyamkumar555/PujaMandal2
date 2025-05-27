package admin;

import static com.example.pujamandal.R.*;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pujamandal.R;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddPujaActivity extends AppCompatActivity {
    private EditText pujaname, samaanprice, priceonlypandit;
    private Button add;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_puja);

        FirebaseApp.initializeApp(this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance());

        pujaname = findViewById(R.id.PujaName);
        samaanprice = findViewById(R.id.SamaanPrice);
        priceonlypandit = findViewById(R.id.priceOnlyPandit);
        add = findViewById(R.id.btnAdd);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = pujaname.getText().toString().trim();
                String priceWithSamaanStr = samaanprice.getText().toString().trim();
                String pricePanditOnlyStr = priceonlypandit.getText().toString().trim();

                if (name.isEmpty() || priceWithSamaanStr.isEmpty() || pricePanditOnlyStr.isEmpty()) {
                    Toast.makeText(AddPujaActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    int priceWithSamaan = Integer.parseInt(priceWithSamaanStr);
                    int pricePanditOnly = Integer.parseInt(pricePanditOnlyStr);

                    // Create a Map to store puja data
                    Map<String, Object> pujaData = new HashMap<>();
                    pujaData.put("name", name);
                    pujaData.put("priceWithSamaan", priceWithSamaan);       // stored as number
                    pujaData.put("pricePanditOnly", pricePanditOnly);       // stored as number

                    // Store in Firestore
                    db.collection("puja").document(name)
                            .set(pujaData)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(AddPujaActivity.this, "Puja added successfully", Toast.LENGTH_SHORT).show();
                                pujaname.setText("");
                                samaanprice.setText("");
                                priceonlypandit.setText("");
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(AddPujaActivity.this, "Failed to add Puja: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });

                } catch (NumberFormatException e) {
                    Toast.makeText(AddPujaActivity.this, "Please enter valid numeric prices", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
