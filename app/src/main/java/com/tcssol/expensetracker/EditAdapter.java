package com.tcssol.expensetracker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.Group;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.tcssol.expensetracker.Adapters.EditAdapterCategory;
import com.tcssol.expensetracker.Adapters.EditAdapterMedium;
import com.tcssol.expensetracker.Adapters.EditAdapterSubCategory;
import com.tcssol.expensetracker.Adapters.OnEditItemClickListner;
import com.tcssol.expensetracker.Adapters.OnEditMediumItemClickListner;
import com.tcssol.expensetracker.Adapters.OnEditSubItemClickListner;
import com.tcssol.expensetracker.Utils.Utils;
import com.tcssol.expensetracker.Utils.WorkwithJSONStrings;

import java.util.ArrayList;
import java.util.List;

/*
Class for editing the categories/subcategories and medium
 */

public class EditAdapter extends AppCompatActivity implements OnEditItemClickListner, OnEditSubItemClickListner, OnEditMediumItemClickListner {
    private Context context;
    private Toolbar toolbar;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private RecyclerView recyclerViewCategories;
    private RecyclerView recyclerViewSubCategories;
    private RecyclerView recyclerViewMode;
    private Group groupCategories;
    private Group groupMode;
    private Group addItem;
    private Group addItemMedium;
    private ImageButton buttonCategories;
    private ImageButton buttonMode;
    private ImageButton check;
    private ImageButton check2;
    private EditText editText;
    private EditText editText2;
    private WorkwithJSONStrings jsonStrings;
    private List<String> categories;
    private List<String> subcategory;
    private List<String> medium;
    private EditAdapterCategory adapter;
    private EditAdapterMedium adapter2;
    private EditAdapterSubCategory adapter3;
    private String currentCategoryHelper;
    private WorkwithJSONStrings jsonStrings1;
    private Button saveButtonCategories;
    private Button saveButtonMedium;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
        Setting Activity
         */
        context=getApplicationContext();
        setContentView(R.layout.activity_edit_adapters);
        toolbar=findViewById(R.id.materialToolbarEditAdapter);
        toolbar.setSubtitle("Edit Categories");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        Drawable drawable = toolbar.getNavigationIcon();
        if (drawable != null) {
            drawable = DrawableCompat.wrap(drawable);
            DrawableCompat.setTint(drawable, Color.WHITE);
            toolbar.setNavigationIcon(drawable);

        }
        // Easy Fix
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        /*
        TODO Add animation to only show category or subcategory based on what user is adding
         */
        buttonCategories=findViewById(R.id.imageButtonEditAdaptersCategories);
        groupCategories=findViewById(R.id.groupEditAdaptersCategories);
        buttonCategories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float rotation=buttonCategories.getRotation();
                rotation+=180.00;
                buttonCategories.setRotation(rotation);
                groupCategories.setVisibility(groupCategories.getVisibility()==View.GONE?View.VISIBLE:View.GONE);
            }
        });
        /*
        Setting Up recycler view, onclick listners and shared preferences to persist the updates!
         */
        addItemMedium=findViewById(R.id.groupAddItemMedium);
        check2=findViewById(R.id.imageButtonMediumDone);
        editText2=findViewById(R.id.createMediumEditText);

        addItem=findViewById(R.id.groupAddItem);
        check=findViewById(R.id.imageButtonDone);
        editText=findViewById(R.id.createCategoryEditText);
        buttonMode=findViewById(R.id.imageButtonEditAdaptersMode);
        groupMode=findViewById(R.id.groupEditAdaptersMode);
        buttonMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Rotating the based on visisbility of view
                float rotation=buttonMode.getRotation();
                rotation+=180.00;
                buttonMode.setRotation(rotation);
                groupMode.setVisibility(groupMode.getVisibility()==View.GONE?View.VISIBLE:View.GONE);
            }
        });
        recyclerViewCategories=findViewById(R.id.recyclerViewEditAdaptersCategories);
        recyclerViewCategories.setHasFixedSize(true);
        recyclerViewCategories.setLayoutManager(new LinearLayoutManager(context));
        recyclerViewSubCategories=findViewById(R.id.recyclerViewEditAdaptersSubCategories);
        recyclerViewSubCategories.setHasFixedSize(true);
        recyclerViewSubCategories.setLayoutManager(new LinearLayoutManager(context));
        recyclerViewMode=findViewById(R.id.recyclerViewEdiAdaptersMode);
        recyclerViewMode.setHasFixedSize(true);
        recyclerViewMode.setLayoutManager(new LinearLayoutManager(context));
        sharedPreferences=getSharedPreferences("com.tcs.expensetracker.stored_categories",MODE_PRIVATE);
        String storedCategories = sharedPreferences.getString("STORED_CATEGORIES",getResources().getString(R.string.category_subcategory));
        Log.d("EditAdapter","Stored categories"+storedCategories);
        String storedMode=sharedPreferences.getString("STORED_MEDIUM",getResources().getString(R.string.transfer_medium));
        Log.d("EditAdapter","Stored Medium"+storedMode);
        editor=sharedPreferences.edit();

        jsonStrings=new WorkwithJSONStrings(storedCategories);
        jsonStrings1=new WorkwithJSONStrings(storedMode);
        medium=jsonStrings1.getList("_list_medium");
        Log.d("EditAdapter","Stored Medium list"+medium.toString());
        categories=jsonStrings.getList("_elementlist");
        Log.d("EditAdapter","Stored Categories"+categories.toString());
        adapter=new EditAdapterCategory(context,categories,this);
        adapter2=new EditAdapterMedium(context,medium,this);
        recyclerViewCategories.setAdapter(adapter);
        recyclerViewMode.setAdapter(adapter2);
        saveButtonCategories=findViewById(R.id.buttonEditAdaptersCategoriesSave);
        saveButtonCategories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String temp=jsonStrings.getJSONString();
                editor.putString("STORED_CATEGORIES",temp);
                editor.commit();
                Snackbar.make(v,"Changes Successfull",Snackbar.LENGTH_SHORT).show();
            }
        });
        saveButtonMedium=findViewById(R.id.buttonEditAdaptersModeSave);
        saveButtonMedium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String temp=jsonStrings1.getJSONString();
                editor.putString("STORED_MEDIUM",temp);
                editor.commit();
                Snackbar.make(v,"Changes Successfull",Snackbar.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onEditTextViewClick(String category) {
        currentCategoryHelper=category;
        subcategory=jsonStrings.getList(category);
        adapter3=new EditAdapterSubCategory(context,subcategory,this);
        recyclerViewSubCategories.setAdapter(adapter3);
    }

    @Override
    public void onEditCrossViewClick(String item) {
        categories.remove(item);
        jsonStrings.removeKey(item);
        jsonStrings.updateElementList("_elementlist",categories);
        adapter.notifyDataSetChanged();
        Log.d("JSOS",jsonStrings.getJSONString());

    }

    @Override
    public void addItem() {
        addItem.setVisibility(View.VISIBLE);
        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!(editText.getText().toString().trim().isEmpty())){
                    String temp=editText.getText().toString().trim();
                    categories.add(temp);
                    adapter.notifyDataSetChanged();
                    jsonStrings.updateElementList("_elementlist",categories);
                    List<String> element=new ArrayList<>();
                    element.add("Filler");
                    editText.getText().clear();
                    jsonStrings.updateElementList(temp,element);
                    Log.d("JSOS",jsonStrings.getJSONString());
                }
            }
        });


    }


    @Override
    public void onEditSubCrossViewClick(String item) {
        subcategory.remove(item);
        jsonStrings.updateElementList(currentCategoryHelper,subcategory);
        adapter3.notifyDataSetChanged();
        Log.d("JSOS",jsonStrings.getJSONString());
    }

    @Override
    public void addSubItem() {
        addItem.setVisibility(View.VISIBLE);
        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!(editText.getText().toString().trim().isEmpty())){
                    String temp=editText.getText().toString().trim();
                    subcategory.add(temp);
                    adapter3.notifyDataSetChanged();
                    jsonStrings.updateElementList(currentCategoryHelper,subcategory);
                    editText.getText().clear();
                    Log.d("JSOS",jsonStrings.getJSONString());
                }
            }
        });
    }

    @Override
    public void onEditMediumCrossViewClick(String item) {
        medium.remove(item);
        jsonStrings1.updateElementList("_list_medium",medium);
        adapter2.notifyDataSetChanged();
    }

    @Override
    public void addMediumItem() {
        addItemMedium.setVisibility(View.VISIBLE);
        check2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!(editText2.getText().toString().trim().isEmpty())){
                    String temp=editText2.getText().toString().trim();
                    medium.add(temp);
                    adapter2.notifyDataSetChanged();
                    editText2.getText().clear();
                    jsonStrings1.updateElementList("_list_medium",medium);
                    Log.d("JSOS",jsonStrings1.getJSONString());
                }
            }
        });

    }
}
