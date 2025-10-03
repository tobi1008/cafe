package com.example.cafe;

import android.content.Context;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class CartManager {

    private static volatile CartManager INSTANCE;
    private AppDatabase appDatabase;
    private ExecutorService executorService;

    private CartManager(Context context) {
        appDatabase = AppDatabase.getDatabase(context.getApplicationContext());
        executorService = Executors.newSingleThreadExecutor();
    }

    public static CartManager getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (CartManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new CartManager(context);
                }
            }
        }
        return INSTANCE;
    }

    public void addProduct(Product product) {
        executorService.execute(() -> {
            CartItem existingItem = appDatabase.cartItemDao().getCartItemByProductId(product.getId());
            if (existingItem != null) {
                // Nếu sản phẩm đã có, tăng số lượng
                existingItem.setQuantity(existingItem.getQuantity() + 1);
                appDatabase.cartItemDao().update(existingItem);
            } else {
                // Nếu chưa có, tạo item mới
                CartItem newItem = new CartItem(product.getId(), product.getTen(), product.getGia(), product.getHinhAnh(), 1);
                appDatabase.cartItemDao().insert(newItem);
            }
        });
    }

    public void updateCartItem(CartItem cartItem) {
        executorService.execute(() -> appDatabase.cartItemDao().update(cartItem));
    }

    public void deleteCartItem(CartItem cartItem) {
        executorService.execute(() -> appDatabase.cartItemDao().delete(cartItem));
    }

    public void clearCart() {
        executorService.execute(() -> appDatabase.cartItemDao().deleteAllItems());
    }

    // Lấy danh sách sản phẩm (hàm này cần trả về kết quả từ luồng nền)
    public Future<List<CartItem>> getCartItems() {
        return executorService.submit(() -> appDatabase.cartItemDao().getAllCartItems());
    }
}

