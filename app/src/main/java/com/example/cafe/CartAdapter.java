package com.example.cafe;

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
        holder.size.setText("Size: " + cartItem.getSelectedSize());

        // Hiển thị tùy chọn Đá và Đường
        String optionsText = "";
        if (cartItem.getIceOption() != null) {
            optionsText += cartItem.getIceOption();
        }
        if (cartItem.getSugarLevel() != null) {
            if (!optionsText.isEmpty()) optionsText += ", ";
            optionsText += cartItem.getSugarLevel();
        }
        holder.options.setText(optionsText);
        holder.options.setVisibility(optionsText.isEmpty() ? View.GONE : View.VISIBLE);


        // Hiển thị ghi chú
        if (cartItem.getNote() != null && !cartItem.getNote().isEmpty()) {
            holder.note.setText("Ghi chú: " + cartItem.getNote());
            holder.note.setVisibility(View.VISIBLE);
        } else {
            holder.note.setVisibility(View.GONE);
        }

        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        holder.productPrice.setText(formatter.format(cartItem.getPrice()));

        Glide.with(context)
                .load(cartItem.getProductImage())
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder) // Use placeholder also on error
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
        Button btnIncrease, btnDecrease, btnDelete;

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

