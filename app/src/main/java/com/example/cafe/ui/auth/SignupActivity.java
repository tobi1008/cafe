package com.example.cafe.ui.auth;

import com.example.cafe.R;
import com.example.cafe.model.*;
import com.example.cafe.ui.home.*;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignupActivity extends AppCompatActivity {

    private TextInputEditText emailEditText, passwordEditText, confirmPasswordEditText, nameEditText, phoneEditText;
    private MaterialButton signupButton;
    private TextView loginTextView;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        nameEditText = findViewById(R.id.editTextName);
        phoneEditText = findViewById(R.id.editTextPhone);
        emailEditText = findViewById(R.id.editTextEmail);
        passwordEditText = findViewById(R.id.editTextPassword);
        confirmPasswordEditText = findViewById(R.id.editTextConfirmPassword);
        signupButton = findViewById(R.id.buttonSignUp);
        loginTextView = findViewById(R.id.textViewLogin);

        signupButton.setOnClickListener(v -> {
            // Sử dụng getText() chắc chắn không null với TextInputEditText nhưng cần kiểm
            // tra cẩn thận
            String name = nameEditText.getText() != null ? nameEditText.getText().toString().trim() : "";
            String phone = phoneEditText.getText() != null ? phoneEditText.getText().toString().trim() : "";
            String email = emailEditText.getText() != null ? emailEditText.getText().toString().trim() : "";
            String password = passwordEditText.getText() != null ? passwordEditText.getText().toString().trim() : "";
            String confirmPassword = confirmPasswordEditText.getText() != null
                    ? confirmPasswordEditText.getText().toString().trim()
                    : "";

            // CẬP NHẬT KIỂM TRA VALIDATION
            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || name.isEmpty()
                    || phone.isEmpty()) {
                Toast.makeText(SignupActivity.this, "Vui lòng điền đầy đủ thông tin.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(SignupActivity.this, "Mật khẩu không khớp.", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                // GỌI CONSTRUCTOR MỚI (VỚI TÊN VÀ SĐT)
                                User newUser = new User(firebaseUser.getEmail(), name, phone);

                                newUser.setRole("user");
                                newUser.setMemberTier("Thành viên");
                                newUser.setTotalSpending(0);

                                db.collection("users").document(firebaseUser.getUid()).set(newUser)
                                        .addOnSuccessListener(aVoid -> {
                                            // Chỉ chuyển màn hình sau khi lưu Firestore thành công
                                            Toast.makeText(SignupActivity.this, "Đăng ký thành công!",
                                                    Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(SignupActivity.this, "Đăng ký thất bại: " + e.getMessage(),
                                                    Toast.LENGTH_LONG).show();
                                        });
                            }
                        } else {
                            if (task.getException() != null) {
                                Toast.makeText(SignupActivity.this,
                                        "Đăng ký thất bại: " + task.getException().getMessage(),
                                        Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(SignupActivity.this, "Đăng ký thất bại.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        });

        loginTextView.setOnClickListener(v -> finish());
    }
}