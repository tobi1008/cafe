package com.example.cafe;

import java.util.ArrayList;
import java.util.List;

// Lớp này dùng mẫu Singleton để đảm bảo chỉ có một giỏ hàng duy nhất trong toàn ứng dụng
public class CartManager {
    private static CartManager instance;
    private List<CartItem> cartItems;

    private CartManager() {
        cartItems = new ArrayList<>();
    }

    public static synchronized CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public void addItem(Product product) {
        // Kiểm tra xem sản phẩm đã có trong giỏ chưa
        for (CartItem item : cartItems) {
            if (item.getProduct().getName().equals(product.getName())) {
                item.setQuantity(item.getQuantity() + 1); // Nếu có rồi thì tăng số lượng
                return;
            }
        }
        // Nếu chưa có, thêm mới vào giỏ
        cartItems.add(new CartItem(product));
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public void clearCart() {
        cartItems.clear();
    }
}
