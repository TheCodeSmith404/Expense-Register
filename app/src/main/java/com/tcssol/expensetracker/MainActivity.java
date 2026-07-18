package com.tcssol.expensetracker;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.tcssol.expensetracker.Adapters.PagerAdapter;
import com.tcssol.expensetracker.Data.ExpensesDatabase;
import com.tcssol.expensetracker.Data.PersonExpDatabase;
import com.tcssol.expensetracker.Data.PersonExpRepository;
import com.tcssol.expensetracker.Model.ExpenseViewModel;
import com.tcssol.expensetracker.Model.Expenses;
import com.tcssol.expensetracker.Model.PersonExp;
import com.tcssol.expensetracker.Model.PersonExpViewModel;
import com.tcssol.expensetracker.Model.SharedExpenseViewModel;
import com.tcssol.expensetracker.Utils.DataBaseExporter;
import com.tcssol.expensetracker.Utils.Wrapped;
import com.tcssol.expensetracker.Utils.TimeViewManager;
import com.tcssol.expensetracker.Utils.BackupExporter;
import com.tcssol.expensetracker.Data.ExpensesDatabase;
import com.tcssol.expensetracker.Model.CategoryConfig;
import com.tcssol.expensetracker.Model.EarningsHistory;

import android.app.AlertDialog;
import android.net.Uri;
import android.widget.ProgressBar;

