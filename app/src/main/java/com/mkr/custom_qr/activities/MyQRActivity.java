package com.mkr.custom_qr.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.ChipGroup;
import com.mkr.custom_qr.CustomQRApplication;
import com.mkr.custom_qr.R;
import com.mkr.custom_qr.adapters.QRCodeAdapter;
import com.mkr.custom_qr.models.QRCodeModel;
import java.util.List;

public class MyQRActivity extends AppCompatActivity {

    private RecyclerView rvList;
    private LinearLayout layoutEmpty;
    private QRCodeAdapter adapter;
    private String currentFilter = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_qr);

        Toolbar toolbar = findViewById(R.id.toolbar_my_qr);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        rvList = findViewById(R.id.rv_my_qr_list);
        layoutEmpty = findViewById(R.id.layout_empty_my_qr);
        rvList.setLayoutManager(new LinearLayoutManager(this));

        ChipGroup chipGroup = findViewById(R.id.chip_group_filters);
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chip_filter_normal)) {
                currentFilter = "normal";
            } else if (checkedIds.contains(R.id.chip_filter_private)) {
                currentFilter = "private";
            } else if (checkedIds.contains(R.id.chip_filter_favorites)) {
                currentFilter = "favorites";
            } else {
                currentFilter = "all";
            }
            loadQRCodes();
        });

        loadQRCodes();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadQRCodes();
    }

    private void loadQRCodes() {
        List<QRCodeModel> list = CustomQRApplication.getInstance().getRepository().getAllQRCodes(currentFilter);
        if (list.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvList.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvList.setVisibility(View.VISIBLE);

            adapter = new QRCodeAdapter(list, new QRCodeAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(QRCodeModel item) {
                    Intent intent = new Intent(MyQRActivity.this, QRDetailsActivity.class);
                    intent.putExtra("qr_item", item);
                    startActivity(intent);
                }

                @Override
                public void onFavoriteClick(QRCodeModel item) {
                    CustomQRApplication.getInstance().getRepository().toggleFavoriteQR(item.getId(), item.isFavorite());
                    loadQRCodes();
                }
            });
            rvList.setAdapter(adapter);
        }
    }
}
