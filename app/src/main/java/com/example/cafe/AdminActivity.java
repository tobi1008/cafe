package com.example.cafe;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminActivity extends AppCompatActivity {

    private EditText etName, etPriceS, etPriceM, etPriceL, etDescription, etImageUrl, etSalePercent;
    private AutoCompleteTextView spinnerCategoryAdmin;
    private ArrayAdapter<String> categoryAdapterAdmin;
    private List<Category> categoryListAdmin = new ArrayList<>();
    private List<String> categoryNamesAdmin = new ArrayList<>();

    // Thêm nút mới
    private MaterialButton btnAddProduct, btnManageProducts, btnManageOrders, btnManageVouchers, btnManageHappyHour, btnManageCategories;
    private MaterialButton btnManageMembership, btnManageUsers; // NÚT MỚI

    private FirebaseFirestore db;
    private static final String TAG = "AdminActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        db = FirebaseFirestore.getInstance();

        // Ánh xạ UI Thêm Sản Phẩm
        etName = findViewById(R.id.editTextProductName);
        etPriceS = findViewById(R.id.editTextPriceS);
        etPriceM = findViewById(R.id.editTextPriceM);
        etPriceL = findViewById(R.id.editTextPriceL);
        etDescription = findViewById(R.id.editTextProductDescription);
        etImageUrl = findViewById(R.id.editTextProductImageUrl);
        etSalePercent = findViewById(R.id.editTextSalePercent);
        btnAddProduct = findViewById(R.id.buttonAddProduct);
        spinnerCategoryAdmin = findViewById(R.id.spinnerCategoryAdmin);

        // Ánh xạ Nút Quản Lý
        btnManageProducts = findViewById(R.id.buttonManageProducts);
        btnManageOrders = findViewById(R.id.buttonManageOrders);
        btnManageVouchers = findViewById(R.id.buttonManageVouchers);
        btnManageHappyHour = findViewById(R.id.buttonManageHappyHour);
        btnManageCategories = findViewById(R.id.buttonManageCategories);
        btnManageMembership = findViewById(R.id.buttonManageMembership);
        btnManageUsers = findViewById(R.id.buttonManageUsers);

        categoryAdapterAdmin = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categoryNamesAdmin);
        spinnerCategoryAdmin.setAdapter(categoryAdapterAdmin);

        loadCategoriesAdmin();

        // Gán sự kiện click cho các nút
        btnAddProduct.setOnClickListener(v -> addProduct());
        btnManageProducts.setOnClickListener(v -> startActivity(new Intent(AdminActivity.this, ManageProductsActivity.class)));
        btnManageOrders.setOnClickListener(v -> startActivity(new Intent(AdminActivity.this, ManageOrdersActivity.class)));
        btnManageVouchers.setOnClickListener(v -> startActivity(new Intent(AdminActivity.this, ManageVouchersActivity.class)));
        btnManageHappyHour.setOnClickListener(v -> startActivity(new Intent(AdminActivity.this, ManageHappyHourActivity.class)));
        btnManageCategories.setOnClickListener(v -> {
            Log.d(TAG, "Nút Quản lý Danh mục đã được bấm!");
            startActivity(new Intent(AdminActivity.this, ManageCategoriesActivity.class));
        });
        btnManageMembership.setOnClickListener(v -> {
            startActivity(new Intent(AdminActivity.this, MembershipSettingsActivity.class));
        });

        // SỰ KIỆN CLICK CHO NÚT MỚI
        btnManageUsers.setOnClickListener(v -> {
            startActivity(new Intent(AdminActivity.this, ManageUsersActivity.class));
        });
    }

    // (Giữ nguyên hàm loadCategoriesAdmin())
    private void loadCategoriesAdmin() {
        Log.d(TAG, "Bắt đầu tải Categories cho Admin Spinner...");
        db.collection("Categories")
                .orderBy("thuTuUuTien", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    categoryListAdmin.clear();
                    categoryNamesAdmin.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            Category cat = document.toObject(Category.class);
                            cat.setId(document.getId());
                            categoryListAdmin.add(cat);
                            categoryNamesAdmin.add(cat.getTenDanhMuc());
                        } catch (Exception e) {
                            Log.e(TAG, "Lỗi khi chuyển đổi Category: " + document.getId(), e);
                        }
                    }
                    categoryAdapterAdmin.notifyDataSetChanged();
                    Log.d(TAG, "Đã tải " + categoryNamesAdmin.size() + " categories vào Admin Spinner.");
                    if(categoryNamesAdmin.isEmpty()){
                        Toast.makeText(this, "Chưa có danh mục nào. Vui lòng vào 'Quản lý Danh mục' để thêm.", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi khi tải Categories cho Admin Spinner", e);
                    Toast.makeText(this, "Lỗi tải danh mục", Toast.LENGTH_SHORT).show();
                    categoryListAdmin.clear();
                    categoryNamesAdmin.clear();
                    categoryAdapterAdmin.notifyDataSetChanged();
                });
    }

    private void addProduct() {
        String ten = etName.getText().toString().trim();
        String priceSStr = etPriceS.getText().toString().trim();
        String priceMStr = etPriceM.getText().toString().trim();
        String priceLStr = etPriceL.getText().toString().trim();
        String moTa = etDescription.getText().toString().trim();
        String hinhAnh = etImageUrl.getText().toString().trim();
        String salePercentStr = etSalePercent.getText().toString().trim();
        String selectedCategoryName = spinnerCategoryAdmin.getText().toString();

        if (ten.isEmpty() || priceMStr.isEmpty() || selectedCategoryName == null || selectedCategoryName.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên, giá size M và chọn danh mục", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Double> gia = new HashMap<>();
        try {
            if (!priceSStr.isEmpty()) gia.put("S", Double.parseDouble(priceSStr));
            gia.put("M", Double.parseDouble(priceMStr));
            if (!priceLStr.isEmpty()) gia.put("L", Double.parseDouble(priceLStr));
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Giá tiền không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        int phanTramGiamGia = 0;
        if (!salePercentStr.isEmpty()) {
            try {
                phanTramGiamGia = Integer.parseInt(salePercentStr);
                if (phanTramGiamGia < 0 || phanTramGiamGia > 100) {
                    Toast.makeText(this, "Phần trăm giảm giá phải từ 0 đến 100", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Phần trăm giảm giá không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Product newProduct = new Product(null, ten, gia, moTa, hinhAnh, phanTramGiamGia, selectedCategoryName, null);

        db.collection("cafe")
                .add(newProduct)
                .addOnSuccessListener(documentReference -> {
                    String docId = documentReference.getId();
                    documentReference.update("id", docId)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(AdminActivity.this, "Thêm sản phẩm thành công!", Toast.LENGTH_SHORT).show();
                                etName.setText("");
                                etPriceS.setText("");
                                etPriceM.setText("");
                                etPriceL.setText("");
                                etDescription.setText("");
                                etImageUrl.setText("");
                                etSalePercent.setText("");
                                spinnerCategoryAdmin.setText("", false);

                            })
                            .addOnFailureListener(e -> {
                                Log.e("AdminActivity", "Lỗi khi cập nhật ID cho sản phẩm mới", e);
                                Toast.makeText(AdminActivity.this, "Thêm sản phẩm thành công (Lỗi cập nhật ID)", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AdminActivity.this, "Thêm sản phẩm thất bại!", Toast.LENGTH_SHORT).show();
                    Log.e("AdminActivity", "Lỗi khi thêm sản phẩm", e);
                });
    }
}