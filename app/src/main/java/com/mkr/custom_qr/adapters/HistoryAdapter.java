package com.mkr.custom_qr.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.mkr.custom_qr.R;
import com.mkr.custom_qr.models.ScanHistoryModel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    public interface OnHistoryClickListener {
        void onHistoryClick(ScanHistoryModel item);
    }

    private final List<ScanHistoryModel> items;
    private final OnHistoryClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());

    public HistoryAdapter(List<ScanHistoryModel> items, OnHistoryClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        ScanHistoryModel item = items.get(position);
        holder.tvTitle.setText(item.getTitle() != null ? item.getTitle() : "Scanned QR");
        holder.tvTimestamp.setText(dateFormat.format(new Date(item.getScannedAt())));

        if (item.isPrivate()) {
            holder.ivLock.setVisibility(View.VISIBLE);
        } else {
            holder.ivLock.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onHistoryClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvTimestamp;
        ImageView ivLock, ivIcon;

        HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_history_title);
            tvTimestamp = itemView.findViewById(R.id.tv_history_timestamp);
            ivLock = itemView.findViewById(R.id.iv_history_lock);
            ivIcon = itemView.findViewById(R.id.iv_history_type_icon);
        }
    }
}
