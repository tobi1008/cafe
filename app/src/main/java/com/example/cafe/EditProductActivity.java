package com.example.cafe;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EditProductActivity extends AppCompatActivity {

    private EditText etName, etPrice, etDescription, etImageUrl;
    private Button btnUpdateProduct;
    private AppDatabase appDatabase;
    private ExecutorService executorService;
    private int productId;
    private Product currentProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        appDatabase = AppDatabase.getDatabase(this);
        executorService = Executors.newSingleThreadExecutor();

        etName = findViewById(R.id.editTextEditProductName);
        etPrice = findViewById(R.id.editTextEditProductPrice);
        etDescription = findViewById(R.id.editTextEditProductDescription);
        etImageUrl = findViewById(R.id.editTextEditProductImageUrl);
        btnUpdateProduct = findViewById(R.id.buttonUpdateProduct);

        productId = getIntent().getIntExtra("PRODUCT_ID", -1);
        if (productId == -1) {
            Toast.makeText(this, "Lỗi: Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadProductDetails();

        btnUpdateProduct.setOnClickListener(v -> updateProduct());
    }

    private void loadProductDetails() {
        executorService.execute(() -> {
            currentProduct = appDatabase.productDao().getProductById(productId);
            runOnUiThread(() -> {
                if (currentProduct != null) {
                    etName.setText(currentProduct.getTen());
                    etPrice.setText(String.valueOf(currentProduct.getGia()));
                    etDescription.setText(currentProduct.getMoTa());
                    etImageUrl.setText(currentProduct.getHinhAnh());
                }
            });
        });
    }

    private void updateProduct() {
        String ten = etName.getText().toString().trim();
        String giaStr = etPrice.getText().toString().trim();
        String moTa = etDescription.getText().toString().trim();
        String hinhAnh = etImageUrl.getText().toString().trim();

        if (ten.isEmpty() || giaStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên và giá", Toast.LENGTH_SHORT).show();
            return;
        }

        double gia = Double.parseDouble(giaStr);

        // Cập nhật thông tin cho sản phẩm hiện tại
        currentProduct.setTen(ten);
        currentProduct.setGia(gia);
        currentProduct.setMoTa(moTa);
        currentProduct.setHinhAnh(hinhAnh);

        executorService.execute(() -> {
            appDatabase.productDao().updateProduct(currentProduct);
            runOnUiThread(() -> {
                Toast.makeText(EditProductActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                finish(); // Đóng màn hình sửa và quay lại danh sách
            });
        });
    }
}

