package com.tcssol.expensetracker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.tcssol.expensetracker.Adapters.CategoriesAdapter;
import com.tcssol.expensetracker.Adapters.CategoriesClickListener;
import com.tcssol.expensetracker.Adapters.SubcategorySheetAdapter;
import com.tcssol.expensetracker.Adapters.PopUpRecycleViewAdapter;
import com.tcssol.expensetracker.Data.ExpenseDao;
import com.tcssol.expensetracker.Model.CategoryConfig;
import android.widget.Toast;
import com.tcssol.expensetracker.Model.ExpenseViewModel;
import com.tcssol.expensetracker.Model.Expenses;
import com.tcssol.expensetracker.Model.SharedExpenseViewModel;

import java.time.LocalDate;
import java.util.Currency;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class CategoriesFragment extends Fragment implements CategoriesClickListener {
    private ExpenseViewModel expenseViewModel;
    private SharedExpenseViewModel sharedExpenseViewModel;
    private RecyclerView recyclerView;
    private CategoriesAdapter adapter;
    public ExpenseDao expenseDao;
    private View view;
    private Context context;
    private ActivityResultLauncher<Intent> someActivityResultLauncher;
    int month = -12;
    int year = -2024;

    private TextView tvNetBalance;
    private TextView tvSummaryEarned;
    private TextView tvSummarySpent;
    private LinearProgressIndicator budgetProgressBar;
    private View budgetProgressContainer;
    private TextView tvBudgetInfo;

    private final Set<String> fixedCategoriesSet = new HashSet<>();
    private final java.util.Map<String, Boolean> configFixedMap = new java.util.HashMap<>();
    private final java.util.Map<String, Double> categoryBudgetsMap = new java.util.HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_categories, container, false);
        recyclerView = view.findViewById(R.id.frag1Recycle);
        context = requireContext();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        someActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> { }
        );

        tvNetBalance = view.findViewById(R.id.tvNetBalance);
        tvSummaryEarned = view.findViewById(R.id.tvSummaryEarned);
        tvSummarySpent = view.findViewById(R.id.tvSummarySpent);
        budgetProgressBar = view.findViewById(R.id.budgetProgressBar);
        budgetProgressContainer = view.findViewById(R.id.budgetProgressContainer);
        tvBudgetInfo = view.findViewById(R.id.tvBudgetLabel);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        expenseViewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);
        expenseDao = expenseViewModel.getExpenseDao();

        // Initialize adapter with empty list
        adapter = new CategoriesAdapter(new java.util.ArrayList<>(), context, expenseDao, this);
        recyclerView.setAdapter(adapter);

        // Observe CategoryConfig to update fixed categories set and budget map
        expenseViewModel.getAllCategoryConfigs().observe(getViewLifecycleOwner(), configs -> {
            fixedCategoriesSet.clear();
            configFixedMap.clear();
            categoryBudgetsMap.clear();
            if (configs != null) {
                for (CategoryConfig config : configs) {
                    configFixedMap.put(config.getCategoryName(), config.isFixed());
                    categoryBudgetsMap.put(config.getCategoryName(), config.getBudget());
                    if (config.isFixed()) {
                        fixedCategoriesSet.add(config.getCategoryName());
                    }
                }
            }
            adapter.setFixedCategories(fixedCategoriesSet);
            adapter.setCategoryBudgets(categoryBudgetsMap);
        });

        // Load grouped expenses
        expenseViewModel.getAllExpensesGrouped().observe(getViewLifecycleOwner(), expensesList1 -> {
            if (sharedExpenseViewModel == null || sharedExpenseViewModel.getObject().getValue() == null) {
                adapter.expensesList = expensesList1;
                adapter.notifyDataSetChanged();
            }
        });

        sharedExpenseViewModel = new ViewModelProvider(requireActivity()).get(SharedExpenseViewModel.class);

        sharedExpenseViewModel.getObject().observe(getViewLifecycleOwner(), expensesList -> {
            month = expensesList.getMonth();
            year = expensesList.getYear();

            if (expensesList.getYear() > 0 || expensesList.getMonth() > 0) {
                LocalDate date = LocalDate.of(expensesList.getYear(), expensesList.getMonth(), 1);

                expenseViewModel.getAllExpensesGroupedMonthly(date).observe(getViewLifecycleOwner(), tasks -> {
                    adapter.expensesList = tasks;
                    adapter.notifyDataSetChanged();
                });

                String mon = month < 10 ? "0" + month : String.valueOf(month);
                String yr = String.valueOf(year);
                updateNetBalanceCard(mon, yr);
            } else {
                expenseViewModel.getAllExpensesGrouped().observe(getViewLifecycleOwner(), expensesList1 -> {
                    adapter.expensesList = expensesList1;
                    adapter.notifyDataSetChanged();
                });

                String mon = LocalDate.now().getMonthValue() < 10
                        ? "0" + LocalDate.now().getMonthValue()
                        : String.valueOf(LocalDate.now().getMonthValue());
                String yr = String.valueOf(LocalDate.now().getYear());
                updateNetBalanceCard(mon, yr);
            }
        });

        String mon = LocalDate.now().getMonthValue() < 10
                ? "0" + LocalDate.now().getMonthValue()
                : String.valueOf(LocalDate.now().getMonthValue());
        String yr = String.valueOf(LocalDate.now().getYear());
        updateNetBalanceCard(mon, yr);
    }

    private void updateNetBalanceCard(String month, String year) {
        String symbol = Currency.getInstance(Locale.getDefault()).getSymbol();

        expenseViewModel.getNetBalance(month, year).observe(getViewLifecycleOwner(), net -> {
            if (net == null) net = 0.0;
            String formatted = symbol + String.format(Locale.getDefault(), "%,.0f", Math.abs(net));
            if (net < 0) {
                tvNetBalance.setText("−" + formatted);
                tvNetBalance.setTextColor(ContextCompat.getColor(context, R.color.red));
            } else {
                tvNetBalance.setText(formatted);
                tvNetBalance.setTextColor(ContextCompat.getColor(context, R.color.green));
            }
        });

        expenseViewModel.getFrag3DataFiltered(
                Integer.parseInt(month.replaceFirst("^0", "")),
                Integer.parseInt(year)
        ).observe(getViewLifecycleOwner(), list -> {
            if (list == null || list.size() < 2) return;
            Integer earned = list.get(0);
            Integer spent = list.get(1);
            tvSummaryEarned.setText(symbol + String.format(Locale.getDefault(), "%,d", earned != null ? earned : 0));
            tvSummarySpent.setText(symbol + String.format(Locale.getDefault(), "%,d", spent != null ? spent : 0));

            SharedPreferences prefs = context.getSharedPreferences(
                    "com.tcs.expensetracker.stored_categories", Context.MODE_PRIVATE);
            float budget = prefs.getFloat("MONTHLY_BUDGET", 0f);
            if (budget > 0) {
                budgetProgressContainer.setVisibility(View.VISIBLE);
                int spentVal = spent != null ? spent : 0;
                int progress = (int) Math.min(100, (spentVal / budget) * 100);
                budgetProgressBar.setProgress(progress);
                tvBudgetInfo.setText(String.format(Locale.getDefault(),
                        "BUDGET: %s%,.0f of %s%,.0f used", symbol, (float) spentVal, symbol, budget));
                if (progress >= 100) {
                    budgetProgressBar.setIndicatorColor(ContextCompat.getColor(context, R.color.red));
                } else {
                    budgetProgressBar.setIndicatorColor(ContextCompat.getColor(context, R.color.colorSecondary));
                }
            } else {
                budgetProgressContainer.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onCategoryClick(Expenses expenses) {
        String categoryName = expenses.getCategory();
        com.google.android.material.bottomsheet.BottomSheetDialog bottomSheet = new com.google.android.material.bottomsheet.BottomSheetDialog(context);
        View sheetView = LayoutInflater.from(context).inflate(R.layout.dialog_subcategory_breakdown, null);
        bottomSheet.setContentView(sheetView);

        TextView tvTitle = sheetView.findViewById(R.id.tvSheetCategoryName);
        tvTitle.setText(categoryName + " Details");
        tvTitle.setTextColor(expenses.isType()
                ? ContextCompat.getColor(context, R.color.green)
                : ContextCompat.getColor(context, R.color.red));

        TextInputEditText etBudget = sheetView.findViewById(R.id.etSheetCategoryBudget);
        Double parentBudgetObj = categoryBudgetsMap.get(categoryName);
        double parentBudget = parentBudgetObj != null ? parentBudgetObj : 0.0;
        if (parentBudget > 0) {
            etBudget.setText(String.format(Locale.getDefault(), "%.0f", parentBudget));
        }

        sheetView.findViewById(R.id.btnSaveCategoryBudget).setOnClickListener(v -> {
            String text = etBudget.getText() != null ? etBudget.getText().toString().trim() : "";
            double newBudget = 0.0;
            if (!text.isEmpty()) {
                try {
                    newBudget = Double.parseDouble(text);
                } catch (NumberFormatException e) {
                    Toast.makeText(context, "Invalid amount", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            boolean isFixed = fixedCategoriesSet.contains(categoryName);
            CategoryConfig config = new CategoryConfig(categoryName, isFixed);
            config.setBudget(newBudget);
            expenseViewModel.insertCategoryConfig(config);
            Toast.makeText(context, "Category budget updated", Toast.LENGTH_SHORT).show();
            bottomSheet.dismiss();
        });

        RecyclerView rvSubcategories = sheetView.findViewById(R.id.rvSubcategories);
        rvSubcategories.setHasFixedSize(true);
        rvSubcategories.setLayoutManager(new LinearLayoutManager(context));

        com.tcssol.expensetracker.Data.ExpensesDatabase.databaseWriterExecutor.execute(() -> {
            List<Expenses> subCats = expenseViewModel.getSubCatsF(month, year, categoryName, expenses.isType());
            requireActivity().runOnUiThread(() -> {
                SubcategorySheetAdapter sheetAdapter = new SubcategorySheetAdapter(
                        subCats, context, categoryBudgetsMap, (subKey, newBudget) -> {
                            boolean subFixed = configFixedMap.containsKey(subKey) && configFixedMap.get(subKey);
                            CategoryConfig config = new CategoryConfig(subKey, subFixed);
                            config.setBudget(newBudget);
                            expenseViewModel.insertCategoryConfig(config);
                            Toast.makeText(context, "Subcategory budget updated", Toast.LENGTH_SHORT).show();
                            bottomSheet.dismiss();
                        }
                );
                rvSubcategories.setAdapter(sheetAdapter);
            });
        });

        bottomSheet.show();
    }

    @Override
    public void onCategoryLongClick(Expenses expenses) {
        String categoryName = expenses.getCategory();
        boolean isFixed = fixedCategoriesSet.contains(categoryName);

        String toggleOption = isFixed ? "Mark as Variable Expense" : "Mark as Fixed Expense";

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(categoryName)
                .setItems(new String[]{"Add Transaction", toggleOption, "Cancel"}, (dialog, which) -> {
                    if (which == 0) {
                        launchCreateExpense(expenses);
                    } else if (which == 1) {
                        CategoryConfig config = new CategoryConfig(categoryName, !isFixed);
                        expenseViewModel.insertCategoryConfig(config);
                    }
                })
                .show();
    }

    private void launchCreateExpense(Expenses expenses) {
        Intent intent = new Intent(getActivity(), CreateExpenses.class);
        if (expenses.getCategory().equals("Money Received") || expenses.getCategory().equals("Money Given")) {
            intent.putExtra("Type", 1);
            intent.putExtra("ChangeView", 1);
            intent.putExtra("TypeExpense", expenses.isType());
        } else {
            intent.putExtra("Type", 1);
            intent.putExtra("ChangeView", 0);
            intent.putExtra("Category", expenses.getCategory());
            intent.putExtra("TypeExpense", expenses.isType());
        }
        someActivityResultLauncher.launch(intent);
    }
}
