package com.example.cafe;

public class CartItem {
    private Product product;
    private int quantity;

    // Constructor rỗng cần thiết cho Firestore
    public CartItem() {
    }

    // --- SỬA LỖI Ở ĐÂY ---
    // Tạo constructor nhận cả sản phẩm và số lượng
    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void increaseQuantity() {
        this.quantity++;
    }
}

