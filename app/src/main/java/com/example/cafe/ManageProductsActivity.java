package com.example.cafe;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ManageProductsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ManageProductsAdapter adapter;
    private List<Product> productList;
    private FirebaseFirestore db;
    private static final String TAG = "ManageProductsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_products);

        db = FirebaseFirestore.getInstance();
        productList = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerViewManageProducts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ManageProductsAdapter(this, productList, product -> {
            // --- XỬ LÝ SỰ KIỆN SỬA ---
            Intent intent = new Intent(ManageProductsActivity.this, EditProductActivity.class);
            intent.putExtra("PRODUCT_ID", product.getId()); // Truyền ID của sản phẩm qua
            startActivity(intent);
            // -------------------------
        }, product -> {
            // Xử lý sự kiện XOÁ sản phẩm
            new AlertDialog.Builder(this)
                    .setTitle("Xác nhận Xoá")
                    .setMessage("Bạn có chắc chắn muốn xoá sản phẩm '" + product.getTen() + "' không?")
                    .setPositiveButton("Xoá", (dialog, which) -> deleteProduct(product))
                    .setNegativeButton("Huỷ", null)
                    .show();
        });

        recyclerView.setAdapter(adapter);

        // NÚT QUAY LẠI
        findViewById(R.id.buttonBack).setOnClickListener(v -> finish());
    }

    // Tải lại dữ liệu mỗi khi quay lại màn hình này
    @Override
    protected void onResume() {
        super.onResume();
        fetchProducts();
    }

    private void fetchProducts() {
        db.collection("cafe")
                // *** SẮP XẾP Theo "ten" (tên), thứ tự Tăng dần (A-Z) ***
                .orderBy("ten", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        productList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try { // Thêm try-catch
                                Product product = document.toObject(Product.class);
                                // (Quan trọng: Đảm bảo sản phẩm có ID, nếu không Sửa/Xoá sẽ lỗi)
                                // ID đã được cập nhật khi thêm mới, nên ở đây OK
                                productList.add(product);
                            } catch (Exception e) {
                                Log.e(TAG, "Lỗi khi chuyển đổi Product: " + document.getId(), e);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.w("Firestore", "Error getting documents.", task.getException());
                    }
                });
    }

    private void deleteProduct(Product product) {
        if (product.getId() == null || product.getId().isEmpty()) {
            Toast.makeText(this, "Không thể xoá sản phẩm không có ID", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("cafe").document(product.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đã xoá sản phẩm: " + product.getTen(), Toast.LENGTH_SHORT).show();

                    int position = productList.indexOf(product);
                    if (position != -1) {
                        productList.remove(position);
                        adapter.notifyItemRemoved(position);
                    } else {
                        fetchProducts(); // Nếu không tìm thấy, tải lại
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi xoá sản phẩm", Toast.LENGTH_SHORT).show();
                    Log.w("Firestore", "Error deleting document", e);
                });
    }
}
