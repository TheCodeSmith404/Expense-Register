package com.tcssol.expensetracker;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.tcssol.expensetracker.Adapters.Frag1RcvAdapter;
import com.tcssol.expensetracker.Adapters.Fragment1ClickListner;
import com.tcssol.expensetracker.Adapters.PopUpRecycleViewAdapter;
import com.tcssol.expensetracker.Data.ExpenseDao;
import com.tcssol.expensetracker.Model.ExpenseViewModel;
import com.tcssol.expensetracker.Model.Expenses;
import com.tcssol.expensetracker.Model.SharedExpenseViewModel;

import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.Gravity;
import android.widget.PopupWindow;

import androidx.core.content.res.ResourcesCompat;

public class Fragment1 extends Fragment implements Fragment1ClickListner {
    private ExpenseViewModel expenseViewModel;
    private SharedExpenseViewModel sharedExpenseViewModel;
    private RecyclerView recyclerView;
    private Frag1RcvAdapter adapter;
    public ExpenseDao expenseDao;
    private View view;
    private Context context;
    private ActivityResultLauncher<Intent> someActivityResultLauncher;
    int month = -12;
    int year = -2024;

    // v2.0 views
    private TextView tvNetBalance;
    private TextView tvSummaryEarned;
    private TextView tvSummarySpent;
    private LinearProgressIndicator budgetProgressBar;
    private View budgetProgressContainer;
    private TextView tvBudgetInfo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment1, container, false);
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

        // Net balance card views
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

        expenseViewModel.getAllExpensesGrouped().observe(getViewLifecycleOwner(), expensesList1 -> {
            adapter = new Frag1RcvAdapter(expensesList1, context, expenseDao, this);
            recyclerView.setAdapter(adapter);
        });

        sharedExpenseViewModel = new ViewModelProvider(requireActivity()).get(SharedExpenseViewModel.class);

        sharedExpenseViewModel.getObject().observe(getViewLifecycleOwner(), expensesList -> {
            month = expensesList.getMonth();
            year = expensesList.getYear();

            if (expensesList.getYear() > 0 || expensesList.getMonth() > 0) {
                LocalDate date = LocalDate.of(expensesList.getYear(), expensesList.getMonth(), 1);

                expenseViewModel.getAllExpensesGroupedMonthly(date).observe(getViewLifecycleOwner(), tasks -> {
                    adapter = new Frag1RcvAdapter(tasks, context, expenseDao, this);
                    recyclerView.setAdapter(adapter);
                });

                // Update net balance card for selected month
                String mon = month < 10 ? "0" + month : String.valueOf(month);
                String yr = String.valueOf(year);
                updateNetBalanceCard(mon, yr);
            } else {
                expenseViewModel.getAllExpensesGrouped().observe(getViewLifecycleOwner(), expensesList1 -> {
                    adapter = new Frag1RcvAdapter(expensesList1, context, expenseDao, this);
                    recyclerView.setAdapter(adapter);
                });

                // Default: current month
                String mon = LocalDate.now().getMonthValue() < 10
                        ? "0" + LocalDate.now().getMonthValue()
                        : String.valueOf(LocalDate.now().getMonthValue());
                String yr = String.valueOf(LocalDate.now().getYear());
                updateNetBalanceCard(mon, yr);
            }
        });

        // Initial net balance for current month
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
            String formatted = symbol + String.format(Locale.getDefault(), "%.0f", Math.abs(net));
            if (net < 0) {
                tvNetBalance.setText("−" + formatted);
                tvNetBalance.setTextColor(ContextCompat.getColor(context, R.color.red));
            } else {
                tvNetBalance.setText(formatted);
                tvNetBalance.setTextColor(ContextCompat.getColor(context, R.color.green));
            }
        });

        // Update earned/spent labels via Frag3 mechanism
        expenseViewModel.getFrag3DataFiltered(
                Integer.parseInt(month.replaceFirst("^0", "")),
                Integer.parseInt(year)
        ).observe(getViewLifecycleOwner(), list -> {
            if (list == null || list.size() < 2) return;
            Integer earned = list.get(0);
            Integer spent = list.get(1);
            tvSummaryEarned.setText(symbol + (earned != null ? earned : 0));
            tvSummarySpent.setText(symbol + (spent != null ? spent : 0));

            // Budget progress
            SharedPreferences prefs = context.getSharedPreferences(
                    "com.tcs.expensetracker.stored_categories", Context.MODE_PRIVATE);
            float budget = prefs.getFloat("MONTHLY_BUDGET", 0f);
            if (budget > 0) {
                budgetProgressContainer.setVisibility(View.VISIBLE);
                int spentVal = spent != null ? spent : 0;
                int progress = (int) Math.min(100, (spentVal / budget) * 100);
                budgetProgressBar.setProgress(progress);
                tvBudgetInfo.setText(String.format(Locale.getDefault(),
                        "BUDGET: %s%.0f of %s%.0f used", symbol, (float) spentVal, symbol, budget));
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
    public void fragment1ClickListner(Expenses expenses) {
        PopupWindow popupWindow = new PopupWindow(context);
        View popupView = LayoutInflater.from(context).inflate(R.layout.fragment_popup, null);
        popupWindow.setContentView(popupView);
        popupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.rectangle_shape, null));
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);

        RecyclerView subcategoryRecyclerView = popupView.findViewById(R.id.popUpRecycleView);
        subcategoryRecyclerView.setHasFixedSize(true);
        subcategoryRecyclerView.setLayoutManager(new LinearLayoutManager(context));

        TextView textView = popupView.findViewById(R.id.popUpCategoryName);
        textView.setText(expenses.getCategory());
        textView.setTextColor(expenses.isType()
                ? ContextCompat.getColor(context, R.color.green)
                : ContextCompat.getColor(context, R.color.red));

        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        ViewGroup root = (ViewGroup) requireActivity().getWindow().getDecorView().getRootView();
        Drawable dim = new ColorDrawable(Color.LTGRAY);
        dim.setBounds(0, 0, root.getWidth(), root.getHeight());
        dim.setAlpha(100);
        root.getOverlay().add(dim);

        // Load subcats on background thread
        com.tcssol.expensetracker.Data.ExpensesDatabase.databaseWriterExecutor.execute(() -> {
            List<Expenses> subCats = expenseViewModel.getSubCatsF(month, year, expenses.getCategory(), expenses.isType());
            requireActivity().runOnUiThread(() -> {
                PopUpRecycleViewAdapter recycleViewAdapterPop = new PopUpRecycleViewAdapter(subCats, context);
                subcategoryRecyclerView.setAdapter(recycleViewAdapterPop);
            });
        });

        popupWindow.setOnDismissListener(() -> root.getOverlay().clear());
        popupView.setOnClickListener(v -> popupWindow.dismiss());
    }

    @Override
    public void fragment1LongClickListner(Expenses expenses) {
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