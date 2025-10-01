package com.example.cafe;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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

    public ManageProductsAdapter(Context context, List<Product> productList, OnEditClickListener onEditClickListener, OnDeleteClickListener onDeleteClickListener) {
        this.context = context;
        this.productList = productList;
        this.onEditClickListener = onEditClickListener;
        this.onDeleteClickListener = onDeleteClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_manage_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.productName.setText(product.getTen());
        // The price TextView is likely missing from your item_manage_product.xml, this might be the next error.
        // For now, let's keep it but be aware.
        // holder.productPrice.setText(String.format("%,.0f đ", product.getGia()));

        holder.editButton.setOnClickListener(v -> onEditClickListener.onEditClick(product));
        holder.deleteButton.setOnClickListener(v -> onDeleteClickListener.onDeleteClick(product));
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView productName;
        // TextView productPrice; // This might be null if not in layout
        Button editButton, deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.textViewProductName);
            // productPrice = itemView.findViewById(R.id.textViewProductPrice);
            // Sửa lại ID cho đúng với file item_manage_product.xml
            editButton = itemView.findViewById(R.id.buttonEditProduct);
            deleteButton = itemView.findViewById(R.id.buttonDeleteProduct);
        }
    }
}

