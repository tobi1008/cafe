package com.example.cafe.ui.profile;

import com.example.cafe.R;
import com.example.cafe.model.*;
import com.example.cafe.ui.auth.*;
import com.example.cafe.ui.home.*;
import com.example.cafe.ui.cart.*;
import com.example.cafe.ui.order.*;
import com.example.cafe.ui.admin.*;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfileActivity";

    private EditText etName, etPhone, etAddress, etEmail, etPassword;
    private MaterialButton btnSave;
    private Toolbar toolbar;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private User currentUserProfile; // Để lưu thông tin user tải về

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            // Không có user, quay về Login
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Ánh xạ UI
        etName = findViewById(R.id.etEditName);
        etPhone = findViewById(R.id.etEditPhone);
        etAddress = findViewById(R.id.etEditAddress);
        etEmail = findViewById(R.id.etEditEmail);
        etPassword = findViewById(R.id.etEditPassword);
        btnSave = findViewById(R.id.btnSaveProfile);

        // Custom Back Button
        findViewById(R.id.buttonBack).setOnClickListener(v -> finish());

        // Tải thông tin hiện tại
        loadExistingUserInfo();

        // Gán sự kiện
        btnSave.setOnClickListener(v -> saveProfileChanges());
    }

    private void loadExistingUserInfo() {
        if (currentUser != null) {
            etEmail.setText(currentUser.getEmail()); // Set email từ Auth
        }

        db.collection("users").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentUserProfile = documentSnapshot.toObject(User.class);
                        if (currentUserProfile != null) {
                            // Điền thông tin cũ vào các ô
                            etName.setText(currentUserProfile.getName());
                            etPhone.setText(currentUserProfile.getPhone());
                            etAddress.setText(currentUserProfile.getAddress());
                        }
                    } else {
                        Log.d(TAG, "No user document found, creating new one...");
                        // Có thể tạo document mới ở đây nếu cần
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load user info", e);
                    Toast.makeText(this, "Lỗi khi tải thông tin", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveProfileChanges() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (name.isEmpty()) {
            etName.setError("Tên không được để trống");
            return;
        }
        if (email.isEmpty()) {
            etEmail.setError("Email không được để trống");
            return;
        }

        // Show loading or disable button to prevent multiple clicks (Optional but
        // recommended)
        btnSave.setEnabled(false);
        Toast.makeText(this, "Đang cập nhật...", Toast.LENGTH_SHORT).show();

        // 1. Cập nhật Firestore trước
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("phone", phone);
        updates.put("address", address);
        updates.put("email", email);

        db.collection("users").document(currentUser.getUid())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    // Firestore thành công -> Tiếp tục cập nhật Auth
                    updateAuthInfo(email, password);
                })
                .addOnFailureListener(e -> {
                    btnSave.setEnabled(true);
                    Toast.makeText(this, "Lỗi cập nhật Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Failed to update profile", e);
                });
    }

    private void updateAuthInfo(String newEmail, String newPassword) {
        // Cập nhật Email nếu thay đổi
        if (!newEmail.equals(currentUser.getEmail())) {
            currentUser.updateEmail(newEmail)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "User email updated.");
                        // Sau khi xong email -> update password
                        updatePasswordInSequence(newPassword);
                    })
                    .addOnFailureListener(e -> {
                        btnSave.setEnabled(true);
                        Toast.makeText(this, "Lỗi cập nhật Email (Cần đăng nhập lại): " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    });
        } else {
            // Email không đổi -> chuyển sang password
            updatePasswordInSequence(newPassword);
        }
    }

    private void updatePasswordInSequence(String newPassword) {
        if (!newPassword.isEmpty()) {
            currentUser.updatePassword(newPassword)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        btnSave.setEnabled(true);
                        Toast.makeText(this, "Lỗi cập nhật Mật khẩu (Cần đăng nhập lại): " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    });
        } else {
            // Không đổi password -> Hoàn tất
            Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
