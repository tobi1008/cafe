package com.example.cafe;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class CheckoutActivity extends AppCompatActivity {

    private EditText editTextName, editTextPhone, editTextAddress;
    private Button buttonConfirmOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        editTextName = findViewById(R.id.editTextName);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextAddress = findViewById(R.id.editTextAddress);
        buttonConfirmOrder = findViewById(R.id.buttonConfirmOrder);

        buttonConfirmOrder.setOnClickListener(v -> {
            confirmOrder();
        });
    }

    private void confirmOrder() {
        String name = editTextName.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String address = editTextAddress.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(address)) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        // Xử lý logic đặt hàng thành công
        Toast.makeText(this, "Đặt hàng thành công!", Toast.LENGTH_LONG).show();

        // Xóa giỏ hàng
        CartManager.getInstance().clearCart();

        // Quay về trang chủ và xóa các màn hình cũ (giỏ hàng, checkout) khỏi stack
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish(); // Đóng màn hình hiện tại
    }
}
