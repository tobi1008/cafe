package com.example.cafe;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MembershipSettingsActivity extends AppCompatActivity {

    private static final String TAG = "MembershipSettings";
    private TextInputEditText etBronzeThreshold, etSilverThreshold, etGoldThreshold, etPlatinumThreshold,
            etDiamondThreshold;
    private TextInputEditText etBronzeVoucher, etSilverVoucher, etGoldVoucher, etPlatinumVoucher, etDiamondVoucher;
    private MaterialButton btnSaveSettings;
    private FirebaseFirestore db;
    private DocumentReference settingsRef;

    // Giá trị mặc định nếu không tìm thấy trên Firestore
    public static final long DEFAULT_BRONZE_THRESHOLD = 500000;
    public static final long DEFAULT_SILVER_THRESHOLD = 1000000;
    public static final long DEFAULT_GOLD_THRESHOLD = 4000000;
    public static final long DEFAULT_PLATINUM_THRESHOLD = 10000000;
    public static final long DEFAULT_DIAMOND_THRESHOLD = 20000000;

    public static final String DEFAULT_BRONZE_VOUCHER = "BRONZEUP";
    public static final String DEFAULT_SILVER_VOUCHER = "SILVERUP";
    public static final String DEFAULT_GOLD_VOUCHER = "GOLDUP";
    public static final String DEFAULT_PLATINUM_VOUCHER = "PLATINUMUP";
    public static final String DEFAULT_DIAMOND_VOUCHER = "DIAMONDUP";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_membership_settings);

        db = FirebaseFirestore.getInstance();
        settingsRef = db.collection("Settings").document("Membership");

        // Custom Back Button
        findViewById(R.id.buttonBack).setOnClickListener(v -> finish());

        // Ánh xạ UI Thresholds
        etBronzeThreshold = findViewById(R.id.etBronzeThreshold);
        etSilverThreshold = findViewById(R.id.etSilverThreshold);
        etGoldThreshold = findViewById(R.id.etGoldThreshold);
        etPlatinumThreshold = findViewById(R.id.etPlatinumThreshold);
        etDiamondThreshold = findViewById(R.id.etDiamondThreshold);

        // Ánh xạ UI Vouchers
        etBronzeVoucher = findViewById(R.id.etBronzeVoucher);
        etSilverVoucher = findViewById(R.id.etSilverVoucher);
        etGoldVoucher = findViewById(R.id.etGoldVoucher);
        etPlatinumVoucher = findViewById(R.id.etPlatinumVoucher);
        etDiamondVoucher = findViewById(R.id.etDiamondVoucher);

        btnSaveSettings = findViewById(R.id.btnSaveSettings);

        loadSettings();

        btnSaveSettings.setOnClickListener(v -> saveSettings());
    }

    private void loadSettings() {
        settingsRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                long bronze = documentSnapshot.contains("bronzeThreshold") ? documentSnapshot.getLong("bronzeThreshold")
                        : DEFAULT_BRONZE_THRESHOLD;
                long silver = documentSnapshot.contains("silverThreshold") ? documentSnapshot.getLong("silverThreshold")
                        : DEFAULT_SILVER_THRESHOLD;
                long gold = documentSnapshot.contains("goldThreshold") ? documentSnapshot.getLong("goldThreshold")
                        : DEFAULT_GOLD_THRESHOLD;
                long platinum = documentSnapshot.contains("platinumThreshold")
                        ? documentSnapshot.getLong("platinumThreshold")
                        : DEFAULT_PLATINUM_THRESHOLD;
                long diamond = documentSnapshot.contains("diamondThreshold")
                        ? documentSnapshot.getLong("diamondThreshold")
                        : DEFAULT_DIAMOND_THRESHOLD;

                String bronzeV = documentSnapshot.contains("bronzeVoucher")
                        ? documentSnapshot.getString("bronzeVoucher")
                        : DEFAULT_BRONZE_VOUCHER;
                String silverV = documentSnapshot.contains("silverVoucher")
                        ? documentSnapshot.getString("silverVoucher")
                        : DEFAULT_SILVER_VOUCHER;
                String goldV = documentSnapshot.contains("goldVoucher") ? documentSnapshot.getString("goldVoucher")
                        : DEFAULT_GOLD_VOUCHER;
                String platinumV = documentSnapshot.contains("platinumVoucher")
                        ? documentSnapshot.getString("platinumVoucher")
                        : DEFAULT_PLATINUM_VOUCHER;
                String diamondV = documentSnapshot.contains("diamondVoucher")
                        ? documentSnapshot.getString("diamondVoucher")
                        : DEFAULT_DIAMOND_VOUCHER;

                etBronzeThreshold.setText(String.format(Locale.US, "%d", bronze));
                etSilverThreshold.setText(String.format(Locale.US, "%d", silver));
                etGoldThreshold.setText(String.format(Locale.US, "%d", gold));
                etPlatinumThreshold.setText(String.format(Locale.US, "%d", platinum));
                etDiamondThreshold.setText(String.format(Locale.US, "%d", diamond));

                etBronzeVoucher.setText(bronzeV);
                etSilverVoucher.setText(silverV);
                etGoldVoucher.setText(goldV);
                etPlatinumVoucher.setText(platinumV);
                etDiamondVoucher.setText(diamondV);

            } else {
                etBronzeThreshold.setText(String.format(Locale.US, "%d", DEFAULT_BRONZE_THRESHOLD));
                etSilverThreshold.setText(String.format(Locale.US, "%d", DEFAULT_SILVER_THRESHOLD));
                etGoldThreshold.setText(String.format(Locale.US, "%d", DEFAULT_GOLD_THRESHOLD));
                etPlatinumThreshold.setText(String.format(Locale.US, "%d", DEFAULT_PLATINUM_THRESHOLD));
                etDiamondThreshold.setText(String.format(Locale.US, "%d", DEFAULT_DIAMOND_THRESHOLD));

                etBronzeVoucher.setText(DEFAULT_BRONZE_VOUCHER);
                etSilverVoucher.setText(DEFAULT_SILVER_VOUCHER);
                etGoldVoucher.setText(DEFAULT_GOLD_VOUCHER);
                etPlatinumVoucher.setText(DEFAULT_PLATINUM_VOUCHER);
                etDiamondVoucher.setText(DEFAULT_DIAMOND_VOUCHER);
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Lỗi khi tải cài đặt", e);
            Toast.makeText(this, "Lỗi khi tải cài đặt, dùng giá trị mặc định.", Toast.LENGTH_SHORT).show();
        });
    }

    private void saveSettings() {
        try {
            String bronzeStr = etBronzeThreshold.getText().toString().replace(".", "").trim();
            String silverStr = etSilverThreshold.getText().toString().replace(".", "").trim();
            String goldStr = etGoldThreshold.getText().toString().replace(".", "").trim();
            String platinumStr = etPlatinumThreshold.getText().toString().replace(".", "").trim();
            String diamondStr = etDiamondThreshold.getText().toString().replace(".", "").trim();

            String bronzeV = etBronzeVoucher.getText().toString().trim();
            String silverV = etSilverVoucher.getText().toString().trim();
            String goldV = etGoldVoucher.getText().toString().trim();
            String platinumV = etPlatinumVoucher.getText().toString().trim();
            String diamondV = etDiamondVoucher.getText().toString().trim();

            if (bronzeStr.isEmpty() || silverStr.isEmpty() || goldStr.isEmpty() || platinumStr.isEmpty()
                    || diamondStr.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đủ các mốc chi tiêu", Toast.LENGTH_SHORT).show();
                return;
            }

            long bronze = Long.parseLong(bronzeStr);
            long silver = Long.parseLong(silverStr);
            long gold = Long.parseLong(goldStr);
            long platinum = Long.parseLong(platinumStr);
            long diamond = Long.parseLong(diamondStr);

            if (bronze <= 0 || silver <= bronze || gold <= silver || platinum <= gold || diamond <= platinum) {
                Toast.makeText(this, "Giá trị phải tăng dần: Đồng < Bạc < Vàng < Platinum < Kim Cương",
                        Toast.LENGTH_LONG).show();
                return;
            }

            Map<String, Object> settings = new HashMap<>();
            settings.put("bronzeThreshold", bronze);
            settings.put("silverThreshold", silver);
            settings.put("goldThreshold", gold);
            settings.put("platinumThreshold", platinum);
            settings.put("diamondThreshold", diamond);

            settings.put("bronzeVoucher", bronzeV.isEmpty() ? DEFAULT_BRONZE_VOUCHER : bronzeV);
            settings.put("silverVoucher", silverV.isEmpty() ? DEFAULT_SILVER_VOUCHER : silverV);
            settings.put("goldVoucher", goldV.isEmpty() ? DEFAULT_GOLD_VOUCHER : goldV);
            settings.put("platinumVoucher", platinumV.isEmpty() ? DEFAULT_PLATINUM_VOUCHER : platinumV);
            settings.put("diamondVoucher", diamondV.isEmpty() ? DEFAULT_DIAMOND_VOUCHER : diamondV);

            settingsRef.set(settings)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Đã lưu cài đặt!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Lỗi khi lưu", Toast.LENGTH_SHORT).show());

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Vui lòng nhập số hợp lệ", Toast.LENGTH_SHORT).show();
        }
    }
}
