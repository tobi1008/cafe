package com.example.cafe;

public class User {
    private String email;
    private String name;
    private String phone;
    private String address;
    private String role; // <-- TRƯỜNG MỚI ĐỂ PHÂN QUYỀN

    public User() {
        // Constructor rỗng cần thiết cho Firestore
    }

    public User(String email) {
        this.email = email;
    }

    // --- Getters & Setters ---
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
}

