package com.example.cafe.ui.order;

import com.example.cafe.R;
import com.example.cafe.model.*;
import com.example.cafe.adapter.*;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class OrderHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private OrderAdapter adapter;
    private List<Order> orderList = new ArrayList<>();
    private TextView textViewNoOrders;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        recyclerView = findViewById(R.id.recyclerViewOrders);
        textViewNoOrders = findViewById(R.id.textViewNoOrders);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new OrderAdapter(this, orderList);
        recyclerView.setAdapter(adapter);

        loadOrders();
    }

    private void loadOrders() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để xem lịch sử", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        String userId = currentUser.getUid();

        db.collection("orders")
                .whereEqualTo("userId", userId)
                .orderBy("orderDate", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    orderList.clear();
                    Log.d("OrderHistory", "Tìm thấy " + queryDocumentSnapshots.size() + " đơn hàng.");

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        // --- NÂNG CẤP GỠ LỖI Ở ĐÂY ---
                        try {
                            Order order = doc.toObject(Order.class);
                            orderList.add(order);
                        } catch (Exception e) {
                            // Ghi lại nhật ký chi tiết khi có lỗi chuyển đổi
                            Log.e("OrderHistory", "LỖI KHI CHUYỂN ĐỔI ĐƠN HÀNG: " + doc.getId(), e);
                        }
                    }

                    if (orderList.isEmpty()) {
                        textViewNoOrders.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        textViewNoOrders.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("OrderHistory", "Lỗi khi tải lịch sử đơn hàng", e);
                });
    }
}

