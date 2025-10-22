package com.example.cafe;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Calendar;

public class ManageVouchersActivity extends AppCompatActivity {

    private EditText etCode, etDescription, etValue;
    private RadioGroup rgType;
    private Button btnAddVoucher;
    private RecyclerView recyclerViewVouchers;
    private VoucherAdapter adapter;
    private List<Voucher> voucherList = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_vouchers);

        db = FirebaseFirestore.getInstance();

        // Ánh xạ UI
        etCode = findViewById(R.id.editTextVoucherCode);
        etDescription = findViewById(R.id.editTextVoucherDescription);
        etValue = findViewById(R.id.editTextDiscountValue);
        rgType = findViewById(R.id.radioGroupDiscountType);
        btnAddVoucher = findViewById(R.id.buttonAddVoucher);
        recyclerViewVouchers = findViewById(R.id.recyclerViewVouchers);

        setupRecyclerView();
        btnAddVoucher.setOnClickListener(v -> createVoucher());

        loadVouchers();
    }

    private void setupRecyclerView() {
        adapter = new VoucherAdapter(this, voucherList, voucher -> {
            // Xử lý sự kiện khi nhấn nút xóa
            new AlertDialog.Builder(this)
                    .setTitle("Xác nhận Xóa")
                    .setMessage("Bạn có chắc chắn muốn xóa voucher '" + voucher.getCode() + "' không?")
                    .setPositiveButton("Xóa", (dialog, which) -> deleteVoucher(voucher))
                    .setNegativeButton("Hủy", null)
                    .show();
        });
        recyclerViewVouchers.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewVouchers.setAdapter(adapter);
    }

    private void loadVouchers() {
        db.collection("vouchers")
                .orderBy("expiryDate", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    voucherList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        voucherList.add(doc.toObject(Voucher.class));
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void deleteVoucher(Voucher voucher) {
        db.collection("vouchers").document(voucher.getCode()).delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đã xóa voucher", Toast.LENGTH_SHORT).show();
                    loadVouchers(); // Tải lại danh sách
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void createVoucher() {
        String code = etCode.getText().toString().trim().toUpperCase();
        String description = etDescription.getText().toString().trim();
        String valueStr = etValue.getText().toString().trim();

        if (code.isEmpty() || description.isEmpty() || valueStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        Voucher voucher = new Voucher();
        voucher.setCode(code);
        voucher.setDescription(description);
        voucher.setDiscountValue(Double.parseDouble(valueStr));

        int selectedTypeId = rgType.getCheckedRadioButtonId();
        if (selectedTypeId == R.id.radioPercent) {
            voucher.setDiscountType("PERCENT");
        } else {
            voucher.setDiscountType("FIXED_AMOUNT");
        }

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 30);
        voucher.setExpiryDate(calendar.getTime());

        db.collection("vouchers").document(code).set(voucher)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Tạo voucher thành công!", Toast.LENGTH_SHORT).show();
                    // Xóa trống các ô và tải lại danh sách
                    etCode.setText("");
                    etDescription.setText("");
                    etValue.setText("");
                    loadVouchers();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}

