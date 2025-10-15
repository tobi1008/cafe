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

public class OrderDetailActivity extends AppCompatActivity {

    // --- SỬA LỖI Ở ĐÂY: Bổ sung các biến còn thiếu ---
    private TextView tvOrderId, tvOrderDate, tvOrderStatus, tvCustomerName, tvCustomerPhone, tvCustomerAddress, tvTotalPrice;
    private RecyclerView recyclerViewItems;
    private OrderDetailAdapter adapter;
    private Order order;
    // --- Kết thúc phần sửa lỗi ---

    private LinearLayout adminActionLayout;
    private Spinner spinnerStatus;
    private Button buttonUpdateStatus;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Ánh xạ UI
        // SỬA LỖI Ở ĐÂY: Bổ sung phần ánh xạ còn thiếu
        tvOrderId = findViewById(R.id.textViewDetailOrderId);
        tvOrderDate = findViewById(R.id.textViewDetailOrderDate);
        tvOrderStatus = findViewById(R.id.textViewDetailOrderStatus);
        tvCustomerName = findViewById(R.id.textViewDetailCustomerName);
        tvCustomerPhone = findViewById(R.id.textViewDetailCustomerPhone);
        tvCustomerAddress = findViewById(R.id.textViewDetailCustomerAddress);
        tvTotalPrice = findViewById(R.id.textViewDetailTotalPrice);
        recyclerViewItems = findViewById(R.id.recyclerViewOrderDetailItems);
        recyclerViewItems.setLayoutManager(new LinearLayoutManager(this));
        // --- Kết thúc phần sửa lỗi ---

        adminActionLayout = findViewById(R.id.adminActionLayout);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        buttonUpdateStatus = findViewById(R.id.buttonUpdateStatus);

        order = (Order) getIntent().getSerializableExtra("ORDER_DETAIL");

        if (order != null) {
            populateUI();
            checkUserRoleAndSetupAdminUI();
        }
    }

    private void populateUI() {
        // SỬA LỖI Ở ĐÂY: Bổ sung phần hiển thị thông tin còn thiếu
        tvOrderId.setText("Mã đơn hàng: #" + order.getOrderId().substring(0, 8).toUpperCase());

        if (order.getOrderDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            tvOrderDate.setText("Ngày đặt: " + sdf.format(order.getOrderDate()));
        }

        tvOrderStatus.setText("Trạng thái: " + order.getStatus());
        tvCustomerName.setText(order.getCustomerName());
        tvCustomerPhone.setText(order.getCustomerPhone());
        tvCustomerAddress.setText(order.getCustomerAddress());

        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        tvTotalPrice.setText("Tổng Cộng: " + formatter.format(order.getTotalPrice()));

        // Hiển thị danh sách sản phẩm
        if (order.getItems() != null) {
            adapter = new OrderDetailAdapter(this, order.getItems());
            recyclerViewItems.setAdapter(adapter);
        }
        // --- Kết thúc phần sửa lỗi ---
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
                        }
                    }
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

        db.collection("orders").document(order.getOrderId())
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Cập nhật trạng thái thành công!", Toast.LENGTH_SHORT).show();
                    // Cập nhật lại TextView trạng thái trên màn hình
                    tvOrderStatus.setText("Trạng thái: " + newStatus);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Cập nhật thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}

