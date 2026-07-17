package com.tcssol.expensetracker.Adapters;

import android.view.View;
import android.widget.PopupWindow;

import com.tcssol.expensetracker.Model.PersonExp;

public interface PeerToPeerClickListener {
    void onPeerClick(PersonExp personExp);
    void onPeerLongClick(PersonExp personExp, int position);
    void onPeerPopupClick(PopupWindow window, View v);
}
