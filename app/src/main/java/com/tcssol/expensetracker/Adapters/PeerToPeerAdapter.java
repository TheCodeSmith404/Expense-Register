package com.tcssol.expensetracker.Adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.tcssol.expensetracker.Data.PersonExpDao;
import com.tcssol.expensetracker.Model.PersonExp;
import com.tcssol.expensetracker.R;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

public class PeerToPeerAdapter extends RecyclerView.Adapter<PeerToPeerAdapter.ViewHolder> {
    public List<PersonExp> personExpList;
    public List<PersonExp> newPersonExpList;
    public final Context mContext;
    private final PersonExpDao personExpDao;
    private final PeerToPeerClickListener clickListener;
    private final int type;

    public PeerToPeerAdapter(Context mContext, PersonExpDao personExpDao, List<PersonExp> personExpList, PeerToPeerClickListener clickListener, int type) {
        this.mContext = mContext;
        this.personExpDao = personExpDao;
        this.personExpList = personExpList;
        this.type = type;
        Log.d("Sup", "Adapter Initialized");
        if (type != 2 && personExpList.size() != 0) {
            newPersonExpList = getNewList(personExpList);
            Log.d("Sup", "either not type 2 or size == 0");
        }
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case 1:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment2item, parent, false);
                break;
            case 2:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment2itemtype2, parent, false);
                break;
            case 3:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment4date, parent, false);
                break;
            default:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment2item, parent, false);
        }
        return new ViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int color;
        PersonExp personExp;
        if (type != 2) {
            personExp = newPersonExpList.get(position);
        } else {
            personExp = personExpList.get(position);
        }
        
        if (personExp.getName().equals("_*_*_")) {
            LocalDate date = personExp.getDateCreated();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
            holder.date2.setText(date.format(formatter));
        } else {
            String symbol = Currency.getInstance(Locale.getDefault()).getSymbol();
            holder.amount.setText(String.valueOf(personExp.getAmount()));
            if (!personExp.getType()) {
                color = ContextCompat.getColor(mContext, R.color.expense);
                holder.amount.setText("-" + symbol + String.format(Locale.getDefault(), "%,.2f", personExp.getAmount()));
            } else {
                color = ContextCompat.getColor(mContext, R.color.income);
                holder.amount.setText(symbol + String.format(Locale.getDefault(), "%,.2f", personExp.getAmount()));
            }
            if (holder.view != null) {
                holder.view.setBackgroundTintList(ColorStateList.valueOf(color));
                holder.view.setVisibility(View.VISIBLE);
            }
            holder.amount.setTextColor(color);
            holder.name.setText(personExp.getName());
            if (type == 1) {
                holder.mode.setText(personExp.getMode());
                if (personExp.getContactNumber().isEmpty()) {
                    holder.contactNumber.setVisibility(View.GONE);
                } else {
                    holder.contactNumber.setText(personExp.getContactNumber());
                }
                if (personExp.getHasDate() != null && !personExp.getHasDate()) {
                    holder.date.setVisibility(View.GONE);
                } else {
                    holder.date.setVisibility(View.VISIBLE);
                }

                if (personExp.getNote() != null && !personExp.getNote().isEmpty()) {
                    holder.note.setText(personExp.getNote());
                    holder.note.setVisibility(View.VISIBLE);
                } else {
                    holder.note.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (type != 2 && newPersonExpList.get(position).getName().equals("_*_*_"))
            return 3;
        else
            return type;
    }

    @Override
    public int getItemCount() {
        if (type == 2 || personExpList.isEmpty()) {
            return personExpList.size();
        } else {
            return newPersonExpList.size();
        }
    }

    public List<PersonExp> getNewList(List<PersonExp> list) {
        List<PersonExp> ret = new ArrayList<>();
        PersonExp test = new PersonExp(list.get(0).getDateCreated(), false, "_*_*_", "_*_*_", "_*_*_", false, null, 0D, "");
        ret.add(0, test);
        ret.add(1, list.get(0));
        for (int i = 1; i < list.size(); i++) {
            PersonExp temp = list.get(i);
            PersonExp temp2 = list.get(i - 1);
            if (temp.getDateCreated().isEqual(temp2.getDateCreated())) {
                ret.add(temp);
            } else {
                test = new PersonExp(temp.getDateCreated(), false, "_*_*_", "_*_*_", "_*_*_", false, null, 0D, "");
                ret.add(test);
                ret.add(temp);
            }
        }
        return ret;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener, View.OnClickListener {
        public TextView name;
        public TextView contactNumber;
        public TextView amount;
        public ImageButton date;
        public TextView date2;
        public View view;
        public TextView mode;
        public TextView note;

        public ViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            if (viewType == 1) {
                name = itemView.findViewById(R.id.frag2name);
                contactNumber = itemView.findViewById(R.id.frag2contact);
                amount = itemView.findViewById(R.id.frag2amount_txt);
                date = itemView.findViewById(R.id.frag2date);
                view = itemView.findViewById(R.id.frag2line);
                mode = itemView.findViewById(R.id.frag2Mode);
                note = itemView.findViewById(R.id.frag2note);
                date.setOnClickListener(this);
                itemView.getRootView().setOnLongClickListener(this);
                itemView.getRootView().setOnClickListener(this);
            } else if (viewType == 3) {
                date2 = itemView.findViewById(R.id.textViewDate);
            } else {
                name = itemView.findViewById(R.id.frag2name);
                amount = itemView.findViewById(R.id.frag2amount_txt);
                view = itemView.findViewById(R.id.frag2line);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                PersonExp data = (type != 2) ? newPersonExpList.get(getAdapterPosition()) : personExpList.get(getAdapterPosition());
                clickListener.onPeerLongClick(data, getAdapterPosition());
                return true;
            }
            return false;
        }

        @Override
        public void onClick(View v) {
            if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                PersonExp data = (type != 2) ? newPersonExpList.get(getAdapterPosition()) : personExpList.get(getAdapterPosition());
                if (v.getId() == R.id.frag2date) {
                    View popUp = LayoutInflater.from(mContext).inflate(R.layout.pending_date_recycle_view, null);
                    TextView tv = popUp.findViewById(R.id.frag2ShowPendingDate);
                    if (data.getPendingDate() != null) {
                        tv.setText(data.getPendingDate().toString());
                    }
                    PopupWindow window = new PopupWindow(popUp, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
                    clickListener.onPeerPopupClick(window, v);
                } else {
                    clickListener.onPeerClick(data);
                }
            }
        }
    }
}
