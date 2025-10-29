package com.example.cafe;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class EditProductActivity extends AppCompatActivity {

    private static final String TAG = "EditProductActivity";

    private EditText etName, etPriceS, etPriceM, etPriceL, etDescription, etImageUrl, etSalePercent, etCategory;
    private Button btnUpdateProduct;

    private Spinner spinnerHappyHour;
    private ArrayAdapter<String> happyHourAdapter;
    private List<HappyHour> happyHourList = new ArrayList<>();
    private List<String> happyHourNames = new ArrayList<>();

    private FirebaseFirestore db;
    private Product currentProduct;
    private String currentProductId;

    private boolean isHappyHoursLoaded = false;
    private boolean isProductLoaded = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        db = FirebaseFirestore.getInstance();

        if (getIntent().hasExtra("PRODUCT_ID")) {
            currentProductId = getIntent().getStringExtra("PRODUCT_ID");
        } else {
            Toast.makeText(this, "Không tìm thấy ID sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        etName = findViewById(R.id.editProductName);
        etPriceS = findViewById(R.id.editPriceS);
        etPriceM = findViewById(R.id.editPriceM);
        etPriceL = findViewById(R.id.editPriceL);
        etDescription = findViewById(R.id.editProductDescription);
        etImageUrl = findViewById(R.id.editProductImageUrl);
        etSalePercent = findViewById(R.id.editSalePercent);
        etCategory = findViewById(R.id.editCategory);
        btnUpdateProduct = findViewById(R.id.buttonUpdateProduct);

        spinnerHappyHour = findViewById(R.id.spinnerHappyHour);

        happyHourNames.add("Không áp dụng");
        happyHourList.add(null);

        happyHourAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, happyHourNames);
        happyHourAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHappyHour.setAdapter(happyHourAdapter);

        loadHappyHours();
        loadProductDetails();

        btnUpdateProduct.setOnClickListener(v -> updateProduct());
    }


    private void loadHappyHours() {
        db.collection("HappyHours")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        HappyHour hh = document.toObject(HappyHour.class);

                        hh.setId(document.getId());

                        happyHourList.add(hh);
                        happyHourNames.add(hh.getTenKhungGio());
                    }
                    happyHourAdapter.notifyDataSetChanged();

                    isHappyHoursLoaded = true;
                    checkIfDataReady();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi khi tải Happy Hours", e);
                    isHappyHoursLoaded = true;
                    checkIfDataReady();
                });
    }


    private void loadProductDetails() {
        db.collection("cafe").document(currentProductId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentProduct = documentSnapshot.toObject(Product.class);
                        if (currentProduct != null) {
                            isProductLoaded = true;
                            checkIfDataReady();
                        }
                    } else {
                        Toast.makeText(this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi khi tải sản phẩm", e);
                });
    }


    private void checkIfDataReady() {
        if (isHappyHoursLoaded && isProductLoaded) {

            populateProductData();
        }
    }


    private void populateProductData() {

        etName.setText(currentProduct.getTen());
        etDescription.setText(currentProduct.getMoTa());
        etImageUrl.setText(currentProduct.getHinhAnh());
        etSalePercent.setText(String.valueOf(currentProduct.getPhanTramGiamGia()));
        etCategory.setText(currentProduct.getCategory());


        if (currentProduct.getGia() != null) {
            etPriceS.setText(String.valueOf(currentProduct.getPriceForSize("S")));
            etPriceM.setText(String.valueOf(currentProduct.getPriceForSize("M")));
            etPriceL.setText(String.valueOf(currentProduct.getPriceForSize("L")));
        }


        String productHappyHourId = currentProduct.getHappyHourId();
        int spinnerPosition = 0;

        if (productHappyHourId != null && !productHappyHourId.isEmpty()) {

            for (int i = 1; i < happyHourList.size(); i++) {
                HappyHour hh = happyHourList.get(i);

                if (hh != null && productHappyHourId.equals(hh.getId())) {
                    spinnerPosition = i;
                    break;
                }
            }
        }
        spinnerHappyHour.setSelection(spinnerPosition);
    }

    /**
     * Lấy dữ liệu từ các ô, cập nhật và lưu lên Firestore
     */
    private void updateProduct() {
        // Lấy dữ liệu đã sửa
        String ten = etName.getText().toString().trim();
        String priceSStr = etPriceS.getText().toString().trim();
        String priceMStr = etPriceM.getText().toString().trim();
        String priceLStr = etPriceL.getText().toString().trim();
        String moTa = etDescription.getText().toString().trim();
        String hinhAnh = etImageUrl.getText().toString().trim();
        String salePercentStr = etSalePercent.getText().toString().trim();
        String category = etCategory.getText().toString().trim();

        if (ten.isEmpty() || priceMStr.isEmpty() || category.isEmpty()) {
            Toast.makeText(this, "Tên, Giá size M và Danh mục là bắt buộc", Toast.LENGTH_SHORT).show();
            return;
        }


        Map<String, Double> gia = new HashMap<>();
        if (!priceSStr.isEmpty()) gia.put("S", Double.parseDouble(priceSStr));
        if (!priceMStr.isEmpty()) gia.put("M", Double.parseDouble(priceMStr));
        if (!priceLStr.isEmpty()) gia.put("L", Double.parseDouble(priceLStr));

        int phanTramGiamGia = salePercentStr.isEmpty() ? 0 : Integer.parseInt(salePercentStr);


        int selectedPosition = spinnerHappyHour.getSelectedItemPosition();
        String selectedHappyHourId = null;
        if (selectedPosition > 0) {
            HappyHour selectedHH = happyHourList.get(selectedPosition);
            if (selectedHH != null) {
                selectedHappyHourId = selectedHH.getId(); // Lấy ID (đã được gán từ Document ID)
            }
        }


        currentProduct.setTen(ten);
        currentProduct.setGia(gia);
        currentProduct.setMoTa(moTa);
        currentProduct.setHinhAnh(hinhAnh);
        currentProduct.setPhanTramGiamGia(phanTramGiamGia);
        currentProduct.setCategory(category);
        currentProduct.setHappyHourId(selectedHappyHourId); // Gán ID mới


        db.collection("cafe").document(currentProductId)
                .set(currentProduct)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EditProductActivity.this, "Cập nhật sản phẩm thành công", Toast.LENGTH_SHORT).show();
                    finish(); // Đóng Activity và quay lại
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EditProductActivity.this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Lỗi khi cập nhật", e);
                });
    }
}