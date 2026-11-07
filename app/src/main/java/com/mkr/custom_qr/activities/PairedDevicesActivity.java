package com.mkr.custom_qr.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.mkr.custom_qr.CustomQRApplication;
import com.mkr.custom_qr.R;
import com.mkr.custom_qr.adapters.PairedDeviceAdapter;
import com.mkr.custom_qr.models.PairedDeviceModel;
import java.util.List;

public class PairedDevicesActivity extends AppCompatActivity {

    private RecyclerView rvDevices;
    private LinearLayout layoutEmpty;
    private PairedDeviceAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paired_devices);

        Toolbar toolbar = findViewById(R.id.toolbar_paired_devices);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        rvDevices = findViewById(R.id.rv_paired_devices);
        layoutEmpty = findViewById(R.id.layout_empty_paired);
        rvDevices.setLayoutManager(new LinearLayoutManager(this));

        loadDevices();
    }

    private void loadDevices() {
        List<PairedDeviceModel> list = CustomQRApplication.getInstance().getRepository().getAllPairedDevices();
        if (list.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvDevices.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvDevices.setVisibility(View.VISIBLE);

            adapter = new PairedDeviceAdapter(list, device -> {
                CustomQRApplication.getInstance().getRepository().unpairDevice(device.getId());
                Toast.makeText(this, "Device unpaired and access key revoked", Toast.LENGTH_SHORT).show();
                loadDevices();
            });
            rvDevices.setAdapter(adapter);
        }
    }
}
