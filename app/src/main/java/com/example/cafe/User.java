package com.example.cafe;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class User implements Serializable {
    private String uid;
    private String email;
    private String name;
    private String phone;
    private String address;
    private String role;
    private List<String> favoriteProductIds = new ArrayList<>();
    private double totalSpending;
    private String memberTier;

    public User() {
        // Constructor rỗng cần thiết cho Firebase
        this.favoriteProductIds = new ArrayList<>();
    }

    // Constructor chuẩn cho user mới
    public User(String email, String name, String phone) {
        this.email = email;
        this.name = name; // LƯU TÊN
        this.phone = phone; // LƯU SĐT
        this.role = "user";
        this.totalSpending = 0;
        this.memberTier = "Thành viên";
        this.favoriteProductIds = new ArrayList<>();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<String> getFavoriteProductIds() {
        if (favoriteProductIds == null) {
            favoriteProductIds = new ArrayList<>();
        }
        return favoriteProductIds;
    }

    public void setFavoriteProductIds(List<String> favoriteProductIds) {
        this.favoriteProductIds = favoriteProductIds;
    }

    public double getTotalSpending() {
        return totalSpending;
    }

    public void setTotalSpending(double totalSpending) {
        this.totalSpending = totalSpending;
    }

    public String getMemberTier() {
        return memberTier;
    }

    public void setMemberTier(String memberTier) {
        this.memberTier = memberTier;
    }
}