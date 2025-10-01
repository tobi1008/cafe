package com.example.cafe;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private TextView userEmailTextView;
    private Button logoutButton, adminButton; // Thêm biến cho nút Admin

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userEmailTextView = findViewById(R.id.textViewUserEmail);
        logoutButton = findViewById(R.id.buttonLogout);
        adminButton = findViewById(R.id.buttonAdminPanel); // Ánh xạ nút Admin

        // Lấy email từ SharedPreferences và hiển thị
        SharedPreferences prefs = getSharedPreferences("MyPrefsFile", MODE_PRIVATE);
        String email = prefs.getString("email", "Không tìm thấy email");
        userEmailTextView.setText(email);

        // Xử lý sự kiện đăng xuất
        logoutButton.setOnClickListener(v -> {
            // Xóa thông tin đăng nhập và quay về màn hình Login
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();

            Toast.makeText(ProfileActivity.this, "Đăng xuất thành công", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // --- LOGIC MỚI ĐƯỢC THÊM VÀO ---
        // Xử lý sự kiện khi nhấn nút Admin
        adminButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, AdminActivity.class);
            startActivity(intent);
        });
        // ------------------------------------
    }
}

