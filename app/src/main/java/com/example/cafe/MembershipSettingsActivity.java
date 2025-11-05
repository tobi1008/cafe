package com.example.cafe;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MembershipSettingsActivity extends AppCompatActivity {

    private static final String TAG = "MembershipSettings";
    private TextInputEditText etSilverThreshold, etGoldThreshold;
    private MaterialButton btnSaveSettings;
    private FirebaseFirestore db;
    private DocumentReference settingsRef;

    // Giá trị mặc định nếu không tìm thấy trên Firestore
    public static final long DEFAULT_SILVER_THRESHOLD = 1000000;
    public static final long DEFAULT_GOLD_THRESHOLD = 4000000;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_membership_settings);

        db = FirebaseFirestore.getInstance();
        settingsRef = db.collection("Settings").document("Membership");

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbarMembershipSettings);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Ánh xạ UI
        etSilverThreshold = findViewById(R.id.etSilverThreshold);
        etGoldThreshold = findViewById(R.id.etGoldThreshold);
        btnSaveSettings = findViewById(R.id.btnSaveSettings);

        loadSettings();

        btnSaveSettings.setOnClickListener(v -> saveSettings());
    }

    private void loadSettings() {
        settingsRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                long silver = documentSnapshot.contains("silverThreshold") ?
                        documentSnapshot.getLong("silverThreshold") : DEFAULT_SILVER_THRESHOLD;
                long gold = documentSnapshot.contains("goldThreshold") ?
                        documentSnapshot.getLong("goldThreshold") : DEFAULT_GOLD_THRESHOLD;

                etSilverThreshold.setText(String.format(Locale.US, "%d", silver));
                etGoldThreshold.setText(String.format(Locale.US, "%d", gold));
                Log.d(TAG, "Tải cài đặt thành công: Bạc=" + silver + ", Vàng=" + gold);
            } else {
                Log.d(TAG, "Không tìm thấy tài liệu cài đặt, dùng giá trị mặc định.");
                etSilverThreshold.setText(String.format(Locale.US, "%d", DEFAULT_SILVER_THRESHOLD));
                etGoldThreshold.setText(String.format(Locale.US, "%d", DEFAULT_GOLD_THRESHOLD));
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Lỗi khi tải cài đặt", e);
            Toast.makeText(this, "Lỗi khi tải cài đặt, dùng giá trị mặc định.", Toast.LENGTH_SHORT).show();
            etSilverThreshold.setText(String.format(Locale.US, "%d", DEFAULT_SILVER_THRESHOLD));
            etGoldThreshold.setText(String.format(Locale.US, "%d", DEFAULT_GOLD_THRESHOLD));
        });
    }

    private void saveSettings() {
        String silverStr = etSilverThreshold.getText().toString().trim();
        String goldStr = etGoldThreshold.getText().toString().trim();

        String cleanSilverStr = silverStr.replace(".", "").replace(",", "");
        String cleanGoldStr = goldStr.replace(".", "").replace(",", "");

        if (cleanSilverStr.isEmpty() || cleanGoldStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập cả hai mốc chi tiêu", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Dùng chuỗi đã được làm sạch
            long silverValue = Long.parseLong(cleanSilverStr);
            long goldValue = Long.parseLong(cleanGoldStr);

            if (silverValue <= 0 || goldValue <= silverValue) {
                Toast.makeText(this, "Giá trị không hợp lệ. Vàng phải > Bạc > 0.", Toast.LENGTH_LONG).show();
                return;
            }

            Map<String, Object> settings = new HashMap<>();
            settings.put("silverThreshold", silverValue);
            settings.put("goldThreshold", goldValue);

            settingsRef.set(settings)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(MembershipSettingsActivity.this, "Lưu cài đặt thành công!", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Lưu cài đặt thành công: Bạc=" + silverValue + ", Vàng=" + goldValue);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(MembershipSettingsActivity.this, "Lưu thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Lỗi khi lưu cài đặt", e);
                    });

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Giá trị mốc tiền không hợp lệ. Vui lòng chỉ nhập số.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Lỗi NumberFormatException khi lưu cài đặt", e);
        }
    }
}

