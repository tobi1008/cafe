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
    private CartManager cartManager;
    private CartItemListener listener;

    public interface CartItemListener {
        void onQuantityChanged();
        void onItemDeleted();
    }

    public CartAdapter(Context context, List<CartItem> cartItemList, CartItemListener listener) {
        this.context = context;
        this.cartItemList = cartItemList;
        this.cartManager = CartManager.getInstance(context);
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

        // Lấy thông tin trực tiếp từ cartItem, không cần getProduct() nữa
        holder.productName.setText(cartItem.getProductName());
        holder.quantity.setText(String.valueOf(cartItem.getQuantity()));

        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        holder.productPrice.setText(formatter.format(cartItem.getProductPrice()));

        Glide.with(context)
                .load(cartItem.getProductImage())
                .placeholder(R.drawable.ic_placeholder)
                .into(holder.productImage);

        holder.btnIncrease.setOnClickListener(v -> {
            cartItem.setQuantity(cartItem.getQuantity() + 1);
            cartManager.updateCartItem(cartItem);
            holder.quantity.setText(String.valueOf(cartItem.getQuantity()));
            listener.onQuantityChanged();
        });

        holder.btnDecrease.setOnClickListener(v -> {
            if (cartItem.getQuantity() > 1) {
                cartItem.setQuantity(cartItem.getQuantity() - 1);
                cartManager.updateCartItem(cartItem);
                holder.quantity.setText(String.valueOf(cartItem.getQuantity()));
                listener.onQuantityChanged();
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            cartManager.deleteCartItem(cartItem);
            // Không cần xóa khỏi list ở đây nữa vì CartActivity sẽ tải lại
            listener.onItemDeleted();
        });
    }

    @Override
    public int getItemCount() {
        return cartItemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName, productPrice, quantity;
        Button btnIncrease, btnDecrease, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.imageViewCartItem);
            productName = itemView.findViewById(R.id.textViewCartItemName);
            productPrice = itemView.findViewById(R.id.textViewCartItemPrice);
            quantity = itemView.findViewById(R.id.textViewQuantity);
            btnIncrease = itemView.findViewById(R.id.buttonIncrease);
            btnDecrease = itemView.findViewById(R.id.buttonDecrease);
            btnDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }
}

