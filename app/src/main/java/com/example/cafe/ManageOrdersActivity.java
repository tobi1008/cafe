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

public class ManageOrdersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private OrderAdapter adapter; // Tái sử dụng OrderAdapter
    private List<Order> orderList = new ArrayList<>();
    private TextView textViewNoOrders;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_orders);

        db = FirebaseFirestore.getInstance();

        recyclerView = findViewById(R.id.recyclerViewManageOrders);
        textViewNoOrders = findViewById(R.id.textViewNoOrdersAdmin);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new OrderAdapter(this, orderList);
        recyclerView.setAdapter(adapter);

        loadAllOrders();
    }

    private void loadAllOrders() {
        db.collection("orders")
                .orderBy("orderDate", Query.Direction.DESCENDING) // Sắp xếp mới nhất lên đầu
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    orderList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            Order order = doc.toObject(Order.class);
                            orderList.add(order);
                        } catch (Exception e) {
                            Log.e("ManageOrders", "Lỗi khi chuyển đổi đơn hàng: " + doc.getId(), e);
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
                    Log.e("ManageOrders", "Lỗi khi tải tất cả đơn hàng", e);
                });
    }
}
