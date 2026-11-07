package com.mkr.custom_qr.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.mkr.custom_qr.CustomQRApplication;
import com.mkr.custom_qr.R;
import com.mkr.custom_qr.adapters.HistoryAdapter;
import com.mkr.custom_qr.models.ScanHistoryModel;
import java.util.List;

public class QRHistoryActivity extends AppCompatActivity {

    private RecyclerView rvHistory;
    private LinearLayout layoutEmpty;
    private HistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_history);

        Toolbar toolbar = findViewById(R.id.toolbar_history);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        rvHistory = findViewById(R.id.rv_history_list);
        layoutEmpty = findViewById(R.id.layout_empty_history_screen);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));

        loadHistory();
    }

    private void loadHistory() {
        List<ScanHistoryModel> list = CustomQRApplication.getInstance().getRepository().getRecentScanHistory(50);
        if (list.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvHistory.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvHistory.setVisibility(View.VISIBLE);

            adapter = new HistoryAdapter(list, item -> {
                if (item.isPrivate()) {
                    Intent intent = new Intent(this, PasswordUnlockActivity.class);
                    intent.putExtra("raw_encrypted_payload", item.getRawContent());
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(this, QRDetailsActivity.class);
                    intent.putExtra("scanned_raw_content", item.getRawContent());
                    startActivity(intent);
                }
            });
            rvHistory.setAdapter(adapter);
        }
    }
}
