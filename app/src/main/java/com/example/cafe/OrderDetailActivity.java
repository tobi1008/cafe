package com.example.cafe;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Locale;

// Thêm các import còn thiếu
import java.util.ArrayList;
import java.util.List;
import android.content.Intent;
import android.widget.ImageView; // Cần cho nút Back
import com.bumptech.glide.Glide; // Cần cho ảnh sản phẩm trong adapter
import java.util.Date; // Cần cho kiểu Date

public class OrderDetailActivity extends AppCompatActivity {

    // Khai báo đầy đủ các biến
    private TextView tvOrderId, tvOrderDate, tvOrderStatus, tvCustomerName, tvCustomerPhone, tvCustomerAddress, tvTotalPrice;
    private RecyclerView recyclerViewItems;
    private OrderDetailAdapter adapter;
    private Order order;
    private LinearLayout adminActionLayout;
    private Spinner spinnerStatus;
    private Button buttonUpdateStatus;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ImageView imageViewBack; // Thêm nút Back

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Ánh xạ UI
        tvOrderId = findViewById(R.id.textViewDetailOrderId);
        tvOrderDate = findViewById(R.id.textViewDetailOrderDate);
        tvOrderStatus = findViewById(R.id.textViewDetailOrderStatus);
        tvCustomerName = findViewById(R.id.textViewDetailCustomerName);
        tvCustomerPhone = findViewById(R.id.textViewDetailCustomerPhone);
        tvCustomerAddress = findViewById(R.id.textViewDetailCustomerAddress);
        tvTotalPrice = findViewById(R.id.textViewDetailTotalPrice);
        recyclerViewItems = findViewById(R.id.recyclerViewOrderDetailItems);
        recyclerViewItems.setLayoutManager(new LinearLayoutManager(this));
        adminActionLayout = findViewById(R.id.adminActionLayout);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        buttonUpdateStatus = findViewById(R.id.buttonUpdateStatus);
        // imageViewBack = findViewById(R.id.imageViewBack); // Giao diện activity_order_detail chưa có nút back

        // Nhận đối tượng Order từ Intent
        order = (Order) getIntent().getSerializableExtra("ORDER_DETAIL");

        if (order != null) {
            populateUI();
            checkUserRoleAndSetupAdminUI();
        }

        // if (imageViewBack != null) {
        //     imageViewBack.setOnClickListener(v -> finish());
        // }
    }

    private void populateUI() {
        if (order.getOrderId() != null && order.getOrderId().length() >= 8) {
            tvOrderId.setText("Mã đơn hàng: #" + order.getOrderId().substring(0, 8).toUpperCase());
        } else {
            tvOrderId.setText("Mã đơn hàng: N/A");
        }

        if (order.getOrderDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            tvOrderDate.setText("Ngày đặt: " + sdf.format(order.getOrderDate()));
        }

        tvOrderStatus.setText("Trạng thái: " + order.getStatus());
        tvCustomerName.setText(order.getCustomerName());
        tvCustomerPhone.setText(order.getCustomerPhone() != null ? order.getCustomerPhone() : "Chưa có SĐT");
        tvCustomerAddress.setText(order.getCustomerAddress());

        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        tvTotalPrice.setText("Tổng Cộng: " + formatter.format(order.getTotalPrice()));

        // Hiển thị danh sách sản phẩm
        if (order.getItems() != null) {
            adapter = new OrderDetailAdapter(this, order.getItems());
            recyclerViewItems.setAdapter(adapter);
        }
    }

    private void checkUserRoleAndSetupAdminUI() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        db.collection("users").document(currentUser.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        // Nếu người dùng có vai trò là "admin", hiển thị khu vực admin
                        if (user != null && "admin".equals(user.getRole())) {
                            adminActionLayout.setVisibility(View.VISIBLE);
                            setupStatusSpinner();
                        } else {
                            adminActionLayout.setVisibility(View.GONE);
                        }
                    } else {
                        adminActionLayout.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    adminActionLayout.setVisibility(View.GONE); // Ẩn nếu có lỗi
                });
    }

    private void setupStatusSpinner() {
        // Tạo adapter cho Spinner từ mảng string trong strings.xml
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.order_status_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(adapter);

        // Đặt giá trị mặc định cho Spinner là trạng thái hiện tại của đơn hàng
        if (order.getStatus() != null) {
            String[] statuses = getResources().getStringArray(R.array.order_status_options);
            int currentStatusPosition = Arrays.asList(statuses).indexOf(order.getStatus());
            if (currentStatusPosition >= 0) {
                spinnerStatus.setSelection(currentStatusPosition);
            }
        }

        buttonUpdateStatus.setOnClickListener(v -> updateOrderStatus());
    }

    private void updateOrderStatus() {
        String newStatus = spinnerStatus.getSelectedItem().toString();
        if (order == null || order.getOrderId() == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy ID đơn hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("orders").document(order.getOrderId())
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Cập nhật trạng thái thành công!", Toast.LENGTH_SHORT).show();
                    // Cập nhật lại TextView trạng thái trên màn hình
                    tvOrderStatus.setText("Trạng thái: " + newStatus);
                    order.setStatus(newStatus); // Cập nhật cả đối tượng order cục bộ
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Cập nhật thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}

