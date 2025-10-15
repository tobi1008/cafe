package com.example.cafe;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
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
import java.util.List;

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
        setupProductRecyclerView();
        setupSearch();
        loadBannerImage();
        loadProductsFromFirestore();
    }

    private void loadBannerImage() {
        String bannerUrl = "https://images.unsplash.com/photo-1559925393-8be0ec4767c8?q=80&w=2070&auto=format&fit=crop";
        Glide.with(this).load(bannerUrl).into(bannerImageView);
    }

    private void setupCategoryRecyclerView() {
        List<String> categories = new ArrayList<>(Arrays.asList("Tất cả", "Cà Phê", "Trà", "Đá Xay", "Bánh Ngọt"));
        categoryAdapter = new CategoryAdapter(this, categories, category -> {
            selectedCategory = category;
            filterProductsByCategory(selectedCategory);
        });
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        categoryRecyclerView.setAdapter(categoryAdapter);
    }

    private void setupProductRecyclerView() {
        productAdapter = new ProductAdapter(this, currentlyDisplayedList);
        productRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        productRecyclerView.setAdapter(productAdapter);
    }

    private void loadProductsFromFirestore() {
        db.collection("cafe")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        fullProductList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            fullProductList.add(document.toObject(Product.class));
                        }
                        filterProductsByCategory(selectedCategory);
                    }
                });
    }

    private void filterProductsByCategory(String category) {
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
        List<Product> filteredList = new ArrayList<>();
        for (Product product : sourceList) {
            if (product.getTen().toLowerCase().contains(searchText.toLowerCase())) {
                filteredList.add(product);
            }
        }
        currentlyDisplayedList.clear();
        currentlyDisplayedList.addAll(filteredList);
        productAdapter.notifyDataSetChanged();
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProductsByCategory(selectedCategory);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupBottomNavigationView() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);

        // SỬA LỖI Ở ĐÂY: Bổ sung đầy đủ logic cho menu
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                // Đang ở trang Home, không làm gì cả
                return true;
            } else if (itemId == R.id.navigation_cart) {
                // Chuyển sang trang Giỏ hàng
                startActivity(new Intent(getApplicationContext(), CartActivity.class));
                overridePendingTransition(0, 0); // Bỏ hiệu ứng chuyển cảnh
                return true;
            } else if (itemId == R.id.navigation_profile) {
                // Chuyển sang trang Profile
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                overridePendingTransition(0, 0); // Bỏ hiệu ứng chuyển cảnh
                return true;
            }
            return false;
        });
    }
}

