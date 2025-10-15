package com.example.cafe;

// CÁC IMPORT CẦN THIẾT
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cafe.Product; // <-- SỬA LẠI IMPORT

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
        TextView productName;
        Button editButton, deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.textViewProductName);
            editButton = itemView.findViewById(R.id.buttonEditProduct);
            deleteButton = itemView.findViewById(R.id.buttonDeleteProduct);
        }
    }
}

