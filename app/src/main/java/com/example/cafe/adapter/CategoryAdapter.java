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
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private Context context;
    private List<String> categoryList;
    private OnCategoryClickListener listener;
    private int selectedPosition = 0; // Mặc định chọn item đầu tiên

    public interface OnCategoryClickListener {
        void onCategoryClick(String category);
    }

    public CategoryAdapter(Context context, List<String> categoryList, OnCategoryClickListener listener) {
        this.context = context;
        this.categoryList = categoryList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String category = categoryList.get(position);
        holder.categoryName.setText(category);

        // Thay đổi giao diện dựa trên item có được chọn hay không
        if (position == selectedPosition) {
            holder.categoryName.setBackgroundResource(R.drawable.category_selected_background);
            holder.categoryName.setTextColor(context.getResources().getColor(R.color.white));
        } else {
            holder.categoryName.setBackgroundResource(R.drawable.category_unselected_background);
            holder.categoryName.setTextColor(context.getResources().getColor(R.color.colorPrimary));
        }

        holder.itemView.setOnClickListener(v -> {
            listener.onCategoryClick(category);
            int previousPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView categoryName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.textViewCategoryName);
        }
    }
}
