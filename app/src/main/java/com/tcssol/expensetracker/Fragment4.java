package com.tcssol.expensetracker;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.tcssol.expensetracker.Adapters.Fragment4ClickListner;
import com.tcssol.expensetracker.Adapters.Frag4RcvAdapter;
import com.tcssol.expensetracker.Data.ExpenseDao;
import com.tcssol.expensetracker.Model.ExpenseViewModel;
import com.tcssol.expensetracker.Model.Expenses;
import com.tcssol.expensetracker.Model.SharedExpenseViewModel;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

/**
 * Fragment4 – All Entries tab with v2.0 search bar.
 */
public class Fragment4 extends Fragment implements Fragment4ClickListner {

    private ExpenseViewModel expenseViewModel;
    private SharedExpenseViewModel sharedExpenseViewModel;
    private RecyclerView recyclerView;
    private Frag4RcvAdapter adapter;
    private ExpenseDao expenseDao;
    private TextInputEditText searchEditText;
    private Context context;
    private ActivityResultLauncher<Intent> editLauncher;

    // Track current LiveData source to avoid stacking observers
    private LiveData<List<Expenses>> currentLiveData;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment4, container, false);
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

        sharedExpenseViewModel = new ViewModelProvider.AndroidViewModelFactory(requireActivity().getApplication())
                .create(SharedExpenseViewModel.class);

        // Observe filter changes
        sharedExpenseViewModel.getObject().observe(getViewLifecycleOwner(), wrapped -> {
            // Clear search when filter changes
            if (searchEditText.getText() != null) searchEditText.setText("");

            if (wrapped.getMonth() > 0 && wrapped.getYear() > 0) {
                LocalDate date = LocalDate.of(wrapped.getYear(), wrapped.getMonth(), 1);
                attachList(expenseViewModel.getAllExpensesMonthly(date));
            } else {
                attachList(expenseViewModel.getAllExpenses());
            }
        });

        // Initial: show all
        attachList(expenseViewModel.getAllExpenses());

        // Search watcher
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
            adapter = new Frag4RcvAdapter(context, expenseDao, list, this);
            recyclerView.setAdapter(adapter);
        });
    }

    @Override
    public void fragment4ClickListner(Expenses expenses) {
        // Open CreateExpenses in edit mode (pre-fill via ID)
        Intent intent = new Intent(getActivity(), CreateExpenses.class);
        intent.putExtra("Type", expenses.isType() ? 1 : 0);
        intent.putExtra("edit_mode", true);
        intent.putExtra("expense_id", expenses.getId());
        editLauncher.launch(intent);
    }

    @Override
    public void fragment4LongClickListner(Expenses expenses, int position) {
        // Delete + Undo snackbar
        ExpenseViewModel.delete(expenses);
        View rootView = requireActivity().getWindow().getDecorView().getRootView();
        com.google.android.material.snackbar.Snackbar.make(rootView, "Entry deleted", com.google.android.material.snackbar.Snackbar.LENGTH_LONG)
                .setAction("UNDO", v -> ExpenseViewModel.insert(expenses, context))
                .show();
    }
}