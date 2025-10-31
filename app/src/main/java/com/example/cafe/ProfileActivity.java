package com.example.cafe;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvUserName, tvUserEmailPhone;
    private RelativeLayout layoutFavorites, layoutOrderHistory, layoutAdminPanel, layoutLogout;
    private ImageView imageViewAvatar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private static final String TAG = "ProfileActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Ánh xạ UI components
        tvUserName = findViewById(R.id.textViewUserName);
        tvUserEmailPhone = findViewById(R.id.textViewUserEmailPhone);
        layoutFavorites = findViewById(R.id.layoutFavorites);
        layoutOrderHistory = findViewById(R.id.layoutOrderHistory);
        layoutAdminPanel = findViewById(R.id.layoutAdminPanel);
        layoutLogout = findViewById(R.id.layoutLogout);
        imageViewAvatar = findViewById(R.id.imageViewAvatar);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            loadUserInfo(currentUser.getUid(), currentUser.getEmail());
        } else {
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
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
        final String userEmail = (email == null) ? "N/A" : email;

        if (userEmail.equals("N/A")) {
            Log.w(TAG, "Email is null for user: " + userId);
        }

        // 2. Dùng biến final mới
        tvUserEmailPhone.setText(userEmail);

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            // Hiển thị tên nếu có
                            if (user.getName() != null && !user.getName().isEmpty()) {
                                tvUserName.setText(user.getName());
                            } else {
                                tvUserName.setText(userEmail.split("@")[0]);
                            }

                            // Kiểm tra vai trò để ẩn/hiện nút Admin
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
                        // Nếu không có document user, mặc định là user thường
                        Log.d(TAG, "No user document found for ID: " + userId);
                        tvUserName.setText(userEmail.split("@")[0]);
                        layoutAdminPanel.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load user info", e);
                    tvUserName.setText(userEmail.split("@")[0]);
                    layoutAdminPanel.setVisibility(View.GONE);
                });
    }
}

