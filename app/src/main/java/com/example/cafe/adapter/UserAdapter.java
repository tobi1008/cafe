package com.example.cafe.adapter;

import com.example.cafe.model.*;
import com.example.cafe.R;
import com.example.cafe.ui.product.*;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private Context context;
    private List<User> userList;
    private OnUserInteractionListener listener;

    public interface OnUserInteractionListener {
        void onEditClick(User user);
        void onDeleteClick(User user);
    }

    public UserAdapter(Context context, List<User> userList, OnUserInteractionListener listener) {
        this.context = context;
        this.userList = userList;
        this.listener = listener; // GÁN LISTENER
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        if (user == null) return;

        holder.tvUserEmail.setText(user.getEmail() != null ? user.getEmail() : "N/A");

        String role = (user.getRole() != null) ? user.getRole() : "user";
        holder.tvUserRole.setText("Role: " + role);
        if ("admin".equals(role)) {
            holder.tvUserRole.setTextColor(ContextCompat.getColor(context, R.color.colorDelete));
        } else {
            holder.tvUserRole.setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray));
        }

        String tier = (user.getMemberTier() != null) ? user.getMemberTier() : "Đồng";
        holder.tvUserTier.setText("Hạng: " + tier);
        switch (tier) {
            case "Vàng":
                holder.tvUserTier.setTextColor(ContextCompat.getColor(context, R.color.colorWarning));
                break;
            case "Bạc":
                holder.tvUserTier.setTextColor(Color.parseColor("#808080")); // Màu Bạc (Xám)
                break;
            default:
                holder.tvUserTier.setTextColor(ContextCompat.getColor(context, android.R.color.black));
                break;
        }

        Locale locale = new Locale("vi", "VN");
        NumberFormat formatter = NumberFormat.getCurrencyInstance(locale);
        formatter.setMaximumFractionDigits(0);
        holder.tvUserSpending.setText("Chi tiêu: " + formatter.format(user.getTotalSpending()));

        holder.btnEditUser.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(user);
            }
        });

        holder.btnDeleteUser.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView ivUserIcon;
        TextView tvUserEmail, tvUserRole, tvUserTier, tvUserSpending;
        ImageButton btnEditUser, btnDeleteUser;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            ivUserIcon = itemView.findViewById(R.id.ivUserIcon);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvUserRole = itemView.findViewById(R.id.tvUserRole);
            tvUserTier = itemView.findViewById(R.id.tvUserTier);
            tvUserSpending = itemView.findViewById(R.id.tvUserSpending);
            btnEditUser = itemView.findViewById(R.id.btnEditUser);
            btnDeleteUser = itemView.findViewById(R.id.btnDeleteUser);
        }
    }
}