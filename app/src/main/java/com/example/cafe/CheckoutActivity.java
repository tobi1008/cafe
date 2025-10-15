package com.example.cafe;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date; // <-- THÊM IMPORT NÀY
import java.util.List;

public class CheckoutActivity extends AppCompatActivity {

    private EditText etName, etPhone, etAddress;
    private CheckBox cbSaveAddress;
    private Button btnConfirmOrder;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Lỗi: Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        userId = currentUser.getUid();

        etName = findViewById(R.id.editTextCheckoutName);
        etPhone = findViewById(R.id.editTextCheckoutPhone);
        etAddress = findViewById(R.id.editTextCheckoutAddress);
        cbSaveAddress = findViewById(R.id.checkboxSaveAddress);
        btnConfirmOrder = findViewById(R.id.buttonConfirmOrder);

        loadSavedAddress();

        btnConfirmOrder.setOnClickListener(v -> {
            if (validateInput()) {
                confirmOrder();
            }
        });
    }

    private void loadSavedAddress() {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            if (user.getName() != null) etName.setText(user.getName());
                            if (user.getPhone() != null) etPhone.setText(user.getPhone());
                            if (user.getAddress() != null) etAddress.setText(user.getAddress());
                        }
                    }
                });
    }

    private boolean validateInput() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void confirmOrder() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        db.collection("users").document(userId).collection("cart").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(this, "Giỏ hàng trống, không thể đặt hàng", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<CartItem> cartItems = new ArrayList<>();
                    double totalPrice = 0;
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        CartItem item = doc.toObject(CartItem.class);
                        cartItems.add(item);
                        totalPrice += item.getPrice() * item.getQuantity();
                    }

                    DocumentReference orderRef = db.collection("orders").document();
                    Order newOrder = new Order();
                    newOrder.setOrderId(orderRef.getId());
                    newOrder.setUserId(userId);
                    newOrder.setCustomerName(name);
                    newOrder.setCustomerPhone(phone);
                    newOrder.setCustomerAddress(address);
                    newOrder.setItems(cartItems);
                    newOrder.setTotalPrice(totalPrice);
                    // SỬA LỖI Ở ĐÂY: Dùng new Date() thay vì Timestamp.now()
                    newOrder.setOrderDate(new Date());
                    newOrder.setStatus("Đang chờ xử lý");

                    orderRef.set(newOrder).addOnSuccessListener(aVoid -> {
                        if (cbSaveAddress.isChecked()) {
                            db.collection("users").document(userId)
                                    .update("name", name, "phone", phone, "address", address);
                        }
                        clearCart();
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