import java.io.OutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String[] MonthList = {
            "January","February","March","April","May","June",
            "July","August","September","October","November","December"
    };
    private static final Integer[] Years = {
            2015,2016,2017,2018,2019,2020,2021,2022,2023,2024,2025,2026,2027,2028,2029,2030,2031,2032,2033,2034,2035
    };

    private ViewPager2 viewPager;
    private FloatingActionButton fab;
    private ExpenseViewModel expenseViewModel;
    private Toolbar toolbar;
    private BottomNavigationView bottomNavigation;

    private AutoCompleteTextView selectMonth;
    private AutoCompleteTextView selectYear;
    private Chip all;
    private Chip thisMonth;
    private Chip previousMonth;

    private PersonExpViewModel personExpViewModel;
    private int month = LocalDate.now().getMonthValue();
    private int year = LocalDate.now().getYear();
    private SharedExpenseViewModel sharedExpenseViewModel;

    private Boolean userSelectMonthFlag = false;
    private Boolean userSelectYearFlag = false;

    private ActivityResultLauncher<Intent> someActivityResultLauncher;
    private int tableId = -1;

    // Backup SAF launchers
    private ActivityResultLauncher<Intent> backupExportLauncher;
    private ActivityResultLauncher<Intent> backupImportLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TimeViewManager.init(this);
        setContentView(R.layout.activity_main);
        View mainContent = findViewById(R.id.mainActivity);
        ViewCompat.setOnApplyWindowInsetsListener(mainContent, (v, windowInsets) -> {
            androidx.core.graphics.Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(insets.left,
                    insets.top,
                    insets.right,
                    insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        someActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // Logic for other activities if needed
                }
        );

        // Register SAF export launcher
        backupExportLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) performBackupExport(uri);
                    }
                }
        );

        // Register SAF import launcher
        backupImportLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) showImportModeDialog(uri);
                    }
                }
        );

        viewPager = findViewById(R.id.viewpager2);
        fab = findViewById(R.id.fab);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        selectMonth = findViewById(R.id.spinnerSelectMonth);
        selectYear = findViewById(R.id.spinnerSelectYear);
        all = findViewById(R.id.chipAll);
        thisMonth = findViewById(R.id.chipThisMonth);
        previousMonth = findViewById(R.id.chipPreviousMonth);

        // --- Pager ---
        PagerAdapter pagerAdapter = new PagerAdapter(getSupportFragmentManager(), getLifecycle());
        viewPager.setAdapter(pagerAdapter);

        // Add padding so content scrolls above BottomNav
        viewPager.setPadding(0, 0, 0, 0);

        // --- ViewModels ---
        sharedExpenseViewModel = new ViewModelProvider(this).get(SharedExpenseViewModel.class);
        expenseViewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);
        personExpViewModel = new ViewModelProvider(this).get(PersonExpViewModel.class);

        // --- ExposedDropdownMenu: Months ---
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, MonthList);
        selectMonth.setAdapter(monthAdapter);
        selectMonth.setText(MonthList[LocalDate.now().getMonthValue() - 1], false);
        selectMonth.setOnItemClickListener((parent, view, position, id) -> {
            unSelectAllChip(-1);
            String selected = parent.getItemAtPosition(position).toString();
            for (int i = 0; i < MonthList.length; i++) {
                if (MonthList[i].equalsIgnoreCase(selected)) {
                    month = i + 1;
                    break;
                }
            }
            sharedExpenseViewModel.setObject(new Wrapped(month, year));
        });

        // --- ExposedDropdownMenu: Years ---
        String[] yearStrings = new String[Years.length];
        for (int i = 0; i < Years.length; i++) yearStrings[i] = String.valueOf(Years[i]);
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, yearStrings);
        selectYear.setAdapter(yearAdapter);
        selectYear.setText(String.valueOf(LocalDate.now().getYear()), false);
        selectYear.setOnItemClickListener((parent, view, position, id) -> {
            unSelectAllChip(-1);
            String selected = parent.getItemAtPosition(position).toString();
            try {
                year = Integer.parseInt(selected.trim());
            } catch (NumberFormatException e) {
                year = Years[position];
            }
            sharedExpenseViewModel.setObject(new Wrapped(month, year));
        });

        // --- Bottom Navigation <-> ViewPager2  ---
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_categories)      viewPager.setCurrentItem(0);
            else if (itemId == R.id.nav_summary)    viewPager.setCurrentItem(1);
            else if (itemId == R.id.nav_all)        viewPager.setCurrentItem(2);
            else if (itemId == R.id.nav_settings)   viewPager.setCurrentItem(3);
            return true;
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                View periodSelector = findViewById(R.id.scrollPeriodSelector);
                if (periodSelector != null) {
                    periodSelector.setVisibility(position == 3 ? View.GONE : View.VISIBLE);
                }
                switch (position) {
                    case 0: bottomNavigation.setSelectedItemId(R.id.nav_categories); break;
                    case 1: bottomNavigation.setSelectedItemId(R.id.nav_summary); break;
                    case 2: bottomNavigation.setSelectedItemId(R.id.nav_all); break;
                    case 3: bottomNavigation.setSelectedItemId(R.id.nav_settings); break;
                }
            }
        });

        // --- Filter chips — default to "This Month" ---
        all.setOnClickListener(this);
        thisMonth.setOnClickListener(this);
        previousMonth.setOnClickListener(this);

        // Default: select "This Month" and emit immediately so all fragments start filtered
        month = LocalDate.now().getMonthValue();
        year = LocalDate.now().getYear();
        thisMonth.setChecked(true);
        thisMonth.setSelected(true);
        sharedExpenseViewModel.setObject(new Wrapped(month, year));

        // --- FAB ---
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), CreateExpenses.class);
            intent.putExtra("Type", 0);
            someActivityResultLauncher.launch(intent);
        });

        // Budget check removed or kept if needed
        checkBudgetWarning();

        // --- Restore tab from intent (e.g. returning from EditAdapter) ---
        Intent intent = getIntent();
        if (intent != null && intent.getComponent() != null) {
            int tab = intent.getIntExtra("selected_tab_pager", 0);
            viewPager.setCurrentItem(tab, false);
        }
    }

    private void checkBudgetWarning() {
        SharedPreferences prefs = getSharedPreferences("com.tcs.expensetracker.stored_categories", Context.MODE_PRIVATE);
        double budget = prefs.getFloat("MONTHLY_BUDGET", 0f);
        if (budget <= 0) return;

        String mon = month < 10 ? "0" + month : String.valueOf(month);
        String yr = String.valueOf(year);
        expenseViewModel.getMonthSpendAsync(mon, yr, totalSpend -> {
            if (totalSpend >= budget) {
                View rootView = getWindow().getDecorView().getRootView();
                runOnUiThread(() ->
                    Snackbar.make(rootView,
                            String.format("⚠️ Budget exceeded! Spent ₹%.0f of ₹%.0f", totalSpend, budget),
                            Snackbar.LENGTH_LONG).show()
                );
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem toggleItem = menu.findItem(R.id.menu_toggle_view_mode);
        if (toggleItem != null) {
            updateMenuToggleIcon(toggleItem);
        }
        return true;
    }

    private void updateMenuToggleIcon(MenuItem item) {
        if (TimeViewManager.isTimeViewMode()) {
            item.setIcon(R.drawable.baseline_money_24_white);
            item.setTitle("Currency View");
        } else {
            item.setIcon(R.drawable.baseline_access_time_24_white);
            item.setTitle("Time View");
        }
    }

    private void refreshAllViews() {
        if (sharedExpenseViewModel != null) {
            Wrapped wrapped = sharedExpenseViewModel.getObject().getValue();
            if (wrapped != null) {
                sharedExpenseViewModel.setObject(wrapped);
            } else {
                sharedExpenseViewModel.setObject(new Wrapped(month, year));
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_calculator) {
            showCalculatorDialog();
            return true;
        } else if (itemId == R.id.menu_toggle_view_mode) {
            boolean currentMode = TimeViewManager.isTimeViewMode();
            if (!currentMode && !TimeViewManager.isConfigured()) {
                showCalculatorDialog();
            } else {
                TimeViewManager.setTimeViewMode(this, !currentMode);
                updateMenuToggleIcon(item);
                refreshAllViews();
            }
            return true;
        } else if (itemId == R.id.menu_overflow) {
            PopupMenu popupMenu = new PopupMenu(MainActivity.this, toolbar, Gravity.END);
            popupMenu.getMenuInflater().inflate(R.menu.menu_menu_fetch, popupMenu.getMenu());
            popupMenu.show();
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                int id = menuItem.getItemId();
                if (id == R.id.menu_manage_categories) {
                    Intent i = new Intent(getApplicationContext(), EditAdapter.class);
                    i.putExtra("calling_activity_class", "com.tcssol.expensetracker.MainActivity");
                    i.putExtra("selected_tab_pager", viewPager.getCurrentItem());
                    someActivityResultLauncher.launch(i);
                } else if (id == R.id.menu_About) {
                    someActivityResultLauncher.launch(new Intent(getApplicationContext(), AboutActivity.class));
                } else if (id == R.id.menu_settings) {
                    someActivityResultLauncher.launch(new Intent(getApplicationContext(), Settings.class));
                } else if (id == R.id.menu_export_data) {
                    showExportDialog();
                }
                return false;
            });
        }
        return super.onOptionsItemSelected(item);
    }

    public void showExportDialog() {
        PopupWindow popupWindow = new PopupWindow();
        View popupView = LayoutInflater.from(this).inflate(R.layout.export_data_popup, null);
        popupWindow.setContentView(popupView);
        popupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);

        Button exportDataCsv = popupView.findViewById(R.id.buttonExportCsvData);
        Button exportDataTxt = popupView.findViewById(R.id.buttonExportTxtData);
        RadioGroup selectTable = popupView.findViewById(R.id.radioGrpSelectTable);

        popupWindow.showAtLocation(toolbar, Gravity.CENTER, 0, 0);

        selectTable.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioButtonTable1) tableId = 1;
            else if (checkedId == R.id.radioButtonTable2) tableId = 2;
        });

        View rootView = getWindow().getDecorView().getRootView();

        exportDataCsv.setOnClickListener(v -> {
            if (tableId > 0) {
                if (tableId == 1) {
                    ExpensesDatabase.databaseWriterExecutor.execute(() -> {
                        List<Expenses> list = expenseViewModel.getAllExpensesList();
                        runOnUiThread(() -> {
                            popupWindow.dismiss();
                            DataBaseExporter.exportCSVExpenses(MainActivity.this, rootView, list);
                        });
                    });
                } else if (tableId == 2) {
                    PersonExpDatabase.databaseWriterExecutor.execute(() -> {
                        List<PersonExp> list = personExpViewModel.getAllExpensesListSync();
                        runOnUiThread(() -> {
                            popupWindow.dismiss();
                            DataBaseExporter.exportCsvPersonExpenses(MainActivity.this, rootView, list);
                        });
                    });
                }
            } else {
                Snackbar.make(rootView, "Please select a table to export", Snackbar.LENGTH_SHORT).show();
            }
        });

        exportDataTxt.setOnClickListener(v -> {
            if (tableId > 0) {
                if (tableId == 1) {
                    ExpensesDatabase.databaseWriterExecutor.execute(() -> {
                        List<Expenses> list = expenseViewModel.getAllExpensesList();
                        runOnUiThread(() -> {
                            popupWindow.dismiss();
                            DataBaseExporter.exportTxtExpenses(MainActivity.this, rootView, list);
                        });
                    });
                } else if (tableId == 2) {
                    PersonExpDatabase.databaseWriterExecutor.execute(() -> {
                        List<PersonExp> list = personExpViewModel.getAllExpensesListSync();
                        runOnUiThread(() -> {
                            popupWindow.dismiss();
                            DataBaseExporter.exportTxtPersonExpenses(MainActivity.this, rootView, list);
                        });
                    });
                }
            } else {
                Snackbar.make(rootView, "Please select a table to export", Snackbar.LENGTH_SHORT).show();
            }
        });

        popupView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                popupWindow.dismiss();
                return true;
            }
            return false;
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        unSelectAllChip(id);
        if (id == R.id.chipAll) {
            sharedExpenseViewModel.setObject(new Wrapped(-12, -2024));
        } else if (id == R.id.chipThisMonth) {
            month = LocalDate.now().getMonthValue();
            year = LocalDate.now().getYear();
            sharedExpenseViewModel.setObject(new Wrapped(month, year));
            // Sync dropdown text
            selectMonth.setText(MonthList[month - 1], false);
            selectYear.setText(String.valueOf(year), false);
        } else if (id == R.id.chipPreviousMonth) {
            LocalDate prev = LocalDate.now().minusMonths(1);
            month = prev.getMonthValue();
            year = prev.getYear();
            sharedExpenseViewModel.setObject(new Wrapped(month, year));
            selectMonth.setText(MonthList[month - 1], false);
            selectYear.setText(String.valueOf(year), false);
        }
    }

    private void unSelectAllChip(int selectedId) {
        int[] chips = {R.id.chipAll, R.id.chipThisMonth, R.id.chipPreviousMonth};
        for (int chipId : chips) {
            Chip chip = findViewById(chipId);
            chip.setChecked(chipId == selectedId);
            chip.setSelected(chipId == selectedId);
        }
    }

    public void unSelectAll() {
        unSelectAllChip(-1);
    }

    public void showOverlayFragment(androidx.fragment.app.Fragment fragment) {
        View container = findViewById(R.id.fragmentOverlayContainer);
        if (container != null) {
            container.setVisibility(View.VISIBLE);
        }
        View bottomNav = findViewById(R.id.bottomNavigation);
        if (bottomNav != null) {
            bottomNav.setVisibility(View.GONE);
        }
        View fabView = findViewById(R.id.fab);
        if (fabView != null) {
            fabView.setVisibility(View.GONE);
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentOverlayContainer, fragment)
                .commit();
    }

    public void hideOverlayContainer() {
        View container = findViewById(R.id.fragmentOverlayContainer);
        if (container != null) {
            container.setVisibility(View.GONE);
        }
        View bottomNav = findViewById(R.id.bottomNavigation);
        if (bottomNav != null) {
            bottomNav.setVisibility(View.VISIBLE);
        }
        View fabView = findViewById(R.id.fab);
        if (fabView != null) {
            fabView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        View container = findViewById(R.id.fragmentOverlayContainer);
        if (container != null && container.getVisibility() == View.VISIBLE) {
            hideOverlayContainer();
            // Find and remove any fragment in overlay
            androidx.fragment.app.Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragmentOverlayContainer);
            if (fragment != null) {
                getSupportFragmentManager().beginTransaction().remove(fragment).commit();
            }
        } else {
            super.onBackPressed();
        }
    }

    private void showCalculatorDialog() {
        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this).create();
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_earnings_calculator, null);
        dialog.setView(dialogView);

        LinearLayout llCalculatorPanel = dialogView.findViewById(R.id.llCalculatorPanel);
        LinearLayout llConfigPanel = dialogView.findViewById(R.id.llConfigPanel);

        TextView tvHourlyRate = dialogView.findViewById(R.id.tvCalcHourlyRate);
        TextView tvInput = dialogView.findViewById(R.id.tvCalcInput);
        TextView tvOutput = dialogView.findViewById(R.id.tvCalcOutput);

        // Config inputs
        com.google.android.material.textfield.TextInputEditText etMonthlyEarnings = dialogView.findViewById(R.id.etMonthlyEarnings);
        com.google.android.material.textfield.TextInputEditText etDaysWorked = dialogView.findViewById(R.id.etDaysWorked);
        com.google.android.material.textfield.TextInputEditText etHoursWorked = dialogView.findViewById(R.id.etHoursWorked);

        Button btnSaveConfig = dialogView.findViewById(R.id.btnSaveEarningsConfig);
        Button btnCancelConfig = dialogView.findViewById(R.id.btnCancelConfig);
        Button btnSettings = dialogView.findViewById(R.id.btnKeySettings);
        Button btnStats = dialogView.findViewById(R.id.btnKeyStats);

        final StringBuilder currentInput = new StringBuilder();

        Runnable updateDisplay = new Runnable() {
            @Override
            public void run() {
                String inputStr = currentInput.toString();
                tvInput.setText(inputStr.isEmpty() ? "₹0" : "₹" + inputStr);

                if (inputStr.isEmpty() || !TimeViewManager.isConfigured()) {
                    tvOutput.setText("0s");
                    return;
                }

                try {
                    double amt = Double.parseDouble(inputStr);
                    String formattedTime = TimeViewManager.formatAmountAsTime(amt, false);
                    tvOutput.setText(formattedTime);
                } catch (NumberFormatException e) {
                    tvOutput.setText("0s");
                }
            }
        };

        Runnable refreshCalculatorUI = new Runnable() {
            @Override
            public void run() {
                if (TimeViewManager.isConfigured()) {
                    llConfigPanel.setVisibility(View.GONE);
                    llCalculatorPanel.setVisibility(View.VISIBLE);

                    double rate = TimeViewManager.getHourlyRate();
                    String symbol = java.util.Currency.getInstance(java.util.Locale.getDefault()).getSymbol();
                    tvHourlyRate.setText(String.format(java.util.Locale.getDefault(), "Rate: %s%,.2f/hr", symbol, rate));
                    updateDisplay.run();
                } else {
                    llCalculatorPanel.setVisibility(View.GONE);
                    llConfigPanel.setVisibility(View.VISIBLE);
                    btnCancelConfig.setVisibility(View.GONE);
                }
            }
        };

        refreshCalculatorUI.run();

        // Keypad inputs
        View.OnClickListener numberClickListener = v -> {
            Button btn = (Button) v;
            String btnText = btn.getText().toString();
            if (currentInput.toString().equals("0")) {
                currentInput.setLength(0);
            }
            currentInput.append(btnText);
            updateDisplay.run();
        };

        dialogView.findViewById(R.id.btnKey0).setOnClickListener(numberClickListener);
        dialogView.findViewById(R.id.btnKey1).setOnClickListener(numberClickListener);
        dialogView.findViewById(R.id.btnKey2).setOnClickListener(numberClickListener);
        dialogView.findViewById(R.id.btnKey3).setOnClickListener(numberClickListener);
        dialogView.findViewById(R.id.btnKey4).setOnClickListener(numberClickListener);
        dialogView.findViewById(R.id.btnKey5).setOnClickListener(numberClickListener);
        dialogView.findViewById(R.id.btnKey6).setOnClickListener(numberClickListener);
        dialogView.findViewById(R.id.btnKey7).setOnClickListener(numberClickListener);
        dialogView.findViewById(R.id.btnKey8).setOnClickListener(numberClickListener);
        dialogView.findViewById(R.id.btnKey9).setOnClickListener(numberClickListener);

        dialogView.findViewById(R.id.btnKeyDot).setOnClickListener(v -> {
            if (!currentInput.toString().contains(".")) {
                if (currentInput.length() == 0) {
                    currentInput.append("0.");
                } else {
                    currentInput.append(".");
                }
                updateDisplay.run();
            }
        });

        dialogView.findViewById(R.id.btnKeyDel).setOnClickListener(v -> {
            if (currentInput.length() > 0) {
                currentInput.setLength(currentInput.length() - 1);
                updateDisplay.run();
            }
        });

        dialogView.findViewById(R.id.btnKeyClear).setOnClickListener(v -> {
            currentInput.setLength(0);
            updateDisplay.run();
        });

        dialogView.findViewById(R.id.btnKeyClose).setOnClickListener(v -> dialog.dismiss());

        // Toggle Config settings view
        btnSettings.setOnClickListener(v -> {
            llCalculatorPanel.setVisibility(View.GONE);
            llConfigPanel.setVisibility(View.VISIBLE);
            btnCancelConfig.setVisibility(View.VISIBLE);
            etMonthlyEarnings.setText(String.valueOf(TimeViewManager.getMonthlyEarnings()));
            etDaysWorked.setText(String.valueOf(TimeViewManager.getDaysWorked()));
            etHoursWorked.setText(String.valueOf(TimeViewManager.getHoursWorked()));
        });

        btnCancelConfig.setOnClickListener(v -> {
            if (TimeViewManager.isConfigured()) {
                refreshCalculatorUI.run();
            } else {
                dialog.dismiss();
            }
        });

        btnSaveConfig.setOnClickListener(v -> {
            String earningsStr = etMonthlyEarnings.getText() != null ? etMonthlyEarnings.getText().toString().trim() : "";
            String daysStr = etDaysWorked.getText() != null ? etDaysWorked.getText().toString().trim() : "";
            String hoursStr = etHoursWorked.getText() != null ? etHoursWorked.getText().toString().trim() : "";

            if (earningsStr.isEmpty() || daysStr.isEmpty() || hoursStr.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double earnings = Double.parseDouble(earningsStr);
                int days = Integer.parseInt(daysStr);
                double hours = Double.parseDouble(hoursStr);

                if (earnings <= 0 || days <= 0 || hours <= 0) {
                    Toast.makeText(this, "Values must be greater than zero", Toast.LENGTH_SHORT).show();
                    return;
                }

                TimeViewManager.saveConfig(this, earnings, days, hours);

                long now = System.currentTimeMillis();
                double hourlyRate = earnings / (days * hours);
                com.tcssol.expensetracker.Model.EarningsHistory history =
                        new com.tcssol.expensetracker.Model.EarningsHistory(now, earnings, days, hours, hourlyRate);
                expenseViewModel.insertEarningsHistory(history);

                Toast.makeText(this, "Earnings configured successfully", Toast.LENGTH_SHORT).show();

                refreshAllViews();
                refreshCalculatorUI.run();

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid number inputs", Toast.LENGTH_SHORT).show();
            }
        });

        btnStats.setOnClickListener(v -> {
            dialog.dismiss();
            EarningsTrendFragment trendFragment = new EarningsTrendFragment();
            showOverlayFragment(trendFragment);
        });

        dialog.show();
    }

    // ─── BACKUP: SAF TRIGGER ───────────────────────────────────────────────────

    public void triggerBackupExport() {
        String dateStr = new SimpleDateFormat("yyyy_MM_dd", Locale.getDefault()).format(new Date());
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/octet-stream");
        intent.putExtra(Intent.EXTRA_TITLE, "ExpenseTracker_Backup_" + dateStr + ".etb");
        backupExportLauncher.launch(intent);
    }

    public void triggerBackupImport() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/octet-stream");
        backupImportLauncher.launch(intent);
    }

    // ─── BACKUP EXPORT: PROGRESS DIALOG + ASYNC WORK ──────────────────────────

    private void performBackupExport(Uri uri) {
        // Build progress dialog
        android.app.Dialog progressDialog = new android.app.Dialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setContentView(R.layout.dialog_backup_progress);
        if (progressDialog.getWindow() != null) {
            progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        TextView tvStep = progressDialog.findViewById(R.id.tvBackupStep);
        ProgressBar progressBar = progressDialog.findViewById(R.id.progressBackup);
        progressDialog.show();

        ExpensesDatabase.databaseWriterExecutor.execute(() -> {
            try {
                // Fetch all data synchronously
                List<Expenses> expenses = expenseViewModel.getAllExpensesList();
                List<CategoryConfig> configs = expenseViewModel.getAllCategoryConfigsSync();
                List<EarningsHistory> histories = expenseViewModel.getAllEarningsHistorySync();
                List<PersonExp> personExps = personExpViewModel.getAllExpensesListSync();

                OutputStream os = getContentResolver().openOutputStream(uri);
                if (os == null) throw new Exception("Failed to open output stream");

                BackupExporter.exportBackup(
                        this, expenses, configs, histories, personExps, os,
                        (progress, step) -> runOnUiThread(() -> {
                            progressBar.setProgress(progress);
                            tvStep.setText(step);
                        })
                );

                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    new AlertDialog.Builder(this)
                            .setTitle("Backup Complete")
                            .setMessage("Your data has been saved successfully as an .etb backup file.")
                            .setPositiveButton("OK", null)
                            .show();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    new AlertDialog.Builder(this)
                            .setTitle("Backup Failed")
                            .setMessage("An error occurred: " + e.getMessage())
                            .setPositiveButton("OK", null)
                            .show();
                });
            }
        });
    }

    // ─── RESTORE: CONFLICT RESOLUTION DIALOG ──────────────────────────────────

    private void showImportModeDialog(Uri uri) {
        new AlertDialog.Builder(this)
                .setTitle("Restore Backup")
                .setMessage("How would you like to import this backup?")
                .setPositiveButton("Overwrite (Full Restore)", (d, w) -> performBackupImport(uri, true))
                .setNeutralButton("Merge (Keep Existing)", (d, w) -> performBackupImport(uri, false))
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ─── RESTORE IMPORT: PROGRESS DIALOG + ASYNC SEED ENGINE ──────────────────

    private void performBackupImport(Uri uri, boolean overwrite) {
        android.app.Dialog progressDialog = new android.app.Dialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setContentView(R.layout.dialog_backup_progress);
        if (progressDialog.getWindow() != null) {
            progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        TextView tvStep = progressDialog.findViewById(R.id.tvBackupStep);
        ProgressBar progressBar = progressDialog.findViewById(R.id.progressBackup);
        progressDialog.show();

        ExpensesDatabase.databaseWriterExecutor.execute(() -> {
            try {
                InputStream is = getContentResolver().openInputStream(uri);
                if (is == null) throw new Exception("Failed to open file");

                BackupExporter.BackupData data = BackupExporter.importBackup(is, (progress, step) ->
                        runOnUiThread(() -> {
                            progressBar.setProgress(progress);
                            tvStep.setText(step);
                        })
                );

                runOnUiThread(() -> tvStep.setText("Seeding databases..."));

                // ── Wipe + Restore or Merge ────────────────────────────────
                if (overwrite) {
                    // Wipe all existing data
                    expenseViewModel.getExpenseDao().deleteAll();
                    ExpensesDatabase.getDatabase(this).categoryConfigDao().deleteAll();
                    ExpensesDatabase.getDatabase(this).earningsHistoryDao().deleteAll();
                    personExpViewModel.getPersonExpDao().deleteAll();
                }

                // Insert expenses
                if (data.expenses != null) {
                    for (Expenses exp : data.expenses) {
                        ExpensesDatabase.getDatabase(this).expenseDao().insertExpense(exp);
                    }
                }

                // Insert category configs
                if (data.categoryConfigs != null) {
                    for (CategoryConfig cfg : data.categoryConfigs) {
                        ExpensesDatabase.getDatabase(this).categoryConfigDao().insertOrUpdateCategoryConfig(cfg);
                    }
                }

                // Insert earnings history
                if (data.earningsHistory != null) {
                    for (EarningsHistory hist : data.earningsHistory) {
                        ExpensesDatabase.getDatabase(this).earningsHistoryDao().insertEarningsHistory(hist);
                    }
                }

                // Insert person expenses
                if (data.personExpenses != null) {
                    for (PersonExp pExp : data.personExpenses) {
                        PersonExpDatabase.getDatabase(this).personExpDao().insertPersonExpense(pExp);
                    }
                }

                // Restore preferences if available
                if (data.preferences != null) {
                    android.content.SharedPreferences prefs = getSharedPreferences("TimeViewPrefs", android.content.Context.MODE_PRIVATE);
                    android.content.SharedPreferences.Editor editor = prefs.edit();
                    java.util.Iterator<String> keys = data.preferences.keys();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        Object val = data.preferences.get(key);
                        if (val instanceof Boolean) editor.putBoolean(key, (Boolean) val);
                        else if (val instanceof Integer) editor.putInt(key, (Integer) val);
                        else if (val instanceof Long) editor.putLong(key, (Long) val);
                        else if (val instanceof Double || val instanceof Float)
                            editor.putFloat(key, (float) ((Number) val).doubleValue());
                        else editor.putString(key, val.toString());
                    }
                    editor.apply();
                    TimeViewManager.init(this);
                }

                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    new AlertDialog.Builder(this)
                            .setTitle("Restore Complete")
                            .setMessage("Your backup has been restored successfully. Restart the app to see all changes.")
                            .setPositiveButton("OK", null)
                            .show();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    new AlertDialog.Builder(this)
                            .setTitle("Restore Failed")
                            .setMessage("Could not restore backup: " + e.getMessage())
                            .setPositiveButton("OK", null)
                            .show();
                });
            }
        });
    }
}