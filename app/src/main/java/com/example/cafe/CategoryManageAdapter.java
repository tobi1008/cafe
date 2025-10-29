package com.example.cafe;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CategoryManageAdapter extends RecyclerView.Adapter<CategoryManageAdapter.ViewHolder> {

    private Context context;
    private List<Category> categoryList;
    private OnCategoryManageListener listener;

    public interface OnCategoryManageListener {
        void onEditCategoryClick(Category category);
        void onDeleteCategoryClick(Category category);
    }

    public CategoryManageAdapter(Context context, List<Category> categoryList, OnCategoryManageListener listener) {
        this.context = context;
        this.categoryList = categoryList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category_manage, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = categoryList.get(position);
        holder.tvName.setText(category.getTenDanhMuc());

        holder.btnEdit.setOnClickListener(v -> listener.onEditCategoryClick(category));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteCategoryClick(category));
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        ImageView btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCategoryManageName);
            btnEdit = itemView.findViewById(R.id.ivEditCategory);
            btnDelete = itemView.findViewById(R.id.ivDeleteCategory);
        }
    }
}

