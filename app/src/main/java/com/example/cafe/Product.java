package com.example.cafe;

public class Product {
    private String id; // <-- THÊM DÒNG NÀY
    private String ten;
    private double gia;
    private String moTa;
    private String hinhAnh;

    public Product() {
        // Constructor rỗng cần thiết cho Firestore
    }

    // Constructor để tạo sản phẩm mới (id ban đầu là null)
    public Product(String ten, double gia, String moTa, String hinhAnh) {
        this.id = null; // ID sẽ được tạo và cập nhật sau
        this.ten = ten;
        this.gia = gia;
        this.moTa = moTa;
        this.hinhAnh = hinhAnh;
    }

    // Getters
    public String getId() { return id; } // <-- THÊM HÀM NÀY
    public String getTen() { return ten; }
    public double getGia() { return gia; }
    public String getMoTa() { return moTa; }
    public String getHinhAnh() { return hinhAnh; }

    // Setters
    public void setId(String id) { this.id = id; } // <-- THÊM HÀM NÀY
}

