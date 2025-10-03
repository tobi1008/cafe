package com.example.cafe;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class CheckoutActivity extends AppCompatActivity {

    private EditText etName, etPhone, etAddress;
    private Button btnConfirmOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        etName = findViewById(R.id.editTextCheckoutName);
        etPhone = findViewById(R.id.editTextCheckoutPhone);
        etAddress = findViewById(R.id.editTextCheckoutAddress);
        btnConfirmOrder = findViewById(R.id.buttonConfirmOrder);

        btnConfirmOrder.setOnClickListener(v -> {
            if (validateInput()) {
                confirmOrder();
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
        // Xử lý logic đặt hàng ở đây (ví dụ: lưu vào database đơn hàng...)

        // SỬA LỖI Ở ĐÂY: Truyền `getApplicationContext()` vào khi gọi getInstance()
        CartManager.getInstance(getApplicationContext()).clearCart();

        Toast.makeText(this, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show();

        // Mở màn hình chính và xóa các màn hình cũ khỏi stack
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish(); // Đóng màn hình checkout
    }
}

