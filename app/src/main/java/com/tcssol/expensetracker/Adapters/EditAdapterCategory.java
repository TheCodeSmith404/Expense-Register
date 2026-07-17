package com.tcssol.expensetracker.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tcssol.expensetracker.R;

import java.util.List;

public class EditAdapterCategory extends RecyclerView.Adapter<EditAdapterCategory.ViewHolder> {
    public List<String> list;
    public final Context context;
    private final OnEditItemClickListner EditItemClickListner;
    public java.util.Set<String> fixedCategories = new java.util.HashSet<>();

    public EditAdapterCategory(Context context, List<String> list, OnEditItemClickListner editItemClickListner){
        this.list=list;
        this.context=context;
        this.EditItemClickListner=editItemClickListner;
    }

    public void setFixedCategories(java.util.Set<String> fixedCategories) {
        this.fixedCategories = fixedCategories != null ? fixedCategories : new java.util.HashSet<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.edit_adapter_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if(position==list.size()){
            holder.text.setText("Add Item");
            holder.text.setVisibility(View.GONE);
            holder.more.setVisibility(View.GONE);
            holder.fixedBadge.setVisibility(View.GONE);
            holder.insert.setVisibility(View.VISIBLE);
        }else if(list.get(position).equals("General")){
            holder.more.setVisibility(View.GONE); // Cannot edit or delete General
            holder.text.setText(list.get(position));
            holder.text.setVisibility(View.VISIBLE);
            
            // Check if General category itself is fixed
            boolean isFixed = fixedCategories.contains(list.get(position));
            holder.fixedBadge.setVisibility(isFixed ? View.VISIBLE : View.GONE);
            holder.insert.setVisibility(View.GONE);
        }
        else{
            String category = list.get(position);
            holder.text.setText(category);
            holder.text.setVisibility(View.VISIBLE);
            holder.more.setVisibility(View.VISIBLE);
            
            boolean isFixed = fixedCategories.contains(category);
            holder.fixedBadge.setVisibility(isFixed ? View.VISIBLE : View.GONE);
            holder.insert.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return list.size()+1;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener  {
        public TextView text;
        public TextView fixedBadge;
        public ImageButton more;
        public Button insert;
        OnEditItemClickListner onEditItemClickListner;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            onEditItemClickListner=EditItemClickListner;
            text=itemView.findViewById(R.id.textView12);
            fixedBadge=itemView.findViewById(R.id.tvFixedBadge);
            more=itemView.findViewById(R.id.imageButtonMore);
            insert=itemView.findViewById(R.id.buttonAddEditAdapter);
            text.setOnClickListener(this);
            insert.setOnClickListener(this);
            more.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int id=v.getId();
            if(id==R.id.imageButtonMore){
                String str=list.get(getAdapterPosition());
                onEditItemClickListner.onCategoryOptionsClick(str, more);
            }else if(id==R.id.textView12){
                String str=list.get(getAdapterPosition());
                onEditItemClickListner.onEditTextViewClick(str);
            }else if(id==R.id.buttonAddEditAdapter){
                onEditItemClickListner.addItem();
            }
        }
    }
}
