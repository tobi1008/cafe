package com.example.cafe.adapter;

import com.example.cafe.model.*;
import com.example.cafe.R;
import com.example.cafe.ui.product.*;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.ImageButton;
import android.widget.ImageView;
import com.bumptech.glide.Glide;

import java.util.List;

public class ManageProductsAdapter extends RecyclerView.Adapter<ManageProductsAdapter.ViewHolder> {

    private Context context;
    private List<Product> productList;
    private OnEditClickListener onEditClickListener;
    private OnDeleteClickListener onDeleteClickListener;

    public interface OnEditClickListener {
        void onEditClick(Product product);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(Product product);
    }

    public ManageProductsAdapter(Context context, List<Product> productList, OnEditClickListener onEditClickListener,
            OnDeleteClickListener onDeleteClickListener) {
        this.context = context;
        this.productList = productList;
        this.onEditClickListener = onEditClickListener;
        this.onDeleteClickListener = onDeleteClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // *** Đảm bảo file layout này khớp (item_manage_product.xml) ***
        View view = LayoutInflater.from(context).inflate(R.layout.item_manage_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = productList.get(position);

        // *** Hiển thị Tên và Danh mục ***
        holder.productName.setText(product.getTen());
        if (product.getCategory() != null && !product.getCategory().isEmpty()) {
            holder.tvProductManageCategory.setText(product.getCategory());
        } else {
            holder.tvProductManageCategory.setText("Không có danh mục");
        }

        Glide.with(context)
                .load(product.getHinhAnh())
                .placeholder(R.drawable.placeholder_image) // Ảnh giữ chỗ
                .error(R.drawable.placeholder_image) // Ảnh khi lỗi
                .into(holder.ivProductManageImage);

        // Gán sự kiện (vẫn như cũ)
        holder.editButton.setOnClickListener(v -> {
            if (onEditClickListener != null) {
                onEditClickListener.onEditClick(product);
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (onDeleteClickListener != null) {
                onDeleteClickListener.onDeleteClick(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // *** Ảnh và Danh mục ***
        ImageView ivProductManageImage;
        TextView productName, tvProductManageCategory;

        ImageButton editButton, deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ các views mới
            ivProductManageImage = itemView.findViewById(R.id.ivProductManageImage);
            productName = itemView.findViewById(R.id.textViewProductName);
            tvProductManageCategory = itemView.findViewById(R.id.tvProductManageCategory);

            editButton = itemView.findViewById(R.id.buttonEditProduct);
            deleteButton = itemView.findViewById(R.id.buttonDeleteProduct);
        }
    }
}
