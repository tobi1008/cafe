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

import java.text.NumberFormat;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    // (Giữ nguyên các biến UI cũ)
    private TextView tvUserName, tvUserEmailPhone, tvUserPhone, tvUserAddress, tvMemberTier;
    private RelativeLayout layoutFavorites, layoutOrderHistory, layoutAdminPanel, layoutLogout;
    private ImageView imageViewAvatar;
    private ImageButton btnEditProfile;

    private ProgressBar progressMembership;
    private TextView tvMembershipProgress;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private static final String TAG = "ProfileActivity";

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
            loadUserInfo(currentUser.getUid(), currentUser.getEmail());
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

    private void loadUserInfo(String userId, String email) {
        final String userEmail = (email == null) ? "N/A" : email;

        if (userEmail.equals("N/A")) {
            Log.w(TAG, "Email is null for user: " + userId);
        }

        tvUserEmailPhone.setText(userEmail);

        db.collection("users").document(userId)
                .addSnapshotListener((documentSnapshot, error) -> {
                    // (Giữ nguyên code xử lý lỗi)
                    if (error != null) {
                        Log.e(TAG, "Listen failed.", error);
                        tvUserName.setText(userEmail.split("@")[0]);
                        layoutAdminPanel.setVisibility(View.GONE);
                        tvUserPhone.setText("Chưa cập nhật");
                        tvUserAddress.setText("Chưa cập nhật");

                        tvMemberTier.setText("Đồng");
                        updateMembershipProgress("Đồng", 0);
                        return;
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            // hiển thị Tên, SĐT, Địa chỉ
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

                            // *** HIỂN THỊ HẠNG VÀ TIẾN TRÌNH ***
                            String tier = user.getMemberTier();
                            double spending = user.getTotalSpending();

                            if (tier == null || tier.isEmpty()) {
                                tier = "Đồng";
                            }

                            tvMemberTier.setText(tier);
                            updateMembershipProgress(tier, spending);


                            //  Ẩn/Hiện nút
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

    // *** UI TIẾN TRÌNH HẠNG ***
    private void updateMembershipProgress(String tier, double spending) {
        // Định nghĩa các mốc chi tiêu
        final long TIER_SILVER_START = 1000000;
        final long TIER_GOLD_START = 4000000;
        long currentSpending = (long) spending;

        // Định dạng tiền tệ VNĐ
        Locale locale = new Locale("vi", "VN");
        NumberFormat formatter = NumberFormat.getCurrencyInstance(locale);
        formatter.setMaximumFractionDigits(0);

        String formattedSpending = formatter.format(currentSpending);

        if (tier.equals("Đồng")) {
            // Hạng Đồng: Tiến trình từ 0 -> 1.000.000
            String formattedMax = formatter.format(TIER_SILVER_START);
            progressMembership.setMax((int) TIER_SILVER_START);
            progressMembership.setProgress((int) currentSpending);
            tvMembershipProgress.setText(String.format("Chi tiêu: %s / %s", formattedSpending, formattedMax));
            tvMemberTier.setTextColor(ContextCompat.getColor(this, android.R.color.black));

        } else if (tier.equals("Bạc")) {
            // Hạng Bạc:  từ 1.000.000 -> 4.000.000
            String formattedMax = formatter.format(TIER_GOLD_START);
            progressMembership.setMax((int) (TIER_GOLD_START - TIER_SILVER_START));
            progressMembership.setProgress((int) (currentSpending - TIER_SILVER_START));
            tvMembershipProgress.setText(String.format("Chi tiêu: %s / %s", formattedSpending, formattedMax));
            // Set màu chữ (ví dụ: màu xám bạc)
            tvMemberTier.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));

        } else { // Vàng
            // Hạng Vàng: Đã max
            progressMembership.setMax(100);
            progressMembership.setProgress(100);
            tvMembershipProgress.setText(String.format("Đã đạt hạng cao nhất! (Tổng chi tiêu: %s)", formattedSpending));
            tvMemberTier.setTextColor(ContextCompat.getColor(this, R.color.colorWarning));
        }
    }
}
