package com.example.cafe;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CheckoutActivity extends AppCompatActivity {

    private ImageView imageViewBack;
    private TextView tvDeliveryAddressLine1, tvDeliveryAddressLine2, tvSubtotal, tvShippingFee, tvTotalAmount, tvChangeAddress;
    private CheckBox checkboxPaymentCard, checkboxPaymentCash;
    private Button buttonPlaceOrder, btnApplyVoucher;
    private EditText etVoucher;
    private LinearLayout layoutDiscount;
    private TextView tvDiscountAmount;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String userId;
    private List<CartItem> cartItems = new ArrayList<>();
    private double subtotal = 0;
    private double shippingFee = 15000; // Phí vận chuyển cố định
    private Voucher appliedVoucher = null;
    private double discountAmount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để thanh toán", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        userId = currentUser.getUid();

        // Ánh xạ UI
        imageViewBack = findViewById(R.id.imageViewBack);
        tvDeliveryAddressLine1 = findViewById(R.id.tvDeliveryAddressLine1);
        tvDeliveryAddressLine2 = findViewById(R.id.tvDeliveryAddressLine2);
        tvChangeAddress = findViewById(R.id.tvChangeAddress);
        checkboxPaymentCard = findViewById(R.id.checkboxPaymentCard);
        checkboxPaymentCash = findViewById(R.id.checkboxPaymentCash);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvShippingFee = findViewById(R.id.tvShippingFee);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        buttonPlaceOrder = findViewById(R.id.buttonPlaceOrder);
        etVoucher = findViewById(R.id.editTextVoucher);
        btnApplyVoucher = findViewById(R.id.buttonApplyVoucher);
        layoutDiscount = findViewById(R.id.layoutDiscount);
        tvDiscountAmount = findViewById(R.id.tvDiscountAmount);

        setupListeners();
        loadUserData();
        loadCartData();
    }

    private void setupListeners() {
        imageViewBack.setOnClickListener(v -> finish());
        tvChangeAddress.setOnClickListener(v -> showEditAddressDialog());

        checkboxPaymentCard.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                checkboxPaymentCash.setChecked(false);
            }
        });

        checkboxPaymentCash.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                checkboxPaymentCard.setChecked(false);
            }
        });

        btnApplyVoucher.setOnClickListener(v -> applyVoucher());
        buttonPlaceOrder.setOnClickListener(v -> {
            if (!checkboxPaymentCard.isChecked() && !checkboxPaymentCash.isChecked()) {
                Toast.makeText(this, "Vui lòng chọn hình thức thanh toán", Toast.LENGTH_SHORT).show();
                return;
            }
            placeOrder();
        });
    }

    private void showEditAddressDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_address, null);
        builder.setView(dialogView);

        final EditText etNewName = dialogView.findViewById(R.id.editTextNewName);
        final EditText etNewPhone = dialogView.findViewById(R.id.editTextNewPhone);
        final EditText etNewAddress = dialogView.findViewById(R.id.editTextNewAddress);
        final CheckBox cbSaveNewAddress = dialogView.findViewById(R.id.checkboxSaveNewAddress);

        etNewName.setText(tvDeliveryAddressLine1.getText());
        db.collection("users").document(userId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                User user = doc.toObject(User.class);
                if (user != null && user.getPhone() != null) {
                    etNewPhone.setText(user.getPhone());
                }
            }
        });
        etNewAddress.setText(tvDeliveryAddressLine2.getText());

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String newName = etNewName.getText().toString().trim();
            String newPhone = etNewPhone.getText().toString().trim();
            String newAddress = etNewAddress.getText().toString().trim();

            if (!newName.isEmpty() && !newAddress.isEmpty()) {
                tvDeliveryAddressLine1.setText(newName);
                tvDeliveryAddressLine2.setText(newAddress);

                if (cbSaveNewAddress.isChecked()) {
                    db.collection("users").document(userId).update(
                            "name", newName,
                            "phone", newPhone,
                            "address", newAddress
                    );
                }
            }
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void loadUserData() {
        db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                User user = documentSnapshot.toObject(User.class);
                if (user != null && user.getAddress() != null && !user.getAddress().isEmpty()) {
                    tvDeliveryAddressLine1.setText(user.getName() != null ? user.getName() : "Nhà");
                    tvDeliveryAddressLine2.setText(user.getAddress());
                } else {
                    tvDeliveryAddressLine1.setText("Chưa có địa chỉ");
                    tvDeliveryAddressLine2.setText("Vui lòng nhấn 'Thay đổi' để thêm");
                }
            }
        });
    }

    private void loadCartData() {
        db.collection("users").document(userId).collection("cart").get().addOnSuccessListener(queryDocumentSnapshots -> {
            cartItems.clear();
            subtotal = 0;
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                CartItem item = doc.toObject(CartItem.class);
                cartItems.add(item);
                subtotal += item.getPrice() * item.getQuantity();
            }
            updateSummary();
        });
    }

    private void applyVoucher() {
        String code = etVoucher.getText().toString().trim().toUpperCase();
        if (code.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mã voucher", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("vouchers").document(code).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Voucher voucher = documentSnapshot.toObject(Voucher.class);
                        if (voucher != null && voucher.getExpiryDate().after(new Date())) {
                            appliedVoucher = voucher;
                            calculateDiscount();
                            updateSummary();
                            Toast.makeText(this, "Áp dụng voucher thành công!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Voucher đã hết hạn hoặc không hợp lệ.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Mã voucher không hợp lệ", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void calculateDiscount() {
        if (appliedVoucher == null) {
            discountAmount = 0;
            return;
        }

        if ("PERCENT".equals(appliedVoucher.getDiscountType())) {
            discountAmount = subtotal * (appliedVoucher.getDiscountValue() / 100.0);
        } else { // FIXED_AMOUNT
            discountAmount = appliedVoucher.getDiscountValue();
        }
    }

    private void updateSummary() {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        tvSubtotal.setText(formatter.format(subtotal));
        tvShippingFee.setText(formatter.format(shippingFee));

        if (discountAmount > 0) {
            layoutDiscount.setVisibility(View.VISIBLE);
            tvDiscountAmount.setText("- " + formatter.format(discountAmount));
        } else {
            layoutDiscount.setVisibility(View.GONE);
        }

        double finalTotal = subtotal + shippingFee - discountAmount;
        if (finalTotal < 0) finalTotal = 0;
        tvTotalAmount.setText(formatter.format(finalTotal));
    }

    private void placeOrder() {
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng của bạn trống.", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference orderRef = db.collection("orders").document();
        Order newOrder = new Order();
        newOrder.setOrderId(orderRef.getId());
        newOrder.setUserId(userId);
        newOrder.setCustomerName(tvDeliveryAddressLine1.getText().toString());
        newOrder.setCustomerAddress(tvDeliveryAddressLine2.getText().toString());
        newOrder.setItems(cartItems);

        double finalTotal = subtotal + shippingFee - discountAmount;
        newOrder.setTotalPrice(finalTotal < 0 ? 0 : finalTotal);

        if (appliedVoucher != null) {
            newOrder.setAppliedVoucher(appliedVoucher.getCode());
            newOrder.setDiscountAmount(discountAmount);
        }

        newOrder.setOrderDate(new Date());
        newOrder.setStatus("Đang chờ xử lý");

        // Lấy SĐT từ hồ sơ user để lưu vào đơn hàng
        db.collection("users").document(userId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                User user = doc.toObject(User.class);
                if (user != null && user.getPhone() != null) {
                    newOrder.setCustomerPhone(user.getPhone());
                }
            }
            // Lưu đơn hàng
            orderRef.set(newOrder).addOnSuccessListener(aVoid -> {
                clearCart();
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Đặt hàng thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void clearCart() {
        db.collection("users").document(userId).collection("cart").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        doc.getReference().delete();
                    }
                    Toast.makeText(CheckoutActivity.this, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                });
    }
}

