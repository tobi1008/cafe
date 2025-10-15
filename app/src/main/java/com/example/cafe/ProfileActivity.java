package com.example.cafe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private TextView userEmailTextView;
    private Button adminPanelButton, logoutButton, orderHistoryButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        userEmailTextView = findViewById(R.id.textViewUserEmail);
        adminPanelButton = findViewById(R.id.buttonAdminPanel);
        logoutButton = findViewById(R.id.buttonLogout);
        orderHistoryButton = findViewById(R.id.buttonOrderHistory);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userEmailTextView.setText(currentUser.getEmail());
            // KIỂM TRA VAI TRÒ NGƯỜI DÙNG
            checkUserRole(currentUser.getUid());
        }

        orderHistoryButton.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, OrderHistoryActivity.class));
        });

        adminPanelButton.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, AdminActivity.class));
        });

        logoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void checkUserRole(String userId) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        // Nếu người dùng có vai trò là "admin", hiển thị nút
                        if (user != null && "admin".equals(user.getRole())) {
                            adminPanelButton.setVisibility(View.VISIBLE);
                        } else {
                            adminPanelButton.setVisibility(View.GONE);
                        }
                    } else {
                        // Mặc định ẩn nếu không tìm thấy thông tin
                        adminPanelButton.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    // Ẩn nếu có lỗi
                    adminPanelButton.setVisibility(View.GONE);
                });
    }
}

