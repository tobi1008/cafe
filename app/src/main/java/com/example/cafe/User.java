package com.example.cafe;

import java.util.List;

public class User {
    private String email;
    private String name;
    private String phone;
    private String address;
    private String role;
    private List<String> favoriteProductIds;

    // ---  TRƯỜNG CHO HẠNG THÀNH VIÊN ---
    private double totalSpending;
    private String memberTier;

    public User() {
    }

    public User(String email) {
        this.email = email;
        this.role = "user";
        this.totalSpending = 0;
        this.memberTier = "Đồng";
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public List<String> getFavoriteProductIds() { return favoriteProductIds; }
    public void setFavoriteProductIds(List<String> favoriteProductIds) { this.favoriteProductIds = favoriteProductIds; }

    public double getTotalSpending() { return totalSpending; }
    public void setTotalSpending(double totalSpending) { this.totalSpending = totalSpending; }
    public String getMemberTier() { return memberTier; }
    public void setMemberTier(String memberTier) { this.memberTier = memberTier; }
}

