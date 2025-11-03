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
import com.google.firebase.firestore.Query;
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
    private List<String> categoryNames = new ArrayList<>();
    private EditText searchEditText;
    private FirebaseFirestore db;
    private ImageView bannerImageView;
    private String selectedCategory = "Tất cả";

    private Map<String, HappyHour> activeHappyHourMap = new HashMap<>();
    private static final String TAG = "HomeActivity";

    // Lưu trữ thông tin Category (Tên danh mục -> Đối tượng Category)
    private Map<String, Category> categoryMap = new HashMap<>();

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

        setupSearch();
        loadBannerImage();

        loadCategories();
    }

    private void loadBannerImage() {
        String bannerUrl = "https://images.unsplash.com/photo-1559925393-8be0ec4767c8?q=80&w=2070&auto=format&fit=crop";
        Glide.with(this).load(bannerUrl).into(bannerImageView);
    }

    private void setupCategoryRecyclerView() {
        Log.d(TAG, "Setting up Category RecyclerView with " + categoryNames.size() + " categories.");
        categoryAdapter = new CategoryAdapter(this, categoryNames, category -> {
            selectedCategory = category;
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

    private void loadCategories() {
        Log.d(TAG, "Bắt đầu tải Categories...");
        db.collection("Categories")
                .orderBy("thuTuUuTien", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    categoryNames.clear();
                    categoryMap.clear();
                    categoryNames.add("Tất cả");

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            Category cat = document.toObject(Category.class);
                            if (cat != null && cat.getTenDanhMuc() != null && !cat.getTenDanhMuc().isEmpty()) {
                                categoryNames.add(cat.getTenDanhMuc());
                                categoryMap.put(cat.getTenDanhMuc(), cat);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Lỗi khi chuyển đổi Category: " + document.getId(), e);
                        }
                    }
                    Log.d(TAG, "Đã tải " + (categoryNames.size() - 1) + " categories từ Firebase.");
                    setupCategoryRecyclerView();
                    loadHappyHours();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi khi tải Categories", e);
                    categoryNames.clear();
                    categoryNames.add("Tất cả");
                    setupCategoryRecyclerView();
                    loadHappyHours();
                });
    }


    private void loadHappyHours() {
        Log.d(TAG, "Bắt đầu tải Happy Hours...");
        db.collection("HappyHours")
                .whereEqualTo("dangBat", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    activeHappyHourMap.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            HappyHour hh = document.toObject(HappyHour.class);
                            hh.setId(document.getId());
                            activeHappyHourMap.put(hh.getId(), hh);
                        } catch (Exception e) {
                            Log.e(TAG, "Lỗi khi chuyển đổi HappyHour: " + document.getId(), e);
                        }
                    }
                    Log.d(TAG, "Đã tải " + activeHappyHourMap.size() + " khung giờ vàng đang bật vào map.");
                    loadProductsFromFirestore(); // Tải sản phẩm sau khi tải HappyHour
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
                                Product product = document.toObject(Product.class);

                                // --- ƯU TIÊN GIỜ VÀNG ---
                                // 1. Kiểm tra Giờ Vàng của Sản phẩm
                                String productHHId = product.getHappyHourId();

                                // 2. Nếu sản phẩm KHÔNG có, kiểm tra Giờ Vàng của Danh mục
                                if (productHHId == null || productHHId.isEmpty()) {
                                    String categoryName = product.getCategory();
                                    if (categoryName != null && categoryMap.containsKey(categoryName)) {
                                        Category cat = categoryMap.get(categoryName);
                                        if (cat != null && cat.getHappyHourId() != null && !cat.getHappyHourId().isEmpty()) {
                                            // Gán Giờ Vàng của Danh mục cho Sản phẩm
                                            product.setHappyHourId(cat.getHappyHourId());
                                        }
                                    }
                                }

                                fullProductList.add(product);

                            } catch (Exception e) {
                                Log.e(TAG, "Lỗi khi chuyển đổi Product: " + document.getId(), e);
                            }
                        }
                        Log.d(TAG, "Đã tải " + fullProductList.size() + " products.");
                        if (productAdapter == null) {
                            setupProductRecyclerView();
                        }
                        filterProductsByCategory(selectedCategory); // Lọc theo category "Tất cả"
                    } else {
                        Log.e(TAG, "Lỗi khi tải Products: ", task.getException());
                        Toast.makeText(HomeActivity.this, "Lỗi tải sản phẩm", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void filterProductsByCategory(String categoryNameToFilter) {
        Log.d(TAG, "Filtering by category: " + categoryNameToFilter);
        List<Product> filteredByCategoryList = new ArrayList<>();
        if (categoryNameToFilter.equals("Tất cả")) {
            filteredByCategoryList.addAll(fullProductList);
        } else {
            for (Product product : fullProductList) {
                if (product.getCategory() != null && product.getCategory().equalsIgnoreCase(categoryNameToFilter)) {
                    filteredByCategoryList.add(product);
                }
            }
        }
        filterProductsBySearch(searchEditText.getText().toString(), filteredByCategoryList);
    }

    private void filterProductsBySearch(String searchText, List<Product> sourceList) {
        Log.d(TAG, "Filtering by search: " + searchText);
        List<Product> filteredList = new ArrayList<>();
        if (searchText.isEmpty()) {
            filteredList.addAll(sourceList);
        } else {
            for (Product product : sourceList) {
                if (product.getTen() != null && product.getTen().toLowerCase().contains(searchText.toLowerCase())) {
                    filteredList.add(product);
                }
            }
        }

        currentlyDisplayedList.clear();
        currentlyDisplayedList.addAll(filteredList);

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
