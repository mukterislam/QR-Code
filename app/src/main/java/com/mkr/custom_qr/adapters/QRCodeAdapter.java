package com.mkr.custom_qr.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.mkr.custom_qr.R;
import com.mkr.custom_qr.models.QRCodeModel;
import java.util.List;

public class QRCodeAdapter extends RecyclerView.Adapter<QRCodeAdapter.QRViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(QRCodeModel item);
        void onFavoriteClick(QRCodeModel item);
    }

    private final List<QRCodeModel> items;
    private final OnItemClickListener listener;

    public QRCodeAdapter(List<QRCodeModel> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public QRViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_qr_card, parent, false);
        return new QRViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QRViewHolder holder, int position) {
        QRCodeModel item = items.get(position);
        holder.tvTitle.setText(item.getTitle() != null && !item.getTitle().isEmpty() ? item.getTitle() : "Untitled QR");

        if (item.isPrivate()) {
            holder.tvSubtitle.setText("🔒 Private Encrypted (AES-256-GCM)");
            holder.ivLockBadge.setVisibility(View.VISIBLE);
        } else {
            holder.tvSubtitle.setText(item.getRawPayload());
            holder.ivLockBadge.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });

        holder.ivFavorite.setOnClickListener(v -> {
            if (listener != null) listener.onFavoriteClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class QRViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSubtitle;
        ImageView ivIcon, ivLockBadge, ivFavorite;

        QRViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_item_title);
            tvSubtitle = itemView.findViewById(R.id.tv_item_subtitle);
            ivIcon = itemView.findViewById(R.id.iv_item_icon);
            ivLockBadge = itemView.findViewById(R.id.iv_item_lock_badge);
            ivFavorite = itemView.findViewById(R.id.iv_item_favorite);
        }
    }
}
