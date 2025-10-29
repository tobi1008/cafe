package com.example.cafe;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log; // *** THÊM IMPORT NÀY ***
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;


import java.util.ArrayList;
import java.util.List;

public class ManageHappyHourActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private HappyHourAdapter adapter;
    private List<HappyHour> happyHourList = new ArrayList<>();
    private FirebaseFirestore db;
    private static final String TAG = "ManageHappyHourActivity"; // *** THÊM TAG ĐỂ DEBUG ***

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_happy_hour);

        db = FirebaseFirestore.getInstance();

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbarManageHappyHour);
        setSupportActionBar(toolbar);
        // Bỏ dòng này nếu bạn không muốn nút back
        // getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // RecyclerView
        recyclerView = findViewById(R.id.recyclerViewHappyHours);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Adapter
        adapter = new HappyHourAdapter(this, happyHourList, new HappyHourAdapter.OnHappyHourListener() {
            @Override
            public void onEditClick(HappyHour happyHour) {
                // *** KIỂM TRA ID TRƯỚC KHI SỬA ***
                if (happyHour == null || happyHour.getId() == null || happyHour.getId().isEmpty()) {
                    Toast.makeText(ManageHappyHourActivity.this, "Lỗi: Không tìm thấy ID để sửa", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "onEditClick: HappyHour ID is null or empty");
                    return;
                }
                showAddEditDialog(happyHour);
            }

            @Override
            public void onDeleteClick(HappyHour happyHour) {
                // *** KIỂM TRA ID TRƯỚC KHI XOÁ ***
                if (happyHour == null || happyHour.getId() == null || happyHour.getId().isEmpty()) {
                    Toast.makeText(ManageHappyHourActivity.this, "Lỗi: Không tìm thấy ID để xóa", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "onDeleteClick: HappyHour ID is null or empty");
                    return;
                }
                deleteHappyHour(happyHour);
            }
        });
        recyclerView.setAdapter(adapter);

        // Nút FAB
        FloatingActionButton fab = findViewById(R.id.fabAddHappyHour);
        fab.setOnClickListener(v -> showAddEditDialog(null)); // null nghĩa là Thêm Mới

        // Tải dữ liệu
        loadHappyHours();
    }

    // *** HÀM LOADHAPPYHOURS ĐÃ SỬA LỖI ***
    private void loadHappyHours() {
        db.collection("HappyHours")
                .orderBy("gioBatDau", Query.Direction.ASCENDING) // Sắp xếp theo giờ bắt đầu
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Lỗi khi tải HappyHours", error); // Log lỗi chi tiết
                        Toast.makeText(this, "Lỗi khi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (value != null) {
                        happyHourList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            try {
                                HappyHour hh = doc.toObject(HappyHour.class);
                                // *** SỬA LỖI QUAN TRỌNG: Gán ID từ DocumentSnapshot ***
                                hh.setId(doc.getId());
                                happyHourList.add(hh);
                            } catch (Exception e) {
                                Log.e(TAG, "Lỗi khi chuyển đổi document: " + doc.getId(), e);
                                // Bỏ qua document lỗi và tiếp tục
                            }
                        }
                        adapter.notifyDataSetChanged();
                        Log.d(TAG, "Đã tải " + happyHourList.size() + " khung giờ vàng."); // Log số lượng tải được
                    } else {
                        Log.d(TAG, "Không có dữ liệu HappyHours.");
                    }
                });
    }


    private void deleteHappyHour(HappyHour happyHour) {
        // Hỏi xác nhận trước khi xóa
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận Xóa")
                .setMessage("Bạn có chắc muốn xóa khung giờ '" + happyHour.getTenKhungGio() + "'?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    Log.d(TAG, "Đang xóa HappyHour ID: " + happyHour.getId()); // Log ID trước khi xóa
                    db.collection("HappyHours").document(happyHour.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Xóa thành công ID: " + happyHour.getId());
                                Toast.makeText(this, "Xóa thành công", Toast.LENGTH_SHORT).show();
                                // Không cần gọi loadHappyHours() vì addSnapshotListener tự cập nhật
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Xóa thất bại ID: " + happyHour.getId(), e);
                                Toast.makeText(this, "Xóa thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showAddEditDialog(HappyHour happyHourToEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_edit_happy_hour, null);
        builder.setView(dialogView);

        // Ánh xạ UI của Dialog (Khớp với file XML trong Canvas)
        EditText etName = dialogView.findViewById(R.id.etHappyHourName);
        EditText etStartHour = dialogView.findViewById(R.id.etStartHour);
        EditText etEndHour = dialogView.findViewById(R.id.etEndHour);
        EditText etDiscount = dialogView.findViewById(R.id.etDiscountPercent);
        Switch switchStatus = dialogView.findViewById(R.id.switchStatus);
        Button btnSave = dialogView.findViewById(R.id.btnSaveHappyHour);

        // Đặt tiêu đề
        AlertDialog dialog = builder.create();
        if (happyHourToEdit != null) {
            dialog.setTitle("Sửa Khung Giờ Vàng");
            etName.setText(happyHourToEdit.getTenKhungGio());
            etStartHour.setText(String.valueOf(happyHourToEdit.getGioBatDau()));
            etEndHour.setText(String.valueOf(happyHourToEdit.getGioKetThuc()));
            etDiscount.setText(String.valueOf(happyHourToEdit.getPhanTramGiamGia()));
            switchStatus.setChecked(happyHourToEdit.isDangBat());
        } else {
            dialog.setTitle("Thêm Khung Giờ Vàng Mới");
        }

        btnSave.setOnClickListener(v -> {
            // Lấy dữ liệu
            String name = etName.getText().toString().trim();
            String startStr = etStartHour.getText().toString().trim();
            String endStr = etEndHour.getText().toString().trim();
            String discountStr = etDiscount.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(startStr) || TextUtils.isEmpty(endStr) || TextUtils.isEmpty(discountStr)) {
                Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            // *** THÊM KIỂM TRA GIỜ HỢP LỆ ***
            int start, end, discount;
            try {
                start = Integer.parseInt(startStr);
                end = Integer.parseInt(endStr);
                discount = Integer.parseInt(discountStr);
                if (start < 0 || start > 23 || end < 0 || end > 23 || start >= end || discount < 0 || discount > 100) {
                    Toast.makeText(this, "Giờ (0-23), Giảm giá (0-100) không hợp lệ hoặc Giờ bắt đầu >= Giờ kết thúc", Toast.LENGTH_LONG).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Giờ và Giảm giá phải là số", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean status = switchStatus.isChecked();

            if (happyHourToEdit != null) {
                // --- CHẾ ĐỘ SỬA ---
                HappyHour updatedHappyHour = new HappyHour(happyHourToEdit.getId(), name, start, end, discount, status);
                Log.d(TAG, "Đang cập nhật HappyHour ID: " + happyHourToEdit.getId()); // Log ID trước khi cập nhật
                db.collection("HappyHours").document(happyHourToEdit.getId())
                        .set(updatedHappyHour)
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "Cập nhật thành công ID: " + happyHourToEdit.getId());
                            Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Cập nhật thất bại ID: " + happyHourToEdit.getId(), e);
                            Toast.makeText(this, "Cập nhật thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
            } else {
                // --- CHẾ ĐỘ THÊM MỚI ---
                DocumentReference newDocRef = db.collection("HappyHours").document();
                String newId = newDocRef.getId();
                HappyHour newHappyHour = new HappyHour(newId, name, start, end, discount, status);
                Log.d(TAG, "Đang thêm HappyHour mới với ID: " + newId); // Log ID trước khi thêm
                newDocRef.set(newHappyHour)
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "Thêm thành công ID: " + newId);
                            Toast.makeText(this, "Thêm thành công", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Thêm thất bại ID: " + newId, e);
                            Toast.makeText(this, "Thêm thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
            }
        });

        dialog.show();
    }
}

