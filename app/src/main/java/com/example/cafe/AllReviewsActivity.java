package com.example.cafe;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class AllReviewsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ReviewAdapter adapter;
    private List<Review> reviewList = new ArrayList<>();
    private TextView textViewNoReviews;
    private FirebaseFirestore db;
    private String productId;
    private static final String TAG = "AllReviewsActivity"; // Thêm Tag để lọc log

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_reviews);

        db = FirebaseFirestore.getInstance();
        productId = getIntent().getStringExtra("PRODUCT_ID");

        recyclerView = findViewById(R.id.recyclerViewAllReviews);
        textViewNoReviews = findViewById(R.id.textViewNoReviewsYet);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ReviewAdapter(this, reviewList);
        recyclerView.setAdapter(adapter);

        if (productId != null) {
            loadAllReviews();
        }
    }

    private void loadAllReviews() {
        db.collection("reviews").whereEqualTo("productId", productId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    reviewList.clear();
                    Log.d(TAG, "Tìm thấy " + queryDocumentSnapshots.size() + " đánh giá.");

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        // --- NÂNG CẤP GỠ LỖI Ở ĐÂY ---
                        try {
                            Review review = doc.toObject(Review.class);
                            reviewList.add(review);
                        } catch (Exception e) {
                            // Ghi lại nhật ký chi tiết khi có lỗi chuyển đổi
                            Log.e(TAG, "LỖI KHI CHUYỂN ĐỔI ĐÁNH GIÁ: " + doc.getId(), e);
                        }
                    }

                    adapter.notifyDataSetChanged();

                    if (reviewList.isEmpty()) {
                        textViewNoReviews.setVisibility(View.VISIBLE);
                    } else {
                        textViewNoReviews.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi khi tải tất cả đánh giá", e);
                });
    }
}

