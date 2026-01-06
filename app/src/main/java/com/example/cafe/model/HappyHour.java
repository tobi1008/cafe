package com.example.cafe.model;

import java.io.Serializable;

public class HappyHour implements Serializable {

    private String id;
    private String tenKhungGio;
    private int gioBatDau;
    private int gioKetThuc;
    private int phanTramGiamGia;
    private boolean dangBat; // Field (trường)

    // Constructor rỗng cho Firebase
    public HappyHour() {
    }

    // Constructor đầy đủ
    public HappyHour(String id, String tenKhungGio, int gioBatDau, int gioKetThuc, int phanTramGiamGia, boolean dangBat) {
        this.id = id;
        this.tenKhungGio = tenKhungGio;
        this.gioBatDau = gioBatDau;
        this.gioKetThuc = gioKetThuc;
        this.phanTramGiamGia = phanTramGiamGia;
        this.dangBat = dangBat;
    }

    // --- Getters & Setters ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTenKhungGio() { return tenKhungGio; }
    public void setTenKhungGio(String tenKhungGio) { this.tenKhungGio = tenKhungGio; }

    public int getGioBatDau() { return gioBatDau; }
    public void setGioBatDau(int gioBatDau) { this.gioBatDau = gioBatDau; }

    public int getGioKetThuc() { return gioKetThuc; }
    public void setGioKetThuc(int gioKetThuc) { this.gioKetThuc = gioKetThuc; }

    public int getPhanTramGiamGia() { return phanTramGiamGia; }
    public void setPhanTramGiamGia(int phanTramGiamGia) { this.phanTramGiamGia = phanTramGiamGia; }

    // *** HÀM BỊ THIẾU ĐÂY RỒI (Boolean getter) ***
    public boolean isDangBat() { return dangBat; }
    public void setDangBat(boolean dangBat) { this.dangBat = dangBat; }
}

