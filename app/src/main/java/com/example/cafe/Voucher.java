package com.example.cafe;

import java.util.Date;

public class Voucher {
    private String code; // Mã voucher, cũng sẽ là ID của document
    private String description; // Mô tả (ví dụ: Giảm 10% tối đa 20k)
    private String discountType; // "PERCENT" hoặc "FIXED_AMOUNT"
    private double discountValue; // Giá trị (10 cho 10%, 20000 cho 20k)
    private Date expiryDate; // Ngày hết hạn

    public Voucher() {
        // Constructor rỗng cần thiết cho Firestore
    }

    // --- Getters & Setters ---
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getDiscountType() { return discountType; }
    public void setDiscountType(String discountType) { this.discountType = discountType; }
    public double getDiscountValue() { return discountValue; }
    public void setDiscountValue(double discountValue) { this.discountValue = discountValue; }
    public Date getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Date expiryDate) { this.expiryDate = expiryDate; }
}

