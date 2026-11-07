package com.mkr.custom_qr.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.mkr.custom_qr.R;
import com.mkr.custom_qr.models.PairedDeviceModel;
import java.util.List;

public class PairedDeviceAdapter extends RecyclerView.Adapter<PairedDeviceAdapter.DeviceViewHolder> {

    public interface OnUnpairClickListener {
        void onUnpair(PairedDeviceModel device);
    }

    private final List<PairedDeviceModel> devices;
    private final OnUnpairClickListener listener;

    public PairedDeviceAdapter(List<PairedDeviceModel> devices, OnUnpairClickListener listener) {
        this.devices = devices;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_paired_device, parent, false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        PairedDeviceModel item = devices.get(position);
        holder.tvDeviceName.setText(item.getDeviceName());
        holder.tvFingerprint.setText("Fingerprint: " + item.getFingerprint());

        holder.btnUnpair.setOnClickListener(v -> {
            if (listener != null) listener.onUnpair(item);
        });
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    static class DeviceViewHolder extends RecyclerView.ViewHolder {
        TextView tvDeviceName, tvFingerprint;
        Button btnUnpair;

        DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDeviceName = itemView.findViewById(R.id.tv_device_name);
            tvFingerprint = itemView.findViewById(R.id.tv_device_fingerprint);
            btnUnpair = itemView.findViewById(R.id.btn_unpair_device);
        }
    }
}
