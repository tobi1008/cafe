package com.example.cafe;

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

    private EditText etName, etPhone, etAddress;
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
        toolbar = findViewById(R.id.toolbarEditProfile);
        etName = findViewById(R.id.etEditName);
        etPhone = findViewById(R.id.etEditPhone);
        etAddress = findViewById(R.id.etEditAddress);
        btnSave = findViewById(R.id.btnSaveProfile);

        // Toolbar
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed()); // Nút back

        // Tải thông tin hiện tại
        loadExistingUserInfo();

        // Gán sự kiện
        btnSave.setOnClickListener(v -> saveProfileChanges());
    }

    private void loadExistingUserInfo() {
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

        if (name.isEmpty()) {
            etName.setError("Tên không được để trống");
            return;
        }

        // Tạo Map để cập nhật
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("phone", phone);
        updates.put("address", address);

        db.collection("users").document(currentUser.getUid())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show();
                    finish(); // Đóng màn hình và quay lại Profile
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Failed to update profile", e);
                });
    }
}

