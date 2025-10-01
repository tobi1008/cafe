package com.example.cafe;

import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static CartManager instance;
    private final List<CartItem> cartItems = new ArrayList<>();

    // Constructor private để không ai tạo mới được
    private CartManager() {}

    // Phương thức để lấy instance duy nhất của CartManager
    public static synchronized CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    // PHƯƠNG THỨC BỊ THIẾU LÀ ĐÂY
    public void addProduct(Product product) {
        // Kiểm tra xem sản phẩm đã có trong giỏ chưa
        for (CartItem item : cartItems) {
            if (item.getProduct().getTen().equals(product.getTen())) {
                // Nếu có rồi thì chỉ tăng số lượng
                item.setQuantity(item.getQuantity() + 1);
                return; // Kết thúc
            }
        }
        // Nếu chưa có thì thêm mới vào giỏ với số lượng là 1
        cartItems.add(new CartItem(product, 1));
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public double getTotalPrice() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getProduct().getGia() * item.getQuantity();
        }
        return total;
    }

    public void clearCart() {
        cartItems.clear();
    }
}

