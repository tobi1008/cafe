package com.example.cafe;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminActivity extends AppCompatActivity {

    private EditText etName, etPrice, etDescription, etImageUrl;
    private Button btnAddProduct, btnManageProducts;
    private AppDatabase appDatabase;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Khởi tạo database và ExecutorService để chạy tác vụ nền
        appDatabase = AppDatabase.getDatabase(this);
        executorService = Executors.newSingleThreadExecutor();

        etName = findViewById(R.id.editTextProductName);
        etPrice = findViewById(R.id.editTextProductPrice);
        etDescription = findViewById(R.id.editTextProductDescription);
        etImageUrl = findViewById(R.id.editTextProductImageUrl);
        btnAddProduct = findViewById(R.id.buttonAddProduct);
        btnManageProducts = findViewById(R.id.buttonManageProducts);

        btnAddProduct.setOnClickListener(v -> addProduct());

        btnManageProducts.setOnClickListener(v -> {
            startActivity(new Intent(AdminActivity.this, ManageProductsActivity.class));
        });
    }

    private void addProduct() {
        String ten = etName.getText().toString().trim();
        String giaStr = etPrice.getText().toString().trim();
        String moTa = etDescription.getText().toString().trim();
        String hinhAnh = etImageUrl.getText().toString().trim();

        if (ten.isEmpty() || giaStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên và giá", Toast.LENGTH_SHORT).show();
            return;
        }

        double gia = Double.parseDouble(giaStr);
        Product newProduct = new Product(ten, gia, moTa, hinhAnh);

        // Chạy tác vụ thêm sản phẩm trên một luồng nền
        executorService.execute(() -> {
            appDatabase.productDao().insertProduct(newProduct);
            // Hiển thị thông báo trên luồng UI chính
            runOnUiThread(() -> {
                Toast.makeText(AdminActivity.this, "Thêm sản phẩm thành công!", Toast.LENGTH_SHORT).show();
                etName.setText("");
                etPrice.setText("");
                etDescription.setText("");
                etImageUrl.setText("");
            });
        });
    }
}

