package com.example.cafe;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class ManageCategoriesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CategoryManageAdapter adapter;
    private List<Category> categoryList = new ArrayList<>();
    private FirebaseFirestore db;
    private static final String TAG = "ManageCategoriesAct";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_categories);

        db = FirebaseFirestore.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbarManageCategories);
        setSupportActionBar(toolbar);
        // getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Bật nút back nếu cần
        // toolbar.setNavigationOnClickListener(v -> onBackPressed());

        recyclerView = findViewById(R.id.recyclerViewCategoriesManage);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new CategoryManageAdapter(this, categoryList, new CategoryManageAdapter.OnCategoryManageListener() {
            @Override
            public void onEditCategoryClick(Category category) {
                if (category == null || category.getId() == null || category.getId().isEmpty()) {
                    Toast.makeText(ManageCategoriesActivity.this, "Lỗi: Không tìm thấy ID để sửa", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "onEditCategoryClick: Category ID is null or empty");
                    return;
                }
                showAddEditDialog(category);
            }

            @Override
            public void onDeleteCategoryClick(Category category) {
                if (category == null || category.getId() == null || category.getId().isEmpty()) {
                    Toast.makeText(ManageCategoriesActivity.this, "Lỗi: Không tìm thấy ID để xóa", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "onDeleteCategoryClick: Category ID is null or empty");
                    return;
                }
                deleteCategory(category);
            }
        });
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fabAddCategory);
        fab.setOnClickListener(v -> showAddEditDialog(null));

        loadCategories();
    }

    private void loadCategories() {
        db.collection("Categories") // *** Tên collection mới là "Categories" ***
                .orderBy("tenDanhMuc", Query.Direction.ASCENDING) // Sắp xếp theo tên
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Lỗi khi tải Categories", error);
                        Toast.makeText(this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (value != null) {
                        categoryList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            try {
                                Category cat = doc.toObject(Category.class);
                                cat.setId(doc.getId()); // Gán ID từ DocumentSnapshot
                                categoryList.add(cat);
                            } catch (Exception e) {
                                Log.e(TAG, "Lỗi chuyển đổi category: " + doc.getId(), e);
                            }
                        }
                        adapter.notifyDataSetChanged();
                        Log.d(TAG, "Đã tải " + categoryList.size() + " categories.");
                    } else {
                        Log.d(TAG, "Không có dữ liệu Categories.");
                    }
                });
    }

    private void deleteCategory(Category category) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận Xóa")
                .setMessage("Bạn có chắc muốn xóa danh mục '" + category.getTenDanhMuc() + "'? \n(Lưu ý: Các sản phẩm thuộc danh mục này sẽ không bị xóa)")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    Log.d(TAG, "Đang xóa Category ID: " + category.getId());
                    db.collection("Categories").document(category.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Xóa thành công ID: " + category.getId());
                                Toast.makeText(this, "Xóa thành công", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Xóa thất bại ID: " + category.getId(), e);
                                Toast.makeText(this, "Xóa thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showAddEditDialog(Category categoryToEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_edit_category, null);
        builder.setView(dialogView);

        EditText etName = dialogView.findViewById(R.id.etCategoryName);
        Button btnSave = dialogView.findViewById(R.id.btnSaveCategory);

        AlertDialog dialog = builder.create();
        if (categoryToEdit != null) {
            dialog.setTitle("Sửa Danh Mục");
            etName.setText(categoryToEdit.getTenDanhMuc());
        } else {
            dialog.setTitle("Thêm Danh Mục Mới");
        }

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();

            if (TextUtils.isEmpty(name)) {
                Toast.makeText(this, "Vui lòng nhập tên danh mục", Toast.LENGTH_SHORT).show();
                return;
            }

            if (categoryToEdit != null) {
                // --- CHẾ ĐỘ SỬA ---
                Category updatedCategory = new Category(categoryToEdit.getId(), name);
                Log.d(TAG, "Đang cập nhật Category ID: " + categoryToEdit.getId());
                db.collection("Categories").document(categoryToEdit.getId())
                        .set(updatedCategory) // Dùng set để ghi đè
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "Cập nhật thành công ID: " + categoryToEdit.getId());
                            Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Cập nhật thất bại ID: " + categoryToEdit.getId(), e);
                            Toast.makeText(this, "Cập nhật thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
            } else {
                // --- CHẾ ĐỘ THÊM MỚI ---
                DocumentReference newDocRef = db.collection("Categories").document();
                String newId = newDocRef.getId();
                Category newCategory = new Category(newId, name);
                Log.d(TAG, "Đang thêm Category mới với ID: " + newId);
                newDocRef.set(newCategory)
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

