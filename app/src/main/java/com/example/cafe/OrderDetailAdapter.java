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

        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        holder.itemPrice.setText(formatter.format(item.getPrice() * item.getQuantity()));

        Glide.with(context)
                .load(item.getProductImage())
                .placeholder(R.drawable.ic_placeholder)
                .into(holder.itemImage);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView itemImage;
        TextView itemName, itemSizeAndQuantity, itemPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.imageViewItem);
            itemName = itemView.findViewById(R.id.textViewItemName);
            itemSizeAndQuantity = itemView.findViewById(R.id.textViewItemSizeAndQuantity);
            itemPrice = itemView.findViewById(R.id.textViewItemPrice);
        }
    }
}
