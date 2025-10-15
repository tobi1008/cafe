package com.example.cafe;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.text.NumberFormat;
import java.util.ArrayList;
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

        // Logic hiển thị giá
        if (product.getPhanTramGiamGia() > 0) {
            holder.productPrice.setText(formatter.format(product.getFinalPriceForSize("M"))); // Hiển thị giá size M làm mặc định
            holder.originalPrice.setText(formatter.format(product.getPriceForSize("M")));
            holder.originalPrice.setPaintFlags(holder.originalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.originalPrice.setVisibility(View.VISIBLE);
        } else {
            holder.productPrice.setText(formatter.format(product.getPriceForSize("M")));
            holder.originalPrice.setVisibility(View.GONE);
        }

        Glide.with(context)
                .load(product.getHinhAnh())
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .into(holder.productImage);

        // THAY ĐỔI LOGIC: Nhấn vào toàn bộ item để mở trang chi tiết
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetailActivity.class);
            intent.putExtra("PRODUCT_DETAIL", product); // Truyền cả đối tượng product đi
            context.startActivity(intent);
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

    public List<Product> getCurrentList() {
        // Cần copy list để tránh lỗi ConcurrentModificationException
        return new ArrayList<>(this.productList);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName, productPrice, originalPrice;
        // Đã xóa Button addButton

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.imageViewProduct);
            productName = itemView.findViewById(R.id.textViewProductName);
            productPrice = itemView.findViewById(R.id.textViewProductPrice);
            originalPrice = itemView.findViewById(R.id.textViewOriginalPrice);
            // Đã xóa ánh xạ cho buttonAdd
        }
    }
}

