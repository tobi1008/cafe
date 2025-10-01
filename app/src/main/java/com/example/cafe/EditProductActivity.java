package com.example.cafe;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditProductActivity extends AppCompatActivity {

    private EditText etName, etPrice, etDescription, etImageUrl;
    private Button btnSaveChanges;
    private FirebaseFirestore db;
    private String productId;
    private DocumentReference productRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        db = FirebaseFirestore.getInstance();

        etName = findViewById(R.id.editTextProductName);
        etPrice = findViewById(R.id.editTextProductPrice);
        etDescription = findViewById(R.id.editTextProductDescription);
        etImageUrl = findViewById(R.id.editTextProductImageUrl);
        btnSaveChanges = findViewById(R.id.buttonSaveChanges);

        // Lấy ID của sản phẩm được truyền từ ManageProductsActivity
        productId = getIntent().getStringExtra("PRODUCT_ID");
        if (productId == null || productId.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không tìm thấy ID sản phẩm", Toast.LENGTH_SHORT).show();
            finish(); // Đóng Activity nếu không có ID
            return;
        }

        // Tạo tham chiếu đến document sản phẩm trên Firestore
        productRef = db.collection("cafe").document(productId);

        loadProductData();

        btnSaveChanges.setOnClickListener(v -> saveChanges());
    }

    // Tải dữ liệu hiện tại của sản phẩm và hiển thị lên các ô EditText
    private void loadProductData() {
        productRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Product product = documentSnapshot.toObject(Product.class);
                if (product != null) {
                    etName.setText(product.getTen());
                    etPrice.setText(String.valueOf(product.getGia()));
                    etDescription.setText(product.getMoTa());
                    etImageUrl.setText(product.getHinhAnh());
                }
            } else {
                Toast.makeText(this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Lỗi khi tải dữ liệu", Toast.LENGTH_SHORT).show();
        });
    }

    // Lưu lại những thay đổi lên Firestore
    private void saveChanges() {
        String ten = etName.getText().toString().trim();
        String giaStr = etPrice.getText().toString().trim();
        String moTa = etDescription.getText().toString().trim();
        String hinhAnh = etImageUrl.getText().toString().trim();

        if (ten.isEmpty() || giaStr.isEmpty()) {
            Toast.makeText(this, "Tên và giá không được để trống", Toast.LENGTH_SHORT).show();
            return;
        }

        double gia = Double.parseDouble(giaStr);

        // Cập nhật các trường của document
        productRef.update(
                "ten", ten,
                "gia", gia,
                "moTa", moTa,
                "hinhAnh", hinhAnh
        ).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
            finish(); // Đóng màn hình sửa sau khi cập nhật
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
        });
    }
}
