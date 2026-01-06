package com.example.cafe.adapter;

import com.example.cafe.model.*;
import com.example.cafe.R;
import com.example.cafe.ui.product.*;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class VoucherAdapter extends RecyclerView.Adapter<VoucherAdapter.ViewHolder> {

    private Context context;
    private List<Voucher> voucherList;
    private OnDeleteClickListener listener;

    public interface OnDeleteClickListener {
        void onDeleteClick(Voucher voucher);
    }

    public VoucherAdapter(Context context, List<Voucher> voucherList, OnDeleteClickListener listener) {
        this.context = context;
        this.voucherList = voucherList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_voucher, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Voucher voucher = voucherList.get(position);

        holder.code.setText(voucher.getCode());
        holder.description.setText(voucher.getDescription());

        if (voucher.getExpiryDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            holder.expiryDate.setText("Hết hạn: " + sdf.format(voucher.getExpiryDate()));
        }

        holder.deleteButton.setOnClickListener(v -> listener.onDeleteClick(voucher));
    }

    @Override
    public int getItemCount() {
        return voucherList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView code, description, expiryDate;
        android.widget.ImageButton deleteButton; // Changed from Button to ImageButton

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            code = itemView.findViewById(R.id.textViewVoucherCode);
            description = itemView.findViewById(R.id.textViewVoucherDescription);
            expiryDate = itemView.findViewById(R.id.textViewExpiryDate);
            deleteButton = itemView.findViewById(R.id.buttonDeleteVoucher);
        }
    }
}
