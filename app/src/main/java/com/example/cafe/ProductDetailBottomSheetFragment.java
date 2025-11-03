package com.example.cafe;

import android.app.Dialog;
import android.content.Intent;
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
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
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
    private Button btnContinueSheet;
    private TextView tvHappyHourTagSheet, tvOriginalPriceSheet, tvOriginalPriceHeaderSheet;
    private EditText etNoteSheet;

    // Views Yêu thích & Đánh giá
    private ImageView ivFavoriteSheet;
    private RatingBar ratingBarAverageSheet;
    private TextView tvReviewCountSheet;
    private Button btnWriteReviewSheet;
    private TextView textViewViewAllReviewsSheet;

    // Container Views
    private LinearLayout llStandardOptionsContainer;
    private LinearLayout llComboOptionsContainer;

    // Views Lựa chọn Thường
    private TextView tvSizeLabelSheet, tvIceLabelSheet, tvSugarLabelSheet;
    private RadioGroup rgSizeSheet, rgIceSheet, rgSugarSheet;

    // Data Sản phẩm thường
    private double currentSelectedSizePrice = 0;
    private double currentIcePrice = 0;
    private double currentSugarPrice = 0;
    private String selectedSizeName = "";
    private String selectedIceName = "Đá chung";
    private String selectedSugarName = "100%";
    private boolean isFoodItem = false;

    // Data Chung
    private Product product;
    private int quantity = 1;
    private double basePrice = 0;

    // Biến giảm giá
    private HappyHour activeHappyHour = null;
    private boolean isHappyHourActive = false;
    private int happyHourDiscountPercent = 0;
    private double finalUnitPrice = 0;

    // Biến Yêu thích & Đánh giá
    private boolean isFavorite = false;
    private User currentUserProfile;

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
            loadUserProfile();
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
            setupStandardOptions();
            setupListeners();
            checkCategoryHappyHour();
            checkIfFavorite();
            populateReviewInfo();
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
        btnContinueSheet = view.findViewById(R.id.btnContinueSheet);
        tvHappyHourTagSheet = view.findViewById(R.id.tvHappyHourTagSheet);
        tvOriginalPriceSheet = view.findViewById(R.id.tvOriginalPriceSheet);
        tvOriginalPriceHeaderSheet = view.findViewById(R.id.tvOriginalPriceHeaderSheet);
        etNoteSheet = view.findViewById(R.id.etNoteSheet);

        llStandardOptionsContainer = view.findViewById(R.id.llStandardOptionsContainer);
        llComboOptionsContainer = view.findViewById(R.id.llComboOptionsContainer);

        tvSizeLabelSheet = view.findViewById(R.id.tvSizeLabelSheet);
        rgSizeSheet = view.findViewById(R.id.rgSizeSheet);
        tvIceLabelSheet = view.findViewById(R.id.tvIceLabelSheet);
        rgIceSheet = view.findViewById(R.id.rgIceSheet);
        tvSugarLabelSheet = view.findViewById(R.id.tvSugarLabelSheet);
        rgSugarSheet = view.findViewById(R.id.rgSugarSheet);

        ivFavoriteSheet = view.findViewById(R.id.ivFavoriteSheet);
        ratingBarAverageSheet = view.findViewById(R.id.ratingBarAverageSheet);
        tvReviewCountSheet = view.findViewById(R.id.tvReviewCountSheet);
        btnWriteReviewSheet = view.findViewById(R.id.btnWriteReviewSheet);
        textViewViewAllReviewsSheet = view.findViewById(R.id.textViewViewAllReviewsSheet);
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
        } else if (prices != null && prices.containsKey("S")) {
            basePrice = product.getPriceForSize("S");
            selectedSizeName = "S";
        } else if (prices != null && !prices.isEmpty()) {
            Map.Entry<String, Double> entry = prices.entrySet().iterator().next();
            selectedSizeName = entry.getKey();
            basePrice = product.getPriceForSize(selectedSizeName);
        } else {
            basePrice = 0;
            selectedSizeName = "M";
        }

        if (prices != null && prices.size() == 1) {
            selectedSizeName = prices.keySet().iterator().next();
            basePrice = product.getPriceForSize(selectedSizeName);
        }

        currentSelectedSizePrice = basePrice;

        tvProductPriceSheet.setText(formatter.format(basePrice));
        tvOriginalPriceHeaderSheet.setVisibility(View.GONE);
    }

    private void setupStandardOptions() {
        rgSizeSheet.removeAllViews();
        Map<String, Double> sizes = product.getGia();
        String categoryName = (product.getCategory() != null) ? product.getCategory() : "";

        isFoodItem = categoryName.equalsIgnoreCase("Bánh Ngọt") ||
                categoryName.equalsIgnoreCase("Bánh & Đồ ăn nhẹ") ||
                categoryName.equalsIgnoreCase("Combo") ||
                categoryName.equalsIgnoreCase("Sản phẩm đóng gói");

        // Ẩn/Hiện Size
        if (isFoodItem || sizes == null || sizes.size() <= 1) {
            tvSizeLabelSheet.setVisibility(View.GONE);
            rgSizeSheet.setVisibility(View.GONE);
            if (sizes != null && sizes.size() == 1) {
                selectedSizeName = sizes.keySet().iterator().next();
                currentSelectedSizePrice = product.getPriceForSize(selectedSizeName);
            }
        } else {
            tvSizeLabelSheet.setVisibility(View.VISIBLE);
            rgSizeSheet.setVisibility(View.VISIBLE);
            List<String> sortedSizes = new ArrayList<>(sizes.keySet());
            sortedSizes.sort((s1, s2) -> {
                if (s1.equals("S")) return -1;
                if (s1.equals("M") && s2.equals("L")) return -1;
                if (s1.equals("L")) return 1;
                return 0;
            });
            for (String size : sortedSizes) {
                double price = product.getPriceForSize(size);
                double priceDifference = price - basePrice;
                addRadioButton(rgSizeSheet, size, priceDifference, size.equals(selectedSizeName));
            }
        }

        // Ẩn/Hiện Đá và Đường
        if (isFoodItem) {
            tvIceLabelSheet.setVisibility(View.GONE);
            rgIceSheet.setVisibility(View.GONE);
            tvSugarLabelSheet.setVisibility(View.GONE);
            rgSugarSheet.setVisibility(View.GONE);
            selectedIceName = "";
            selectedSugarName = "";
            currentIcePrice = 0;
            currentSugarPrice = 0;
        } else {
            tvIceLabelSheet.setVisibility(View.VISIBLE);
            rgIceSheet.setVisibility(View.VISIBLE);
            tvSugarLabelSheet.setVisibility(View.VISIBLE);
            rgSugarSheet.setVisibility(View.VISIBLE);
            rgIceSheet.removeAllViews();
            addRadioButton(rgIceSheet, "Đá chung", 0, true);
            addRadioButton(rgIceSheet, "Đá riêng", 0, false);
            rgSugarSheet.removeAllViews();
            List<String> sugarLevels = Arrays.asList("100%", "80%", "50%");
            for (String level : sugarLevels) {
                addRadioButton(rgSugarSheet, level, 0, level.equals(selectedSugarName));
            }
        }
    }

    private void addRadioButton(RadioGroup radioGroup, String text, double priceDifference, boolean isChecked) {
        if (getContext() == null) return;
        RadioButton radioButton = (RadioButton) LayoutInflater.from(getContext()).inflate(R.layout.radio_button_option, radioGroup, false);
        String displayText = text;
        if (radioGroup.getId() == R.id.rgSizeSheet && product.getGia() != null && product.getGia().size() > 1) {
            if (priceDifference > 0) {
                displayText += " (+" + formatPrice(priceDifference) + ")";
            } else if (priceDifference < 0) {
                displayText += " (" + formatPrice(priceDifference) + ")";
            }
            radioButton.setTag(product.getPriceForSize(text));
        } else {
            if (radioGroup.getId() == R.id.rgSizeSheet && product.getGia() != null && product.getGia().containsKey(text)) {
                radioButton.setTag(product.getPriceForSize(text));
            } else {
                radioButton.setTag(0.0);
            }
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

        ivFavoriteSheet.setOnClickListener(v -> toggleFavorite());
        btnWriteReviewSheet.setOnClickListener(v -> showWriteReviewDialog());

        textViewViewAllReviewsSheet.setOnClickListener(v -> {
            if (product != null && product.getId() != null) {
                if (getContext() == null) return;
                Intent intent = new Intent(getContext(), AllReviewsActivity.class);
                intent.putExtra("PRODUCT_ID", product.getId());
                startActivity(intent);
            }
        });
    }

    private void updateTotalPrice() {
        double originalUnitPrice;
        originalUnitPrice = currentSelectedSizePrice + currentIcePrice + currentSugarPrice;
        double discountedUnitPrice;
        boolean isDiscounted = false;

        if (isHappyHourActive) {
            double baseDiscountedPrice = currentSelectedSizePrice * (1 - (happyHourDiscountPercent / 100.0));
            discountedUnitPrice = baseDiscountedPrice + currentIcePrice + currentSugarPrice;
            isDiscounted = true;
            tvHappyHourTagSheet.setText(String.format(Locale.getDefault(), "🔥 -%d %% sale giờ vàng", happyHourDiscountPercent));
            tvHappyHourTagSheet.setVisibility(View.VISIBLE);

        } else if (product.getPhanTramGiamGia() > 0) {
            double discountedSizePrice = currentSelectedSizePrice * (1 - (product.getPhanTramGiamGia() / 100.0));
            discountedUnitPrice = discountedSizePrice + currentIcePrice + currentSugarPrice;
            isDiscounted = true;

            tvHappyHourTagSheet.setText(String.format(Locale.getDefault(), "-%d%%", (int) product.getPhanTramGiamGia()));
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
            double headerOriginalPrice = currentSelectedSizePrice;
            double headerDiscountedPrice = finalUnitPrice - currentIcePrice - currentSugarPrice;
            tvOriginalPriceHeaderSheet.setText(formatter.format(headerOriginalPrice));
            tvOriginalPriceHeaderSheet.setPaintFlags(tvOriginalPriceHeaderSheet.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            tvOriginalPriceHeaderSheet.setVisibility(View.VISIBLE);
            tvProductPriceSheet.setText(formatPrice(headerDiscountedPrice));
        } else {
            tvOriginalPriceSheet.setVisibility(View.GONE);
            tvOriginalPriceHeaderSheet.setVisibility(View.GONE);
            double headerPrice = currentSelectedSizePrice;
            tvProductPriceSheet.setText(formatPrice(headerPrice));
        }
    }

    private String formatPrice(double price) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return formatter.format(price);
    }

    private void addToCart() {
        if (userId == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }
        String note = etNoteSheet.getText().toString().trim();
        String options = "";
        String cartSize = "";
        cartSize = selectedSizeName;
        options = selectedIceName + ", " + selectedSugarName;
        if (isFoodItem) {
            options = "";
            selectedIceName = "";
            selectedSugarName = "";
        }
        if (cartSize.isEmpty()) {
            Toast.makeText(getContext(), "Lỗi: Không thể xác định size sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }
        CartItem newItem = new CartItem(
                product.getId(),
                product.getTen(),
                finalUnitPrice,
                product.getHinhAnh(),
                quantity,
                cartSize,
                selectedIceName,
                selectedSugarName,
                note,
                false,
                false
        );
        String finalCartItemId = product.getId() + "_" + cartSize + "_" + selectedIceName + "_" + selectedSugarName + "_" + note.hashCode();
        DocumentReference cartItemRef = db.collection("users").document(userId).collection("cart").document(finalCartItemId);
        db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(cartItemRef);
            if (snapshot.exists()) {
                CartItem existingItem = snapshot.toObject(CartItem.class);
                if (existingItem != null) {
                    transaction.update(cartItemRef, "quantity", existingItem.getQuantity() + quantity);
                }
            } else {
                transaction.set(cartItemRef, newItem);
            }
            return null;
        }).addOnSuccessListener(aVoid -> {
            Toast.makeText(getContext(), "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
            dismiss();
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Lỗi khi thêm vào giỏ hàng", e);
            Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void checkCategoryHappyHour() {
        if (product != null && product.getHappyHourId() != null && !product.getHappyHourId().isEmpty()) {
            Log.d(TAG, "Sản phẩm có HHId riêng, đang tải: " + product.getHappyHourId());
            fetchHappyHourInfo(product.getHappyHourId());
            return;
        }
        if (product != null && product.getCategory() != null && !product.getCategory().isEmpty()) {
            Log.d(TAG, "Sản phẩm không có HHId, đang kiểm tra danh mục: " + product.getCategory());
            db.collection("Categories")
                    .whereEqualTo("tenDanhMuc", product.getCategory())
                    .limit(1)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        String categoryHappyHourId = null;
                        if (!queryDocumentSnapshots.isEmpty()) {
                            Category cat = queryDocumentSnapshots.getDocuments().get(0).toObject(Category.class);
                            if (cat != null && cat.getHappyHourId() != null && !cat.getHappyHourId().isEmpty()) {
                                Log.d(TAG, "Tìm thấy HHId của danh mục: " + cat.getHappyHourId());
                                categoryHappyHourId = cat.getHappyHourId();
                            } else {
                                Log.d(TAG, "Danh mục không có HHId.");
                            }
                        } else {
                            Log.w(TAG, "Không tìm thấy danh mục: " + product.getCategory());
                        }
                        fetchHappyHourInfo(categoryHappyHourId);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Lỗi khi tải danh mục, bỏ qua HH của danh mục", e);
                        fetchHappyHourInfo(null);
                    });
        } else {
            Log.d(TAG, "Sản phẩm không có HHId và Category, bỏ qua.");
            fetchHappyHourInfo(null);
        }
    }

    private void fetchHappyHourInfo(String happyHourId) {
        Log.d(TAG,"Fetching happy hour info...");
        if (happyHourId == null || happyHourId.isEmpty()) {
            Log.d(TAG,"No Happy Hour ID for this product.");
            updateTotalPrice();
            return;
        }
        Log.d(TAG,"Happy Hour ID found: " + happyHourId);
        db.collection("HappyHours").document(happyHourId)
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
                        Log.d(TAG,"Happy Hour document not found for ID: " + happyHourId);
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

    private int dpToPx(int dp) {
        if (getContext() == null) return dp;
        return (int) (dp * getContext().getResources().getDisplayMetrics().density);
    }

    private void loadUserProfile() {
        if (userId == null) return;
        db.collection("users").document(userId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                currentUserProfile = doc.toObject(User.class);
            }
        });
    }

    private void checkIfFavorite() {
        if (userId == null || product == null || product.getId() == null) {
            if (ivFavoriteSheet != null) ivFavoriteSheet.setVisibility(View.GONE);
            return;
        }
        ivFavoriteSheet.setVisibility(View.VISIBLE);
        db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                User user = documentSnapshot.toObject(User.class);
                if (user != null && user.getFavoriteProductIds() != null && user.getFavoriteProductIds().contains(product.getId())) {
                    isFavorite = true;
                    ivFavoriteSheet.setImageResource(R.drawable.ic_favorite_filled);
                } else {
                    isFavorite = false;
                    ivFavoriteSheet.setImageResource(R.drawable.ic_favorite_border);
                }
            } else {
                isFavorite = false;
                ivFavoriteSheet.setImageResource(R.drawable.ic_favorite_border);
            }
        }).addOnFailureListener(e -> {
            isFavorite = false;
            ivFavoriteSheet.setImageResource(R.drawable.ic_favorite_border);
        });
    }

    private void toggleFavorite() {
        if (userId == null || product == null || product.getId() == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }
        DocumentReference userRef = db.collection("users").document(userId);
        if (isFavorite) {
            userRef.update("favoriteProductIds", FieldValue.arrayRemove(product.getId()));
            ivFavoriteSheet.setImageResource(R.drawable.ic_favorite_border);
            isFavorite = false;
            Toast.makeText(getContext(), "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show();
        } else {
            userRef.update("favoriteProductIds", FieldValue.arrayUnion(product.getId()));
            ivFavoriteSheet.setImageResource(R.drawable.ic_favorite_filled);
            isFavorite = true;
            Toast.makeText(getContext(), "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
        }
    }


    private void populateReviewInfo() {
        if(product != null) {
            ratingBarAverageSheet.setRating((float) product.getAverageRating());
            tvReviewCountSheet.setText("(" + product.getReviewCount() + " đánh giá)");

            if (product.getReviewCount() > 0) {
                textViewViewAllReviewsSheet.setVisibility(View.VISIBLE);
            } else {
                textViewViewAllReviewsSheet.setVisibility(View.GONE);
            }
        }
    }

    private void showWriteReviewDialog() {
        if (userId == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập để đánh giá", Toast.LENGTH_SHORT).show();
            return;
        }
        if (getContext() == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_write_review, null);
        builder.setView(dialogView);
        final RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);
        final EditText editTextComment = dialogView.findViewById(R.id.editTextComment);
        builder.setPositiveButton("Gửi", (dialog, which) -> {
            float rating = ratingBar.getRating();
            String comment = editTextComment.getText().toString().trim();
            if (rating > 0) {
                submitReview(rating, comment);
            } else {
                Toast.makeText(getContext(), "Vui lòng cho điểm sản phẩm", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void submitReview(float rating, String comment) {
        DocumentReference reviewRef = db.collection("reviews").document();
        DocumentReference productRef = db.collection("cafe").document(product.getId());
        Review newReview = new Review();
        newReview.setReviewId(reviewRef.getId());
        newReview.setProductId(product.getId());
        newReview.setUserId(userId);
        String userName = "Anonymous";
        if (currentUserProfile != null && currentUserProfile.getName() != null && !currentUserProfile.getName().isEmpty()) {
            userName = currentUserProfile.getName();
        } else if (mAuth.getCurrentUser() != null && mAuth.getCurrentUser().getEmail() != null) {
            userName = mAuth.getCurrentUser().getEmail().split("@")[0];
        }
        newReview.setUserName(userName);
        newReview.setRating(rating);
        newReview.setComment(comment);
        newReview.setTimestamp(new Date());

        db.runTransaction(transaction -> {
            DocumentSnapshot productSnapshot = transaction.get(productRef);
            Product currentProductData = productSnapshot.toObject(Product.class);
            if (currentProductData == null) {
                throw new RuntimeException("Không tìm thấy sản phẩm để cập nhật đánh giá.");
            }
            double currentAvg = currentProductData.getAverageRating();
            long currentCount = currentProductData.getReviewCount();
            double newAvgRating = ((currentAvg * currentCount) + rating) / (currentCount + 1);
            long newReviewCount = currentCount + 1;
            transaction.set(reviewRef, newReview);
            transaction.update(productRef, "averageRating", newAvgRating);
            transaction.update(productRef, "reviewCount", newReviewCount);
            return newAvgRating;
        }).addOnSuccessListener(newAvgRating -> {
            Toast.makeText(getContext(), "Cảm ơn bạn đã đánh giá!", Toast.LENGTH_SHORT).show();
            product.setAverageRating(newAvgRating);
            product.setReviewCount(product.getReviewCount() + 1);
            populateReviewInfo();
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Gửi đánh giá thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}

