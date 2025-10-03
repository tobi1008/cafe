package com.example.cafe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class CartActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CartAdapter cartAdapter;
    private List<CartItem> cartItemList = new ArrayList<>();
    private TextView textViewTotalPrice, textViewEmptyCart;
    private Button buttonCheckout;
    private CartManager cartManager;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        cartManager = CartManager.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();

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
            public void onQuantityChanged() {
                updateTotalPrice();
            }

            @Override
            public void onItemDeleted() {
                loadCartItems(); // Tải lại toàn bộ giỏ hàng để cập nhật
            }
        });
    }


    private void loadCartItems() {
        Future<List<CartItem>> future = cartManager.getCartItems();
        executorService.execute(() -> {
            try {
                List<CartItem> items = future.get();
                runOnUiThread(() -> {
                    cartItemList.clear();
                    cartItemList.addAll(items);
                    cartAdapter.notifyDataSetChanged();
                    updateTotalPrice();
                    checkIfCartIsEmpty();
                });
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    private void updateTotalPrice() {
        double total = 0;
        for (CartItem item : cartItemList) {
            total += item.getProductPrice() * item.getQuantity();
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
}

