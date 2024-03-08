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
    public EditAdapterSubCategory(Context context, List<String> list, OnEditSubItemClickListner editItemClickListner){
        this.list=list;
        this.context=context;
        this.EditSubItemClickListner=editItemClickListner;
        Log.d("EditAdapterSubCategory","Created EditAdapterAdapter");
    }
    @NonNull
    @Override
    public EditAdapterSubCategory.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.edit_adapter_item,parent,false);
        Log.d("EditAdapter","CreatingView");
        return new EditAdapterSubCategory.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EditAdapterSubCategory.ViewHolder holder, int position) {
        Log.d("EditAdapter","Binding View"+position);
        if(position==list.size()){
            holder.text.setText("Add Item");
            holder.text.setVisibility(View.GONE);
            holder.delete.setVisibility(View.GONE);
            holder.insert.setVisibility(View.VISIBLE);
        }else if(list.get(position).equals("General")){
            holder.delete.setVisibility(View.GONE);
            holder.text.setText(list.get(position));
        }else{
            holder.text.setText(list.get(position));
            holder.text.setVisibility(View.VISIBLE);
            holder.delete.setVisibility(View.VISIBLE);
            holder.insert.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return list.size()+1;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public TextView text;
        public ImageButton delete;
        public Button insert;
        OnEditSubItemClickListner onEditSubItemClickListner;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            onEditSubItemClickListner=EditSubItemClickListner;
            text=itemView.findViewById(R.id.textView12);
            delete=itemView.findViewById(R.id.imageButtonCross);
            insert=itemView.findViewById(R.id.buttonAddEditAdapter);
            insert.setOnClickListener(this);
            delete.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int id=v.getId();
            if(id==R.id.imageButtonCross){
                String str=list.get(getAdapterPosition());
                onEditSubItemClickListner.onEditSubCrossViewClick(str);
            } else if(id==R.id.buttonAddEditAdapter){
                onEditSubItemClickListner.addSubItem();

            }

        }


        @Override
        public boolean onLongClick(View v) {
            Snackbar.make(v,"Helloooo",Snackbar.LENGTH_LONG).show();
            text.setVisibility(View.GONE);
            delete.setVisibility(View.VISIBLE);
            return false;
        }
    }
}

