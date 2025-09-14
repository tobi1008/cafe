package com.example.cafe;

public class Product {
    private String name;
    private double price;
    private int imageResId; // Dùng ID của ảnh trong drawable

    public Product(String name, double price, int imageResId) {
        this.name = name;
        this.price = price;
        this.imageResId = imageResId;
    }

    // Getters
    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getImageResId() {
        return imageResId;
    }
}
