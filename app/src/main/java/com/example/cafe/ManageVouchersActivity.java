package com.example.cafe;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Calendar;
import java.util.Locale;

public class ManageVouchersActivity extends AppCompatActivity {

    private EditText etCode, etDescription, etValue;
    private RadioGroup rgType;
    private Button btnAddVoucher;
    private android.widget.Spinner spinnerMinTier; // *** ADD THIS ***
    private RecyclerView recyclerViewVouchers;
    private VoucherAdapter adapter;
    private List<Voucher> voucherList = new ArrayList<>();
    private FirebaseFirestore db;

    private TextView tvSelectExpiryDate;
    private Calendar selectedCalendar; // Dùng để lưu ngày đã chọn

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_vouchers);

        db = FirebaseFirestore.getInstance();
        selectedCalendar = Calendar.getInstance();

        // Ánh xạ UI
        etCode = findViewById(R.id.editTextVoucherCode);
        etDescription = findViewById(R.id.editTextVoucherDescription);
        etValue = findViewById(R.id.editTextDiscountValue);
        rgType = findViewById(R.id.radioGroupDiscountType);
        spinnerMinTier = findViewById(R.id.spinnerMinTier); // *** ADD THIS ***
        btnAddVoucher = findViewById(R.id.buttonAddVoucher);
        recyclerViewVouchers = findViewById(R.id.recyclerViewVouchers);

        // Header Back Button (Toolbar)
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbarManageVouchers);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // *** ÁNH XẠ VÀ SET CLICK LISTENER *
        tvSelectExpiryDate = findViewById(R.id.tvSelectExpiryDate);
        tvSelectExpiryDate.setOnClickListener(v -> showDatePickerDialog());

        setupRecyclerView();
        btnAddVoucher.setOnClickListener(v -> createVoucher());

        loadVouchers();
    }

    // *** HIỂN THỊ BẢNG CHỌN NGÀY ***
    private void showDatePickerDialog() {
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            selectedCalendar.set(Calendar.YEAR, year);
            selectedCalendar.set(Calendar.MONTH, month);
            selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            // Cập nhật text cho TextView
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", new Locale("vi", "VN"));
            tvSelectExpiryDate.setText("Hết hạn: " + sdf.format(selectedCalendar.getTime()));
        };

        new DatePickerDialog(ManageVouchersActivity.this, dateSetListener,
                selectedCalendar.get(Calendar.YEAR),
                selectedCalendar.get(Calendar.MONTH),
                selectedCalendar.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void setupRecyclerView() {
        adapter = new VoucherAdapter(this, voucherList, voucher -> {
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
                    loadVouchers();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void createVoucher() {
        String code = etCode.getText().toString().trim().toUpperCase();
        String description = etDescription.getText().toString().trim();
        String valueStr = etValue.getText().toString().trim();

        // *** KIỂM TRA NGÀY HẾT HẠN ***
        // Kiểm tra xem đã chọn ngày chưa)
        if (tvSelectExpiryDate.getText().toString().equals("Chọn ngày hết hạn")
                || tvSelectExpiryDate.getText().toString().isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ngày hết hạn", Toast.LENGTH_SHORT).show();
            return;
        }

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

        // *** SET MIN TIER ***
        String selectedTier = spinnerMinTier.getSelectedItem().toString();
        if (selectedTier.equals("Tất cả")) {
            voucher.setMinTier("Thành viên"); // Mặc định là thấp nhất
        } else {
            voucher.setMinTier(selectedTier);
        }

        voucher.setExpiryDate(selectedCalendar.getTime());

        db.collection("vouchers").document(code).set(voucher)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Tạo voucher thành công!", Toast.LENGTH_SHORT).show();
                    // Xóa trống các ô và tải lại danh sách
                    etCode.setText("");
                    etDescription.setText("");
                    etValue.setText("");
                    tvSelectExpiryDate.setText("Chọn ngày hết hạn"); // Reset text
                    selectedCalendar = Calendar.getInstance(); // Reset calendar
                    loadVouchers();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
