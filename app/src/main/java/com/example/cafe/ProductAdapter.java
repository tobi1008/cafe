package com.example.cafe;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    private Context context;
    private List<Product> productList;

    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.productName.setText(product.getTen());

        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        holder.productPrice.setText(formatter.format(product.getGia()));

        Glide.with(context)
                .load(product.getHinhAnh())
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .into(holder.productImage);

        // Logic nút thêm vào giỏ hàng
        holder.addButton.setOnClickListener(v -> {
            // SỬA LỖI Ở ĐÂY: Truyền `context` vào khi gọi getInstance()
            CartManager.getInstance(context).addProduct(product);
            Toast.makeText(context, "Đã thêm " + product.getTen(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public void filterList(List<Product> filteredList) {
        this.productList = filteredList;
        notifyDataSetChanged();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName, productPrice;
        Button addButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.imageViewProduct);
            productName = itemView.findViewById(R.id.textViewProductName);
            productPrice = itemView.findViewById(R.id.textViewProductPrice);
            addButton = itemView.findViewById(R.id.buttonAdd);
        }
    }
}

