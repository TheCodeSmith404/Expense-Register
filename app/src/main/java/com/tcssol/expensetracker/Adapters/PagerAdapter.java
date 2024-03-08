package com.tcssol.expensetracker.Adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.tcssol.expensetracker.Fragment1;
import com.tcssol.expensetracker.Fragment2;
import com.tcssol.expensetracker.Fragment3;
import com.tcssol.expensetracker.Fragment4;

public class PagerAdapter extends FragmentStateAdapter {

    public PagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch(position){
            case 1:
                return new Fragment2();
            case 2:
                return  new Fragment4();
            case 3:
                return new Fragment3();
            default:
                return new Fragment1();
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
