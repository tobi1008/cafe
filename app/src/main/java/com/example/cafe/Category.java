package com.example.cafe;

import java.io.Serializable;

public class Category implements Serializable {
    private String id;
    private String tenDanhMuc;
    private int thuTuUuTien;

    private String happyHourId;
    private String happyHourName;

    public Category() {
    }

    public Category(String id, String tenDanhMuc, int thuTuUuTien) {
        this.id = id;
        this.tenDanhMuc = tenDanhMuc;
        this.thuTuUuTien = thuTuUuTien;
        this.happyHourId = null; // Mặc định
        this.happyHourName = null; // Mặc định
    }

    public Category(String id, String tenDanhMuc, int thuTuUuTien, String happyHourId, String happyHourName) {
        this.id = id;
        this.tenDanhMuc = tenDanhMuc;
        this.thuTuUuTien = thuTuUuTien;
        this.happyHourId = happyHourId;
        this.happyHourName = happyHourName;
    }


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
    public int getThuTuUuTien() {
        return thuTuUuTien;
    }
    public void setThuTuUuTien(int thuTuUuTien) {
        this.thuTuUuTien = thuTuUuTien;
    }

    public String getHappyHourId() {
        return happyHourId;
    }
    public void setHappyHourId(String happyHourId) {
        this.happyHourId = happyHourId;
    }
    public String getHappyHourName() {
        return happyHourName;
    }
    public void setHappyHourName(String happyHourName) {
        this.happyHourName = happyHourName;
    }
}
