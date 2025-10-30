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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class EditProductActivity extends AppCompatActivity {

    private static final String TAG = "EditProductActivity";

    private EditText etName, etPriceS, etPriceM, etPriceL, etDescription, etImageUrl, etSalePercent;
    private Button btnUpdateProduct;

    private Spinner spinnerCategory;
    private ArrayAdapter<String> categoryAdapter;
    private List<Category> categoryList = new ArrayList<>();
    private List<String> categoryNames = new ArrayList<>();

    private Spinner spinnerHappyHour;
    private ArrayAdapter<String> happyHourAdapter;
    private List<HappyHour> happyHourList = new ArrayList<>();
    private List<String> happyHourNames = new ArrayList<>();

    private FirebaseFirestore db;
    private Product currentProduct;
    private String currentProductId;

    private boolean isHappyHoursLoaded = false;
    private boolean isCategoriesLoaded = false;
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
        btnUpdateProduct = findViewById(R.id.buttonUpdateProduct);

        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerHappyHour = findViewById(R.id.spinnerHappyHour);

        categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryNames);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);


        happyHourNames.add("Không áp dụng");
        happyHourList.add(null);
        happyHourAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, happyHourNames);
        happyHourAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHappyHour.setAdapter(happyHourAdapter);

        loadHappyHours();
        loadCategories();
        loadProductDetails();

        btnUpdateProduct.setOnClickListener(v -> updateProduct());
    }


    private void loadHappyHours() {
        db.collection("HappyHours")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    happyHourList.subList(1, happyHourList.size()).clear();
                    happyHourNames.subList(1, happyHourNames.size()).clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            HappyHour hh = document.toObject(HappyHour.class);
                            hh.setId(document.getId());
                            happyHourList.add(hh);
                            happyHourNames.add(hh.getTenKhungGio());
                        } catch (Exception e) {
                            Log.e(TAG, "Lỗi khi chuyển đổi HappyHour: " + document.getId(), e);
                        }
                    }
                    happyHourAdapter.notifyDataSetChanged();

                    isHappyHoursLoaded = true;
                    checkIfAllDataReady();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi khi tải Happy Hours", e);
                    isHappyHoursLoaded = true;
                    checkIfAllDataReady();
                });
    }


    private void loadCategories() {
        db.collection("Categories")
                // *** THAY ĐỔI SẮP XẾP: Theo "thuTuUuTien" thay vì "tenDanhMuc" ***
                .orderBy("thuTuUuTien", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    categoryList.clear();
                    categoryNames.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            Category cat = document.toObject(Category.class);
                            cat.setId(document.getId());
                            categoryList.add(cat);
                            categoryNames.add(cat.getTenDanhMuc());
                        } catch (Exception e) {
                            Log.e(TAG, "Lỗi khi chuyển đổi Category: " + document.getId(), e);
                        }
                    }
                    categoryAdapter.notifyDataSetChanged();

                    isCategoriesLoaded = true;
                    checkIfAllDataReady();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi khi tải Categories", e);
                    Toast.makeText(this, "Lỗi tải danh mục", Toast.LENGTH_SHORT).show();
                    isCategoriesLoaded = true;
                    checkIfAllDataReady();
                });
    }



    private void loadProductDetails() {
        db.collection("cafe").document(currentProductId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        try {
                            currentProduct = documentSnapshot.toObject(Product.class);
                            if (currentProduct != null) {
                                isProductLoaded = true;
                                checkIfAllDataReady();
                            } else {
                                Toast.makeText(this, "Lỗi đọc dữ liệu sản phẩm", Toast.LENGTH_SHORT).show();
                            }
                        } catch(Exception e){
                            Log.e(TAG, "Lỗi chuyển đổi Product: " + currentProductId, e);
                            Toast.makeText(this, "Lỗi đọc dữ liệu sản phẩm", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi khi tải sản phẩm", e);
                    Toast.makeText(this, "Lỗi tải sản phẩm", Toast.LENGTH_SHORT).show();
                });
    }


    private void checkIfAllDataReady() {
        Log.d(TAG, "CheckIfAllDataReady: HH=" + isHappyHoursLoaded + ", Cat=" + isCategoriesLoaded + ", Prod=" + isProductLoaded);
        if (isHappyHoursLoaded && isCategoriesLoaded && isProductLoaded) {
            Log.d(TAG, "All data loaded, populating UI.");
            populateProductData();
        }
    }


    private void populateProductData() {
        if (currentProduct == null) return;

        etName.setText(currentProduct.getTen());
        etDescription.setText(currentProduct.getMoTa());
        etImageUrl.setText(currentProduct.getHinhAnh());
        etSalePercent.setText(String.valueOf(currentProduct.getPhanTramGiamGia()));

        if (currentProduct.getGia() != null) {
            etPriceS.setText(String.valueOf(currentProduct.getPriceForSize("S")));
            etPriceM.setText(String.valueOf(currentProduct.getPriceForSize("M")));
            etPriceL.setText(String.valueOf(currentProduct.getPriceForSize("L")));
        }

        String productCategoryName = currentProduct.getCategory();
        int categorySpinnerPosition = 0;
        if (productCategoryName != null && !productCategoryName.isEmpty() && !categoryNames.isEmpty()) {
            Log.d(TAG, "Searching for category: " + productCategoryName);
            for (int i = 0; i < categoryNames.size(); i++) {
                if (productCategoryName.equalsIgnoreCase(categoryNames.get(i))) {
                    categorySpinnerPosition = i;
                    Log.d(TAG, "Category found at position: " + i);
                    break;
                }
            }
        } else {
            Log.d(TAG, "Product category is null/empty or categoryNames list is empty.");
        }
        spinnerCategory.setSelection(categorySpinnerPosition);


        String productHappyHourId = currentProduct.getHappyHourId();
        int happyHourSpinnerPosition = 0;
        if (productHappyHourId != null && !productHappyHourId.isEmpty() && happyHourList.size() > 1) {
            Log.d(TAG, "Searching for Happy Hour ID: " + productHappyHourId);
            for (int i = 1; i < happyHourList.size(); i++) {
                HappyHour hh = happyHourList.get(i);
                if (hh != null && productHappyHourId.equals(hh.getId())) {
                    happyHourSpinnerPosition = i;
                    Log.d(TAG, "Happy Hour found at position: " + i);
                    break;
                }
            }
        } else {
            Log.d(TAG, "Product Happy Hour ID is null/empty or happyHourList is too small.");
        }
        spinnerHappyHour.setSelection(happyHourSpinnerPosition);
    }


    private void updateProduct() {
        if (currentProduct == null) {
            Toast.makeText(this, "Lỗi: Dữ liệu sản phẩm chưa sẵn sàng", Toast.LENGTH_SHORT).show();
            return;
        }

        String ten = etName.getText().toString().trim();
        String priceSStr = etPriceS.getText().toString().trim();
        String priceMStr = etPriceM.getText().toString().trim();
        String priceLStr = etPriceL.getText().toString().trim();
        String moTa = etDescription.getText().toString().trim();
        String hinhAnh = etImageUrl.getText().toString().trim();
        String salePercentStr = etSalePercent.getText().toString().trim();

        if (ten.isEmpty() || priceMStr.isEmpty()) {
            Toast.makeText(this, "Tên và Giá size M là bắt buộc", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Double> gia = new HashMap<>();
        try {
            if (!priceSStr.isEmpty()) gia.put("S", Double.parseDouble(priceSStr));
            gia.put("M", Double.parseDouble(priceMStr));
            if (!priceLStr.isEmpty()) gia.put("L", Double.parseDouble(priceLStr));
        } catch (NumberFormatException e){
            Toast.makeText(this, "Giá tiền không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        int phanTramGiamGia = 0;
        if (!salePercentStr.isEmpty()){
            try {
                phanTramGiamGia = Integer.parseInt(salePercentStr);
                if (phanTramGiamGia < 0 || phanTramGiamGia > 100){
                    Toast.makeText(this, "Giảm giá phải từ 0-100", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Giảm giá không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        String selectedCategoryName = null;
        int selectedCategoryPosition = spinnerCategory.getSelectedItemPosition();

        if (spinnerCategory.getCount() > 0 && selectedCategoryPosition != Spinner.INVALID_POSITION) {
            selectedCategoryName = categoryAdapter.getItem(selectedCategoryPosition);
        }

        if (selectedCategoryName == null || selectedCategoryName.isEmpty()){
            Toast.makeText(this, "Vui lòng chọn Danh mục", Toast.LENGTH_SHORT).show();
            return;
        }


        int selectedHappyHourPosition = spinnerHappyHour.getSelectedItemPosition();
        String selectedHappyHourId = null;
        if (selectedHappyHourPosition > 0) {
            HappyHour selectedHH = happyHourList.get(selectedHappyHourPosition);
            if (selectedHH != null) {
                selectedHappyHourId = selectedHH.getId();
            }
        }

        currentProduct.setTen(ten);
        currentProduct.setGia(gia);
        currentProduct.setMoTa(moTa);
        currentProduct.setHinhAnh(hinhAnh);
        currentProduct.setPhanTramGiamGia(phanTramGiamGia);
        currentProduct.setCategory(selectedCategoryName);
        currentProduct.setHappyHourId(selectedHappyHourId);

        db.collection("cafe").document(currentProductId)
                .set(currentProduct)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EditProductActivity.this, "Cập nhật sản phẩm thành công", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EditProductActivity.this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Lỗi khi cập nhật", e);
                });
    }
}

