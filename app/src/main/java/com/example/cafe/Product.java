package com.example.cafe;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

// Chú thích @Entity báo cho Room biết đây là một bảng trong database
@Entity(tableName = "products")
public class Product {

    // @PrimaryKey đánh dấu đây là khóa chính.
    // autoGenerate = true để Room tự động tạo ID tăng dần cho mỗi sản phẩm mới.
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String ten;
    private double gia;
    private String moTa;
    private String hinhAnh; // Sẽ lưu URL của ảnh

    // Room cần một constructor có đầy đủ các tham số (trừ khóa chính tự tạo)
    // để có thể tạo đối tượng từ dữ liệu trong database.
    public Product(String ten, double gia, String moTa, String hinhAnh) {
        this.ten = ten;
        this.gia = gia;
        this.moTa = moTa;
        this.hinhAnh = hinhAnh;
    }

    // --- Getters ---
    public int getId() { return id; }
    public String getTen() { return ten; }
    public double getGia() { return gia; }
    public String getMoTa() { return moTa; }
    public String getHinhAnh() { return hinhAnh; }

    // --- Setters (Cần thiết cho Room) ---
    public void setId(int id) { this.id = id; }
    public void setTen(String ten) { this.ten = ten; }
    public void setGia(double gia) { this.gia = gia; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
    public void setHinhAnh(String hinhAnh) { this.hinhAnh = hinhAnh; }
}

