package com.example.cafe;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class OrderDetailAdapter extends RecyclerView.Adapter<OrderDetailAdapter.ViewHolder> {

    private Context context;
    private List<CartItem> itemList;

    public OrderDetailAdapter(Context context, List<CartItem> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem item = itemList.get(position);

        holder.itemName.setText(item.getProductName());
        holder.itemSizeAndQuantity.setText("Size " + item.getSelectedSize() + " x " + item.getQuantity());

        // Hiển thị tùy chọn Đá và Đường
        String optionsText = "";
        if (item.getIceOption() != null) {
            optionsText += item.getIceOption();
        }
        if (item.getSugarLevel() != null) {
            if (!optionsText.isEmpty()) optionsText += ", ";
            optionsText += item.getSugarLevel();
        }
        // Kiểm tra xem TextView itemOptions có tồn tại không trước khi dùng
        if (holder.itemOptions != null) {
            holder.itemOptions.setText(optionsText);
            holder.itemOptions.setVisibility(optionsText.isEmpty() ? View.GONE : View.VISIBLE);
        }


        // Hiển thị ghi chú
        if (holder.itemNote != null) { // Kiểm tra TextView itemNote
            if (item.getNote() != null && !item.getNote().isEmpty()) {
                holder.itemNote.setText("Ghi chú: " + item.getNote());
                holder.itemNote.setVisibility(View.VISIBLE);
            } else {
                holder.itemNote.setVisibility(View.GONE);
            }
        }


        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        holder.itemPrice.setText(formatter.format(item.getPrice() * item.getQuantity()));

        Glide.with(context)
                .load(item.getProductImage())
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder) // Use placeholder also on error
                .into(holder.itemImage);
    }

    @Override
    public int getItemCount() {
        // Thêm kiểm tra null để tránh lỗi
        return itemList != null ? itemList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView itemImage;
        TextView itemName, itemSizeAndQuantity, itemPrice, itemOptions, itemNote;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.imageViewItem);
            itemName = itemView.findViewById(R.id.textViewItemName);
            itemSizeAndQuantity = itemView.findViewById(R.id.textViewItemSizeAndQuantity);
            itemPrice = itemView.findViewById(R.id.textViewItemPrice);
            // Ánh xạ các TextView mới, kiểm tra lại ID trong item_order_detail.xml nếu cần
            itemOptions = itemView.findViewById(R.id.textViewItemOptions);
            itemNote = itemView.findViewById(R.id.textViewItemNote);
        }
    }
}

