package com.example.cafe;

import java.io.Serializable;
import java.util.Map;

public class Product implements Serializable {

    private String id;
    private String ten;
    private Map<String, Double> gia;
    private String moTa;
    private String hinhAnh;
    private int phanTramGiamGia;
    private String category;

    public Product() {
        // Constructor rỗng cần thiết cho Firebase
    }

    // THÊM CONSTRUCTOR MỚI: Dùng để tạo sản phẩm từ màn hình Admin
    public Product(String id, String ten, Map<String, Double> gia, String moTa, String hinhAnh, int phanTramGiamGia, String category) {
        this.id = id;
        this.ten = ten;
        this.gia = gia;
        this.moTa = moTa;
        this.hinhAnh = hinhAnh;
        this.phanTramGiamGia = phanTramGiamGia;
        this.category = category;
    }

    // --- Getters & Setters ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTen() { return ten; }
    public void setTen(String ten) { this.ten = ten; }
    public Map<String, Double> getGia() { return gia; }
    public void setGia(Map<String, Double> gia) { this.gia = gia; }
    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
    public String getHinhAnh() { return hinhAnh; }
    public void setHinhAnh(String hinhAnh) { this.hinhAnh = hinhAnh; }
    public int getPhanTramGiamGia() { return phanTramGiamGia; }
    public void setPhanTramGiamGia(int phanTramGiamGia) { this.phanTramGiamGia = phanTramGiamGia; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    // --- Các hàm tiện ích ---
    public double getPriceForSize(String size) {
        if (gia != null && gia.containsKey(size)) {
            Object priceObject = gia.get(size);
            if (priceObject instanceof Number) {
                return ((Number) priceObject).doubleValue();
            }
        }
        return 0;
    }

    public double getFinalPriceForSize(String size) {
        double originalPrice = getPriceForSize(size);
        if (phanTramGiamGia > 0 && phanTramGiamGia <= 100) {
            return originalPrice * (1 - (phanTramGiamGia / 100.0));
        }
        return originalPrice;
    }
}

