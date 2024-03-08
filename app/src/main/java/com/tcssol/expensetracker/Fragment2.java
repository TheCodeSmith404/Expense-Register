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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.PopupWindow;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.tcssol.expensetracker.Adapters.Frag2RcvAdapter;
import com.tcssol.expensetracker.Adapters.Fragment2ClickListner;
import com.tcssol.expensetracker.Data.PersonExpDao;
import com.tcssol.expensetracker.Model.ExpenseViewModel;
import com.tcssol.expensetracker.Model.PersonExp;
import com.tcssol.expensetracker.Model.PersonExpViewModel;
import com.tcssol.expensetracker.Model.SharedExpenseViewModel;

public class Fragment2 extends Fragment implements Fragment2ClickListner{
    private PersonExpViewModel personExpViewModel;
    private SharedExpenseViewModel sharedExpenseViewModel;
    private RecyclerView recyclerView;
    private Switch aSwitch;
    private Frag2RcvAdapter adapter;
    public PersonExpDao expDao;
    private View view;
    private Context context;
    private ActivityResultLauncher<Intent> someActivityResultLauncher;
    Fragment2ClickListner clicks;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment2,container,false);
        recyclerView=view.findViewById(R.id.frag2Recycle);
        aSwitch=view.findViewById(R.id.switch2);
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
                        // Handle the activity result here
                    }
                }
        );
        /*
        Using view model to update the ui based on user action
        This is not perfect and need optimization but as of right now it works
         */

        /*
        I could use methods to clean the code and make it more readable but do not want to break the code again :/
         */

        sharedExpenseViewModel=new ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication()).create(SharedExpenseViewModel.class);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        clicks=this;
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d("Sup","Inside Switch Changed");

                if (!sharedExpenseViewModel.getObject().isInitialized()) {
                    Log.d("Sup","Shared View Not initialized");
                    if(aSwitch.isChecked()){
                        Log.d("Sup","Switch Checked");
                        personExpViewModel.getAllExpensesGrouped().observe(getViewLifecycleOwner(),data->{
                            if(data!=null) {
                                    adapter = new Frag2RcvAdapter(context, expDao, data, clicks, 2);
                                    recyclerView.setAdapter(adapter);
                            }
                        });
                    }else{
                        Log.d("Sup","Switch Not Checked");
                        personExpViewModel.getAllExpenses().observe(getViewLifecycleOwner(),data->{
                            if(data!=null) {

                                    adapter = new Frag2RcvAdapter(context, expDao, data, clicks, 1);
                                    recyclerView.setAdapter(adapter);

                            }
                        });
                    }

                } else {
                    sharedExpenseViewModel.getObject().observe(getViewLifecycleOwner(), expensesList -> {
                        Log.d("Sup","Shared View initialized");

                        if ((expensesList.getYear() > 0 || expensesList.getMonth() > 0) && expensesList.getYear() != 2021) {
                            if (aSwitch.isChecked()) {
                                personExpViewModel.getAllExpensesGroupedFiltered(String.valueOf(expensesList.getMonth()), String.valueOf(expensesList.getYear())).observe(getViewLifecycleOwner(), tasks -> {
                                    if(tasks!=null) {

                                            adapter = new Frag2RcvAdapter(context, expDao, tasks, clicks, 2);
                                            recyclerView.setAdapter(adapter);

                                    }
                                });
                            } else {
                                personExpViewModel.getAllExpensesFiltered(String.valueOf(expensesList.getMonth()), String.valueOf(expensesList.getYear())).observe(getViewLifecycleOwner(), tasks -> {
                                    if(tasks!=null) {
                                        Log.d("Sup","Adapter Changed 7");

                                            adapter = new Frag2RcvAdapter(context, expDao, tasks, clicks, 1);
                                            recyclerView.setAdapter(adapter);

                                    }
                                });
                            }
                        } else {
                            if (aSwitch.isChecked()) {
                                personExpViewModel.getAllExpensesGrouped().observe(getViewLifecycleOwner(), data -> {
                                    if(data!=null) {
                                        Log.d("Sup","Adapter Changed 6");

                                            adapter = new Frag2RcvAdapter(context, expDao, data, clicks, 2);
                                            recyclerView.setAdapter(adapter);

                                    }
                                });

                            } else {
                                personExpViewModel.getAllExpenses().observe(getViewLifecycleOwner(), expensesList1 -> {
                                    if(expensesList1!=null) {
                                        Log.d("Sup","Adapter Changed 5");

                                            adapter = new Frag2RcvAdapter(context, expDao, expensesList1, clicks, 1);
                                            recyclerView.setAdapter(adapter);

                                    }
                                });
                            }
                        }
                    });
                }
            }
        });

        personExpViewModel=new ViewModelProvider(this).get(PersonExpViewModel.class);
        expDao=personExpViewModel.getPersonExpDao();
        personExpViewModel.getAllExpenses().observe(getViewLifecycleOwner(),tasks->{
            if(tasks!=null) {
                Log.d("Sup","Adapter Changed 4");

                    adapter = new Frag2RcvAdapter(context, expDao, tasks, this, 1);
                    recyclerView.setAdapter(adapter);

            }
        });

        sharedExpenseViewModel.getObject().observe(getViewLifecycleOwner(),expensesList->{
            Log.d("Sup","getObject Changed");

            if((expensesList.getYear()>0||expensesList.getMonth()>0)) {
                Log.d("Sup","inside getObject valid Dates");
                if(aSwitch.isChecked()){
                    Log.d("Sup","inside getObject Changed switch is checked");
                    personExpViewModel.getAllExpensesGroupedFiltered(String.valueOf(expensesList.getMonth()),String.valueOf(expensesList.getYear())).observe(getViewLifecycleOwner(),tasks->{
                        if(tasks!=null) {
                            Log.d("Sup","Adapter Changed 3");

                                adapter = new Frag2RcvAdapter(context, expDao, tasks, this, 2);
                                recyclerView.setAdapter(adapter);

                        }
                    });
                }else{
                    Log.d("Sup","inside getObject Changed switch is not checked");
                personExpViewModel.getAllExpensesFiltered(String.valueOf(expensesList.getMonth()),String.valueOf(expensesList.getYear())).observe(getViewLifecycleOwner(), tasks -> {
                    Log.d("Sup","inside getObject Changed valid dates switch is not checked and view model object change");
                    Log.d("Sup",String.valueOf(tasks!=null)+" "+tasks.size());
                    if(tasks!=null) {

                            Log.d("Sup", "Adapter Changed 2");
                            adapter = new Frag2RcvAdapter(context, expDao, tasks, this, 1);
                            recyclerView.setAdapter(adapter);

                    }
                });}
            }else{

                Log.d("Sup","inside getObject invalid Dates");
                if(aSwitch.isChecked()){
                    Log.d("Sup","inside getObject Changed switch is checked");
                    personExpViewModel.getAllExpensesGrouped().observe(getViewLifecycleOwner(),data->{
                        if(data!=null) {

                                Log.d("Sup", "Adapter Changed 1");
                                adapter = new Frag2RcvAdapter(context, expDao, data, clicks, 2);
                                recyclerView.setAdapter(adapter);

                        }

                    });

                }else {
                    Log.d("Sup","inside getObject Changed switch is checked");
                    personExpViewModel.getAllExpenses().observe(getViewLifecycleOwner(), expensesList1 -> {
                        if(expensesList1!=null) {

                                Log.d("Sup", "Adapter Changed 0");
                                adapter = new Frag2RcvAdapter(context, expDao, expensesList1, this, 1);
                                recyclerView.setAdapter(adapter);

                        }
                    });
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        aSwitch.setChecked(false);
    }

    @Override
    public void fragment2ClickListner(PersonExp personExp) {
        Intent intent=new Intent(context,CreateExpenses.class);
        intent.putExtra("Type",2);
        intent.putExtra("TypeExpense", personExp.getType());
        intent.putExtra("Name",personExp.getName());
        intent.putExtra("ContactNumber",personExp.getContactNumber());
        intent.putExtra("Medium", personExp.getMode());
        someActivityResultLauncher.launch(intent);
    }

    @Override
    public void fragment2LongClickListner(PersonExp personExp,int position) {
        LinearLayoutManager linearLayoutManager=(LinearLayoutManager)recyclerView.getLayoutManager();
        assert linearLayoutManager != null;
        int scroll=linearLayoutManager.findFirstVisibleItemPosition();
        PersonExpViewModel.delete(personExp);
        Snackbar snackbar = Snackbar.make(view, "Item deleted", Snackbar.LENGTH_LONG);
        adapter.notifyItemRemoved(position);
        Handler handler=new Handler();
        Runnable run=new Runnable() {
            @Override
            public void run() {
                recyclerView.scrollToPosition(scroll);
            }
        };
        handler.postDelayed(run,200);


        Log.d("Sup","Item Deleted");
        snackbar.setAction("Undo", new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d("Sup","Item Deleted");
                PersonExpViewModel.insert(personExp);
                adapter.notifyItemInserted(position);
                handler.postDelayed(run,200);
            }
        });
        snackbar.show();
    }

    /**
     *TODO Align the window find another way
     */

    @Override
    public void fragment2PopupClick(PopupWindow window,View v) {
        window.showAsDropDown(v);

    }
}