package com.tcssol.expensetracker.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tcssol.expensetracker.R;
import com.tcssol.expensetracker.Utils.ModeWrapper;

import java.util.List;

public class ModeDistributionAdapter extends RecyclerView.Adapter<ModeDistributionAdapter.ViewHolder> {
    public final Context context;
    public List<ModeWrapper> list;

    public ModeDistributionAdapter(Context context,List<ModeWrapper> list) {
        this.context = context;
        this.list=list;
    }

    @NonNull
    @Override
    public ModeDistributionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context)
                .inflate(R.layout.mode_recycle_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ModeDistributionAdapter.ViewHolder holder, int position) {
        ModeWrapper data=list.get(position);
        holder.mode.setText(data.getName());
        holder.percentage.setText(String.valueOf(Math.round(data.getPerentage()*10)/10.0)+"%");
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mode;
        public TextView percentage;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mode=itemView.findViewById(R.id.textView17);
            percentage=itemView.findViewById(R.id.textView18);
        }
    }
}
