package com.example.cafe;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SignupActivity extends AppCompatActivity {

    // Đặt một tên hằng số cho file SharedPreferences để có thể dùng lại ở màn hình Login
    public static final String MY_PREFS_NAME = "MyPrefsFile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Ánh xạ các view từ layout
        final EditText emailEditText = findViewById(R.id.editTextEmail);
        final EditText passwordEditText = findViewById(R.id.editTextPassword);
        final EditText confirmPasswordEditText = findViewById(R.id.editTextConfirmPassword);
        Button signupButton = findViewById(R.id.buttonSignup);

        // Thiết lập sự kiện click cho nút Đăng ký
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                String confirmPassword = confirmPasswordEditText.getText().toString().trim();

                // Kiểm tra dữ liệu đầu vào
                if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(SignupActivity.this, "Vui lòng điền đầy đủ thông tin.", Toast.LENGTH_SHORT).show();
                } else if (!password.equals(confirmPassword)) {
                    Toast.makeText(SignupActivity.this, "Mật khẩu xác nhận không khớp.", Toast.LENGTH_SHORT).show();
                } else {
                    // --- BẮT ĐẦU LƯU DỮ LIỆU VỚI SharedPreferences ---

                    // 1. Lấy đối tượng SharedPreferences.Editor để có thể chỉnh sửa dữ liệu
                    SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();

                    // 2. Đặt dữ liệu vào editor dưới dạng key-value
                    editor.putString("email", email);
                    editor.putString("password", password);

                    // 3. Gọi apply() để lưu lại những thay đổi.
                    // (apply() lưu trong nền, không làm ứng dụng bị đứng)
                    editor.apply();

                    // Hiển thị thông báo thành công
                    Toast.makeText(SignupActivity.this, "Đăng ký thành công! Giờ bạn có thể đăng nhập.", Toast.LENGTH_LONG).show();

                    // Sau khi đăng ký thành công, tự động quay lại màn hình đăng nhập
                    finish();
                }
            }
        });
    }
}

