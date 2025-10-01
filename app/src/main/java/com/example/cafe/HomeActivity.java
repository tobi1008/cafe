package com.example.cafe;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private ArrayList<Product> allProducts;
    private EditText searchEditText;
    private FirebaseFirestore db; // Khai báo Firestore
    private static final String TAG = "HomeActivity"; // Tag để debug

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        recyclerView = findViewById(R.id.recyclerViewProducts);
        searchEditText = findViewById(R.id.searchEditText);

        // Khởi tạo Firestore
        db = FirebaseFirestore.getInstance();

        // Cài đặt RecyclerView
        allProducts = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        productAdapter = new ProductAdapter(allProducts);
        recyclerView.setAdapter(productAdapter);

        // Lấy dữ liệu từ Firestore
        fetchProductsFromFirestore();

        setupSearch();
        setupBottomNav();
    }

    private void fetchProductsFromFirestore() {
        db.collection("cafe") // Lấy từ collection "cafe"
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        allProducts.clear(); // Xóa dữ liệu cũ
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Tự động chuyển đổi document thành đối tượng Product
                            Product product = document.toObject(Product.class);
                            allProducts.add(product);
                        }
                        productAdapter.notifyDataSetChanged(); // Cập nhật RecyclerView
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                        Toast.makeText(HomeActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString());
            }
        });
    }

    private void filter(String text) {
        ArrayList<Product> filteredList = new ArrayList<>();
        for (Product item : allProducts) {
            // Cập nhật để dùng getTen()
            if (item.getTen().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }
        productAdapter.filterList(filteredList);
    }

    private void setupBottomNav() {
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

