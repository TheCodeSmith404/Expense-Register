package com.tcssol.expensetracker;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.tcssol.expensetracker.Adapters.ModeDistributionAdapter;
import com.tcssol.expensetracker.Model.ExpenseViewModel;
import com.tcssol.expensetracker.Model.Expenses;
import com.tcssol.expensetracker.Model.PersonExp;
import com.tcssol.expensetracker.Model.PersonExpViewModel;
import com.tcssol.expensetracker.Model.SharedExpenseViewModel;
import com.tcssol.expensetracker.Utils.DataBaseExporter;

import java.util.Currency;
import java.util.List;
import java.util.Locale;

/**
 * TODO Add charts and other views to show trends and options to set budgets
 */
public class Fragment3 extends Fragment {
    private TextView amtEarned;
    private TextView amtSpend;
    private TextView amtGiven;
    private TextView amtReceived;
    private ExpenseViewModel expenseViewModel;
    private SharedExpenseViewModel sharedExpenseViewModel;
    private PersonExpViewModel personExpViewModel;

    private RecyclerView modeRecycleView;

    private View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment3,container,false);
        amtEarned=view.findViewById(R.id.frag3SetAmtEarned);
        amtSpend=view.findViewById(R.id.frag3SetAmtSpend);
        amtGiven=view.findViewById(R.id.frag3SetAmtGiven);
        amtReceived=view.findViewById(R.id.frag3SetAmtReceived);
        modeRecycleView=view.findViewById(R.id.modeRecycleViewfFrag3);
        return view;
    }
    /*
    TODO Kotlin coroutines for background updates??
     */

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        personExpViewModel=new ViewModelProvider(this).get(PersonExpViewModel.class);
        expenseViewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);
        String symbol= Currency.getInstance(Locale.getDefault()).getSymbol();
        expenseViewModel.getFrag3Data().observe(getViewLifecycleOwner(),list-> {
                    amtEarned.setText(symbol+String.valueOf(list.get(0) == null ? 0 : list.get(0)));
                    amtSpend.setText("-"+symbol+String.valueOf(list.get(1) == null ? 0 : list.get(1)));
                    amtReceived.setText(symbol+String.valueOf(list.get(2) == null ? 0 : list.get(2)));
                    amtGiven.setText("-"+symbol+String.valueOf(list.get(3) == null ? 0 : list.get(3)));
                });

        sharedExpenseViewModel=new ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication()).create(SharedExpenseViewModel.class);
        sharedExpenseViewModel.getObject().observe(getViewLifecycleOwner(),data->{
            if(data.getYear()>0||data.getMonth()>0){
                Log.d("expdao",data.getMonth()+" Data in frag3 "+data.getYear());
                expenseViewModel.getFrag3DataFiltered(data.getMonth(),data.getYear()).observe(getViewLifecycleOwner(),list->{
                    amtEarned.setText(symbol+String.valueOf(list.get(0)==null?0:list.get(0)));
                    amtSpend.setText("-"+symbol+String.valueOf(list.get(1)==null?0:list.get(1)));
                    amtReceived.setText(symbol+String.valueOf(list.get(2)==null?0:list.get(2)));
                    amtGiven.setText("-"+symbol+String.valueOf(list.get(3)==null?0:list.get(3)));
                });

            }
            else{
                expenseViewModel.getFrag3Data().observe(getViewLifecycleOwner(),list->{
                    amtEarned.setText(String.valueOf(list.get(0)==null?0:list.get(0)));
                    amtSpend.setText("-"+String.valueOf(list.get(1)==null?0:list.get(1)));
                    amtReceived.setText(String.valueOf(list.get(2)==null?0:list.get(2)));
                    amtGiven.setText("-"+String.valueOf(list.get(3)==null?0:list.get(3)));
                });
            }
        });
        modeRecycleView.setHasFixedSize(true);
        modeRecycleView.setLayoutManager(new LinearLayoutManager(getContext()));
        expenseViewModel.getModeDist().observe(getViewLifecycleOwner(),data->{
            ModeDistributionAdapter adapter=new ModeDistributionAdapter(getContext(),data);
            modeRecycleView.setAdapter(adapter);
        });


    }
}