package com.tcssol.expensetracker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.tcssol.expensetracker.Adapters.TransactionsAdapter;
import com.tcssol.expensetracker.Adapters.TransactionsClickListener;
import com.tcssol.expensetracker.Data.ExpenseDao;
import com.tcssol.expensetracker.Model.ExpenseViewModel;
import com.tcssol.expensetracker.Model.Expenses;
import com.tcssol.expensetracker.Model.SharedExpenseViewModel;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TransactionsFragment extends Fragment implements TransactionsClickListener {

    private ExpenseViewModel expenseViewModel;
    private SharedExpenseViewModel sharedExpenseViewModel;
    private RecyclerView recyclerView;
    private TransactionsAdapter adapter;
    private ExpenseDao expenseDao;
    private TextInputEditText searchEditText;
    private Context context;
    private ActivityResultLauncher<Intent> editLauncher;

    private LiveData<List<Expenses>> currentLiveData;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transactions, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = requireContext();

        editLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> { });

        recyclerView = view.findViewById(R.id.frag4Recycle);
        searchEditText = view.findViewById(R.id.searchEditText);

        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        expenseViewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);
        expenseDao = expenseViewModel.getExpenseDao();

        sharedExpenseViewModel = new ViewModelProvider(requireActivity()).get(SharedExpenseViewModel.class);

        sharedExpenseViewModel.getObject().observe(getViewLifecycleOwner(), wrapped -> {
            if (searchEditText.getText() != null) searchEditText.setText("");

            if (wrapped.getMonth() > 0 && wrapped.getYear() > 0) {
                LocalDate date = LocalDate.of(wrapped.getYear(), wrapped.getMonth(), 1);
                attachList(expenseViewModel.getAllExpensesMonthly(date));
            } else {
                attachList(expenseViewModel.getAllExpenses());
            }
        });

        attachList(expenseViewModel.getAllExpenses());

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    attachList(expenseViewModel.getAllExpenses());
                } else {
                    attachList(expenseViewModel.search(query));
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void attachList(LiveData<List<Expenses>> source) {
        if (currentLiveData != null) {
            currentLiveData.removeObservers(getViewLifecycleOwner());
        }
        currentLiveData = source;
        source.observe(getViewLifecycleOwner(), list -> {
            if (list == null) list = new ArrayList<>();
            adapter = new TransactionsAdapter(context, expenseDao, list, this);
            recyclerView.setAdapter(adapter);
        });
    }

    @Override
    public void onTransactionClick(Expenses expenses) {
        Intent intent = new Intent(getActivity(), CreateExpenses.class);
        intent.putExtra("Type", 4);
        intent.putExtra("TypeExpense", expenses.isType());
        intent.putExtra("Category", expenses.getCategory());
        intent.putExtra("SubCategory", expenses.getSubCategory());
        intent.putExtra("Medium", expenses.getMode());
        intent.putExtra("ChangeView", 0);
        editLauncher.launch(intent);
    }

    @Override
    public void onTransactionLongClick(Expenses expenses, int position) {
        ExpenseViewModel.delete(expenses);
        View rootView = requireActivity().getWindow().getDecorView().getRootView();
        com.google.android.material.snackbar.Snackbar.make(rootView, "Entry deleted", com.google.android.material.snackbar.Snackbar.LENGTH_LONG)
                .setAction("UNDO", v -> ExpenseViewModel.insert(expenses, context))
                .show();
    }
}
