package com.example.cafe;

import java.io.Serializable;

public class CartItem implements Serializable { // <-- ĐẢM BẢO CÓ IMPLEMENTS SERIALIZABLE
    private String productId;
    private String productName;
    private double price;
    private String productImage;
    private int quantity;
    private String selectedSize;

    public CartItem() {}

    public CartItem(String productId, String productName, double price, String productImage, int quantity, String selectedSize) {
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.productImage = productImage;
        this.quantity = quantity;
        this.selectedSize = selectedSize;
    }

    // --- Getters & Setters ---
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getProductImage() { return productImage; }
    public void setProductImage(String productImage) { this.productImage = productImage; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getSelectedSize() { return selectedSize; }
    public void setSelectedSize(String selectedSize) { this.selectedSize = selectedSize; }
}

