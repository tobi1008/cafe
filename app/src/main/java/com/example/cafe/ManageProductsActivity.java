package com.example.cafe;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ManageProductsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ManageProductsAdapter adapter;
    private List<Product> productList = new ArrayList<>();
    private AppDatabase appDatabase;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_products);

        appDatabase = AppDatabase.getDatabase(this);
        executorService = Executors.newSingleThreadExecutor();

        recyclerView = findViewById(R.id.recyclerViewManageProducts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ManageProductsAdapter(this, productList,
                product -> { // Edit click listener
                    Intent intent = new Intent(ManageProductsActivity.this, EditProductActivity.class);
                    intent.putExtra("PRODUCT_ID", product.getId());
                    startActivity(intent);
                },
                product -> { // Delete click listener
                    showDeleteConfirmationDialog(product);
                });
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProducts(); // Tải lại danh sách mỗi khi quay lại màn hình này
    }

    private void loadProducts() {
        executorService.execute(() -> {
            List<Product> productsFromDb = appDatabase.productDao().getAllProducts();
            runOnUiThread(() -> {
                productList.clear();
                productList.addAll(productsFromDb);
                adapter.notifyDataSetChanged();
            });
        });
    }

    private void showDeleteConfirmationDialog(Product product) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận Xóa")
                .setMessage("Bạn có chắc chắn muốn xóa sản phẩm '" + product.getTen() + "' không?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteProduct(product))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteProduct(Product product) {
        executorService.execute(() -> {
            appDatabase.productDao().deleteProduct(product);
            // Sau khi xóa, tải lại danh sách trên luồng UI
            runOnUiThread(this::loadProducts);
        });
    }
}

