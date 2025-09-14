package com.example.cafe;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        TextView textViewProfileEmail = findViewById(R.id.textViewProfileEmail);
        Button buttonLogout = findViewById(R.id.buttonLogout);

        // Lấy SharedPreferences để đọc thông tin
        SharedPreferences prefs = getSharedPreferences("MyPrefsFile", MODE_PRIVATE);
        String email = prefs.getString("email", "Không có thông tin"); // Lấy email đã lưu

        // Hiển thị email lên TextView
        textViewProfileEmail.setText(email);

        // Xử lý sự kiện cho nút Đăng xuất
        buttonLogout.setOnClickListener(v -> {
            // Tạo Intent để quay về màn hình đăng nhập
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            // Xóa hết các Activity cũ và tạo một task mới, để người dùng không thể back lại
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish(); // Đóng màn hình Profile
        });
    }
}
