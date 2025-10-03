package com.example.cafe;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface ProductDao {

    @Query("SELECT * FROM products ORDER BY id DESC")
    List<Product> getAllProducts();

    // THÊM HÀM NÀY: Dùng để lấy thông tin chi tiết của 1 sản phẩm cho màn hình Sửa
    @Query("SELECT * FROM products WHERE id = :productId")
    Product getProductById(int productId);

    @Insert
    void insertProduct(Product product);

    @Update
    void updateProduct(Product product);

    @Delete
    void deleteProduct(Product product);
}

