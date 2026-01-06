package com.example.cafe.ui.admin;

import com.example.cafe.R;
import com.example.cafe.model.*;
import com.example.cafe.adapter.*;
import com.example.cafe.ui.order.*;

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
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

public class ManageOrdersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private OrderAdapter adapter;
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

        listenForOrders();
    }

    private void listenForOrders() {
        db.collection("orders")
                .orderBy("orderDate", Query.Direction.DESCENDING) // Sắp xếp mới nhất lên đầu
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.e("ManageOrders", "Listen failed.", error);
                            return;
                        }

                        if (queryDocumentSnapshots != null) {
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
                        } else {
                            Log.d("ManageOrders", "Dữ liệu null");
                        }
                    }
                });
    }
}
