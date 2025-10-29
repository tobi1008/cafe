package com.example.cafe;

import android.app.Dialog;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class ProductDetailBottomSheetFragment extends BottomSheetDialogFragment {

    private static final String ARG_PRODUCT = "product_arg";
    private static final String TAG = "ProductDetailBottomSheet";

    // Views
    private ImageView ivProductImageSheet;
    private TextView tvProductNameSheet, tvProductPriceSheet, tvProductDescriptionSheet, tvQuantitySheet, tvTotalPriceSheet;
    private ImageButton btnIncreaseQuantitySheet, btnDecreaseQuantitySheet, btnCloseSheet;
    private RadioGroup rgSizeSheet, rgIceSheet, rgSugarSheet;
    private Button btnContinueSheet;
    private TextView tvHappyHourTagSheet, tvOriginalPriceSheet, tvOriginalPriceHeaderSheet;
    // *** VIEW MỚI CHO GHI CHÚ ***
    private EditText etNoteSheet;

    // Data
    private Product product;
    private int quantity = 1;
    private double basePrice = 0;
    private double currentSelectedSizePrice = 0;
    private double currentIcePrice = 0;
    private double currentSugarPrice = 0;
    private String selectedSizeName = "";
    private String selectedIceName = "Đá chung";
    private String selectedSugarName = "100%";

    // Biến giảm giá
    private HappyHour activeHappyHour = null;
    private boolean isHappyHourActive = false;
    private int happyHourDiscountPercent = 0;
    private double finalUnitPrice = 0;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String userId;


    public static ProductDetailBottomSheetFragment newInstance(Product product) {
        ProductDetailBottomSheetFragment fragment = new ProductDetailBottomSheetFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PRODUCT, product);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            product = (Product) getArguments().getSerializable(ARG_PRODUCT);
        }
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        }
    }

    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialogInterface;
            View bottomSheetInternal = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheetInternal != null) {
                BottomSheetBehavior.from(bottomSheetInternal).setState(BottomSheetBehavior.STATE_EXPANDED);
                bottomSheetInternal.setBackgroundResource(R.drawable.bottom_sheet_background);
            }
        });
        return dialog;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_product_detail, container, false);
        initViews(view);
        if (product != null) {
            populateInitialData();
            setupOptions();
            setupListeners();
            fetchHappyHourInfo();
        } else {
            Log.e(TAG, "Product is null, cannot populate data.");
            dismiss();
        }
        return view;
    }

    private void initViews(View view) {
        ivProductImageSheet = view.findViewById(R.id.ivProductImageSheet);
        tvProductNameSheet = view.findViewById(R.id.tvProductNameSheet);
        tvProductPriceSheet = view.findViewById(R.id.tvProductPriceSheet);
        tvProductDescriptionSheet = view.findViewById(R.id.tvProductDescriptionSheet);
        tvQuantitySheet = view.findViewById(R.id.tvQuantitySheet);
        tvTotalPriceSheet = view.findViewById(R.id.tvTotalPriceSheet);
        btnIncreaseQuantitySheet = view.findViewById(R.id.btnIncreaseQuantitySheet);
        btnDecreaseQuantitySheet = view.findViewById(R.id.btnDecreaseQuantitySheet);
        btnCloseSheet = view.findViewById(R.id.btnCloseSheet);
        rgSizeSheet = view.findViewById(R.id.rgSizeSheet);
        rgIceSheet = view.findViewById(R.id.rgIceSheet);
        rgSugarSheet = view.findViewById(R.id.rgSugarSheet);
        btnContinueSheet = view.findViewById(R.id.btnContinueSheet);
        tvHappyHourTagSheet = view.findViewById(R.id.tvHappyHourTagSheet);
        tvOriginalPriceSheet = view.findViewById(R.id.tvOriginalPriceSheet);
        tvOriginalPriceHeaderSheet = view.findViewById(R.id.tvOriginalPriceHeaderSheet);
        // *** ÁNH XẠ EditText GHI CHÚ ***
        etNoteSheet = view.findViewById(R.id.etNoteSheet);
    }

    private void populateInitialData() {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        Glide.with(requireContext()).load(product.getHinhAnh()).placeholder(R.drawable.placeholder_image).into(ivProductImageSheet);
        tvProductNameSheet.setText(product.getTen());
        tvProductDescriptionSheet.setText(product.getMoTa());

        Map<String, Double> prices = product.getGia();
        if (prices != null && prices.containsKey("M")) {
            basePrice = product.getPriceForSize("M");
            selectedSizeName = "M";
        } else if (prices != null && !prices.isEmpty()) {
            String firstSize = "";
            if (prices.containsKey("S")) {
                firstSize = "S";
            } else {
                firstSize = new ArrayList<>(prices.keySet()).get(0);
            }
            basePrice = product.getPriceForSize(firstSize);
            selectedSizeName = firstSize;
        } else {
            basePrice = 0;
            selectedSizeName = "";
        }
        currentSelectedSizePrice = basePrice;

        tvProductPriceSheet.setText(formatter.format(basePrice));
        tvOriginalPriceHeaderSheet.setVisibility(View.GONE);
    }

    private void setupOptions() {
        // --- Size Options ---
        rgSizeSheet.removeAllViews();
        Map<String, Double> sizes = product.getGia();
        if (sizes != null && !sizes.isEmpty()) {
            List<String> sortedSizes = new ArrayList<>(sizes.keySet());
            sortedSizes.sort((s1, s2) -> {
                if (s1.equals("S")) return -1;
                if (s1.equals("M") && s2.equals("L")) return -1;
                if (s1.equals("L")) return 1;
                return 0;
            });

            for (String size : sortedSizes) {
                double price = sizes.getOrDefault(size, 0.0);
                double priceDifference = price - basePrice;
                addRadioButton(rgSizeSheet, size, priceDifference, size.equals(selectedSizeName));
            }
        }

        // --- Ice Options ---
        rgIceSheet.removeAllViews();
        addRadioButton(rgIceSheet, "Đá chung", 0, true);
        addRadioButton(rgIceSheet, "Đá riêng", 0, false);

        // --- Sugar Options ---
        rgSugarSheet.removeAllViews();
        List<String> sugarLevels = Arrays.asList("100%", "80%", "50%");
        for (String level : sugarLevels) {
            addRadioButton(rgSugarSheet, level, 0, level.equals(selectedSugarName));
        }

    }

    private void addRadioButton(RadioGroup radioGroup, String text, double priceDifference, boolean isChecked) {
        if (getContext() == null) return;

        RadioButton radioButton = (RadioButton) LayoutInflater.from(getContext()).inflate(R.layout.radio_button_option, radioGroup, false);

        String displayText = text;
        if (radioGroup.getId() == R.id.rgSizeSheet) {
            if (priceDifference > 0) {
                displayText += " (+" + formatPrice(priceDifference) + ")";
            } else if (priceDifference < 0) {
                displayText += " (" + formatPrice(priceDifference) + ")";
            }
            radioButton.setTag(product.getPriceForSize(text));
        } else {
            radioButton.setTag(0.0);
        }

        radioButton.setText(displayText);
        radioButton.setChecked(isChecked);
        radioButton.setId(View.generateViewId());

        radioButton.setTag(R.id.tag_option_name, text);

        radioGroup.addView(radioButton);
    }


    private void setupListeners() {
        btnCloseSheet.setOnClickListener(v -> dismiss());

        btnIncreaseQuantitySheet.setOnClickListener(v -> {
            quantity++;
            tvQuantitySheet.setText(String.valueOf(quantity));
            updateTotalPrice();
        });

        btnDecreaseQuantitySheet.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                tvQuantitySheet.setText(String.valueOf(quantity));
                updateTotalPrice();
            }
        });

        rgSizeSheet.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton checkedRadioButton = group.findViewById(checkedId);
            if (checkedRadioButton != null && checkedRadioButton.getTag() instanceof Double) {
                String sizeName = (String) checkedRadioButton.getTag(R.id.tag_option_name);
                // Lấy giá gốc size trực tiếp từ tag (đã lưu ở addRadioButton)
                currentSelectedSizePrice = (double) checkedRadioButton.getTag();
                selectedSizeName = sizeName;
                updateTotalPrice();
            }
        });

        rgIceSheet.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton checkedRadioButton = group.findViewById(checkedId);
            if (checkedRadioButton != null && checkedRadioButton.getTag() instanceof Double) {
                currentIcePrice = (double) checkedRadioButton.getTag();
                selectedIceName = (String) checkedRadioButton.getTag(R.id.tag_option_name);
                updateTotalPrice();
            }
        });

        rgSugarSheet.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton checkedRadioButton = group.findViewById(checkedId);
            if (checkedRadioButton != null && checkedRadioButton.getTag() instanceof Double) {
                currentSugarPrice = (double) checkedRadioButton.getTag();
                selectedSugarName = (String) checkedRadioButton.getTag(R.id.tag_option_name);
                updateTotalPrice();
            }
        });

        btnContinueSheet.setOnClickListener(v -> addToCart());
    }

    private void updateTotalPrice() {
        double originalUnitPrice = currentSelectedSizePrice + currentIcePrice + currentSugarPrice;
        double discountedUnitPrice;
        boolean isDiscounted = false;

        if (isHappyHourActive) {
            double discountedSizePrice = currentSelectedSizePrice * (1 - (happyHourDiscountPercent / 100.0));
            discountedUnitPrice = discountedSizePrice + currentIcePrice + currentSugarPrice;
            isDiscounted = true;
            tvHappyHourTagSheet.setText(String.format(Locale.US, "🔥 -%d %% sale giờ vàng", happyHourDiscountPercent));
            tvHappyHourTagSheet.setVisibility(View.VISIBLE);

        } else if (product.getPhanTramGiamGia() > 0) {
            double discountedSizePrice = currentSelectedSizePrice * (1 - (product.getPhanTramGiamGia() / 100.0));
            discountedUnitPrice = discountedSizePrice + currentIcePrice + currentSugarPrice;
            isDiscounted = true;
            tvHappyHourTagSheet.setText(String.format(Locale.US, "-%d%%", product.getPhanTramGiamGia()));
            tvHappyHourTagSheet.setVisibility(View.VISIBLE);

        } else {
            discountedUnitPrice = originalUnitPrice;
            isDiscounted = false;
            tvHappyHourTagSheet.setVisibility(View.GONE);
        }

        finalUnitPrice = discountedUnitPrice;

        double totalPrice = finalUnitPrice * quantity;
        tvTotalPriceSheet.setText(formatPrice(totalPrice));

        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        if (isDiscounted) {
            double originalTotalPrice = originalUnitPrice * quantity;
            tvOriginalPriceSheet.setText(formatPrice(originalTotalPrice));
            tvOriginalPriceSheet.setPaintFlags(tvOriginalPriceSheet.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            tvOriginalPriceSheet.setVisibility(View.VISIBLE);

            tvOriginalPriceHeaderSheet.setText(formatter.format(currentSelectedSizePrice));
            tvOriginalPriceHeaderSheet.setPaintFlags(tvOriginalPriceHeaderSheet.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            tvOriginalPriceHeaderSheet.setVisibility(View.VISIBLE);
            tvProductPriceSheet.setText(formatPrice(discountedUnitPrice - currentIcePrice - currentSugarPrice));


        } else {
            tvOriginalPriceSheet.setVisibility(View.GONE);
            tvOriginalPriceHeaderSheet.setVisibility(View.GONE);
            tvProductPriceSheet.setText(formatPrice(currentSelectedSizePrice));
        }
    }


    private String formatPrice(double price) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return formatter.format(price);
    }

    // *** HÀM ADDTOCART ĐÃ CẬP NHẬT ĐỂ LẤY GHI CHÚ ***
    private void addToCart() {
        if (userId == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedSizeName.isEmpty()) {
            Toast.makeText(getContext(), "Lỗi: Size chưa được chọn", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lấy ghi chú từ EditText
        String note = etNoteSheet.getText().toString().trim();

        String options = selectedIceName + ", " + selectedSugarName;

        // *** ĐẢM BẢO CartItem.java KHỚP VỚI HÀM TẠO NÀY (có thêm note) ***
        CartItem newItem = new CartItem(
                product.getId(),
                product.getTen(),
                finalUnitPrice,
                product.getHinhAnh(),
                quantity,
                selectedSizeName,
                selectedIceName,
                selectedSugarName,
                note, // <--- Truyền ghi chú vào đây
                false,
                false
        );

        String cartItemIdBase = product.getId() + "_" + selectedSizeName;
        // Thêm note.hashCode() vào ID để phân biệt item nếu chỉ khác ghi chú
        String finalCartItemId = cartItemIdBase + "_" + selectedIceName + "_" + selectedSugarName + "_" + note.hashCode() + "_" + System.currentTimeMillis();
        DocumentReference cartItemRef = db.collection("users").document(userId).collection("cart").document(finalCartItemId);

        // Luôn tạo item mới (bao gồm cả note) thay vì kiểm tra trùng lặp phức tạp
        cartItemRef.set(newItem)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                    dismiss();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi khi thêm vào giỏ hàng", e);
                    Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchHappyHourInfo() {
        Log.d(TAG,"Fetching happy hour info...");
        if (product.getHappyHourId() == null || product.getHappyHourId().isEmpty()) {
            Log.d(TAG,"No Happy Hour ID for this product.");
            updateTotalPrice();
            return;
        }

        Log.d(TAG,"Happy Hour ID found: " + product.getHappyHourId());
        db.collection("HappyHours").document(product.getHappyHourId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Log.d(TAG,"Happy Hour document found.");
                        HappyHour hh = documentSnapshot.toObject(HappyHour.class);
                        if (hh != null && hh.isDangBat()) {
                            Log.d(TAG,"Happy Hour is active (dangBat=true).");
                            int currentHour = getCurrentHour();
                            Log.d(TAG,"Current hour: " + currentHour + ", Start: " + hh.getGioBatDau() + ", End: " + hh.getGioKetThuc());
                            if (currentHour >= hh.getGioBatDau() && currentHour < hh.getGioKetThuc()) {
                                Log.d(TAG,"Currently within Happy Hour!");
                                isHappyHourActive = true;
                                happyHourDiscountPercent = hh.getPhanTramGiamGia();
                            } else {
                                Log.d(TAG,"Not within Happy Hour time range.");
                            }
                        } else {
                            Log.d(TAG,"Happy Hour is not active (dangBat=false or hh is null).");
                        }
                    } else {
                        Log.d(TAG,"Happy Hour document not found for ID: " + product.getHappyHourId());
                    }
                    updateTotalPrice();
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error loading HappyHour", e);
                    updateTotalPrice();
                });
    }

    private int getCurrentHour() {
        return Calendar.getInstance().get(Calendar.HOUR_OF_DAY); // 0-23
    }

}

