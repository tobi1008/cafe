package com.example.cafe;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItem> cartItems;

    public CartAdapter(List<CartItem> cartItems) {
        this.cartItems = cartItems;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem cartItem = cartItems.get(position);
        Product product = cartItem.getProduct();

        // SỬA LỖI Ở ĐÂY: Dùng getTen() và getGia() thay vì getName() và getPrice()
        holder.productName.setText(product.getTen());

        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        holder.productPrice.setText(formatter.format(product.getGia()));

        holder.quantity.setText("x " + cartItem.getQuantity());
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView productName, productPrice, quantity;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.textViewCartItemName);
            productPrice = itemView.findViewById(R.id.textViewCartItemPrice);
            quantity = itemView.findViewById(R.id.textViewCartItemQuantity);
        }
    }
}

