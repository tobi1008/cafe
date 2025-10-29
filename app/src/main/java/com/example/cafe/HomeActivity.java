package com.example.cafe;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView productRecyclerView, categoryRecyclerView;
    private ProductAdapter productAdapter;
    private CategoryAdapter categoryAdapter;
    private List<Product> fullProductList = new ArrayList<>();
    private List<Product> currentlyDisplayedList = new ArrayList<>();
    private EditText searchEditText;
    private FirebaseFirestore db;
    private ImageView bannerImageView;
    private String selectedCategory = "Tất cả";

    private Map<String, HappyHour> activeHappyHourMap = new HashMap<>();
    private static final String TAG = "HomeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        db = FirebaseFirestore.getInstance();

        productRecyclerView = findViewById(R.id.productRecyclerView);
        categoryRecyclerView = findViewById(R.id.recyclerViewCategories);
        searchEditText = findViewById(R.id.searchEditText);
        bannerImageView = findViewById(R.id.bannerImageView);

        setupBottomNavigationView();
        setupCategoryRecyclerView();
        setupSearch();
        loadBannerImage();

        loadHappyHours();
    }

    private void loadBannerImage() {
        String bannerUrl = "https://images.unsplash.com/photo-1559925393-8be0ec4767c8?q=80&w=2070&auto=format&fit=crop";
        Glide.with(this).load(bannerUrl).into(bannerImageView);
    }

    private void setupCategoryRecyclerView() {
        List<String> categories = new ArrayList<>(Arrays.asList("Tất cả", "Cà Phê", "Trà", "Đá Xay", "Bánh Ngọt"));
        categoryAdapter = new CategoryAdapter(this, categories, category -> {
            selectedCategory = category;
            // Chỉ lọc khi adapter đã sẵn sàng
            if (productAdapter != null) {
                filterProductsByCategory(selectedCategory);
            }
        });
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        categoryRecyclerView.setAdapter(categoryAdapter);
    }

    private void setupProductRecyclerView() {
        productAdapter = new ProductAdapter(this, currentlyDisplayedList, activeHappyHourMap);
        productRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        productRecyclerView.setAdapter(productAdapter);
        Log.d(TAG, "Product RecyclerView setup completed.");
    }


    private void loadHappyHours() {
        Log.d(TAG, "Bắt đầu tải Happy Hours...");
        db.collection("HappyHours")
                // *** SỬA LỖI QUERY: Dùng "dangBat" thay vì "active" ***
                .whereEqualTo("dangBat", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    activeHappyHourMap.clear(); // Xóa map cũ
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            HappyHour hh = document.toObject(HappyHour.class);
                            // *** QUAN TRỌNG: Lấy ID từ document và dùng làm key ***
                            hh.setId(document.getId());
                            activeHappyHourMap.put(hh.getId(), hh);
                        } catch (Exception e) {
                            Log.e(TAG, "Lỗi khi chuyển đổi HappyHour: " + document.getId(), e);
                        }
                    }
                    Log.d(TAG, "Đã tải " + activeHappyHourMap.size() + " khung giờ vàng đang bật vào map.");
                    loadProductsFromFirestore();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi khi tải Happy Hours", e);
                    loadProductsFromFirestore();
                });
    }


    private void loadProductsFromFirestore() {
        Log.d(TAG, "Bắt đầu tải Products...");
        db.collection("cafe")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        fullProductList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                fullProductList.add(document.toObject(Product.class));
                            } catch (Exception e) {
                                Log.e(TAG, "Lỗi khi chuyển đổi Product: " + document.getId(), e);
                            }
                        }
                        Log.d(TAG, "Đã tải " + fullProductList.size() + " products.");
                        if (productAdapter == null) { // Chỉ setup lần đầu
                            setupProductRecyclerView();
                        }
                        // Và lọc lần đầu
                        filterProductsByCategory(selectedCategory);
                    } else {
                        Log.e(TAG, "Lỗi khi tải Products: ", task.getException());
                        Toast.makeText(HomeActivity.this, "Lỗi tải sản phẩm", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void filterProductsByCategory(String category) {
        Log.d(TAG, "Filtering by category: " + category);
        List<Product> filteredByCategoryList = new ArrayList<>();
        if (category.equals("Tất cả")) {
            filteredByCategoryList.addAll(fullProductList);
        } else {
            for (Product product : fullProductList) {
                if (product.getCategory() != null && product.getCategory().equalsIgnoreCase(category)) {
                    filteredByCategoryList.add(product);
                }
            }
        }
        filterProductsBySearch(searchEditText.getText().toString(), filteredByCategoryList);
    }

    private void filterProductsBySearch(String searchText, List<Product> sourceList) {
        Log.d(TAG, "Filtering by search: " + searchText);
        List<Product> filteredList = new ArrayList<>();
        for (Product product : sourceList) {
            // Kiểm tra null cho tên sản phẩm
            if (product.getTen() != null && product.getTen().toLowerCase().contains(searchText.toLowerCase())) {
                filteredList.add(product);
            }
        }

        // *** CẬP NHẬT TRỰC TIẾP currentlyDisplayedList ***
        currentlyDisplayedList.clear();
        currentlyDisplayedList.addAll(filteredList);

        // *** QUAN TRỌNG: Kiểm tra xem adapter đã được tạo chưa ***
        if (productAdapter != null) {
            productAdapter.notifyDataSetChanged();
            Log.d(TAG, "Adapter notified, displaying " + currentlyDisplayedList.size() + " items.");
        } else {
            Log.w(TAG, "Adapter is null when trying to notify!");
        }
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Lọc lại mỗi khi text thay đổi
                // Chỉ lọc khi adapter đã sẵn sàng
                if (productAdapter != null) {
                    filterProductsByCategory(selectedCategory);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
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

