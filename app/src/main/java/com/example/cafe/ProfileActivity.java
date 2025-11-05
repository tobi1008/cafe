package com.example.cafe;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.NumberFormat;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvUserName, tvUserEmailPhone, tvUserPhone, tvUserAddress, tvMemberTier;
    private RelativeLayout layoutFavorites, layoutOrderHistory, layoutAdminPanel, layoutLogout;
    private ImageView imageViewAvatar;
    private ImageButton btnEditProfile;

    private ProgressBar progressMembership;
    private TextView tvMembershipProgress;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private static final String TAG = "ProfileActivity";

    private long TIER_SILVER_START = 1000000;
    private long TIER_GOLD_START = 4000000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvUserName = findViewById(R.id.textViewUserName);
        tvUserEmailPhone = findViewById(R.id.textViewUserEmailPhone);
        layoutFavorites = findViewById(R.id.layoutFavorites);
        layoutOrderHistory = findViewById(R.id.layoutOrderHistory);
        layoutAdminPanel = findViewById(R.id.layoutAdminPanel);
        layoutLogout = findViewById(R.id.layoutLogout);
        imageViewAvatar = findViewById(R.id.imageViewAvatar);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        tvUserPhone = findViewById(R.id.tvUserPhone);
        tvUserAddress = findViewById(R.id.tvUserAddress);
        tvMemberTier = findViewById(R.id.tvMemberTier);
        progressMembership = findViewById(R.id.progressMembership);
        tvMembershipProgress = findViewById(R.id.tvMembershipProgress);


        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Tải mốc cài đặt TRƯỚC, sau đó mới tải thông tin user
            loadMembershipSettingsAndThenUserInfo(currentUser.getUid(), currentUser.getEmail());
        } else {
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

        layoutFavorites.setOnClickListener(v ->
                startActivity(new Intent(ProfileActivity.this, FavoritesActivity.class)));
        layoutOrderHistory.setOnClickListener(v ->
                startActivity(new Intent(ProfileActivity.this, OrderHistoryActivity.class)));
        layoutAdminPanel.setOnClickListener(v ->
                startActivity(new Intent(ProfileActivity.this, AdminActivity.class)));
        layoutLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
        btnEditProfile.setOnClickListener(v ->
                startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class)));
    }

    private void loadMembershipSettingsAndThenUserInfo(String userId, String email) {
        db.collection("Settings").document("Membership").get()
                .addOnSuccessListener(settingsDoc -> {
                    if (settingsDoc.exists()) {
                        // Lấy mốc tiền từ Cài đặt
                        TIER_SILVER_START = settingsDoc.contains("silverThreshold") ?
                                settingsDoc.getLong("silverThreshold") : 1000000;
                        TIER_GOLD_START = settingsDoc.contains("goldThreshold") ?
                                settingsDoc.getLong("goldThreshold") : 4000000;
                        Log.d(TAG, "Tải mốc cài đặt thành công: Bạc=" + TIER_SILVER_START + ", Vàng=" + TIER_GOLD_START);
                    } else {
                        Log.d(TAG, "Không tìm thấy file cài đặt, dùng mốc mặc định.");
                    }
                    // Sau khi có mốc tiền, mới tải thông tin user
                    loadUserInfo(userId, email);
                })
                .addOnFailureListener(e -> {
                    // Nếu lỗi, dùng mốc mặc định
                    Log.e(TAG, "Lỗi tải mốc cài đặt, dùng mốc mặc định.", e);
                    loadUserInfo(userId, email);
                });
    }

    private void loadUserInfo(String userId, String email) {
        final String userEmail = (email == null) ? "N/A" : email;
        tvUserEmailPhone.setText(userEmail);

        db.collection("users").document(userId)
                .addSnapshotListener((documentSnapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Listen failed.", error);
                        tvUserName.setText(userEmail.split("@")[0]);
                        layoutAdminPanel.setVisibility(View.GONE);
                        tvUserPhone.setText("Chưa cập nhật");
                        tvUserAddress.setText("Chưa cập nhật");
                        tvMemberTier.setText("Đồng");
                        // Gọi hàm update UI với mốc tiền đã tải
                        updateMembershipProgressUI("Đồng", 0);
                        return;
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            // logic hiển thị Tên, SĐT, Địa chỉ
                            if (user.getName() != null && !user.getName().isEmpty()) {
                                tvUserName.setText(user.getName());
                            } else {
                                tvUserName.setText(userEmail.split("@")[0]);
                            }
                            if (user.getPhone() != null && !user.getPhone().isEmpty()) {
                                tvUserPhone.setText(user.getPhone());
                            } else {
                                tvUserPhone.setText("Chưa cập nhật");
                            }
                            if (user.getAddress() != null && !user.getAddress().isEmpty()) {
                                tvUserAddress.setText(user.getAddress());
                            } else {
                                tvUserAddress.setText("Chưa cập nhật");
                            }

                            // HIỂN THỊ HẠNG VÀ TIẾN TRÌNH
                            String tier = user.getMemberTier();
                            double spending = user.getTotalSpending();
                            if (tier == null || tier.isEmpty()) {
                                tier = "Đồng";
                            }
                            tvMemberTier.setText(tier);

                            // Gọi hàm update UI với mốc tiền đã tải
                            updateMembershipProgressUI(tier, spending);

                            //  logic Ẩn/Hiện nút Admin
                            if ("admin".equals(user.getRole())) {
                                layoutAdminPanel.setVisibility(View.VISIBLE);
                            } else {
                                layoutAdminPanel.setVisibility(View.GONE);
                            }
                        } else {
                            Log.e(TAG, "User object is null after conversion.");
                            tvUserName.setText(userEmail.split("@")[0]);
                            layoutAdminPanel.setVisibility(View.GONE);
                        }
                    } else {
                        Log.d(TAG, "No user document found for ID: " + userId);
                        tvUserName.setText(userEmail.split("@")[0]);
                        layoutAdminPanel.setVisibility(View.GONE);
                    }
                });
    }

    private void updateMembershipProgressUI(String tier, double spending) {

        long currentSpending = (long) spending;

        Locale locale = new Locale("vi", "VN");
        NumberFormat formatter = NumberFormat.getCurrencyInstance(locale);
        formatter.setMaximumFractionDigits(0);
        String formattedSpending = formatter.format(currentSpending);

        if (tier.equals("Đồng")) {
            String formattedMax = formatter.format(TIER_SILVER_START);
            progressMembership.setMax((int) TIER_SILVER_START);
            progressMembership.setProgress((int) currentSpending);
            tvMembershipProgress.setText(String.format("Chi tiêu: %s / %s", formattedSpending, formattedMax));
            tvMemberTier.setTextColor(ContextCompat.getColor(this, android.R.color.black));

        } else if (tier.equals("Bạc")) {
            String formattedMax = formatter.format(TIER_GOLD_START);
            progressMembership.setMax((int) (TIER_GOLD_START - TIER_SILVER_START));
            progressMembership.setProgress((int) (currentSpending - TIER_SILVER_START));
            tvMembershipProgress.setText(String.format("Chi tiêu: %s / %s", formattedSpending, formattedMax));
            tvMemberTier.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));

        } else { // Vàng
            progressMembership.setMax(100);
            progressMembership.setProgress(100);
            tvMembershipProgress.setText(String.format("Đã đạt hạng cao nhất! (Tổng chi tiêu: %s)", formattedSpending));
            tvMemberTier.setTextColor(ContextCompat.getColor(this, R.color.colorWarning));
        }
    }
}

