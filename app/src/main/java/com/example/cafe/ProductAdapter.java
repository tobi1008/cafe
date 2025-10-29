package com.example.cafe;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    private Context context;
    private List<Product> productList;
    private Map<String, HappyHour> happyHourMap;

    public ProductAdapter(Context context, List<Product> productList, Map<String, HappyHour> happyHourMap) {
        this.context = context;
        this.productList = productList;
        this.happyHourMap = happyHourMap;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = productList.get(position);

        if (product == null) {
            return;
        }


        if (product.getTen() != null) {
            holder.productName.setText(product.getTen());
        } else {
            holder.productName.setText("N/A");
        }

        // Hiển thị Rating
        holder.ratingBar.setRating((float) product.getAverageRating());
        holder.tvRating.setText(String.format(Locale.US, "%.1f", product.getAverageRating()));

        // Tải ảnh
        Glide.with(context)
                .load(product.getHinhAnh())
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .into(holder.productImage);

        calculateAndDisplayPrice(holder, product);

        holder.itemView.setOnClickListener(v -> {
            if (context instanceof AppCompatActivity) {
                AppCompatActivity activity = (AppCompatActivity) context;
                FragmentManager fragmentManager = activity.getSupportFragmentManager();

                ProductDetailBottomSheetFragment bottomSheetFragment = ProductDetailBottomSheetFragment.newInstance(product);

                bottomSheetFragment.show(fragmentManager, bottomSheetFragment.getTag());
            } else {
                Intent intent = new Intent(context, ProductDetailActivity.class);
                intent.putExtra("PRODUCT_DETAIL", product);
                context.startActivity(intent);
            }
        });
    }

    // Hàm "thông minh" tính toán giá
    private void calculateAndDisplayPrice(ViewHolder holder, Product product) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        double basePriceM = product.getPriceForSize("M");
        String formattedOriginalPrice = formatter.format(basePriceM);

        int currentHour = getCurrentHour();
        boolean isHappyHourActive = false;
        HappyHour activeHappyHour = null;

        if (product.getHappyHourId() != null && happyHourMap != null && happyHourMap.containsKey(product.getHappyHourId())) {
            HappyHour hh = happyHourMap.get(product.getHappyHourId());
            if (hh != null && hh.isDangBat() && currentHour >= hh.getGioBatDau() && currentHour < hh.getGioKetThuc()) {
                isHappyHourActive = true;
                activeHappyHour = hh;
            }
        }

        if (isHappyHourActive) {
            double happyHourPrice = basePriceM * (1 - (activeHappyHour.getPhanTramGiamGia() / 100.0));
            holder.productPrice.setText(formatter.format(happyHourPrice));
            holder.tvProductOriginalPrice.setText(formattedOriginalPrice);
            holder.tvProductOriginalPrice.setPaintFlags(holder.tvProductOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvSaleTag.setText(String.format(Locale.US, "-%d %% sale giờ vàng", activeHappyHour.getPhanTramGiamGia()));
            holder.tvProductOriginalPrice.setVisibility(View.VISIBLE);
            holder.tvSaleTag.setVisibility(View.VISIBLE);

        } else if (product.getPhanTramGiamGia() > 0) {
            double salePrice = product.getFinalPriceForSize("M");
            holder.productPrice.setText(formatter.format(salePrice));
            holder.tvProductOriginalPrice.setText(formattedOriginalPrice);
            holder.tvProductOriginalPrice.setPaintFlags(holder.tvProductOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvSaleTag.setText(String.format(Locale.US, "-%d%%", product.getPhanTramGiamGia()));
            holder.tvProductOriginalPrice.setVisibility(View.VISIBLE);
            holder.tvSaleTag.setVisibility(View.VISIBLE);

        } else {
            holder.productPrice.setText(formattedOriginalPrice);
            holder.tvProductOriginalPrice.setVisibility(View.GONE);
            holder.tvSaleTag.setVisibility(View.GONE);
        }
    }

    private int getCurrentHour() {
        return Calendar.getInstance().get(Calendar.HOUR_OF_DAY); // 0-23
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName, productPrice, tvProductOriginalPrice, tvSaleTag, tvRating;
        RatingBar ratingBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.imageViewProduct);
            productName = itemView.findViewById(R.id.textViewProductName);
            productPrice = itemView.findViewById(R.id.textViewProductPrice);
            tvProductOriginalPrice = itemView.findViewById(R.id.textViewOriginalPrice);
            tvSaleTag = itemView.findViewById(R.id.textViewSaleTag);
            ratingBar = itemView.findViewById(R.id.ratingBarProduct);
            tvRating = itemView.findViewById(R.id.textViewRating);
        }
    }
}

