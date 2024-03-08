package com.tcssol.expensetracker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.snackbar.Snackbar;
import com.tcssol.expensetracker.Adapters.Frag1RcvAdapter;
import com.tcssol.expensetracker.Adapters.Frag4RcvAdapter;
import com.tcssol.expensetracker.Adapters.Fragment4ClickListner;
import com.tcssol.expensetracker.Data.ExpenseDao;
import com.tcssol.expensetracker.Model.ExpenseViewModel;
import com.tcssol.expensetracker.Model.Expenses;
import com.tcssol.expensetracker.Model.SharedExpenseViewModel;

import java.time.LocalDate;


public class Fragment4 extends Fragment implements Fragment4ClickListner {
    private ExpenseViewModel expenseViewModel;
    private RecyclerView recyclerView;
    private Frag4RcvAdapter adapter;
    public ExpenseDao expenseDao;
    private View view;
    private Context context;
    private ActivityResultLauncher<Intent> someActivityResultLauncher;
    private SharedExpenseViewModel sharedExpenseViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment4,container,false);
        recyclerView=view.findViewById(R.id.frag4Recycle);
        context=requireContext();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        someActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                    }
                }
        );
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        expenseViewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);
        expenseDao=expenseViewModel.getExpenseDao();
        expenseViewModel.getAllExpenses().observe(getViewLifecycleOwner(), tasks -> {
                adapter = new Frag4RcvAdapter(context, expenseDao, tasks, this);
                recyclerView.setAdapter(adapter);
        });
        sharedExpenseViewModel=new ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication()).create(SharedExpenseViewModel.class);
        sharedExpenseViewModel.getObject().observe(getViewLifecycleOwner(),expensesList-> {
            int month = expensesList.getMonth();
            int year = expensesList.getYear();
            if((month>0||expensesList.getMonth()>0)&&year!=2021) {
                LocalDate date = LocalDate.of(year,month, 01);
                expenseViewModel.getAllExpensesMonthly(date).observe(getViewLifecycleOwner(), tasks -> {
                        adapter = new Frag4RcvAdapter(context, expenseDao, tasks, Fragment4.this);
                        recyclerView.setAdapter(adapter);
                });
            }else{
                expenseViewModel.getAllExpenses().observe(getViewLifecycleOwner(),expensesList1 -> {
                        adapter = new Frag4RcvAdapter(context, expenseDao, expensesList1, Fragment4.this);
                        recyclerView.setAdapter(adapter);
                });
            }
        });
    }

    @Override
    public void fragment4ClickListner(Expenses expenses) {
        Intent intent=new Intent(context,CreateExpenses.class);
        intent.putExtra("Type", 4);
        intent.putExtra("Medium", expenses.getMode());
        intent.putExtra("TypeExpense", expenses.isType());
        if(!expenses.getCategory().equals("Money Received")&&!expenses.getCategory().equals("Money Given")) {
            intent.putExtra("ChangeView", 0);
            intent.putExtra("Category", expenses.getCategory());
            intent.putExtra("SubCategory", expenses.getSubCategory());
        }else{
            intent.putExtra("Type",4);
            intent.putExtra("ChangeView",1);
            intent.putExtra("Name",expenses.getSubCategory());
        }
        someActivityResultLauncher.launch(intent);
    }

    @Override
    public void fragment4LongClickListner(Expenses expenses,int position) {
        ExpenseViewModel.delete(expenses);
        LinearLayoutManager linearLayoutManager=(LinearLayoutManager)recyclerView.getLayoutManager();
        assert linearLayoutManager != null;
        int scroll=linearLayoutManager.findFirstVisibleItemPosition();
        adapter.notifyItemRemoved(position);
        Handler handler=new Handler();
        Runnable run=new Runnable() {
            @Override
            public void run() {
                recyclerView.scrollToPosition(scroll);
            }
        };
        handler.postDelayed(run,200);

        recyclerView.scrollToPosition(position);
        Snackbar snackbar = Snackbar.make(view, "Item deleted", Snackbar.LENGTH_LONG);
        snackbar.setAction("Undo", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expenseDao.insertExpense(expenses);
                adapter.notifyItemInserted(position);
                handler.postDelayed(run,200);
                // Undo action
            }
        });
        snackbar.show();

    }
}