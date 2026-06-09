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

import java.time.LocalDate;
import java.util.List;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
            else if (itemId == R.id.nav_p2p)        viewPager.setCurrentItem(1);
            else if (itemId == R.id.nav_summary)    viewPager.setCurrentItem(2);
            else if (itemId == R.id.nav_all)        viewPager.setCurrentItem(3);
            return true;
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position) {
                    case 0: bottomNavigation.setSelectedItemId(R.id.nav_categories); break;
                    case 1: bottomNavigation.setSelectedItemId(R.id.nav_p2p); break;
                    case 2: bottomNavigation.setSelectedItemId(R.id.nav_summary); break;
                    case 3: bottomNavigation.setSelectedItemId(R.id.nav_all); break;
                }
            }
        });

        // --- Filter chips ---
        all.setOnClickListener(this);
        all.setSelected(true);
        all.setChecked(true);
        thisMonth.setOnClickListener(this);
        previousMonth.setOnClickListener(this);

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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_overflow) {
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

    private void showExportDialog() {
        PopupWindow popupWindow = new PopupWindow();
        View popupView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.export_data_popup, null);
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
}