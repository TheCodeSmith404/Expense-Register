package com.tcssol.expensetracker;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import com.tcssol.expensetracker.Adapters.ObservationsAdapter;
import com.tcssol.expensetracker.Data.ObservationDatabase;
import com.tcssol.expensetracker.Model.Observation;
import com.tcssol.expensetracker.Utils.BankSmsParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Inbox of transactions detected from bank SMS. Each row can be turned into a
 * real expense (opens CreateExpenses pre-filled) or dismissed.
 */
public class ObservationsActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 4001;
    private static final long SCAN_WINDOW_MILLIS = 30L * 24 * 60 * 60 * 1000; // 30 days

    private MaterialCardView permissionCard;
    private Button scanButton;
    private TextView emptyText;
    private ObservationsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_observations);

        MaterialToolbar toolbar = findViewById(R.id.observationsToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.observations);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        Drawable drawable = toolbar.getNavigationIcon();
        if (drawable != null) {
            DrawableCompat.setTint(drawable, Color.WHITE);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        permissionCard = findViewById(R.id.permissionCard);
        scanButton = findViewById(R.id.buttonScanInbox);
        emptyText = findViewById(R.id.observationsEmptyText);

        RecyclerView recyclerView = findViewById(R.id.observationsRecycle);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ObservationsAdapter(this);
        recyclerView.setAdapter(adapter);

        ObservationDatabase.getDatabase(this).observationDao().getAll()
                .observe(this, list -> {
                    adapter.setObservations(list);
                    boolean empty = list == null || list.isEmpty();
                    emptyText.setVisibility(empty ? View.VISIBLE : View.GONE);
                });

        findViewById(R.id.buttonAllowSms).setOnClickListener(v -> requestSmsPermissions());
        scanButton.setOnClickListener(v -> scanInbox());

        refreshPermissionState();
    }

    private boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(this, permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void refreshPermissionState() {
        boolean granted = hasPermission(Manifest.permission.RECEIVE_SMS)
                && hasPermission(Manifest.permission.READ_SMS);
        permissionCard.setVisibility(granted ? View.GONE : View.VISIBLE);
        scanButton.setVisibility(granted ? View.VISIBLE : View.GONE);
    }

    private void requestSmsPermissions() {
        List<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.RECEIVE_SMS);
        permissions.add(Manifest.permission.READ_SMS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && !hasPermission(Manifest.permission.POST_NOTIFICATIONS)) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS);
        }
        ActivityCompat.requestPermissions(this,
                permissions.toArray(new String[0]), PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            refreshPermissionState();
        }
    }

    /** Imports transactions from the last 30 days of the SMS inbox. */
    private void scanInbox() {
        if (!hasPermission(Manifest.permission.READ_SMS)) {
            requestSmsPermissions();
            return;
        }
        scanButton.setEnabled(false);
        long since = System.currentTimeMillis() - SCAN_WINDOW_MILLIS;

        ObservationDatabase.databaseWriterExecutor.execute(() -> {
            int found = 0;
            try (Cursor cursor = getContentResolver().query(
                    Telephony.Sms.Inbox.CONTENT_URI,
                    new String[]{Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.DATE},
                    Telephony.Sms.DATE + " > ?",
                    new String[]{String.valueOf(since)},
                    Telephony.Sms.DATE + " DESC")) {
                if (cursor != null) {
                    int addressIdx = cursor.getColumnIndex(Telephony.Sms.ADDRESS);
                    int bodyIdx = cursor.getColumnIndex(Telephony.Sms.BODY);
                    int dateIdx = cursor.getColumnIndex(Telephony.Sms.DATE);
                    while (cursor.moveToNext()) {
                        Observation observation = BankSmsParser.parse(
                                cursor.getString(addressIdx),
                                cursor.getString(bodyIdx),
                                cursor.getLong(dateIdx));
                        if (observation != null) {
                            long id = ObservationDatabase.getDatabase(getApplicationContext())
                                    .observationDao().insert(observation);
                            if (id != -1) found++;
                        }
                    }
                }
            }
            final int foundFinal = found;
            runOnUiThread(() -> {
                scanButton.setEnabled(true);
                Snackbar.make(findViewById(android.R.id.content),
                        getString(R.string.scan_result, foundFinal),
                        Snackbar.LENGTH_LONG).show();
            });
        });
    }
}
