package com.tcssol.expensetracker.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tcssol.expensetracker.Data.ExpenseDao;
import com.tcssol.expensetracker.Model.Expenses;
import com.tcssol.expensetracker.R;

import java.util.Currency;
import java.util.List;
import java.util.Locale;

import kotlin.jvm.internal.Lambda;

public class PopUpRecycleViewAdapter extends RecyclerView.Adapter<PopUpRecycleViewAdapter.ViewHolder> {
    private List<Expenses> allExpenses;
    public final Context context;

    public PopUpRecycleViewAdapter(List<Expenses> allExpenses, Context context) {
        Log.d("PopUpop","Adapter Initialized");
        this.allExpenses = allExpenses;
        this.context = context;
    }

    @NonNull
    @Override
    public PopUpRecycleViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v=LayoutInflater.from(parent.getContext()).inflate(R.layout.popup_recycle_item,parent,false);
        Log.d("PopUpop","View Created");
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PopUpRecycleViewAdapter.ViewHolder holder, int position) {
        Log.d("PopUpop","View Bound");
        Expenses data=allExpenses.get(position);
        holder.name.setText(data.getSubCategory());
        String currency=Currency.getInstance(Locale.getDefault()).getSymbol();
        if(data.isType())
        holder.amount.setText(currency +String.valueOf(data.getAmount()));
        else
            holder.amount.setText("-"+currency +String.valueOf(data.getAmount()));
    }

    @Override
    public int getItemCount() {
        return allExpenses.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView name;
        private TextView amount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            Log.d("PopUpop","In View Holder");
            name=itemView.findViewById(R.id.textView17);
            amount=itemView.findViewById(R.id.textView18);
        }
    }
}
