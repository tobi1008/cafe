package com.example.cafe;

import java.io.Serializable;
import java.util.Date;

public class Voucher implements Serializable {
    private String code;
    private String description;
    private String discountType;
    private double discountValue;
    private Date expiryDate;

    //  2 TRƯỜNG  ĐỂ QUẢN LÝ KHO VOUCHER CỦA USER
    private boolean used; // Trạng thái đã dùng (false = chưa dùng)
    private String docId; // Dùng để lưu ID của document trong sub-collection

    public Voucher() {
        // Constructor rỗng
    }

    // --- Getters & Setters cũ ---
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

    // --- Getters & Setters MỚI ---
    public boolean isUsed() { return used; }
    public void setUsed(boolean used) { this.used = used; }
    public String getDocId() { return docId; }
    public void setDocId(String docId) { this.docId = docId; }
}

