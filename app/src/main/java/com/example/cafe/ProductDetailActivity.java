package com.example.cafe;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class ProductDetailActivity extends AppCompatActivity {

    // --- Khai báo đầy đủ các biến ---
    private ImageView imageViewDetail, imageViewFavorite;
    private TextView textViewDetailName, textViewDetailDescription, textViewDetailPrice, textViewQuantity, textViewNoReviews, textViewReviewCount, textViewViewAllReviews;
    private RatingBar ratingBarAverage;
    private ChipGroup chipGroupSize, chipGroupSugar;
    private RadioGroup radioGroupIce;
    private EditText editTextNote;
    private CheckBox checkboxExtraCoffee, checkboxExtraSugar; // Thêm biến CheckBox
    private Button buttonAddToCartDetail, btnIncrease, btnDecrease, buttonWriteReview;
    private Product product;
    private String selectedSize = "";
    private int quantity = 1;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String userId;
    private boolean isFavorite = false;
    private User currentUserProfile;
    private String selectedIceOption = "Đá chung"; // Mặc định
    private String selectedSugarLevel = "100%"; // Mặc định
    private boolean addExtraCoffee = false;
    private boolean addExtraSugar = false;
    // --- Kết thúc phần khai báo ---

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // Khởi tạo Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Ánh xạ đầy đủ các UI components
        imageViewDetail = findViewById(R.id.imageViewDetail);
        imageViewFavorite = findViewById(R.id.imageViewFavorite);
        textViewDetailName = findViewById(R.id.textViewDetailName);
        textViewDetailDescription = findViewById(R.id.textViewDetailDescription);
        textViewDetailPrice = findViewById(R.id.textViewDetailPrice);
        chipGroupSize = findViewById(R.id.chipGroupSize);
        buttonAddToCartDetail = findViewById(R.id.buttonAddToCartDetail);
        textViewQuantity = findViewById(R.id.textViewQuantityDetail);
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
        }

        // Gán sự kiện click
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

    @Override
    protected void onResume() {
        super.onResume();
        // Tải lại thông tin sản phẩm để cập nhật rating khi quay lại
        if (product != null && product.getId() != null) {
            db.collection("cafe").document(product.getId()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            product = documentSnapshot.toObject(Product.class);
                            updateRatingUI(); // Cập nhật UI rating
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
        Glide.with(this).load(product.getHinhAnh()).placeholder(R.drawable.ic_placeholder).error(R.drawable.ic_placeholder).into(imageViewDetail);

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

            for (String size : sortedSizes) {
                Chip chip = new Chip(this);
                chip.setText(size);
                chip.setCheckable(true);
                chip.setOnClickListener(v -> {
                    selectedSize = chip.getText().toString();
                    updatePrice();
                });
                chipGroupSize.addView(chip);
            }
            if (chipGroupSize.getChildCount() > 0) {
                ((Chip) chipGroupSize.getChildAt(0)).setChecked(true);
                selectedSize = ((Chip) chipGroupSize.getChildAt(0)).getText().toString();
                updatePrice();
            }
        }
        updateRatingUI(); // Hiển thị rating ban đầu
    }

    private void updateRatingUI() {
        if (product != null) {
            ratingBarAverage.setRating((float) product.getAverageRating());
            textViewReviewCount.setText("(" + product.getReviewCount() + " đánh giá)");
        }
    }

    private void setupOptionListeners() {
        radioGroupIce.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton rb = findViewById(checkedId);
            selectedIceOption = rb.getText().toString();
        });

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
    }

    private void setupToppingListeners() {
        checkboxExtraCoffee.setOnCheckedChangeListener((buttonView, isChecked) -> {
            addExtraCoffee = isChecked;
            updatePrice(); // Tính lại giá khi chọn/bỏ chọn
        });
        checkboxExtraSugar.setOnCheckedChangeListener((buttonView, isChecked) -> {
            addExtraSugar = isChecked;
            updatePrice(); // Tính lại giá khi chọn/bỏ chọn
        });
    }


    private void setupQuantityButtons() {
        btnIncrease.setOnClickListener(v -> {
            quantity++;
            textViewQuantity.setText(String.valueOf(quantity));
            updatePrice();
        });

        btnDecrease.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                textViewQuantity.setText(String.valueOf(quantity));
                updatePrice();
            }
        });
    }

    private void updatePrice() {
        if (!selectedSize.isEmpty() && product != null) {
            double singlePrice = product.getFinalPriceForSize(selectedSize); // Giá gốc size + % giảm giá

            // Cộng thêm giá topping nếu được chọn
            if (addExtraCoffee) {
                singlePrice += CartItem.EXTRA_COFFEE_PRICE;
            }
            if (addExtraSugar) {
                singlePrice += CartItem.EXTRA_SUGAR_PRICE;
            }

            double totalPrice = singlePrice * quantity; // Nhân với số lượng
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            textViewDetailPrice.setText(formatter.format(totalPrice));
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

        String note = editTextNote.getText().toString().trim(); // Lấy ghi chú
        String cartItemId = product.getId() + "_" + selectedSize; // ID cơ bản
        DocumentReference cartItemRef = db.collection("users").document(userId).collection("cart").document(cartItemId);

        db.runTransaction(transaction -> {
            CartItem existingItem = transaction.get(cartItemRef).toObject(CartItem.class);

            // Kiểm tra xem có item nào giống hệt (cả size và TẤT CẢ tùy chọn) không
            boolean optionsMatch = existingItem != null &&
                    Objects.equals(existingItem.getSelectedSize(), selectedSize) && // Check size here too
                    Objects.equals(existingItem.getIceOption(), selectedIceOption) &&
                    Objects.equals(existingItem.getSugarLevel(), selectedSugarLevel) &&
                    Objects.equals(existingItem.getNote(), note) &&
                    existingItem.isExtraCoffeeShot() == addExtraCoffee &&
                    existingItem.isExtraSugarPacket() == addExtraSugar;

            if (optionsMatch) {
                // Nếu giống hệt, cập nhật số lượng
                transaction.update(cartItemRef, "quantity", existingItem.getQuantity() + quantity);
            } else {
                // Nếu khác (hoặc chưa có), tạo một item mới hoàn toàn
                // Tạo ID mới để đảm bảo không ghi đè nếu chỉ khác topping
                String newCartItemId = cartItemId + "_" + selectedIceOption + "_" + selectedSugarLevel + "_" + addExtraCoffee + "_" + addExtraSugar + "_" + note.hashCode() + "_" + System.currentTimeMillis();
                DocumentReference newCartItemRef = db.collection("users").document(userId).collection("cart").document(newCartItemId);

                double price = product.getFinalPriceForSize(selectedSize); // Chỉ lấy giá gốc size + % giảm giá
                CartItem newItem = new CartItem(product.getId(), product.getTen(), price, product.getHinhAnh(), quantity, selectedSize, selectedIceOption, selectedSugarLevel, note, addExtraCoffee, addExtraSugar);
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
        newReview.setTimestamp(new Date()); // Sử dụng java.util.Date

        WriteBatch batch = db.batch();
        batch.set(reviewRef, newReview);

        // Cần tải lại product để đảm bảo lấy đúng reviewCount và averageRating mới nhất
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
            return newAvgRating; // Trả về giá trị để cập nhật UI
        }).addOnSuccessListener(newAvgRating -> {
            Toast.makeText(this, "Cảm ơn bạn đã đánh giá!", Toast.LENGTH_SHORT).show();
            // Cập nhật lại UI sau khi gửi
            product.setAverageRating(newAvgRating);
            product.setReviewCount(product.getReviewCount() + 1); // Cập nhật count cục bộ
            updateRatingUI();
            // loadReviews(); // Không cần tải lại ở đây nữa
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
}

