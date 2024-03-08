package com.tcssol.expensetracker;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.tcssol.expensetracker.Model.SharedExpenseViewModel;

import java.util.List;

public class SelectSpinner extends BottomSheetDialogFragment {
    private View view;
    private Button cancel;
    private RadioGroup radioGroup;
    private ScrollView scrollView;
    private Bundle data=new Bundle();
    SharedExpenseViewModel viewModel=new SharedExpenseViewModel();
    private TextView text;



    public SelectSpinner() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_select_spinner,container,false);
        cancel=view.findViewById(R.id.cancelButtonSelectSpinner);
        radioGroup=view.findViewById(R.id.selectSpinnerRadioGroup);
        text=view.findViewById(R.id.fragSelectSpinnerText);
        Log.d("Sub Categories Error","Creating Select Spinner");
        return  view;

    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        data=getArguments();
        Log.d("Sub Categories Error","Created Select Spinner");

        String set=data.getString("isEdit");
        String type=data.getString("type");
        text.setText(type);
        List<String> items=data.getStringArrayList("items");
        for(int i=0;i<items.size();i++){
            Log.d("Sub Categories Error","Created Radio");
            RadioButton radioButton = new RadioButton(getContext());
            String text=items.get(i);
            radioButton.setText(text);
            radioButton.setId(View.generateViewId()); // Generate unique IDs for each RadioButton
            radioButton.getButtonDrawable().setTint(R.color.colorPrimary);
            RadioGroup.LayoutParams layoutParams = new RadioGroup.LayoutParams(
                    RadioGroup.LayoutParams.MATCH_PARENT, // Set width to match parent
                    RadioGroup.LayoutParams.WRAP_CONTENT // Set height to wrap content
            );
            layoutParams.setMargins(0, 2, 0, 2); // Set top and end margin

            radioButton.setLayoutParams(layoutParams);
            if(set.equals(items.get(i)))
                radioButton.setChecked(true);
            radioGroup.addView(radioButton);
        }
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Log.d("Sub Categories Error","Radio Clicked");
                RadioButton radio=view.findViewById(checkedId);
                if(type.equals("Select Payment Mode"))
                    viewModel.setMedium(radio.getText().toString());
                else if (type.equals("Select Payment Category"))
                    viewModel.setCategory(radio.getText().toString());
                else
                    viewModel.setSubCategory(radio.getText().toString());
                dismiss();
                onDestroy();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                onDestroy();
            }
        });
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        radioGroup=null;
    }

    @Override
    public void onResume() {
        super.onResume();
//        if(SharedExpenseViewModel.getChange()) {
//            data = getArguments();
//            String set = data.getString("isEdit");
//            if (set.isEmpty())
//                set = "General";
//            List<String> items = new ArrayList<>(data.getStringArrayList("items"));
//            for (int i = 0; i < items.size(); i++) {
//                RadioButton radioButton = new RadioButton(getContext());
//                String text = items.get(i);
//                radioButton.setText(text);
//                radioButton.setId(View.generateViewId()); // Generate unique IDs for each RadioButton
//                radioGroup.addView(radioButton);
//                if (text.equals(set)) {
//                    radioButton.setChecked(true);
//                }
//            }
//        }
//        SharedExpenseViewModel.setChange(true);
    }
}