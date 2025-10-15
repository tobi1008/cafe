package com.example.cafe;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CartAdapter cartAdapter;
    private List<CartItem> cartItemList = new ArrayList<>();
    private TextView textViewTotalPrice, textViewEmptyCart;
    private Button buttonCheckout;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để xem giỏ hàng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        userId = mAuth.getCurrentUser().getUid();

        textViewTotalPrice = findViewById(R.id.textViewTotalPrice);
        textViewEmptyCart = findViewById(R.id.textViewEmptyCart);
        buttonCheckout = findViewById(R.id.buttonCheckout);
        recyclerView = findViewById(R.id.recyclerViewCart);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        setupAdapter();
        recyclerView.setAdapter(cartAdapter);

        buttonCheckout.setOnClickListener(v -> {
            if (cartItemList.isEmpty()) {
                Toast.makeText(this, "Giỏ hàng của bạn đang trống", Toast.LENGTH_SHORT).show();
            } else {
                startActivity(new Intent(CartActivity.this, CheckoutActivity.class));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCartItems();
    }

    private void setupAdapter() {
        cartAdapter = new CartAdapter(this, cartItemList, new CartAdapter.CartItemListener() {
            @Override
            public void onQuantityChanged(CartItem item) {
                updateCartItemInFirestore(item);
                updateTotalPrice();
            }

            @Override
            public void onItemDeleted(CartItem item) {
                deleteCartItemFromFirestore(item);
            }
        });
    }

    private void loadCartItems() {
        db.collection("users").document(userId).collection("cart")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    cartItemList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        // --- SỬA LỖI Ở ĐÂY: Thêm try-catch để bỏ qua dữ liệu không hợp lệ ---
                        try {
                            CartItem item = doc.toObject(CartItem.class);
                            cartItemList.add(item);
                        } catch (Exception e) {
                            Log.e("CartActivity", "Lỗi khi chuyển đổi item trong giỏ hàng: " + doc.getId(), e);
                            // Có thể xóa item bị lỗi này đi để dọn dẹp database
                            // doc.getReference().delete();
                        }
                    }
                    cartAdapter.notifyDataSetChanged();
                    updateTotalPrice();
                    checkIfCartIsEmpty();
                })
                .addOnFailureListener(e -> Log.e("CartActivity", "Lỗi khi tải giỏ hàng", e));
    }

    private void updateTotalPrice() {
        double total = 0;
        for (CartItem item : cartItemList) {
            total += item.getPrice() * item.getQuantity();
        }
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        textViewTotalPrice.setText(formatter.format(total));
    }

    private void checkIfCartIsEmpty() {
        if (cartItemList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            textViewEmptyCart.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            textViewEmptyCart.setVisibility(View.GONE);
        }
    }

    private void updateCartItemInFirestore(CartItem item) {
        String cartItemId = item.getProductId() + "_" + item.getSelectedSize();
        DocumentReference itemRef = db.collection("users").document(userId).collection("cart").document(cartItemId);
        itemRef.update("quantity", item.getQuantity());
    }

    private void deleteCartItemFromFirestore(CartItem item) {
        String cartItemId = item.getProductId() + "_" + item.getSelectedSize();
        db.collection("users").document(userId).collection("cart").document(cartItemId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    loadCartItems(); // Tải lại giỏ hàng sau khi xóa thành công
                });
    }
}

