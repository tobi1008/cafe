package com.example.cafe.ui.product;

import com.example.cafe.R;
import com.example.cafe.model.*;
import com.example.cafe.adapter.*;
import com.example.cafe.ui.cart.*;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class ProductDetailActivity extends AppCompatActivity {

    private ImageView imageViewDetail, imageViewFavorite;
    private TextView textViewDetailName, textViewDetailDescription, textViewDetailPrice, textViewQuantityDetail, textViewNoReviews, textViewReviewCount, textViewViewAllReviews;
    private RatingBar ratingBarAverage;
    private ChipGroup chipGroupSize, chipGroupSugar;
    private RadioGroup radioGroupIce;
    private EditText editTextNote;
    private CheckBox checkboxExtraCoffee, checkboxExtraSugar;
    private Button buttonAddToCartDetail, btnIncrease, btnDecrease, buttonWriteReview;
    private Product product;
    private String selectedSize = "";
    private int quantity = 1;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String userId;
    private boolean isFavorite = false;
    private User currentUserProfile;
    private String selectedIceOption = "Đá chung";
    private String selectedSugarLevel = "100%";
    private boolean addExtraCoffee = false;
    private boolean addExtraSugar = false;
    private TextView textViewHappyHourTag, textViewOriginalPriceDetail;
    private HappyHour activeHappyHour = null;
    private boolean isHappyHourActive = false;
    private int happyHourDiscountPercent = 0;
    private double finalUnitPrice = 0;

    private LinearLayout layoutIceOptions, layoutSugarOptions;

    private static final String TAG = "ProductDetailActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        imageViewDetail = findViewById(R.id.imageViewDetail);
        imageViewFavorite = findViewById(R.id.imageViewFavorite);
        textViewDetailName = findViewById(R.id.textViewDetailName);
        textViewDetailDescription = findViewById(R.id.textViewDetailDescription);
        textViewDetailPrice = findViewById(R.id.textViewDetailPrice);
        chipGroupSize = findViewById(R.id.chipGroupSize);
        buttonAddToCartDetail = findViewById(R.id.buttonAddToCartDetail);
        textViewQuantityDetail = findViewById(R.id.textViewQuantityDetail);
        btnIncrease = findViewById(R.id.buttonIncreaseQuantity);
        btnDecrease = findViewById(R.id.buttonDecreaseQuantity);
        ratingBarAverage = findViewById(R.id.ratingBarAverage);
        textViewReviewCount = findViewById(R.id.textViewReviewCount);
        textViewViewAllReviews = findViewById(R.id.textViewViewAllReviews);
        buttonWriteReview = findViewById(R.id.buttonWriteReview);
        radioGroupIce = findViewById(R.id.radioGroupIce);
        chipGroupSugar = findViewById(R.id.chipGroupSugar);
        editTextNote = findViewById(R.id.editTextNote);
        checkboxExtraCoffee = findViewById(R.id.checkboxExtraCoffee);
        checkboxExtraSugar = findViewById(R.id.checkboxExtraSugar);
        textViewHappyHourTag = findViewById(R.id.textViewHappyHourTag);
        textViewOriginalPriceDetail = findViewById(R.id.textViewOriginalPriceDetail);

        // ÁNH XẠ MỚI
        layoutIceOptions = findViewById(R.id.layoutIceOptions);
        layoutSugarOptions = findViewById(R.id.layoutSugarOptions);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
            loadUserProfile();
        }

        product = (Product) getIntent().getSerializableExtra("PRODUCT_DETAIL");

        if (product != null) {
            populateUI();
            setupQuantityButtons();
            checkIfFavorite();
            setupOptionListeners();
            setupToppingListeners();
            checkCategoryHappyHour();

            hideOptionsBasedOnCategory();
        }

        buttonAddToCartDetail.setOnClickListener(v -> addToCart());
        imageViewFavorite.setOnClickListener(v -> toggleFavorite());
        buttonWriteReview.setOnClickListener(v -> showWriteReviewDialog());
        textViewViewAllReviews.setOnClickListener(v -> {
            if (product != null && product.getId() != null) {
                Intent intent = new Intent(ProductDetailActivity.this, AllReviewsActivity.class);
                intent.putExtra("PRODUCT_ID", product.getId());
                startActivity(intent);
            }
        });
    }

    // ẨN/HIỆN TÙY CHỌN
    private void hideOptionsBasedOnCategory() {
        if (product == null || product.getCategory() == null) {
            return; // Không có sản phẩm hoặc danh mục, không làm gì cả
        }

        String category = product.getCategory();

        // Kiểm tra xem danh mục có phải là "Combo" hoặc "Sản phẩm đóng gói" không
        if (category.equalsIgnoreCase("Combo") || category.equalsIgnoreCase("Sản phẩm đóng gói")) {
            // Nếu đúng, ẩn các khu vực này đi
            if (layoutIceOptions != null) {
                layoutIceOptions.setVisibility(View.GONE);
            }
            if (layoutSugarOptions != null) {
                layoutSugarOptions.setVisibility(View.GONE);
            }
        } else {
            // Nếu là các danh mục khác (như Cà phê, Trà), phải hiện ra
            if (layoutIceOptions != null) {
                layoutIceOptions.setVisibility(View.VISIBLE);
            }
            if (layoutSugarOptions != null) {
                layoutSugarOptions.setVisibility(View.VISIBLE);
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (product != null && product.getId() != null) {
            db.collection("cafe").document(product.getId()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            product = documentSnapshot.toObject(Product.class);
                            updateRatingUI();
                        }
                    });
        }
    }

    private void loadUserProfile() {
        if (userId == null) return;
        db.collection("users").document(userId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                currentUserProfile = doc.toObject(User.class);
            }
        });
    }

    private void populateUI() {
        textViewDetailName.setText(product.getTen());
        textViewDetailDescription.setText(product.getMoTa());
        Glide.with(this).load(product.getHinhAnh()).placeholder(R.drawable.placeholder_image).error(R.drawable.placeholder_image).into(imageViewDetail);

        chipGroupSize.removeAllViews();
        Map<String, Double> prices = product.getGia();
        if (prices != null) {
            List<String> sortedSizes = new ArrayList<>(prices.keySet());
            sortedSizes.sort((s1, s2) -> {
                if (s1.equals("S")) return -1;
                if (s1.equals("M") && s2.equals("L")) return -1;
                if (s1.equals("L")) return 1;
                return 0;
            });

            String defaultSize = "M";
            if (!sortedSizes.contains("M") && !sortedSizes.isEmpty()) {
                defaultSize = sortedSizes.get(0);
            }

            for (String size : sortedSizes) {
                Chip chip = new Chip(this);
                chip.setText(size);
                chip.setCheckable(true);
                chip.setClickable(true);
                chip.setCheckedIconVisible(true);

                chip.setOnClickListener(v -> {
                    selectedSize = chip.getText().toString();
                    updatePrice();
                });
                chipGroupSize.addView(chip);

                if (size.equals(defaultSize)) {
                    chip.setChecked(true);
                    selectedSize = size;
                }
            }
            if (!selectedSize.isEmpty()) {
                updatePrice();
            }
        }
        updateRatingUI();
    }

    private void updateRatingUI() {
        if (product != null) {
            ratingBarAverage.setRating((float) product.getAverageRating());
            textViewReviewCount.setText("(" + product.getReviewCount() + " đánh giá)");
        }
    }

    private void setupOptionListeners() {
        // Kiểm tra xem layout có bị ẩn không
        if (layoutIceOptions != null && layoutIceOptions.getVisibility() == View.VISIBLE) {
            radioGroupIce.setOnCheckedChangeListener((group, checkedId) -> {
                RadioButton rb = findViewById(checkedId);
                selectedIceOption = rb.getText().toString();
            });
        } else {
            selectedIceOption = "N/A"; // Hoặc giá trị mặc định nếu bị ẩn
        }

        if (layoutSugarOptions != null && layoutSugarOptions.getVisibility() == View.VISIBLE) {
            chipGroupSugar.setOnCheckedChangeListener((group, checkedId) -> {
                Chip chip = findViewById(checkedId);
                if (chip != null) {
                    selectedSugarLevel = chip.getText().toString();
                } else {
                    selectedSugarLevel = "100%";
                    Chip defaultChip = findViewById(R.id.chipSugar100);
                    if (defaultChip != null) defaultChip.setChecked(true);
                }
            });
            if (chipGroupSugar.getCheckedChipId() == View.NO_ID) {
                Chip defaultChip = findViewById(R.id.chipSugar100);
                if (defaultChip != null) defaultChip.setChecked(true);
                selectedSugarLevel = "100%";
            }
        } else {
            selectedSugarLevel = "N/A"; // Hoặc giá trị mặc định nếu bị ẩn
        }
    }

    private void setupToppingListeners() {
        checkboxExtraCoffee.setOnCheckedChangeListener((buttonView, isChecked) -> {
            addExtraCoffee = isChecked;
            updatePrice();
        });
        checkboxExtraSugar.setOnCheckedChangeListener((buttonView, isChecked) -> {
            addExtraSugar = isChecked;
            updatePrice();
        });
    }


    private void setupQuantityButtons() {
        btnIncrease.setOnClickListener(v -> {
            quantity++;
            textViewQuantityDetail.setText(String.valueOf(quantity));
            updatePrice();
        });

        btnDecrease.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                textViewQuantityDetail.setText(String.valueOf(quantity));
                updatePrice();
            }
        });
    }

    private void updatePrice() {
        if (selectedSize.isEmpty() || product == null) return;
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        double basePrice = product.getPriceForSize(selectedSize);
        double singleItemPrice;
        boolean isDiscounted = false;

        if (isHappyHourActive) {
            singleItemPrice = basePrice * (1 - (happyHourDiscountPercent / 100.0));
            isDiscounted = true;
        } else if (product.getPhanTramGiamGia() > 0) {
            singleItemPrice = product.getFinalPriceForSize(selectedSize);
            isDiscounted = true;
        } else {
            singleItemPrice = basePrice;
            isDiscounted = false;
        }

        if (addExtraCoffee) {
            singleItemPrice += CartItem.EXTRA_COFFEE_PRICE;
        }
        if (addExtraSugar) {
            singleItemPrice += CartItem.EXTRA_SUGAR_PRICE;
        }
        finalUnitPrice = singleItemPrice;
        double totalPrice = finalUnitPrice * quantity;
        textViewDetailPrice.setText(formatter.format(totalPrice));

        if (isDiscounted) {
            double originalSingleItemPrice = basePrice;
            if (addExtraCoffee) originalSingleItemPrice += CartItem.EXTRA_COFFEE_PRICE;
            if (addExtraSugar) originalSingleItemPrice += CartItem.EXTRA_SUGAR_PRICE;
            double originalTotalPrice = originalSingleItemPrice * quantity;
            textViewOriginalPriceDetail.setText(formatter.format(originalTotalPrice));
            textViewOriginalPriceDetail.setPaintFlags(textViewOriginalPriceDetail.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            textViewOriginalPriceDetail.setVisibility(View.VISIBLE);
        } else {
            textViewOriginalPriceDetail.setVisibility(View.GONE);
        }
    }

    private void addToCart() {
        if (selectedSize.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn size", Toast.LENGTH_SHORT).show();
            return;
        }
        if (userId == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
            return;
        }
        if (product == null || product.getId() == null) {
            Toast.makeText(this, "Lỗi sản phẩm không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tự động gán N/A nếu các tùy chọn bị ẩn
        if (layoutIceOptions != null && layoutIceOptions.getVisibility() == View.GONE) {
            selectedIceOption = "N/A";
        }
        if (layoutSugarOptions != null && layoutSugarOptions.getVisibility() == View.GONE) {
            selectedSugarLevel = "N/A";
        }

        String note = editTextNote.getText().toString().trim();
        String cartItemId = product.getId() + "_" + selectedSize;
        DocumentReference cartItemRef = db.collection("users").document(userId).collection("cart").document(cartItemId);

        db.runTransaction(transaction -> {
            CartItem existingItem = transaction.get(cartItemRef).toObject(CartItem.class);
            boolean optionsMatch = existingItem != null &&
                    Objects.equals(existingItem.getSelectedSize(), selectedSize) &&
                    Objects.equals(existingItem.getIceOption(), selectedIceOption) &&
                    Objects.equals(existingItem.getSugarLevel(), selectedSugarLevel) &&
                    Objects.equals(existingItem.getNote(), note) &&
                    existingItem.isExtraCoffeeShot() == addExtraCoffee &&
                    existingItem.isExtraSugarPacket() == addExtraSugar;
            if (optionsMatch) {
                transaction.update(cartItemRef, "quantity", existingItem.getQuantity() + quantity);
            } else {
                String newCartItemId = cartItemId + "_" + selectedIceOption + "_" + selectedSugarLevel + "_" + addExtraCoffee + "_" + addExtraSugar + "_" + note.hashCode() + "_" + System.currentTimeMillis();
                DocumentReference newCartItemRef = db.collection("users").document(userId).collection("cart").document(newCartItemId);
                CartItem newItem = new CartItem(product.getId(), product.getTen(), finalUnitPrice, product.getHinhAnh(), quantity, selectedSize, selectedIceOption, selectedSugarLevel, note, addExtraCoffee, addExtraSugar);
                transaction.set(newCartItemRef, newItem);
            }
            return null;
        }).addOnSuccessListener(aVoid -> {
            Toast.makeText(ProductDetailActivity.this, "Đã thêm vào giỏ hàng!", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> {
            Log.e("ProductDetail", "Lỗi khi thêm vào giỏ hàng", e);
            Toast.makeText(ProductDetailActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void showWriteReviewDialog() {
        if (userId == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để đánh giá", Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
                Toast.makeText(this, "Vui lòng cho điểm sản phẩm", Toast.LENGTH_SHORT).show();
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

        WriteBatch batch = db.batch();
        batch.set(reviewRef, newReview);
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
            transaction.update(productRef, "averageRating", newAvgRating);
            transaction.update(productRef, "reviewCount", newReviewCount);
            return newAvgRating;
        }).addOnSuccessListener(newAvgRating -> {
            Toast.makeText(this, "Cảm ơn bạn đã đánh giá!", Toast.LENGTH_SHORT).show();
            product.setAverageRating(newAvgRating);
            product.setReviewCount(product.getReviewCount() + 1);
            updateRatingUI();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Gửi đánh giá thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void checkIfFavorite() {
        if (userId == null || product == null || product.getId() == null) return;
        db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                User user = documentSnapshot.toObject(User.class);
                if (user != null && user.getFavoriteProductIds() != null && user.getFavoriteProductIds().contains(product.getId())) {
                    isFavorite = true;
                    imageViewFavorite.setImageResource(R.drawable.ic_favorite_filled);
                } else {
                    isFavorite = false;
                    imageViewFavorite.setImageResource(R.drawable.ic_favorite_border);
                }
            } else {
                isFavorite = false;
                imageViewFavorite.setImageResource(R.drawable.ic_favorite_border);
            }
        }).addOnFailureListener(e -> {
            isFavorite = false;
            imageViewFavorite.setImageResource(R.drawable.ic_favorite_border);
        });
    }

    private void toggleFavorite() {
        if (userId == null || product == null || product.getId() == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }
        DocumentReference userRef = db.collection("users").document(userId);
        if (isFavorite) {
            userRef.update("favoriteProductIds", FieldValue.arrayRemove(product.getId()));
            imageViewFavorite.setImageResource(R.drawable.ic_favorite_border);
            isFavorite = false;
            Toast.makeText(this, "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show();
        } else {
            userRef.update("favoriteProductIds", FieldValue.arrayUnion(product.getId()));
            imageViewFavorite.setImageResource(R.drawable.ic_favorite_filled);
            isFavorite = true;
            Toast.makeText(this, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
        }
    }


    private void checkCategoryHappyHour() {
        if (product != null && product.getHappyHourId() != null && !product.getHappyHourId().isEmpty()) {
            Log.d(TAG, "Sản phẩm có HHId riêng, đang tải: " + product.getHappyHourId());
            fetchHappyHourInfo();
            return;
        }

        if (product != null && product.getCategory() != null && !product.getCategory().isEmpty()) {
            Log.d(TAG, "Sản phẩm không có HHId, đang kiểm tra danh mục: " + product.getCategory());
            db.collection("Categories")
                    .whereEqualTo("tenDanhMuc", product.getCategory())
                    .limit(1)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            Category cat = queryDocumentSnapshots.getDocuments().get(0).toObject(Category.class);
                            if (cat != null && cat.getHappyHourId() != null && !cat.getHappyHourId().isEmpty()) {
                                Log.d(TAG, "Tìm thấy HHId của danh mục: " + cat.getHappyHourId());
                                product.setHappyHourId(cat.getHappyHourId());
                            } else {
                                Log.d(TAG, "Danh mục không có HHId.");
                            }
                        } else {
                            Log.w(TAG, "Không tìm thấy danh mục: " + product.getCategory());
                        }
                        fetchHappyHourInfo();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Lỗi khi tải danh mục, bỏ qua HH của danh mục", e);
                        fetchHappyHourInfo();
                    });
        } else {
            Log.d(TAG, "Sản phẩm không có HHId và Category, bỏ qua.");
            fetchHappyHourInfo();
        }
    }


    private void fetchHappyHourInfo() {
        if (product.getHappyHourId() == null || product.getHappyHourId().isEmpty()) {
            updatePrice();
            return;
        }

        db.collection("HappyHours").document(product.getHappyHourId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        HappyHour hh = documentSnapshot.toObject(HappyHour.class);
                        if (hh != null && hh.isDangBat()) {
                            int currentHour = getCurrentHour();
                            if (currentHour >= hh.getGioBatDau() && currentHour < hh.getGioKetThuc()) {
                                isHappyHourActive = true;
                                happyHourDiscountPercent = hh.getPhanTramGiamGia();
                                textViewHappyHourTag.setText("🔥 Đang giảm giá Giờ Vàng " + happyHourDiscountPercent + "%");
                                textViewHappyHourTag.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w("ProductDetail", "Lỗi khi tải HappyHour", e);
                })
                .addOnCompleteListener(task -> {
                    updatePrice();
                });
    }

    private int getCurrentHour() {
        return Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    }
}

