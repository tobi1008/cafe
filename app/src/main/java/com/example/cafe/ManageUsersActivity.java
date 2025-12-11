package com.example.cafe;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ManageUsersActivity extends AppCompatActivity implements UserAdapter.OnUserInteractionListener {

    private static final String TAG = "ManageUsersActivity";

    private RecyclerView recyclerViewUsers;
    private UserAdapter userAdapter;
    private List<User> userList = new ArrayList<>();
    private TextView textViewNoUsers;
    private FirebaseFirestore db;

    private long TIER_SILVER_START = 1000000;
    private long TIER_GOLD_START = 4000000;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_users);

        db = FirebaseFirestore.getInstance();

        MaterialToolbar toolbar = findViewById(R.id.toolbarManageUsers);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);
        textViewNoUsers = findViewById(R.id.textViewNoUsers);

        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new UserAdapter(this, userList, this);
        recyclerViewUsers.setAdapter(userAdapter);

        loadMembershipSettingsAndThenUsers();
    }

    private void loadMembershipSettingsAndThenUsers() {
        db.collection("Settings").document("Membership").get()
                .addOnSuccessListener(settingsDoc -> {
                    if (settingsDoc.exists()) {
                        TIER_SILVER_START = settingsDoc.contains("silverThreshold")
                                ? settingsDoc.getLong("silverThreshold")
                                : 1000000;
                        TIER_GOLD_START = settingsDoc.contains("goldThreshold") ? settingsDoc.getLong("goldThreshold")
                                : 4000000;
                        Log.d(TAG, "Tải mốc cài đặt: Bạc=" + TIER_SILVER_START + ", Vàng=" + TIER_GOLD_START);
                    }
                    loadUsers();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi tải mốc cài đặt, dùng mốc mặc định.", e);
                    loadUsers();
                });
    }

    private void loadUsers() {
        db.collection("users")
                .orderBy("email", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Lỗi khi tải danh sách user", error);
                        Toast.makeText(this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        userList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            try {
                                User user = doc.toObject(User.class);
                                user.setUid(doc.getId());
                                userList.add(user);
                            } catch (Exception e) {
                                Log.e(TAG, "Lỗi khi chuyển đổi user: " + doc.getId(), e);
                            }
                        }
                        userAdapter.notifyDataSetChanged();

                        if (userList.isEmpty()) {
                            textViewNoUsers.setVisibility(View.VISIBLE);
                            recyclerViewUsers.setVisibility(View.GONE);
                        } else {
                            textViewNoUsers.setVisibility(View.GONE);
                            recyclerViewUsers.setVisibility(View.VISIBLE);
                        }
                        Log.d(TAG, "Đã tải " + userList.size() + " users.");
                    } else {
                        Log.d(TAG, "Không có dữ liệu user.");
                        textViewNoUsers.setVisibility(View.VISIBLE);
                        recyclerViewUsers.setVisibility(View.GONE);
                    }
                });
    }

    // Xử lý click Sửa
    @Override
    public void onEditClick(User user) {
        showEditUserDialog(user);
    }

    // Xử lý click Xóa
    @Override
    public void onDeleteClick(User user) {
        showDeleteConfirmationDialog(user);
    }

    private void showDeleteConfirmationDialog(User user) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận Xóa")
                .setMessage("Bạn có chắc muốn xóa user '" + user.getEmail()
                        + "'?\n\nHệ thống sẽ xóa:\n- Thông tin hồ sơ (Database)\n- Lịch sử đơn hàng (Database)\n\nLƯU Ý: Tài khoản đăng nhập (Email) KHÔNG thể xóa từ đây do chính sách bảo mật của Google. User vẫn có thể đăng nhập lại.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    if (user.getUid() == null || user.getUid().isEmpty()) {
                        Toast.makeText(this, "Lỗi: Không tìm thấy User ID", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    deleteUserAndRelatedData(user.getUid());
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteUserAndRelatedData(String userId) {
        // 1. Tìm và xóa tất cả đơn hàng của user trước
        db.collection("orders")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Dùng Batch để xóa nhiều file một lúc cho nhanh và an toàn
                    com.google.firebase.firestore.WriteBatch batch = db.batch();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        batch.delete(doc.getReference());
                    }

                    // Thêm lệnh xóa User vào batch luôn
                    batch.delete(db.collection("users").document(userId));

                    // Thực thi
                    batch.commit()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Đã xóa sạch dữ liệu User và Đơn hàng!", Toast.LENGTH_SHORT)
                                        .show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Lỗi khi xóa dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT)
                                        .show();
                                Log.e(TAG, "Delete failed", e);
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi tìm đơn hàng của user.", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Find orders failed", e);
                });
    }

    private void showEditUserDialog(User user) {
        if (user.getUid() == null || user.getUid().isEmpty()) {
            Toast.makeText(this, "Lỗi: Không tìm thấy User ID", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_user, null);
        builder.setView(dialogView);

        TextView tvTitle = dialogView.findViewById(R.id.tvEditUserTitle);
        RadioGroup rgRole = dialogView.findViewById(R.id.rgRole);
        RadioButton radioUser = dialogView.findViewById(R.id.radioRoleUser);
        RadioButton radioAdmin = dialogView.findViewById(R.id.radioRoleAdmin);
        EditText etSpending = dialogView.findViewById(R.id.etEditTotalSpending);
        Button btnSave = dialogView.findViewById(R.id.btnSaveUserChanges);

        // Hiển thị dữ liệu cũ
        tvTitle.setText("Sửa: " + user.getEmail());
        if ("admin".equals(user.getRole())) {
            radioAdmin.setChecked(true);
        } else {
            radioUser.setChecked(true);
        }

        // Định dạng tiền tệ để hiển thị
        Locale locale = new Locale("vi", "VN");
        NumberFormat formatter = NumberFormat.getNumberInstance(locale);
        formatter.setMaximumFractionDigits(0);
        etSpending.setText(formatter.format(user.getTotalSpending()));

        AlertDialog dialog = builder.create();

        btnSave.setOnClickListener(v -> {
            String newRole = radioAdmin.isChecked() ? "admin" : "user";

            String spendingStr = etSpending.getText().toString().trim().replaceAll("[.,]", "");
            double newSpending;

            if (TextUtils.isEmpty(spendingStr)) {
                newSpending = 0;
            } else {
                try {
                    newSpending = Double.parseDouble(spendingStr);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Tổng chi tiêu không hợp lệ", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // TÍNH TOÁN LẠI HẠNG MỚI
            String newTier = "Đồng";
            if (newSpending >= TIER_GOLD_START) {
                newTier = "Vàng";
            } else if (newSpending >= TIER_SILVER_START) {
                newTier = "Bạc";
            }

            // Chuẩn bị dữ liệu cập nhật
            Map<String, Object> updates = new HashMap<>();
            updates.put("role", newRole);
            updates.put("totalSpending", newSpending);
            updates.put("memberTier", newTier);

            // Cập nhật lên Firestore
            db.collection("users").document(user.getUid())
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Cập nhật thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        dialog.show();
    }
}