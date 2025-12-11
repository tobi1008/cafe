package com.example.cafe;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CheckoutActivity extends AppCompatActivity {

    private ImageView iconLocation; // Placeholder if needed in logic, else remove
    private ImageButton imageViewBack;
    private TextView tvDeliveryAddressLine1, tvDeliveryAddressLine2, tvSubtotal, tvShippingFee, tvTotalAmount,
            tvChangeAddress;
    private CheckBox checkboxPaymentCard, checkboxPaymentCash;
    private Button buttonPlaceOrder, btnApplyVoucher;
    private EditText etVoucher; // Ideally change to TextInputEditText but EditText is compatible parent
    private LinearLayout layoutDiscount;
    private TextView tvDiscountAmount;

    private TextView tvSelectUserVoucher;
    private List<Voucher> availableVouchers = new ArrayList<>();
    private String appliedUserVoucherId = null;
    private static final String TAG = "CheckoutActivity";

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String userId;
    private List<CartItem> cartItems = new ArrayList<>();
    private double subtotal = 0;
    private double shippingFee = 15000;
    private Voucher appliedVoucher = null;
    private double discountAmount = 0;

    // View Containers for Click Listeners
    private View layoutPaymentCardContainer, layoutPaymentCashContainer;

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

        imageViewBack = findViewById(R.id.imageViewBack);
        tvDeliveryAddressLine1 = findViewById(R.id.tvDeliveryAddressLine1);
        tvDeliveryAddressLine2 = findViewById(R.id.tvDeliveryAddressLine2);
        tvChangeAddress = findViewById(R.id.tvChangeAddress);

        // Find CheckBoxes
        checkboxPaymentCard = findViewById(R.id.checkboxPaymentCard);
        checkboxPaymentCash = findViewById(R.id.checkboxPaymentCash);

        // Find Containers for Payment Methods
        layoutPaymentCardContainer = findViewById(R.id.layoutPaymentCard);
        layoutPaymentCashContainer = findViewById(R.id.layoutPaymentCash);

        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvShippingFee = findViewById(R.id.tvShippingFee);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        buttonPlaceOrder = findViewById(R.id.buttonPlaceOrder);
        etVoucher = findViewById(R.id.editTextVoucher);
        btnApplyVoucher = findViewById(R.id.buttonApplyVoucher);
        layoutDiscount = findViewById(R.id.layoutDiscount);
        tvDiscountAmount = findViewById(R.id.tvDiscountAmount);
        tvSelectUserVoucher = findViewById(R.id.tvSelectUserVoucher);

        setupListeners();
        loadUserData();
        loadCartData();
        loadUserVouchers();
    }

    private void setupListeners() {
        imageViewBack.setOnClickListener(v -> finish());
        tvChangeAddress.setOnClickListener(v -> showEditAddressDialog());

        // Payment Method Logic: Handle clicks on both CheckBox and Container
        View.OnClickListener cardClickListener = v -> {
            checkboxPaymentCard.setChecked(true);
            checkboxPaymentCash.setChecked(false);
        };

        View.OnClickListener cashClickListener = v -> {
            checkboxPaymentCash.setChecked(true);
            checkboxPaymentCard.setChecked(false);
        };

        checkboxPaymentCard.setOnClickListener(cardClickListener);
        layoutPaymentCardContainer.setOnClickListener(cardClickListener);

        checkboxPaymentCash.setOnClickListener(cashClickListener);
        layoutPaymentCashContainer.setOnClickListener(cashClickListener);

        tvSelectUserVoucher.setOnClickListener(v -> showVoucherSelectionDialog());

        btnApplyVoucher.setOnClickListener(v -> {
            appliedUserVoucherId = null;
            applyVoucher();
        });

        buttonPlaceOrder.setOnClickListener(v -> {
            if (!checkboxPaymentCard.isChecked() && !checkboxPaymentCash.isChecked()) {
                Toast.makeText(this, "Vui lòng chọn hình thức thanh toán", Toast.LENGTH_SHORT).show();
                return;
            }
            if (tvDeliveryAddressLine1.getText().toString().equals("Chưa có địa chỉ")) {
                Toast.makeText(this, "Vui lòng thêm địa chỉ giao hàng", Toast.LENGTH_SHORT).show();
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

        if (!tvDeliveryAddressLine1.getText().toString().equals("Chưa có địa chỉ")) {
            etNewName.setText(tvDeliveryAddressLine1.getText());
            etNewAddress.setText(tvDeliveryAddressLine2.getText());
        }
        db.collection("users").document(userId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                User user = doc.toObject(User.class);
                if (user != null && user.getPhone() != null) {
                    etNewPhone.setText(user.getPhone());
                }
            }
        });

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String newName = etNewName.getText().toString().trim();
            String newPhone = etNewPhone.getText().toString().trim();
            String newAddress = etNewAddress.getText().toString().trim();

            if (newName.isEmpty() || newPhone.isEmpty() || newAddress.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            tvDeliveryAddressLine1.setText(newName);
            tvDeliveryAddressLine2.setText(newAddress);

            if (cbSaveNewAddress.isChecked()) {
                db.collection("users").document(userId).update(
                        "name", newName,
                        "phone", newPhone,
                        "address", newAddress);
            }
            dialog.dismiss();
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
            } else {
                tvDeliveryAddressLine1.setText("Chưa có địa chỉ");
                tvDeliveryAddressLine2.setText("Vui lòng nhấn 'Thay đổi' để thêm");
            }
        });
    }

    private void loadCartData() {
        db.collection("users").document(userId).collection("cart").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    cartItems.clear();
                    subtotal = 0;
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        CartItem item = doc.toObject(CartItem.class);
                        cartItems.add(item);
                        subtotal += item.getTotalItemPrice();
                    }
                    calculateDiscount();
                    updateSummary();
                });
    }

    private void loadUserVouchers() {
        db.collection("users").document(userId).collection("userVouchers")
                .whereEqualTo("used", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    availableVouchers.clear();
                    Date now = new Date();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Voucher voucher = doc.toObject(Voucher.class);
                        if (voucher != null && voucher.getExpiryDate() != null && voucher.getExpiryDate().after(now)) {
                            voucher.setDocId(doc.getId());
                            availableVouchers.add(voucher);
                        }
                    }
                    Log.d(TAG, "Đã tải " + availableVouchers.size() + " voucher hợp lệ.");
                    tvSelectUserVoucher
                            .setText("Kho voucher của bạn (" + availableVouchers.size() + ") (Nhấn để chọn)");
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Lỗi khi tải userVouchers", e);
                });
    }

    private void showVoucherSelectionDialog() {
        if (availableVouchers.isEmpty()) {
            Toast.makeText(this, "Bạn không có voucher nào hợp lệ.", Toast.LENGTH_SHORT).show();
            return;
        }

        CharSequence[] voucherDisplayList = new CharSequence[availableVouchers.size()];
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy", Locale.US);

        for (int i = 0; i < availableVouchers.size(); i++) {
            Voucher v = availableVouchers.get(i);
            String expiry = (v.getExpiryDate() != null) ? sdf.format(v.getExpiryDate()) : "N/A";
            voucherDisplayList[i] = v.getCode() + ": " + v.getDescription() + " (Hết hạn: " + expiry + ")";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn voucher của bạn");
        builder.setItems(voucherDisplayList, (dialog, which) -> {
            Voucher selectedVoucher = availableVouchers.get(which);
            etVoucher.setText(selectedVoucher.getCode());
            appliedUserVoucherId = selectedVoucher.getDocId();
            applyVoucher();
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void applyVoucher() {
        String code = etVoucher.getText().toString().trim().toUpperCase();
        if (code.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mã voucher", Toast.LENGTH_SHORT).show();
            return;
        }

        if (appliedUserVoucherId != null) {
            Voucher selectedFromList = null;
            for (Voucher v : availableVouchers) {
                if (v.getDocId().equals(appliedUserVoucherId)) {
                    selectedFromList = v;
                    break;
                }
            }

            if (selectedFromList != null && selectedFromList.getCode().equals(code)) {
                appliedVoucher = selectedFromList;
                calculateDiscount();
                updateSummary();
                Toast.makeText(this, "Áp dụng voucher thành công!", Toast.LENGTH_SHORT).show();
                etVoucher.setError(null);
                return;
            } else {
                appliedUserVoucherId = null;
            }
        }

        db.collection("users").document(userId).collection("userVouchers")
                .whereEqualTo("code", code)
                .whereEqualTo("used", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot userVoucherDoc = queryDocumentSnapshots.getDocuments().get(0);
                        Voucher userVoucher = userVoucherDoc.toObject(Voucher.class);

                        if (userVoucher != null && userVoucher.getExpiryDate() != null
                                && userVoucher.getExpiryDate().after(new Date())) {
                            appliedVoucher = userVoucher;
                            appliedUserVoucherId = userVoucherDoc.getId();
                            calculateDiscount();
                            updateSummary();
                            Toast.makeText(this, "Áp dụng voucher (của bạn) thành công!", Toast.LENGTH_SHORT).show();
                            etVoucher.setError(null);
                        } else {
                            Toast.makeText(this, "Voucher này (của bạn) đã hết hạn.", Toast.LENGTH_SHORT).show();
                            resetVoucher();
                        }
                    } else {
                        checkGlobalVoucher(code);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Lỗi khi kiểm tra userVouchers", e);
                    checkGlobalVoucher(code);
                });
    }

    private void checkGlobalVoucher(String code) {
        db.collection("vouchers").document(code).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Voucher voucher = documentSnapshot.toObject(Voucher.class);
                        if (voucher != null && voucher.getExpiryDate() != null
                                && voucher.getExpiryDate().after(new Date())) {

                            // *** KIỂM TRA HẠNG THÀNH VIÊN ***
                            checkTierEligibility(voucher);

                        } else {
                            Toast.makeText(this, "Voucher đã hết hạn hoặc không hợp lệ.", Toast.LENGTH_SHORT).show();
                            resetVoucher();
                        }
                    } else {
                        Toast.makeText(this, "Mã voucher không hợp lệ", Toast.LENGTH_SHORT).show();
                        resetVoucher();
                    }
                }).addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi kiểm tra voucher: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    resetVoucher();
                });
    }

    private void checkTierEligibility(Voucher voucher) {
        db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                User user = documentSnapshot.toObject(User.class);
                if (user != null) {
                    String userTier = user.getMemberTier();
                    String minTier = voucher.getMinTier();

                    if (minTier == null || minTier.isEmpty())
                        minTier = "Thành viên";
                    if (userTier == null || userTier.isEmpty())
                        userTier = "Thành viên";

                    if (getTierLevel(userTier) >= getTierLevel(minTier)) {
                        appliedVoucher = voucher;
                        appliedUserVoucherId = null;
                        calculateDiscount();
                        updateSummary();
                        Toast.makeText(this, "Áp dụng voucher thành công!", Toast.LENGTH_SHORT).show();
                        etVoucher.setError(null);
                    } else {
                        Toast.makeText(this, "Bạn chưa đủ hạng thành viên (" + minTier + ") để dùng voucher này.",
                                Toast.LENGTH_LONG).show();
                        resetVoucher();
                    }
                }
            }
        });
    }

    private int getTierLevel(String tier) {
        switch (tier) {
            case "Thành viên":
                return 1;
            case "Đồng":
                return 2;
            case "Bạc":
                return 3;
            case "Vàng":
                return 4;
            case "Platinum":
                return 5;
            case "Kim Cương":
                return 6;
            default:
                return 0;
        }
    }

    private void resetVoucher() {
        appliedVoucher = null;
        appliedUserVoucherId = null;
        calculateDiscount();
        updateSummary();
    }

    private void calculateDiscount() {
        if (appliedVoucher == null) {
            discountAmount = 0;
            return;
        }
        if ("PERCENT".equals(appliedVoucher.getDiscountType())) {
            discountAmount = subtotal * (appliedVoucher.getDiscountValue() / 100.0);
        } else {
            discountAmount = appliedVoucher.getDiscountValue();
        }
        if (discountAmount > subtotal) {
            discountAmount = subtotal;
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
        if (finalTotal < 0)
            finalTotal = 0;
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

        db.collection("users").document(userId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                User user = doc.toObject(User.class);
                if (user != null && user.getPhone() != null) {
                    newOrder.setCustomerPhone(user.getPhone());
                }
            }
            orderRef.set(newOrder).addOnSuccessListener(aVoid -> {

                if (appliedUserVoucherId != null) {
                    markVoucherAsUsed(appliedUserVoucherId);
                }

                clearCart();
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Đặt hàng thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void markVoucherAsUsed(String userVoucherDocId) {
        db.collection("users").document(userId).collection("userVouchers")
                .document(userVoucherDocId)
                .update("used", true)
                .addOnSuccessListener(
                        aVoid -> Log.d(TAG, "Đã đánh dấu voucher " + userVoucherDocId + " là đã sử dụng."))
                .addOnFailureListener(e -> Log.w(TAG, "Lỗi khi đánh dấu voucher " + userVoucherDocId, e));
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
