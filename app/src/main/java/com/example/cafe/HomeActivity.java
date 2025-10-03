package com.example.cafe;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView productRecyclerView;
    private ProductAdapter productAdapter;
    private List<Product> productList = new ArrayList<>();
    private List<Product> fullProductList = new ArrayList<>(); // Danh sách gốc để tìm kiếm
    private EditText searchEditText;
    private AppDatabase appDatabase;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        appDatabase = AppDatabase.getDatabase(this);
        executorService = Executors.newSingleThreadExecutor();

        productRecyclerView = findViewById(R.id.productRecyclerView);
        searchEditText = findViewById(R.id.searchEditText);

        setupRecyclerView();
        setupBottomNavigationView();
        setupSearch();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProductsFromDb(); // Tải lại dữ liệu mỗi khi quay lại màn hình
    }

    private void setupRecyclerView() {
        productAdapter = new ProductAdapter(this, productList);
        productRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        productRecyclerView.setAdapter(productAdapter);
    }

    private void loadProductsFromDb() {
        executorService.execute(() -> {
            List<Product> products = appDatabase.productDao().getAllProducts();
            runOnUiThread(() -> {
                fullProductList.clear();
                fullProductList.addAll(products);
                filter(searchEditText.getText().toString()); // Lọc lại theo text đang có trong ô search
            });
        });
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filter(String text) {
        List<Product> filteredList = new ArrayList<>();
        for (Product item : fullProductList) {
            if (item.getTen().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }
        productAdapter.filterList(filteredList);
    }

    private void setupBottomNavigationView() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                return true;
            } else if (itemId == R.id.navigation_cart) {
                startActivity(new Intent(getApplicationContext(), CartActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.navigation_profile) {
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }
}

