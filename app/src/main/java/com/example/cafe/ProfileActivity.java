package com.example.cafe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvUserName, tvUserEmailPhone;
    private RelativeLayout layoutFavorites, layoutOrderHistory, layoutAdminPanel, layoutLogout;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Ánh xạ UI components mới
        tvUserName = findViewById(R.id.textViewUserName);
        tvUserEmailPhone = findViewById(R.id.textViewUserEmailPhone);
        layoutFavorites = findViewById(R.id.layoutFavorites);
        layoutOrderHistory = findViewById(R.id.layoutOrderHistory);
        layoutAdminPanel = findViewById(R.id.layoutAdminPanel);
        layoutLogout = findViewById(R.id.layoutLogout);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Lấy thông tin user từ Firestore để hiển thị
            loadUserInfo(currentUser.getUid(), currentUser.getEmail());
        }

        // Gán sự kiện click cho các menu item
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
    }

    private void loadUserInfo(String userId, String email) {
        tvUserEmailPhone.setText(email); // Hiển thị email ngay lập tức

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            // Hiển thị tên nếu có
                            if (user.getName() != null && !user.getName().isEmpty()) {
                                tvUserName.setText(user.getName());
                            } else {
                                // Nếu không có tên, tạm hiển thị phần đầu của email
                                tvUserName.setText(email.split("@")[0]);
                            }

                            // Kiểm tra vai trò để ẩn/hiện nút Admin
                            if ("admin".equals(user.getRole())) {
                                layoutAdminPanel.setVisibility(View.VISIBLE);
                            } else {
                                layoutAdminPanel.setVisibility(View.GONE);
                            }
                        }
                    } else {
                        // Nếu không có document user, mặc định là user thường
                        tvUserName.setText(email.split("@")[0]);
                        layoutAdminPanel.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    // Xử lý lỗi, mặc định ẩn nút admin
                    tvUserName.setText(email.split("@")[0]);
                    layoutAdminPanel.setVisibility(View.GONE);
                });
    }
}

