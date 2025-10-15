package com.example.cafe;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProductDetailActivity extends AppCompatActivity {

    private ImageView imageViewDetail;
    private TextView textViewDetailName, textViewDetailDescription, textViewDetailPrice, textViewQuantity;
    private ChipGroup chipGroupSize;
    private Button buttonAddToCartDetail, btnIncrease, btnDecrease;
    private Product product;
    private String selectedSize = "";
    private int quantity = 1;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        imageViewDetail = findViewById(R.id.imageViewDetail);
        textViewDetailName = findViewById(R.id.textViewDetailName);
        textViewDetailDescription = findViewById(R.id.textViewDetailDescription);
        textViewDetailPrice = findViewById(R.id.textViewDetailPrice);
        chipGroupSize = findViewById(R.id.chipGroupSize);
        buttonAddToCartDetail = findViewById(R.id.buttonAddToCartDetail);
        textViewQuantity = findViewById(R.id.textViewQuantityDetail);
        btnIncrease = findViewById(R.id.buttonIncreaseQuantity);
        btnDecrease = findViewById(R.id.buttonDecreaseQuantity);

        product = (Product) getIntent().getSerializableExtra("PRODUCT_DETAIL");

        if (product != null) {
            populateUI();
            setupQuantityButtons();
        }

        buttonAddToCartDetail.setOnClickListener(v -> addToCart());
    }

    private void populateUI() {
        textViewDetailName.setText(product.getTen());
        textViewDetailDescription.setText(product.getMoTa());
        Glide.with(this).load(product.getHinhAnh()).into(imageViewDetail);

        chipGroupSize.removeAllViews();
        Map<String, Double> prices = product.getGia();
        if (prices != null) {
            // Sắp xếp các size (S, M, L) - nếu cần
            List<String> sortedSizes = new ArrayList<>(prices.keySet());
            Collections.sort(sortedSizes, (s1, s2) -> {
                if (s1.equals("S")) return -1;
                if (s2.equals("S")) return 1;
                if (s1.equals("M") && s2.equals("L")) return -1;
                if (s1.equals("L") && s2.equals("M")) return 1;
                return 0;
            });

            for (String size : sortedSizes) {
                Chip chip = new Chip(this);
                chip.setText(size);
                chip.setCheckable(true);
                chip.setOnClickListener(v -> {
                    selectedSize = chip.getText().toString();
                    updatePrice();
                });
                chipGroupSize.addView(chip);
            }
            if (chipGroupSize.getChildCount() > 0) {
                ((Chip)chipGroupSize.getChildAt(0)).setChecked(true);
                selectedSize = ((Chip)chipGroupSize.getChildAt(0)).getText().toString();
                updatePrice();
            }
        }
    }

    private void setupQuantityButtons() {
        btnIncrease.setOnClickListener(v -> {
            quantity++;
            textViewQuantity.setText(String.valueOf(quantity));
            updatePrice();
        });

        btnDecrease.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                textViewQuantity.setText(String.valueOf(quantity));
                updatePrice();
            }
        });
    }

    private void updatePrice() {
        if (!selectedSize.isEmpty()) {
            double singlePrice = product.getFinalPriceForSize(selectedSize);
            double totalPrice = singlePrice * quantity;
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            textViewDetailPrice.setText(formatter.format(totalPrice));
        }
    }

    private void addToCart() {
        if (selectedSize.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn size", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = currentUser.getUid();

        String cartItemId = product.getId() + "_" + selectedSize;
        DocumentReference cartItemRef = db.collection("users").document(userId).collection("cart").document(cartItemId);

        db.runTransaction(transaction -> {
            CartItem existingItem = transaction.get(cartItemRef).toObject(CartItem.class);
            if (existingItem != null) {
                transaction.update(cartItemRef, "quantity", existingItem.getQuantity() + quantity);
            } else {
                double price = product.getFinalPriceForSize(selectedSize);
                CartItem newItem = new CartItem(product.getId(), product.getTen(), price, product.getHinhAnh(), quantity, selectedSize);
                transaction.set(cartItemRef, newItem);
            }
            return null;
        }).addOnSuccessListener(aVoid -> {
            Toast.makeText(ProductDetailActivity.this, "Đã thêm vào giỏ hàng!", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> {
            Log.e("ProductDetail", "Lỗi khi thêm vào giỏ hàng", e);
            Toast.makeText(ProductDetailActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}

