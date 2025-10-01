package com.example.cafe;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class AdminActivity extends AppCompatActivity {

    private EditText etName, etPrice, etDescription, etImageUrl;
    private Button btnAddProduct, btnManageProducts; // Thêm nút quản lý
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        db = FirebaseFirestore.getInstance();

        etName = findViewById(R.id.editTextProductName);
        etPrice = findViewById(R.id.editTextProductPrice);
        etDescription = findViewById(R.id.editTextProductDescription);
        etImageUrl = findViewById(R.id.editTextProductImageUrl);
        btnAddProduct = findViewById(R.id.buttonAddProduct);
        btnManageProducts = findViewById(R.id.buttonManageProducts); // Ánh xạ nút mới

        btnAddProduct.setOnClickListener(v -> {
            addProduct();
        });

        // Mở màn hình quản lý sản phẩm khi nhấn nút
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

        db.collection("cafe")
                .add(newProduct)
                .addOnSuccessListener(documentReference -> {
                    String docId = documentReference.getId();
                    Log.d("Firestore", "Sản phẩm đã được thêm với ID: " + docId);
                    Toast.makeText(AdminActivity.this, "Thêm sản phẩm thành công!", Toast.LENGTH_SHORT).show();
                    documentReference.update("id", docId);
                    etName.setText("");
                    etPrice.setText("");
                    etDescription.setText("");
                    etImageUrl.setText("");
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Lỗi khi thêm sản phẩm", e);
                    Toast.makeText(AdminActivity.this, "Thêm sản phẩm thất bại!", Toast.LENGTH_SHORT).show();
                });
    }
}

