package com.tcssol.expensetracker.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.tcssol.expensetracker.CreateExpenses;
import com.tcssol.expensetracker.Data.ObservationDatabase;
import com.tcssol.expensetracker.Model.Observation;
import com.tcssol.expensetracker.R;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

public class ObservationsAdapter extends RecyclerView.Adapter<ObservationsAdapter.ViewHolder> {

    private final Context context;
    private final List<Observation> observations = new ArrayList<>();
    private final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("dd MMMM yyyy, h:mm a", Locale.getDefault());

    public ObservationsAdapter(Context context) {
        this.context = context;
    }

    public void setObservations(List<Observation> data) {
        observations.clear();
        if (data != null) observations.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.observation_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Observation observation = observations.get(position);
        String symbol = Currency.getInstance(Locale.getDefault()).getSymbol();
        String amount = String.format(Locale.getDefault(), "%s%,.2f", symbol, observation.getAmount());

        if (observation.isCredit()) {
            holder.amount.setText(amount);
            holder.amount.setTextColor(ContextCompat.getColor(context, R.color.income));
            holder.type.setText(R.string.credited);
            holder.type.setTextColor(ContextCompat.getColor(context, R.color.income));
        } else {
            holder.amount.setText("-" + amount);
            holder.amount.setTextColor(ContextCompat.getColor(context, R.color.expense));
            holder.type.setText(R.string.debited);
            holder.type.setTextColor(ContextCompat.getColor(context, R.color.expense));
        }

        holder.date.setText(formatter.format(Instant.ofEpochMilli(observation.getTimeMillis())
                .atZone(ZoneId.systemDefault())));
        holder.body.setText(observation.getBody());

        holder.add.setOnClickListener(v -> {
            Intent intent = new Intent(context, CreateExpenses.class);
            intent.putExtra("Type", 5);
            intent.putExtra("TypeExpense", observation.isCredit());
            double value = observation.getAmount();
            String amountText = value == Math.floor(value)
                    ? String.valueOf((long) value)
                    : String.valueOf(value);
            intent.putExtra("Amount", amountText);
            intent.putExtra("ObservationId", observation.getId());
            context.startActivity(intent);
        });

        holder.dismiss.setOnClickListener(v -> {
            long id = observation.getId();
            ObservationDatabase.databaseWriterExecutor.execute(() ->
                    ObservationDatabase.getDatabase(context.getApplicationContext())
                            .observationDao().deleteById(id));
        });
    }

    @Override
    public int getItemCount() {
        return observations.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView amount;
        final TextView type;
        final TextView date;
        final TextView body;
        final Button add;
        final ImageButton dismiss;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            amount = itemView.findViewById(R.id.obsAmount);
            type = itemView.findViewById(R.id.obsType);
            date = itemView.findViewById(R.id.obsDate);
            body = itemView.findViewById(R.id.obsBody);
            add = itemView.findViewById(R.id.obsAddButton);
            dismiss = itemView.findViewById(R.id.obsDismissButton);
        }
    }
}
