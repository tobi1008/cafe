package com.example.cafe;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide; // Import thư viện Glide
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> productList;

    public ProductAdapter(List<Product> productList) {
        this.productList = productList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        // Cập nhật để dùng các getter mới từ Product.java
        holder.productNameTextView.setText(product.getTen());
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        holder.productPriceTextView.setText(formatter.format(product.getGia()));

        // Dùng Glide để tải ảnh từ URL
        Glide.with(holder.itemView.getContext())
                .load(product.getHinhAnh()) // Lấy URL từ product
                .placeholder(R.drawable.ic_placeholder) // Ảnh hiển thị trong lúc chờ tải
                .into(holder.productImageView); // Nơi hiển thị ảnh

        holder.addButton.setOnClickListener(v -> {
            CartManager.getInstance().addProduct(product);
            Toast.makeText(v.getContext(), "Đã thêm " + product.getTen(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    // Cập nhật danh sách cho chức năng tìm kiếm
    public void filterList(ArrayList<Product> filteredList) {
        productList = filteredList;
        notifyDataSetChanged();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView productImageView;
        TextView productNameTextView;
        TextView productPriceTextView;
        Button addButton;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImageView = itemView.findViewById(R.id.imageViewProduct);
            productNameTextView = itemView.findViewById(R.id.textViewProductName);
            productPriceTextView = itemView.findViewById(R.id.textViewProductPrice);
            addButton = itemView.findViewById(R.id.buttonAdd);
        }
    }
}

