package com.example.cafe;

import java.io.Serializable;

public class Category implements Serializable {
    private String id;
    private String tenDanhMuc;

    // *** TRƯỜNG MỚI ĐỂ SẮP XẾP ***
    private int thuTuUuTien; // Ví dụ: 1, 2, 3...

    public Category() {
        // Constructor rỗng
    }

    // *** CẬP NHẬT HÀM TẠO (3 THAM SỐ) ***
    public Category(String id, String tenDanhMuc, int thuTuUuTien) {
        this.id = id;
        this.tenDanhMuc = tenDanhMuc;
        this.thuTuUuTien = thuTuUuTien;
    }

    // --- Getters & Setters ---
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getTenDanhMuc() {
        return tenDanhMuc;
    }
    public void setTenDanhMuc(String tenDanhMuc) {
        this.tenDanhMuc = tenDanhMuc;
    }

    // *** GETTER & SETTER MỚI ***
    public int getThuTuUuTien() {
        return thuTuUuTien;
    }
    public void setThuTuUuTien(int thuTuUuTien) {
        this.thuTuUuTien = thuTuUuTien;
    }
}

