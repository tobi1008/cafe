package com.example.cafe.ui.order;

import com.example.cafe.R;
import com.example.cafe.model.*;
import com.example.cafe.adapter.*;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;
import android.content.Intent;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import java.util.Date;
import java.util.Map;

public class OrderDetailActivity extends AppCompatActivity {

    private TextView tvOrderId, tvOrderDate, tvOrderStatus, tvCustomerName, tvCustomerPhone, tvCustomerAddress,
            tvTotalPrice;
    private RecyclerView recyclerViewItems;
    private OrderDetailAdapter adapter;
    private Order order;
    private LinearLayout adminActionLayout;
    private Spinner spinnerStatus;
    private Button buttonUpdateStatus;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ImageView imageViewBack;

    private static final String TAG = "OrderDetailActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        imageViewBack = findViewById(R.id.imageViewBack);
        imageViewBack.setOnClickListener(v -> finish());

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

        order = (Order) getIntent().getSerializableExtra("ORDER_DETAIL");

        if (order != null) {
            populateUI();
            checkUserRoleAndSetupAdminUI();
        }

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

        if (order.getItems() != null) {
            adapter = new OrderDetailAdapter(this, order.getItems());
            recyclerViewItems.setAdapter(adapter);
        }
    }

    private void checkUserRoleAndSetupAdminUI() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null)
            return;

        db.collection("users").document(currentUser.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
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
                    adminActionLayout.setVisibility(View.GONE);
                });
    }

    private void setupStatusSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.order_status_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(adapter);

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
        String oldStatus = order.getStatus();

        if (order == null || order.getOrderId() == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy ID đơn hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean shouldAwardPoints = !oldStatus.equals("Đã hoàn thành") && newStatus.equals("Đã hoàn thành");

        db.collection("orders").document(order.getOrderId())
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Cập nhật trạng thái thành công!", Toast.LENGTH_SHORT).show();
                    tvOrderStatus.setText("Trạng thái: " + newStatus);
                    order.setStatus(newStatus);

                    if (shouldAwardPoints) {
                        if (order.getUserId() != null && order.getTotalPrice() > 0) {
                            awardLoyaltyPoints(order.getUserId(), order.getTotalPrice());
                        } else {
                            Log.w(TAG, "Không thể cộng điểm: userId rỗng hoặc totalPrice = 0");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Cập nhật thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // CỘNG ĐIỂM VÀ THĂNG HẠNG (ĐÃ CẬP NHẬT)
    private void awardLoyaltyPoints(String userId, double orderTotal) {
        Log.d(TAG, "Bắt đầu cộng điểm cho user: " + userId + " với số tiền: " + orderTotal);
        final DocumentReference userRef = db.collection("users").document(userId);

        // Đọc mốc tiền từ Firestore CÀI ĐẶT
        db.collection("Settings").document("Membership").get()
                .addOnSuccessListener(settingsDoc -> {

                    // Lấy mốc tiền từ Cài đặt, nếu không có thì dùng mốc MẶC ĐỊNH
                    final long TIER_BRONZE_START = settingsDoc.contains("bronzeThreshold")
                            ? settingsDoc.getLong("bronzeThreshold")
                            : 500000;
                    final long TIER_SILVER_START = settingsDoc.contains("silverThreshold")
                            ? settingsDoc.getLong("silverThreshold")
                            : 1000000;
                    final long TIER_GOLD_START = settingsDoc.contains("goldThreshold")
                            ? settingsDoc.getLong("goldThreshold")
                            : 4000000;
                    final long TIER_PLATINUM_START = settingsDoc.contains("platinumThreshold")
                            ? settingsDoc.getLong("platinumThreshold")
                            : 10000000;
                    final long TIER_DIAMOND_START = settingsDoc.contains("diamondThreshold")
                            ? settingsDoc.getLong("diamondThreshold")
                            : 20000000;

                    // Lấy mã voucher từ Cài đặt
                    final String VOUCHER_BRONZE = settingsDoc.contains("bronzeVoucher")
                            ? settingsDoc.getString("bronzeVoucher")
                            : "BRONZEUP";
                    final String VOUCHER_SILVER = settingsDoc.contains("silverVoucher")
                            ? settingsDoc.getString("silverVoucher")
                            : "SILVERUP";
                    final String VOUCHER_GOLD = settingsDoc.contains("goldVoucher")
                            ? settingsDoc.getString("goldVoucher")
                            : "GOLDUP";
                    final String VOUCHER_PLATINUM = settingsDoc.contains("platinumVoucher")
                            ? settingsDoc.getString("platinumVoucher")
                            : "PLATINUMUP";
                    final String VOUCHER_DIAMOND = settingsDoc.contains("diamondVoucher")
                            ? settingsDoc.getString("diamondVoucher")
                            : "DIAMONDUP";

                    Log.d(TAG, "Sử dụng mốc: Đồng=" + TIER_BRONZE_START + ", Bạc=" + TIER_SILVER_START);

                    // Chạy Transaction SAU KHI đã lấy được mốc tiền
                    db.runTransaction((Transaction.Function<String>) transaction -> {
                        DocumentSnapshot userSnapshot = transaction.get(userRef);
                        double currentSpending = 0;
                        String currentTier = "Thành viên"; // Mặc định ĐÚNG phải là Thành viên

                        if (userSnapshot.contains("totalSpending")) {
                            currentSpending = userSnapshot.getDouble("totalSpending");
                        }
                        if (userSnapshot.contains("memberTier")) {
                            String tierFromDb = userSnapshot.getString("memberTier");
                            if (tierFromDb != null && !tierFromDb.isEmpty()) {
                                currentTier = tierFromDb;
                            }
                        }

                        double newTotalSpending = currentSpending + orderTotal;
                        String newTier = "Thành viên";

                        if (newTotalSpending >= TIER_DIAMOND_START) {
                            newTier = "Kim Cương";
                        } else if (newTotalSpending >= TIER_PLATINUM_START) {
                            newTier = "Platinum";
                        } else if (newTotalSpending >= TIER_GOLD_START) {
                            newTier = "Vàng";
                        } else if (newTotalSpending >= TIER_SILVER_START) {
                            newTier = "Bạc";
                        } else if (newTotalSpending >= TIER_BRONZE_START) {
                            newTier = "Đồng";
                        }

                        transaction.update(userRef, "totalSpending", newTotalSpending);

                        // Chỉ cập nhật nếu hạng thay đổi (thang hạng)
                        if (!currentTier.equals(newTier)) {
                            transaction.update(userRef, "memberTier", newTier);
                            return newTier;
                        }

                        return null; // Không đổi hạng -> trả về null
                    }).addOnSuccessListener(newTier -> {
                        if (newTier != null) {
                            Log.d(TAG, "Cộng điểm và thăng hạng thành công lên: " + newTier);
                            Toast.makeText(this, "Chúc mừng! Bạn đã thăng hạng: " + newTier, Toast.LENGTH_LONG).show();

                            // Xác định mã voucher cần tặng dựa trên tier mới
                            String voucherToGrant = null;
                            if (newTier.equals("Đồng"))
                                voucherToGrant = VOUCHER_BRONZE;
                            else if (newTier.equals("Bạc"))
                                voucherToGrant = VOUCHER_SILVER;
                            else if (newTier.equals("Vàng"))
                                voucherToGrant = VOUCHER_GOLD;
                            else if (newTier.equals("Platinum"))
                                voucherToGrant = VOUCHER_PLATINUM;
                            else if (newTier.equals("Kim Cương"))
                                voucherToGrant = VOUCHER_DIAMOND;

                            if (voucherToGrant != null) {
                                grantTierUpVoucher(userId, voucherToGrant);
                            } else {
                                Log.w(TAG, "Không có mã voucher nào được cấu hình cho hạng: " + newTier);
                            }

                        } else {
                            Log.d(TAG, "Cộng điểm thành công, không thăng hạng.");
                            Toast.makeText(this, "Đã cộng điểm tích lũy.", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(e -> {
                        Log.w(TAG, "Lỗi khi chạy Transaction cộng điểm", e);
                        Toast.makeText(this, "Lỗi khi cộng điểm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "LỖI NGHIÊM TRỌNG: Không thể đọc mốc tiền Hạng Thành Viên.", e);
                    Toast.makeText(this, "Lỗi kết nối Cài đặt. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
                });
    }

    // TẶNG VOUCHER KHI THĂNG HẠNG
    private void grantTierUpVoucher(String userId, String voucherCode) {
        if (voucherCode == null)
            return;

        final String finalVoucherCode = voucherCode;
        DocumentReference templateVoucherRef = db.collection("vouchers").document(finalVoucherCode);

        templateVoucherRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Voucher templateVoucher = documentSnapshot.toObject(Voucher.class);
                // Với voucher thăng hạng, có thể không cần check ngày hết hạn của bản mẫu,
                // hoặc bản mẫu nên set ngày rất xa. Ở đây vẫn giữ check cho an toàn.
                if (templateVoucher == null) {
                    Log.w(TAG, "Voucher mẫu null");
                    return;
                }

                // Tạo một bản sao voucher mới để tặng user
                Map<String, Object> newVoucherData = new HashMap<>();
                newVoucherData.put("code", templateVoucher.getCode());
                newVoucherData.put("description", templateVoucher.getDescription());
                newVoucherData.put("discountType", templateVoucher.getDiscountType());
                newVoucherData.put("discountValue", templateVoucher.getDiscountValue());

                // Nếu voucher mẫu có hạn, dùng hạn đó. Nếu không (hoặc muốn set hạn riêng cho
                // user voucher),
                // có thể tính toán lại ở đây (ví dụ: +30 ngày từ lúc nhận).
                // Hiện tại giữ nguyên logic copy từ mẫu.
                newVoucherData.put("expiryDate", templateVoucher.getExpiryDate());

                newVoucherData.put("minTier", templateVoucher.getMinTier()); // Giữ nguyên yêu cầu hạng nếu có
                newVoucherData.put("used", false);

                // Thêm vào sub-collection của user
                db.collection("users").document(userId).collection("userVouchers")
                        .add(newVoucherData)
                        .addOnSuccessListener(documentReference -> {
                            Log.d(TAG, "Đã tặng voucher thăng hạng: " + finalVoucherCode + " cho user " + userId);
                            Toast.makeText(OrderDetailActivity.this, "Đã tặng voucher " + finalVoucherCode,
                                    Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> Log.w(TAG, "Lỗi khi tặng voucher thăng hạng", e));
            } else {
                Log.w(TAG, "Không tìm thấy voucher mẫu: " + finalVoucherCode
                        + " trong /vouchers. Hãy đảm bảo admin đã tạo voucher này.");
                Toast.makeText(this, "Lỗi: Không tìm thấy voucher mẫu " + finalVoucherCode, Toast.LENGTH_LONG).show();
            }
        });
    }
}
