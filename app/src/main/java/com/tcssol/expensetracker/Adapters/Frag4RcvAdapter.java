package com.tcssol.expensetracker.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.tcssol.expensetracker.Data.ExpenseDao;
import com.tcssol.expensetracker.Model.Expenses;
import com.tcssol.expensetracker.R;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

public class Frag4RcvAdapter extends RecyclerView.Adapter<Frag4RcvAdapter.ViewHolder>{
    public List<Expenses> expensesList;
    public List<Expenses> expensesListNew;
    public final Context mContext;
    private ExpenseDao expenseDao;
    private Fragment4ClickListner fragment4ClickListner;
    private int dateCount=1;

    public Frag4RcvAdapter(Context mContext, ExpenseDao expenseDao,List<Expenses> expensesList,Fragment4ClickListner fragment4ClickListner) {
        this.mContext = mContext;
        this.expenseDao = expenseDao;
        this.expensesList=expensesList;
        if(expensesList!=null&expensesList.size()!=0)
            this.expensesListNew=getNewList(expensesList);
        this.fragment4ClickListner=fragment4ClickListner;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if(viewType==1) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment4item, parent, false);
        }else{
            view=LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment4date,parent,false);
        }
        return new ViewHolder(view,viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int color;
        Expenses expenses=expensesListNew.get(position);
        if(expenses.getCategory().equals("_*_")){
            LocalDate date=expenses.getDateCreated();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");

            holder.date.setText(date.format(formatter));
        }else {
            String symbol = Currency.getInstance(Locale.getDefault()).getSymbol();
            if (expenses.isType() == true) {
                holder.amount.setText(symbol + expenses.getAmount());
                color = Color.GREEN;

            } else {
                holder.amount.setText("-" + symbol + expenses.getAmount());
                color = Color.RED;
            }
            holder.mode.setText(expenses.getMode());
            holder.amount.setTextColor(color);
            holder.category.setText(expenses.getCategory());
            holder.subCategory.setText(expenses.getSubCategory());
        }

    }


    @Override
    public int getItemCount() {
        int size;
        if(expensesListNew==null)
            size=0;
        else
            size=expensesListNew.size();
        return size;
    }

    @Override
    public int getItemViewType(int position) {
        Expenses temp=expensesListNew.get(position);
        if(temp.getCategory().equals("_*_"))
            return 0;
        else
            return 1;

    }

    public List<Expenses> getNewList(List<Expenses> list) {
        List<Expenses> ret = new ArrayList<>();
        Expenses test = new Expenses(list.get(0).getDateCreated(), "_*_", null, null, 0, false);
        ret.add(0, test);
        ret.add(1, list.get(0));
        Log.d("Recycle Date",Expenses.toTxtFormat(test));
        Log.d("Recycle Date",Expenses.toTxtFormat(list.get(0)));
        for (int i = 1; i < list.size(); i++) {
            Expenses temp = list.get(i);
            Expenses temp2 = list.get(i - 1);
            if (temp.getDateCreated().isEqual(temp2.getDateCreated())) {
                ret.add(temp);
                Log.d("Recycle Date",Expenses.toTxtFormat(temp));
            } else {
                test = new Expenses(temp.getDateCreated(), "_*_", null, null, 0, false);
                Log.d("Recycle Date",Expenses.toTxtFormat(test));
                Log.d("Recycle Date",Expenses.toTxtFormat(temp));
                ret.add(test);
                ret.add(temp);
            }
        }
    return ret;
    }



    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener, View.OnClickListener {
        public TextView category;
        public TextView subCategory;
        public TextView amount;
        public TextView mode;
        public TextView date;
        public Fragment4ClickListner clickListner;
        public ViewHolder(@NonNull View itemView,int type) {
            super(itemView);
            if(type==1) {
                category = itemView.findViewById(R.id.frag4category_text);
                subCategory = itemView.findViewById(R.id.frag4sub_category_txt);
                amount = itemView.findViewById(R.id.frag4amount_txt);
                mode = itemView.findViewById(R.id.frag4Mode);
                clickListner = fragment4ClickListner;
                itemView.getRootView().setOnLongClickListener(this);
                itemView.getRootView().setOnClickListener(this);
            }else{
                date=itemView.findViewById(R.id.textViewDate);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            Expenses data=expensesListNew.get(getAdapterPosition());
            clickListner.fragment4LongClickListner(data,getAdapterPosition());
            return false;
        }

        @Override
        public void onClick(View v) {
            Expenses data=expensesListNew.get(getAdapterPosition());
            clickListner.fragment4ClickListner(data);
        }
    }
}