package com.tcssol.expensetracker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.HorizontalScrollView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.chip.Chip;
import com.tcssol.expensetracker.Adapters.Frag1RcvAdapter;
import com.tcssol.expensetracker.Adapters.Fragment1ClickListner;
import com.tcssol.expensetracker.Adapters.PopUpRecycleViewAdapter;
import com.tcssol.expensetracker.Data.ExpenseDao;
import com.tcssol.expensetracker.Model.ExpenseViewModel;
import com.tcssol.expensetracker.Model.Expenses;
import com.tcssol.expensetracker.Model.SharedExpenseViewModel;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Fragment1 extends Fragment implements Fragment1ClickListner {
    private ExpenseViewModel expenseViewModel;
    private SharedExpenseViewModel sharedExpenseViewModel;
    private RecyclerView recyclerView;
    private Frag1RcvAdapter adapter;
    public ExpenseDao expenseDao;
    private View view;
    private Context context;
    TextView testing;
    private ActivityResultLauncher<Intent> someActivityResultLauncher;
    int month=-12;
    int year=-2024;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment1,container,false);

        recyclerView=view.findViewById(R.id.frag1Recycle);
        context=requireContext();
        testing=view.findViewById(R.id.textView);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        /*
        Setting up viewmodels,and recycler view for activity to display content
         */
        someActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        // Handle the activity result here
                    }
                }
        );
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        expenseViewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);
        expenseDao=expenseViewModel.getExpenseDao();
        expenseViewModel.getAllExpensesGrouped().observe(getViewLifecycleOwner(),expensesList1 -> {
            adapter=new Frag1RcvAdapter(expensesList1,context,expenseDao,this);
            recyclerView.setAdapter(adapter);
        });

        sharedExpenseViewModel=new ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication()).create(SharedExpenseViewModel.class);
        sharedExpenseViewModel.getObject().observe(getViewLifecycleOwner(),expensesList->{
            month=expensesList.getMonth();
            year=expensesList.getYear();
            testing.setText(expensesList.getMonth()+" "+expensesList.getYear());

            if((expensesList.getYear()>0||expensesList.getMonth()>0)&&expensesList.getYear()!=2021) {
                LocalDate date = LocalDate.of(expensesList.getYear(), expensesList.getMonth(), 01);
                expenseViewModel.getAllExpensesGroupedMonthly(date).observe(getViewLifecycleOwner(), tasks -> {
                    adapter = new Frag1RcvAdapter(tasks, context, expenseDao,this);
                    recyclerView.setAdapter(adapter);
                });
            }else{
                expenseViewModel.getAllExpensesGrouped().observe(getViewLifecycleOwner(),expensesList1 -> {
                    adapter=new Frag1RcvAdapter(expensesList1,context,expenseDao,this);
                    recyclerView.setAdapter(adapter);
                });
            }


        });

    }

    @Override
    public void fragment1ClickListner(Expenses expenses) {
        PopupWindow popupWindow = new PopupWindow(context);
// Creating the pop up window
        View popupView = LayoutInflater.from(context).inflate(R.layout.fragment_popup, null);

        popupWindow.setContentView(popupView);
        popupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.rectangle_shape,null));
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        RecyclerView subcategoryRecyclerView = popupView.findViewById(R.id.popUpRecycleView);
        subcategoryRecyclerView.setHasFixedSize(true);
        subcategoryRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        TextView textView=popupView.findViewById(R.id.popUpCategoryName);
        textView.setText(expenses.getCategory());
        if(expenses.isType()==false){
            textView.setTextColor(getResources().getColor(R.color.red));
        }else
            textView.setTextColor(getResources().getColor(R.color.green));
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        ViewGroup root = (ViewGroup) requireActivity().getWindow().getDecorView().getRootView();
        Drawable dim = new ColorDrawable(Color.LTGRAY);
        dim.setBounds(0, 0, root.getWidth(), root.getHeight());
        dim.setAlpha((int) (100));
        root.getOverlay().add(dim);
        PopUpRecycleViewAdapter  recycleViewAdapterPop=new PopUpRecycleViewAdapter(expenseViewModel.getSubCatsF(month,year,expenses.getCategory(),expenses.isType()),context);
        subcategoryRecyclerView.setAdapter(recycleViewAdapterPop);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                root.getOverlay().clear();
            }
        });

        popupView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    popupWindow.dismiss();
                    return true;
                }
                return false;
            }
        });

    }

    @Override
    public void fragment1LongClickListner(Expenses expenses) {
        Intent intent= new Intent(getActivity(),CreateExpenses.class);
        if(expenses.getCategory().equals("Money Received")||expenses.getCategory().equals("Money Given")){
            intent.putExtra("Type",1);
            intent.putExtra("ChangeView",1);
            intent.putExtra("TypeExpense", expenses.isType());
            someActivityResultLauncher.launch(intent);

        }else {
            intent.putExtra("Type", 1);
            intent.putExtra("ChangeView",0);
            intent.putExtra("Category", expenses.getCategory());
            intent.putExtra("TypeExpense", expenses.isType());
            someActivityResultLauncher.launch(intent);
        }

    }

}