package com.example.cafe;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton; // SẼ ĐỔI THÀNH ImageView
import android.widget.ImageView; // *** THÊM IMPORT NÀY ***
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

public class HappyHourAdapter extends RecyclerView.Adapter<HappyHourAdapter.ViewHolder> {

    private Context context;
    private List<HappyHour> happyHourList;
    private OnHappyHourListener listener;

    // Interface để xử lý sự kiện click (Sửa, Xóa)
    public interface OnHappyHourListener {
        void onEditClick(HappyHour happyHour);
        void onDeleteClick(HappyHour happyHour);
    }

    public HappyHourAdapter(Context context, List<HappyHour> happyHourList, OnHappyHourListener listener) {
        this.context = context;
        this.happyHourList = happyHourList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_happy_hour, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HappyHour happyHour = happyHourList.get(position);

        holder.tvName.setText(happyHour.getTenKhungGio());
        // *** SỬA LẠI FORMAT GIỜ CHO KHỚP VỚI XML CỦA BẠN (14:00) ***
        holder.tvTime.setText(String.format(Locale.US, "%d:00 — %d:00", happyHour.getGioBatDau(), happyHour.getGioKetThuc()));

        holder.tvDiscount.setText(String.format(Locale.US, "-%d%%", happyHour.getPhanTramGiamGia()));

        // Dùng đúng hàm isDangBat()
        if (happyHour.isDangBat()) {
            holder.tvStatus.setText("Đang Bật");
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));
        } else {
            holder.tvStatus.setText("Đã Tắt");
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
        }

        // Gán sự kiện click cho Sửa và Xóa
        holder.btnEdit.setOnClickListener(v -> listener.onEditClick(happyHour));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(happyHour));
    }

    @Override
    public int getItemCount() {
        return happyHourList.size();
    }

    // (Tùy chọn) Hàm này dùng để cập nhật danh sách khi có thay đổi
    public void setHappyHourList(List<HappyHour> newList) {
        this.happyHourList = newList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvTime, tvDiscount, tvStatus;
        // *** ĐÂY LÀ THAY ĐỔI QUAN TRỌNG: ĐỔI ImageButton THÀNH ImageView ***
        ImageView btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvHappyHourName);
            tvTime = itemView.findViewById(R.id.tvHappyHourTime);
            tvDiscount = itemView.findViewById(R.id.tvHappyHourDiscount);
            tvStatus = itemView.findViewById(R.id.tvHappyHourStatus);

            // *** SỬA LỖI CUỐI CÙNG: Dùng R.id.ivEditHappyHour ***
            btnEdit = itemView.findViewById(R.id.ivEditHappyHour);
            // *** SỬA LỖI CUỐI CÙNG: Dùng R.id.ivDeleteHappyHour ***
            btnDelete = itemView.findViewById(R.id.ivDeleteHappyHour);
        }
    }
}
