package com.example.cafe;

import java.io.Serializable;
// *** ĐÃ XÓA: import java.util.List; ***
import java.util.Map;

public class Product implements Serializable {

    private String id;
    private String ten;
    private Map<String, Double> gia;
    private String moTa;
    private String hinhAnh;
    private int phanTramGiamGia;
    private String category;
    private double averageRating;
    private long reviewCount;
    private String happyHourId;

    // --- ĐÃ XÓA: TRƯỜNG MỚI CHO COMBO ---
    // private List<Map<String, Object>> optionGroups;

    public Product() {
        // Constructor rỗng cần thiết cho Firebase
    }

    // Constructor 7 tham số
    public Product(String id, String ten, Map<String, Double> gia, String moTa, String hinhAnh, int phanTramGiamGia, String category) {
        this(id, ten, gia, moTa, hinhAnh, phanTramGiamGia, category, null);
    }

    // Constructor 8 tham số
    public Product(String id, String ten, Map<String, Double> gia, String moTa, String hinhAnh, int phanTramGiamGia, String category, String happyHourId) {
        this.id = id;
        this.ten = ten;
        this.gia = gia;
        this.moTa = moTa;
        this.hinhAnh = hinhAnh;
        this.phanTramGiamGia = phanTramGiamGia;
        this.category = category;
        this.averageRating = 0;
        this.reviewCount = 0;
        this.happyHourId = happyHourId;
        // this.optionGroups = null; // *** ĐÃ XÓA ***
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
    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }
    public long getReviewCount() { return reviewCount; }
    public void setReviewCount(long reviewCount) { this.reviewCount = reviewCount; }
    public String getHappyHourId() { return happyHourId; }
    public void setHappyHourId(String happyHourId) { this.happyHourId = happyHourId; }

    // --- ĐÃ XÓA: GETTER & SETTER MỚI CHO COMBO ---
    // public List<Map<String, Object>> getOptionGroups() { return optionGroups; }
    // public void setOptionGroups(List<Map<String, Object>> optionGroups) { this.optionGroups = optionGroups; }


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

