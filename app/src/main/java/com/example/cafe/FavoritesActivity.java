package com.example.cafe;

import android.os.Bundle;
import android.util.Log; // *** THÊM IMPORT ***
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot; // *** THÊM IMPORT ***
import java.util.ArrayList;
import java.util.HashMap; // *** THÊM IMPORT ***
import java.util.List;
import java.util.Map; // *** THÊM IMPORT ***

public class FavoritesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private List<Product> favoriteProductList = new ArrayList<>();
    private TextView textViewNoFavorites;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    // *** MỚI: Thêm Map để lưu thông tin Giờ Vàng ***
    private Map<String, HappyHour> happyHourMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        recyclerView = findViewById(R.id.recyclerViewFavorites);
        textViewNoFavorites = findViewById(R.id.textViewNoFavorites);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // *** SỬA LỖI: Truyền happyHourMap (dù đang rỗng) vào Adapter ***
        adapter = new ProductAdapter(this, favoriteProductList, happyHourMap);
        recyclerView.setAdapter(adapter);

        // *** THAY ĐỔI: Tải Giờ Vàng TRƯỚC, sau đó mới tải Sản Phẩm Yêu Thích ***
        loadHappyHoursAndThenFavorites();
    }

    // *** HÀM MỚI: Tải Giờ Vàng (Giống HomeActivity) ***
    private void loadHappyHoursAndThenFavorites() {
        db.collection("HappyHours")
                .whereEqualTo("dangBat", true) // Chỉ lấy các khung giờ đang "Bật"
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    happyHourMap.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        HappyHour hh = doc.toObject(HappyHour.class);
                        if (hh != null && hh.getId() != null) {
                            // Đổi tên hàm cho khớp
                            happyHourMap.put(hh.getId(), hh);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FavoritesActivity", "Lỗi khi tải HappyHours", e);
                })
                .addOnCompleteListener(task -> {
                    // Dù thành công hay thất bại, giờ mới tải sản phẩm
                    loadFavoriteProducts();
                });
    }


    private void loadFavoriteProducts() {
        if (mAuth.getCurrentUser() == null) {
            checkIfListIsEmpty();
            return;
        }
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    User user = documentSnapshot.toObject(User.class);
                    if (user != null && user.getFavoriteProductIds() != null && !user.getFavoriteProductIds().isEmpty()) {
                        // Lấy danh sách ID các sản phẩm yêu thích
                        List<String> favoriteIds = user.getFavoriteProductIds();

                        if (favoriteIds.isEmpty()) {
                            checkIfListIsEmpty();
                            return;
                        }

                        // Lấy thông tin chi tiết của các sản phẩm đó
                        db.collection("cafe").whereIn("id", favoriteIds).get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                    favoriteProductList.clear();
                                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                                        favoriteProductList.add(doc.toObject(Product.class));
                                    }
                                    adapter.notifyDataSetChanged();
                                    checkIfListIsEmpty();
                                })
                                .addOnFailureListener(e -> checkIfListIsEmpty()); // Thêm xử lý lỗi
                    } else {
                        checkIfListIsEmpty();
                    }
                })
                .addOnFailureListener(e -> checkIfListIsEmpty()); // Thêm xử lý lỗi
    }

    private void checkIfListIsEmpty() {
        if (favoriteProductList.isEmpty()) {
            textViewNoFavorites.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            textViewNoFavorites.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}
