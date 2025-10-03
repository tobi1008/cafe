package com.example.cafe;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CartItemDao {

    @Query("SELECT * FROM cart_items")
    List<CartItem> getAllCartItems();

    @Query("SELECT * FROM cart_items WHERE productId = :productId")
    CartItem getCartItemByProductId(int productId);

    // OnConflictStrategy.REPLACE nghĩa là nếu thêm một món đã có, nó sẽ được thay thế
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CartItem cartItem);

    @Update
    void update(CartItem cartItem);

    @Delete
    void delete(CartItem cartItem);

    @Query("DELETE FROM cart_items")
    void deleteAllItems();
}
