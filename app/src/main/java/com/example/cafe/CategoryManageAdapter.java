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
import java.util.Locale;

public class CategoryManageAdapter extends RecyclerView.Adapter<CategoryManageAdapter.ViewHolder> {

    private Context context;
    private List<Category> categoryList;

    private OnCategoryManageListener listener;

    public interface OnCategoryManageListener {
        void onEditClick(Category category);
        void onDeleteClick(Category category);
    }

    public CategoryManageAdapter(Context context, List<Category> categoryList, OnCategoryManageListener listener) {
        this.context = context;
        this.categoryList = categoryList;
        this.listener = listener;
    }
    // *** KẾT THÚC SỬA INTERFACE ***

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

        // *** THÊM: Hiển thị Thứ tự ưu tiên ***
        holder.tvPriority.setText(String.format(Locale.US, "Ưu tiên: %d", category.getThuTuUuTien()));

        // *** SỬA LẠI TÊN HÀM GỌI LISTENER ***
        holder.btnEdit.setOnClickListener(v -> listener.onEditClick(category));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(category));
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // *** THÊM: TextView cho Thứ tự ***
        TextView tvName, tvPriority;
        ImageView btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCategoryManageName);
            // *** THÊM: Ánh xạ TextView Thứ tự ***
            tvPriority = itemView.findViewById(R.id.tvCategoryManagePriority);
            btnEdit = itemView.findViewById(R.id.ivEditCategory);
            btnDelete = itemView.findViewById(R.id.ivDeleteCategory);
        }
    }
}

