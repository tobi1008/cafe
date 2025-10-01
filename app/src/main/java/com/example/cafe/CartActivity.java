package com.example.cafe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CartActivity extends AppCompatActivity {

    private RecyclerView cartRecyclerView;
    private CartAdapter cartAdapter;
    private TextView textViewTotalPrice;
    private Button buttonCheckout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        textViewTotalPrice = findViewById(R.id.textViewTotalPrice);
        buttonCheckout = findViewById(R.id.buttonCheckout);
        cartRecyclerView = findViewById(R.id.recyclerViewCart);
        cartRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        updateCart();

        buttonCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CartActivity.this, CheckoutActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Cập nhật lại giỏ hàng mỗi khi quay lại màn hình này
        updateCart();
    }

    private void updateCart() {
        List<CartItem> cartItems = CartManager.getInstance().getCartItems();
        cartAdapter = new CartAdapter(cartItems);
        cartRecyclerView.setAdapter(cartAdapter);

        // --- SỬA LỖI Ở ĐÂY ---
        // Sử dụng phương thức mới getGia()
        double totalPrice = CartManager.getInstance().getTotalPrice();

        // Định dạng tiền tệ
        Locale localeVN = new Locale("vi", "VN");
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(localeVN);
        textViewTotalPrice.setText(currencyFormatter.format(totalPrice));

        // Ẩn/Hiện nút checkout
        if (cartItems.isEmpty()) {
            buttonCheckout.setVisibility(View.GONE);
            textViewTotalPrice.setText("Giỏ hàng của bạn đang trống");
        } else {
            buttonCheckout.setVisibility(View.VISIBLE);
        }
    }
}

