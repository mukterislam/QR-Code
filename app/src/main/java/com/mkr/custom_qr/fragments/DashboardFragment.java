package com.mkr.custom_qr.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.mkr.custom_qr.CustomQRApplication;
import com.mkr.custom_qr.R;
import com.mkr.custom_qr.activities.PairingActivity;
import com.mkr.custom_qr.activities.QRDetailsActivity;
import com.mkr.custom_qr.activities.QRGeneratorActivity;
import com.mkr.custom_qr.activities.QRHistoryActivity;
import com.mkr.custom_qr.activities.QRScannerActivity;
import com.mkr.custom_qr.activities.SettingsActivity;
import com.mkr.custom_qr.adapters.QRCodeAdapter;
import com.mkr.custom_qr.models.QRCodeModel;
import java.util.List;

public class DashboardFragment extends Fragment {

    private RecyclerView rvRecent;
    private LinearLayout layoutEmpty;
    private QRCodeAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // Header settings
        ImageView ivSettings = view.findViewById(R.id.iv_header_settings);
        ivSettings.setOnClickListener(v -> startActivity(new Intent(getActivity(), SettingsActivity.class)));

        // Quick Actions
        view.findViewById(R.id.card_create_standard).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), QRGeneratorActivity.class);
            intent.putExtra("is_private", false);
            startActivity(intent);
        });

        view.findViewById(R.id.card_create_private).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), QRGeneratorActivity.class);
            intent.putExtra("is_private", true);
            startActivity(intent);
        });

        view.findViewById(R.id.card_scan_qr).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), QRScannerActivity.class));
        });

        view.findViewById(R.id.card_device_pairing).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), PairingActivity.class));
        });

        TextView tvViewAll = view.findViewById(R.id.tv_view_all_history);
        tvViewAll.setOnClickListener(v -> startActivity(new Intent(getActivity(), QRHistoryActivity.class)));

        rvRecent = view.findViewById(R.id.rv_recent_qr);
        layoutEmpty = view.findViewById(R.id.layout_empty_history);

        rvRecent.setLayoutManager(new LinearLayoutManager(getContext()));
        loadRecentQRCodes();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadRecentQRCodes();
    }

    private void loadRecentQRCodes() {
        if (getContext() == null) return;
        List<QRCodeModel> list = CustomQRApplication.getInstance().getRepository().getAllQRCodes("all");
        if (list.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvRecent.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvRecent.setVisibility(View.VISIBLE);

            // Limit to 5 for dashboard
            List<QRCodeModel> subList = list.size() > 5 ? list.subList(0, 5) : list;
            adapter = new QRCodeAdapter(subList, new QRCodeAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(QRCodeModel item) {
                    Intent intent = new Intent(getActivity(), QRDetailsActivity.class);
                    intent.putExtra("qr_item", item);
                    startActivity(intent);
                }

                @Override
                public void onFavoriteClick(QRCodeModel item) {
                    CustomQRApplication.getInstance().getRepository().toggleFavoriteQR(item.getId(), item.isFavorite());
                    loadRecentQRCodes();
                }
            });
            rvRecent.setAdapter(adapter);
        }
    }
}
