package com.example.cafe.ui.cart;

import com.example.cafe.R;
import com.example.cafe.model.*;
import com.example.cafe.adapter.*;
import com.example.cafe.ui.home.*;
import com.example.cafe.ui.profile.*;
import com.example.cafe.ui.order.*;

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
import com.google.firebase.auth.FirebaseUser;
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

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để xem giỏ hàng", Toast.LENGTH_SHORT).show();
            finish(); // Đóng Activity nếu chưa đăng nhập
            return;
        }
        userId = currentUser.getUid();

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
        loadCartItems(); // Tải lại giỏ hàng mỗi khi quay lại màn hình
    }

    private void setupAdapter() {
        cartAdapter = new CartAdapter(this, cartItemList, new CartAdapter.CartItemListener() {
            @Override
            public void onQuantityChanged(CartItem item) {
                // Cập nhật số lượng item đó trên Firestore
                updateCartItemInFirestore(item);
                // Tính lại tổng tiền và cập nhật UI
                updateTotalPrice();
            }

            @Override
            public void onItemDeleted(CartItem item) {
                // Xóa item đó khỏi Firestore
                deleteCartItemFromFirestore(item);
                // Tải lại toàn bộ giỏ hàng để cập nhật UI (đơn giản nhất)
                // loadCartItems(); // Đã gọi trong deleteCartItemFromFirestore
            }
        });
    }

    private void loadCartItems() {
        db.collection("users").document(userId).collection("cart")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    cartItemList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            CartItem item = doc.toObject(CartItem.class);
                            // Cần gán ID document vào CartItem để biết document nào cần xóa/sửa
                            // Firestore không tự làm việc này khi dùng toObject()
                            // ID document của chúng ta có dạng productId_size_optionHash_timestamp
                            // Không cần lưu ID document ở đây vì logic xóa/sửa dùng ID tự tạo

                            cartItemList.add(item);
                        } catch (Exception e) {
                            Log.e("CartActivity", "Lỗi khi chuyển đổi item trong giỏ hàng: " + doc.getId(), e);
                            // Có thể xóa item bị lỗi này đi để dọn dẹp database
                            doc.getReference().delete(); // Xóa item lỗi
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
            total += item.getTotalItemPrice(); // Sử dụng hàm đã bao gồm topping và số lượng
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

    // Hàm tìm ID document trên Firestore dựa vào CartItem
    // Vì ID document giờ phức tạp hơn (có thể có timestamp), cần query để tìm
    private void findDocumentIdAndUpdateQuantity(CartItem item) {
        db.collection("users").document(userId).collection("cart")
                .whereEqualTo("productId", item.getProductId())
                .whereEqualTo("selectedSize", item.getSelectedSize())
                .whereEqualTo("iceOption", item.getIceOption())
                .whereEqualTo("sugarLevel", item.getSugarLevel())
                .whereEqualTo("note", item.getNote())
                .whereEqualTo("extraCoffeeShot", item.isExtraCoffeeShot())
                .whereEqualTo("extraSugarPacket", item.isExtraSugarPacket())
                .limit(1) // Chỉ cần tìm 1 document khớp
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentReference itemRef = queryDocumentSnapshots.getDocuments().get(0).getReference();
                        itemRef.update("quantity", item.getQuantity());
                    } else {
                        Log.w("CartActivity", "Không tìm thấy document để cập nhật số lượng cho: " + item.getProductName());
                    }
                });
    }

    // Hàm tìm ID document trên Firestore dựa vào CartItem để xóa
    private void findDocumentIdAndDelete(CartItem item) {
        db.collection("users").document(userId).collection("cart")
                .whereEqualTo("productId", item.getProductId())
                .whereEqualTo("selectedSize", item.getSelectedSize())
                .whereEqualTo("iceOption", item.getIceOption())
                .whereEqualTo("sugarLevel", item.getSugarLevel())
                .whereEqualTo("note", item.getNote())
                .whereEqualTo("extraCoffeeShot", item.isExtraCoffeeShot())
                .whereEqualTo("extraSugarPacket", item.isExtraSugarPacket())
                .limit(1) // Chỉ cần tìm 1 document khớp
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentReference itemRef = queryDocumentSnapshots.getDocuments().get(0).getReference();
                        itemRef.delete().addOnSuccessListener(aVoid -> loadCartItems()); // Tải lại sau khi xóa
                    } else {
                        Log.w("CartActivity", "Không tìm thấy document để xóa cho: " + item.getProductName());
                        // Nếu không tìm thấy, vẫn tải lại list để xóa item khỏi UI
                        loadCartItems();
                    }
                }).addOnFailureListener(e -> loadCartItems()); // Tải lại nếu có lỗi query
    }


    private void updateCartItemInFirestore(CartItem item) {
        findDocumentIdAndUpdateQuantity(item);
    }

    private void deleteCartItemFromFirestore(CartItem item) {
        findDocumentIdAndDelete(item);
    }
}

