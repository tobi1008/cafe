package com.example.cafe;

import java.io.Serializable;

public class CartItem implements Serializable {
    private String productId;
    private String productName;
    private double price; // Giá gốc của size đã chọn (sau khi trừ % giảm giá nếu có)
    private String productImage;
    private int quantity;
    private String selectedSize;
    private String iceOption;
    private String sugarLevel;
    private String note;

    // --- CÁC TRƯỜỜNG MỚI CHO TOPPING ---
    private boolean extraCoffeeShot;
    private boolean extraSugarPacket;

    public static final double EXTRA_COFFEE_PRICE = 10000;
    public static final double EXTRA_SUGAR_PRICE = 2000;


    public CartItem() {}

    // Cập nhật Constructor
    public CartItem(String productId, String productName, double price, String productImage, int quantity, String selectedSize, String iceOption, String sugarLevel, String note, boolean extraCoffeeShot, boolean extraSugarPacket) {
        this.productId = productId;
        this.productName = productName;
        this.price = price; // Giá gốc của size + % giảm giá
        this.productImage = productImage;
        this.quantity = quantity;
        this.selectedSize = selectedSize;
        this.iceOption = iceOption;
        this.sugarLevel = sugarLevel;
        this.note = note;
        this.extraCoffeeShot = extraCoffeeShot;
        this.extraSugarPacket = extraSugarPacket;
    }

    // --- Getters & Setters ---
    // (Giữ nguyên getters/setters cũ)
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public double getPrice() { return price; } // Giá gốc size + % giảm giá
    public void setPrice(double price) { this.price = price; }
    public String getProductImage() { return productImage; }
    public void setProductImage(String productImage) { this.productImage = productImage; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getSelectedSize() { return selectedSize; }
    public void setSelectedSize(String selectedSize) { this.selectedSize = selectedSize; }
    public String getIceOption() { return iceOption; }
    public void setIceOption(String iceOption) { this.iceOption = iceOption; }
    public String getSugarLevel() { return sugarLevel; }
    public void setSugarLevel(String sugarLevel) { this.sugarLevel = sugarLevel; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    // Getters/Setters cho các trường mới
    public boolean isExtraCoffeeShot() { return extraCoffeeShot; }
    public void setExtraCoffeeShot(boolean extraCoffeeShot) { this.extraCoffeeShot = extraCoffeeShot; }
    public boolean isExtraSugarPacket() { return extraSugarPacket; }
    public void setExtraSugarPacket(boolean extraSugarPacket) { this.extraSugarPacket = extraSugarPacket; }

    // Hàm tiện ích tính tổng tiền cho MỘT item này (bao gồm cả topping)
    public double getTotalItemPrice() {
        double total = price; // Bắt đầu bằng giá gốc của size (đã trừ % giảm giá)
        if (extraCoffeeShot) {
            total += EXTRA_COFFEE_PRICE;
        }
        if (extraSugarPacket) {
            total += EXTRA_SUGAR_PRICE;
        }
        return total * quantity; // Nhân với số lượng
    }

    // Hàm tiện ích lấy mô tả các tùy chọn topping
    public String getToppingDescription() {
        String desc = "";
        if (extraCoffeeShot) {
            desc += "+ Thêm Cà Phê";
        }
        if (extraSugarPacket) {
            if (!desc.isEmpty()) desc += ", ";
            desc += "+ Thêm Đường";
        }
        return desc;
    }
}

