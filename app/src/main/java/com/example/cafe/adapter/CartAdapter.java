package com.example.cafe.adapter;

import com.example.cafe.model.*;
import com.example.cafe.R;
import com.example.cafe.ui.product.*;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    private Context context;
    private List<CartItem> cartItemList;
    private CartItemListener listener;

    public interface CartItemListener {
        void onQuantityChanged(CartItem item);

        void onItemDeleted(CartItem item);
    }

    public CartAdapter(Context context, List<CartItem> cartItemList, CartItemListener listener) {
        this.context = context;
        this.cartItemList = cartItemList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem cartItem = cartItemList.get(position);

        holder.productName.setText(cartItem.getProductName());
        holder.quantity.setText(String.valueOf(cartItem.getQuantity()));

        // Ẩn Size nếu không có
        if (cartItem.getSelectedSize() != null && !cartItem.getSelectedSize().isEmpty()) {
            holder.size.setText("Size: " + cartItem.getSelectedSize());
            holder.size.setVisibility(View.VISIBLE);
        } else {
            holder.size.setVisibility(View.GONE);
        }

        // Hiển thị tùy chọn Đá và Đường (Ẩn nếu không có)
        String optionsText = "";
        if (cartItem.getIceOption() != null && !cartItem.getIceOption().isEmpty()
                && !cartItem.getIceOption().equals("N/A")) {
            optionsText += cartItem.getIceOption();
        }
        if (cartItem.getSugarLevel() != null && !cartItem.getSugarLevel().isEmpty()
                && !cartItem.getSugarLevel().equals("N/A")) {
            if (!optionsText.isEmpty())
                optionsText += ", ";
            optionsText += cartItem.getSugarLevel();
        }

        if (optionsText.isEmpty()) {
            holder.options.setVisibility(View.GONE);
        } else {
            holder.options.setText(optionsText);
            holder.options.setVisibility(View.VISIBLE);
        }

        // Hiển thị ghi chú
        if (cartItem.getNote() != null && !cartItem.getNote().isEmpty()) {
            holder.note.setText("Ghi chú: " + cartItem.getNote());
            holder.note.setVisibility(View.VISIBLE);
        } else {
            holder.note.setVisibility(View.GONE);
        }

        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        // Cập nhật giá theo tổng số lượng
        holder.productPrice.setText(formatter.format(cartItem.getTotalItemPrice()));

        Glide.with(context)
                .load(cartItem.getProductImage())
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .into(holder.productImage);

        holder.btnIncrease.setOnClickListener(v -> {
            cartItem.setQuantity(cartItem.getQuantity() + 1);
            holder.quantity.setText(String.valueOf(cartItem.getQuantity()));
            listener.onQuantityChanged(cartItem);
        });

        holder.btnDecrease.setOnClickListener(v -> {
            if (cartItem.getQuantity() > 1) {
                cartItem.setQuantity(cartItem.getQuantity() - 1);
                holder.quantity.setText(String.valueOf(cartItem.getQuantity()));
                listener.onQuantityChanged(cartItem);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            listener.onItemDeleted(cartItem);
        });
    }

    @Override
    public int getItemCount() {
        return cartItemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName, productPrice, quantity, size, options, note;
        ImageView btnIncrease, btnDecrease, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.imageViewCartItem);
            productName = itemView.findViewById(R.id.textViewCartItemName);
            productPrice = itemView.findViewById(R.id.textViewCartItemPrice);
            quantity = itemView.findViewById(R.id.textViewQuantity);
            size = itemView.findViewById(R.id.textViewCartItemSize);
            options = itemView.findViewById(R.id.textViewCartItemOptions);
            note = itemView.findViewById(R.id.textViewCartItemNote);
            btnIncrease = itemView.findViewById(R.id.buttonIncrease);
            btnDecrease = itemView.findViewById(R.id.buttonDecrease);
            btnDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }
}