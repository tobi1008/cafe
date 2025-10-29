package com.example.cafe;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class AdminActivity extends AppCompatActivity {

    private EditText etName, etPriceS, etPriceM, etPriceL, etDescription, etImageUrl, etSalePercent, etCategory;
    private Button btnAddProduct, btnManageProducts, btnManageOrders, btnManageVouchers, btnManageHappyHour;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        db = FirebaseFirestore.getInstance();

        // Ánh xạ đầy đủ các UI components
        etName = findViewById(R.id.editTextProductName);
        etPriceS = findViewById(R.id.editTextPriceS);
        etPriceM = findViewById(R.id.editTextPriceM);
        etPriceL = findViewById(R.id.editTextPriceL);
        etDescription = findViewById(R.id.editTextProductDescription);
        etImageUrl = findViewById(R.id.editTextProductImageUrl);
        etSalePercent = findViewById(R.id.editTextSalePercent);
        etCategory = findViewById(R.id.editTextCategory);
        btnAddProduct = findViewById(R.id.buttonAddProduct);
        btnManageProducts = findViewById(R.id.buttonManageProducts);
        btnManageOrders = findViewById(R.id.buttonManageOrders);
        btnManageVouchers = findViewById(R.id.buttonManageVouchers);
        btnManageHappyHour = findViewById(R.id.buttonManageHappyHour);

        // Gán sự kiện click cho các nút
        btnAddProduct.setOnClickListener(v -> addProduct());
        btnManageProducts.setOnClickListener(v -> startActivity(new Intent(AdminActivity.this, ManageProductsActivity.class)));
        btnManageOrders.setOnClickListener(v -> startActivity(new Intent(AdminActivity.this, ManageOrdersActivity.class)));
        btnManageVouchers.setOnClickListener(v -> startActivity(new Intent(AdminActivity.this, ManageVouchersActivity.class)));
        btnManageHappyHour.setOnClickListener(v -> startActivity(new Intent(AdminActivity.this, ManageHappyHourActivity.class)));
    }

    private void addProduct() {
        // Lấy dữ liệu từ các ô nhập liệu
        String ten = etName.getText().toString().trim();
        String priceSStr = etPriceS.getText().toString().trim();
        String priceMStr = etPriceM.getText().toString().trim();
        String priceLStr = etPriceL.getText().toString().trim();
        String moTa = etDescription.getText().toString().trim();
        String hinhAnh = etImageUrl.getText().toString().trim();
        String salePercentStr = etSalePercent.getText().toString().trim();
        String category = etCategory.getText().toString().trim();

        // Kiểm tra các trường bắt buộc
        if (ten.isEmpty() || priceMStr.isEmpty() || category.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên, giá size M và danh mục", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo một Map để chứa giá các size
        Map<String, Double> gia = new HashMap<>();
        if (!priceSStr.isEmpty()) gia.put("S", Double.parseDouble(priceSStr));
        if (!priceMStr.isEmpty()) gia.put("M", Double.parseDouble(priceMStr));
        if (!priceLStr.isEmpty()) gia.put("L", Double.parseDouble(priceLStr));

        int phanTramGiamGia = salePercentStr.isEmpty() ? 0 : Integer.parseInt(salePercentStr);


        Product newProduct = new Product(null, ten, gia, moTa, hinhAnh, phanTramGiamGia, category);


        db.collection("cafe")
                .add(newProduct)
                .addOnSuccessListener(documentReference -> {
                    String docId = documentReference.getId();
                    documentReference.update("id", docId);
                    Toast.makeText(AdminActivity.this, "Thêm sản phẩm thành công!", Toast.LENGTH_SHORT).show();
                    // Xóa trống các ô nhập liệu
                    etName.setText("");
                    etPriceS.setText("");
                    etPriceM.setText("");
                    etPriceL.setText("");
                    etDescription.setText("");
                    etImageUrl.setText("");
                    etSalePercent.setText("");
                    etCategory.setText("");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AdminActivity.this, "Thêm sản phẩm thất bại!", Toast.LENGTH_SHORT).show();
                    Log.e("AdminActivity", "Lỗi khi thêm sản phẩm", e);
                });
    }
}