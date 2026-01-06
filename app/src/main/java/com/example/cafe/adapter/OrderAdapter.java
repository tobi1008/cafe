package com.example.cafe.adapter;

import com.example.cafe.model.*;
import com.example.cafe.R;
import com.example.cafe.ui.product.*;
import com.example.cafe.ui.order.*;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    private Context context;
    private List<Order> orderList;

    public OrderAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orderList.get(position);

        if (order.getOrderId() != null && order.getOrderId().length() >= 8) {
            holder.orderId.setText(order.getOrderId().substring(0, 8).toUpperCase() + "...");
        } else {
            holder.orderId.setText("N/A");
        }

        holder.orderStatus.setText(order.getStatus());

        if (order.getOrderDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            // SỬA LỖI Ở ĐÂY: Xóa ".toDate()"
            holder.orderDate.setText("Ngày đặt: " + sdf.format(order.getOrderDate()));
        }

        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        holder.orderTotal.setText(formatter.format(order.getTotalPrice()));

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, OrderDetailActivity.class);
            intent.putExtra("ORDER_DETAIL", order);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView orderId, orderStatus, orderDate, orderTotal;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            orderId = itemView.findViewById(R.id.textViewOrderId);
            orderStatus = itemView.findViewById(R.id.textViewOrderStatus);
            orderDate = itemView.findViewById(R.id.textViewOrderDate);
            orderTotal = itemView.findViewById(R.id.textViewOrderTotal);
        }
    }
}
