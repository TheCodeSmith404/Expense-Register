package com.tcssol.expensetracker.Adapters;

import android.view.View;
import android.widget.PopupWindow;

import com.tcssol.expensetracker.Model.PersonExp;

public interface Fragment2ClickListner {
    public void fragment2ClickListner(PersonExp personExp);
    public void fragment2LongClickListner(PersonExp personExp,int position);
    public void fragment2PopupClick(PopupWindow window, View v);
}
