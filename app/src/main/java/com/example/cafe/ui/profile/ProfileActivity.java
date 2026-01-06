package com.example.cafe.ui.profile;

import com.example.cafe.R;
import com.example.cafe.model.*;
import com.example.cafe.ui.auth.*;
import com.example.cafe.ui.home.*;
import com.example.cafe.ui.cart.*;
import com.example.cafe.ui.order.*;
import com.example.cafe.ui.admin.*;
import com.example.cafe.ui.product.*;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.LinearLayout; // Add import if missing or ensure it's there
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
    private LinearLayout layoutFavorites, layoutOrderHistory, layoutAdminPanel, layoutLogout; // Changed to LinearLayout
    private ImageView imageViewAvatar;
    private ImageButton btnEditProfile;

    private ProgressBar progressMembership;
    private TextView tvMembershipProgress;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private static final String TAG = "ProfileActivity";

    private long TIER_BRONZE_START = 500000;
    private long TIER_SILVER_START = 1000000;
    private long TIER_GOLD_START = 4000000;
    private long TIER_PLATINUM_START = 10000000;
    private long TIER_DIAMOND_START = 20000000;

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

        layoutFavorites
                .setOnClickListener(v -> startActivity(new Intent(ProfileActivity.this, FavoritesActivity.class)));
        layoutOrderHistory
                .setOnClickListener(v -> startActivity(new Intent(ProfileActivity.this, OrderHistoryActivity.class)));
        layoutAdminPanel.setOnClickListener(v -> startActivity(new Intent(ProfileActivity.this, AdminActivity.class)));
        layoutLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
        btnEditProfile
                .setOnClickListener(v -> startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class)));
    }

    private void loadMembershipSettingsAndThenUserInfo(String userId, String email) {
        db.collection("Settings").document("Membership").get()
                .addOnSuccessListener(settingsDoc -> {
                    if (settingsDoc.exists()) {
                        // Lấy mốc tiền từ Cài đặt
                        TIER_BRONZE_START = settingsDoc.contains("bronzeThreshold")
                                ? settingsDoc.getLong("bronzeThreshold")
                                : 500000;
                        TIER_SILVER_START = settingsDoc.contains("silverThreshold")
                                ? settingsDoc.getLong("silverThreshold")
                                : 1000000;
                        TIER_GOLD_START = settingsDoc.contains("goldThreshold") ? settingsDoc.getLong("goldThreshold")
                                : 4000000;
                        TIER_PLATINUM_START = settingsDoc.contains("platinumThreshold")
                                ? settingsDoc.getLong("platinumThreshold")
                                : 10000000;
                        TIER_DIAMOND_START = settingsDoc.contains("diamondThreshold")
                                ? settingsDoc.getLong("diamondThreshold")
                                : 20000000;

                        Log.d(TAG, "Tải mốc cài đặt thành công.");
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
                        tvMemberTier.setText("Thành viên");
                        // Gọi hàm update UI
                        updateMembershipProgressUI("Thành viên", 0);
                        return;
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {

                            // TỰ ĐỘNG ĐỒNG BỘ HẠNG (Auto-Sync)
                            // Kiểm tra xem hạng hiện tại có khớp với số tiền chi tiêu không
                            verifyAndSyncTier(user, userId);

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
                                tier = "Thành viên";
                            }
                            tvMemberTier.setText(tier);

                            // Gọi hàm update UI với mốc tiền đã tải
                            updateMembershipProgressUI(tier, spending);

                            // logic Ẩn/Hiện nút Admin
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

    private void verifyAndSyncTier(User user, String userId) {
        double spending = user.getTotalSpending();
        String currentTier = user.getMemberTier();
        if (currentTier == null)
            currentTier = "Thành viên";

        String calculatedTier = "Thành viên";
        if (spending >= TIER_DIAMOND_START)
            calculatedTier = "Kim Cương";
        else if (spending >= TIER_PLATINUM_START)
            calculatedTier = "Platinum";
        else if (spending >= TIER_GOLD_START)
            calculatedTier = "Vàng";
        else if (spending >= TIER_SILVER_START)
            calculatedTier = "Bạc";
        else if (spending >= TIER_BRONZE_START)
            calculatedTier = "Đồng";

        final String finalCorrectTier = calculatedTier; // Biến final để dùng trong Lambda

        if (!currentTier.equals(finalCorrectTier)) {
            Log.w(TAG, "Phát hiện sai lệch hạng! Hiện tại: " + currentTier + ", Đúng: " + finalCorrectTier
                    + ". Đang đồng bộ...");
            // Cập nhật lại Firestore
            db.collection("users").document(userId).update("memberTier", finalCorrectTier)
                    .addOnSuccessListener(
                            aVoid -> Log.d(TAG, "Đã tự động đồng bộ hạng thành công: " + finalCorrectTier))
                    .addOnFailureListener(e -> Log.e(TAG, "Lỗi khi đồng bộ hạng", e));
        }
    }

    private void updateMembershipProgressUI(String tier, double spending) {

        long currentSpending = (long) spending;

        Locale locale = new Locale("vi", "VN");
        NumberFormat formatter = NumberFormat.getCurrencyInstance(locale);
        formatter.setMaximumFractionDigits(0);
        String formattedSpending = formatter.format(currentSpending);

        long maxProgress = 0;
        long currentProgress = 0;
        String nextTierName = "";
        int tierColor = android.R.color.black;

        switch (tier) {
            case "Thành viên":
                maxProgress = TIER_BRONZE_START;
                currentProgress = currentSpending;
                nextTierName = "Đồng";
                tierColor = android.R.color.black;
                break;
            case "Đồng":
                maxProgress = TIER_SILVER_START - TIER_BRONZE_START;
                currentProgress = currentSpending - TIER_BRONZE_START;
                nextTierName = "Bạc";
                tierColor = R.color.colorBronze;
                break;
            case "Bạc":
                maxProgress = TIER_GOLD_START - TIER_SILVER_START;
                currentProgress = currentSpending - TIER_SILVER_START;
                nextTierName = "Vàng";
                tierColor = android.R.color.darker_gray;
                break;
            case "Vàng":
                maxProgress = TIER_PLATINUM_START - TIER_GOLD_START;
                currentProgress = currentSpending - TIER_GOLD_START;
                nextTierName = "Platinum";
                tierColor = R.color.colorGold;
                break;
            case "Platinum":
                maxProgress = TIER_DIAMOND_START - TIER_PLATINUM_START;
                currentProgress = currentSpending - TIER_PLATINUM_START;
                nextTierName = "Kim Cương";
                tierColor = R.color.colorPlatinum;
                break;
            case "Kim Cương":
                maxProgress = 100;
                currentProgress = 100;
                nextTierName = "Max";
                tierColor = R.color.colorDiamond;
                break;
            default:
                maxProgress = TIER_BRONZE_START;
                currentProgress = currentSpending;
        }

        if (nextTierName.equals("Max")) {
            progressMembership.setMax(100);
            progressMembership.setProgress(100);
            tvMembershipProgress.setText(String.format("Đã đạt hạng cao nhất! (%s)", formattedSpending));
        } else {
            // Đảm bảo không âm
            if (currentProgress < 0)
                currentProgress = 0;
            // Nếu current > max (do chưa update tier), set full
            if (currentProgress > maxProgress)
                currentProgress = maxProgress;

            progressMembership.setMax((int) maxProgress);
            progressMembership.setProgress((int) currentProgress);

            String formattedMax = formatter.format(maxProgress + (spending - currentProgress)); // Hiển thị mốc tổng
            // Hoặc đơn giản hiển thị số dư cần thiết:
            long remaining = maxProgress - currentProgress;
            String formattedRemaining = formatter.format(remaining);

            // Logic hiển thị cũ: Chi tiêu: [Hiện tại] / [Mốc kế tiếp]
            long nextTierValue = 0;
            if (tier.equals("Thành viên"))
                nextTierValue = TIER_BRONZE_START;
            else if (tier.equals("Đồng"))
                nextTierValue = TIER_SILVER_START;
            else if (tier.equals("Bạc"))
                nextTierValue = TIER_GOLD_START;
            else if (tier.equals("Vàng"))
                nextTierValue = TIER_PLATINUM_START;
            else if (tier.equals("Platinum"))
                nextTierValue = TIER_DIAMOND_START;

            String formattedNext = formatter.format(nextTierValue);
            tvMembershipProgress.setText(String.format("Chi tiêu: %s / %s", formattedSpending, formattedNext));
        }

        tvMemberTier.setTextColor(ContextCompat.getColor(this, tierColor));
    }
}
