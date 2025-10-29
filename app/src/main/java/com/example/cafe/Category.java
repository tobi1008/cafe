package com.example.cafe;

import java.io.Serializable;


public class Category implements Serializable {
    private String id;
    private String tenDanhMuc;

    // Constructor rỗng cần thiết cho Firebase
    public Category() {
    }

    // Constructor để tạo đối tượng trong code
    public Category(String id, String tenDanhMuc) {
        this.id = id;
        this.tenDanhMuc = tenDanhMuc;
    }

    // Getters and Setters
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
}
