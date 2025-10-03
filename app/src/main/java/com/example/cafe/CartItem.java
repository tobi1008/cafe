package com.example.cafe;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

// Chú thích @Entity báo cho Room biết đây là một bảng trong database
// Đặt tên bảng là "cart_items"
@Entity(tableName = "cart_items")
public class CartItem {

    // Chúng ta sẽ dùng chính ID của sản phẩm làm khóa chính
    // để đảm bảo mỗi sản phẩm chỉ xuất hiện 1 lần trong giỏ hàng.
    @PrimaryKey
    private int productId;

    private String productName;
    private double productPrice;
    private String productImage;
    private int quantity;

    // Room cần một constructor rỗng
    public CartItem() {}

    // Constructor để dễ dàng tạo đối tượng
    public CartItem(int productId, String productName, double productPrice, String productImage, int quantity) {
        this.productId = productId;
        this.productName = productName;
        this.productPrice = productPrice;
        this.productImage = productImage;
        this.quantity = quantity;
    }


    // --- Getters ---
    public int getProductId() { return productId; }
    public String getProductName() { return productName; }
    public double getProductPrice() { return productPrice; }
    public String getProductImage() { return productImage; }
    public int getQuantity() { return quantity; }

    // --- Setters ---
    public void setProductId(int productId) { this.productId = productId; }
    public void setProductName(String productName) { this.productName = productName; }
    public void setProductPrice(double productPrice) { this.productPrice = productPrice; }
    public void setProductImage(String productImage) { this.productImage = productImage; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}

