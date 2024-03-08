package com.tcssol.expensetracker.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.tcssol.expensetracker.Data.ExpenseDao;
import com.tcssol.expensetracker.Model.Expenses;
import com.tcssol.expensetracker.R;

import java.util.Currency;
import java.util.List;
import java.util.Locale;

public class Frag1RcvAdapter extends RecyclerView.Adapter<Frag1RcvAdapter.ViewHolder>{
    public List<Expenses> expensesList;
    public final Context mContext;
    private ExpenseDao expenseDao;
    private Fragment1ClickListner fragment1ClickListner;

    public Frag1RcvAdapter(List<Expenses> expensesList, Context mContext, ExpenseDao expenseDao,Fragment1ClickListner fragment1ClickListner) {
        this.mContext = mContext;
        this.expenseDao=expenseDao;
        this.expensesList=expensesList;
        this.fragment1ClickListner=fragment1ClickListner;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment1item,parent,false);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int color;
        Expenses expenses=expensesList.get(position);
        holder.category.setText(expenses.getCategory());
        holder.subCategory.setText(expenses.getSubCategory());
        String symbol= Currency.getInstance(Locale.getDefault()).getSymbol();

        if(expenses.isType()==true) {
            color = Color.GREEN;
            holder.amount.setText(symbol+String.valueOf(expenses.getAmount()));
        }
        else {
            color = Color.RED;
            holder.amount.setText("-"+symbol+String.valueOf(expenses.getAmount()));
        }
//        holder.category.setTextColor(color);
        holder.amount.setTextColor(color);



    }


    @Override
    public int getItemCount() {
        return expensesList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public TextView category;
        public TextView subCategory;
        public TextView amount;
        public Fragment1ClickListner click;
        public ViewHolder(@NonNull View itemView) {

            super(itemView);
            category=itemView.findViewById(R.id.category_text);
            subCategory=itemView.findViewById(R.id.sub_category_txt);
            amount=itemView.findViewById(R.id.amount_txt);
            click=fragment1ClickListner;
            itemView.getRootView().setOnClickListener(this);
            itemView.getRootView().setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            click.fragment1ClickListner(expensesList.get(getAdapterPosition()));

        }

        @Override
        public boolean onLongClick(View v) {
            click.fragment1LongClickListner(expensesList.get(getAdapterPosition()));
            return false;
        }
    }
}
