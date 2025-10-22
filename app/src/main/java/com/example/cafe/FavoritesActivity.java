package com.example.cafe;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private List<Product> favoriteProductList = new ArrayList<>();
    private TextView textViewNoFavorites;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        recyclerView = findViewById(R.id.recyclerViewFavorites);
        textViewNoFavorites = findViewById(R.id.textViewNoFavorites);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        adapter = new ProductAdapter(this, favoriteProductList);
        recyclerView.setAdapter(adapter);

        loadFavoriteProducts();
    }

    private void loadFavoriteProducts() {
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    User user = documentSnapshot.toObject(User.class);
                    if (user != null && user.getFavoriteProductIds() != null && !user.getFavoriteProductIds().isEmpty()) {
                        // Lấy danh sách ID các sản phẩm yêu thích
                        List<String> favoriteIds = user.getFavoriteProductIds();

                        // Lấy thông tin chi tiết của các sản phẩm đó
                        db.collection("cafe").whereIn("id", favoriteIds).get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                    favoriteProductList.clear();
                                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                                        favoriteProductList.add(doc.toObject(Product.class));
                                    }
                                    adapter.notifyDataSetChanged();
                                    checkIfListIsEmpty();
                                });
                    } else {
                        checkIfListIsEmpty();
                    }
                });
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
