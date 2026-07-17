package com.tcssol.expensetracker.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.tcssol.expensetracker.R;

import java.util.List;

public class EditAdapterSubCategory extends RecyclerView.Adapter<EditAdapterSubCategory.ViewHolder> {
    public List<String> list;
    public final Context context;
    private final OnEditSubItemClickListner EditSubItemClickListner;
    private java.util.Set<String> fixedSubCategories = new java.util.HashSet<>();

    public EditAdapterSubCategory(Context context, List<String> list, OnEditSubItemClickListner editItemClickListner){
        this.list=list;
        this.context=context;
        this.EditSubItemClickListner=editItemClickListner;
    }

    public void setFixedSubCategories(java.util.Set<String> fixedSubCategories) {
        this.fixedSubCategories = fixedSubCategories != null ? fixedSubCategories : new java.util.HashSet<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EditAdapterSubCategory.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.edit_adapter_item,parent,false);
        return new EditAdapterSubCategory.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EditAdapterSubCategory.ViewHolder holder, int position) {
        if(position==list.size()){
            holder.text.setText("Add Item");
            holder.text.setVisibility(View.GONE);
            holder.more.setVisibility(View.GONE);
            holder.fixedBadge.setVisibility(View.GONE);
            holder.insert.setVisibility(View.VISIBLE);
        }else if(list.get(position).equals("General")){
            holder.more.setVisibility(View.GONE);
            holder.text.setText(list.get(position));
            holder.text.setVisibility(View.VISIBLE);
            
            boolean isFixed = fixedSubCategories.contains(list.get(position));
            holder.fixedBadge.setVisibility(isFixed ? View.VISIBLE : View.GONE);
            holder.insert.setVisibility(View.GONE);
        }else{
            String subCat = list.get(position);
            holder.text.setText(subCat);
            holder.text.setVisibility(View.VISIBLE);
            holder.more.setVisibility(View.VISIBLE);
            
            boolean isFixed = fixedSubCategories.contains(subCat);
            holder.fixedBadge.setVisibility(isFixed ? View.VISIBLE : View.GONE);
            holder.insert.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return list.size()+1;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView text;
        public TextView fixedBadge;
        public ImageButton more;
        public Button insert;
        OnEditSubItemClickListner onEditSubItemClickListner;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            onEditSubItemClickListner=EditSubItemClickListner;
            text=itemView.findViewById(R.id.textView12);
            fixedBadge=itemView.findViewById(R.id.tvFixedBadge);
            more=itemView.findViewById(R.id.imageButtonMore);
            insert=itemView.findViewById(R.id.buttonAddEditAdapter);
            insert.setOnClickListener(this);
            more.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int id=v.getId();
            if(id==R.id.imageButtonMore){
                String str=list.get(getAdapterPosition());
                onEditSubItemClickListner.onSubCategoryOptionsClick(str, more);
            } else if(id==R.id.buttonAddEditAdapter){
                onEditSubItemClickListner.addSubItem();
            }
        }
    }
}
