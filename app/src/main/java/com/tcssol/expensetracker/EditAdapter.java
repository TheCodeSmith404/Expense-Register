package com.tcssol.expensetracker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.tcssol.expensetracker.Adapters.EditAdapterCategory;
import com.tcssol.expensetracker.Adapters.EditAdapterMedium;
import com.tcssol.expensetracker.Adapters.EditAdapterSubCategory;
import com.tcssol.expensetracker.Adapters.OnEditItemClickListner;
import com.tcssol.expensetracker.Adapters.OnEditMediumItemClickListner;
import com.tcssol.expensetracker.Adapters.OnEditSubItemClickListner;
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
    private TextView tvSubcategoriesTitle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        setContentView(R.layout.activity_edit_adapters);

        toolbar = findViewById(R.id.materialToolbarEditAdapter);
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
        toolbar.setNavigationOnClickListener(v -> finish());

        // Bind TabLayout and Containers
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        final View layoutCategories = findViewById(R.id.layoutCategoriesContainer);
        final View layoutModes = findViewById(R.id.layoutModesContainer);
        tvSubcategoriesTitle = findViewById(R.id.tvSubcategoriesTitle);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    layoutCategories.setVisibility(View.VISIBLE);
                    layoutModes.setVisibility(View.GONE);
                } else {
                    layoutCategories.setVisibility(View.GONE);
                    layoutModes.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        recyclerViewCategories = findViewById(R.id.recyclerViewEditAdaptersCategories);
        recyclerViewCategories.setHasFixedSize(true);
        recyclerViewCategories.setLayoutManager(new LinearLayoutManager(context));

        recyclerViewSubCategories = findViewById(R.id.recyclerViewEditAdaptersSubCategories);
        recyclerViewSubCategories.setHasFixedSize(true);
        recyclerViewSubCategories.setLayoutManager(new LinearLayoutManager(context));

        recyclerViewMode = findViewById(R.id.recyclerViewEdiAdaptersMode);
        recyclerViewMode.setHasFixedSize(true);
        recyclerViewMode.setLayoutManager(new LinearLayoutManager(context));

        sharedPreferences = getSharedPreferences("com.tcs.expensetracker.stored_categories", MODE_PRIVATE);
        String storedCategories = sharedPreferences.getString("STORED_CATEGORIES", getResources().getString(R.string.category_subcategory));
        String storedMode = sharedPreferences.getString("STORED_MEDIUM", getResources().getString(R.string.transfer_medium));
        editor = sharedPreferences.edit();

        jsonStrings = new WorkwithJSONStrings(storedCategories);
        jsonStrings1 = new WorkwithJSONStrings(storedMode);

        medium = jsonStrings1.getList("_list_medium");
        categories = jsonStrings.getList("_elementlist");

        adapter = new EditAdapterCategory(context, categories, this);
        adapter2 = new EditAdapterMedium(context, medium, this);

        recyclerViewCategories.setAdapter(adapter);
        recyclerViewMode.setAdapter(adapter2);

        // Pre-select first category if available to avoid blank list
        if (!categories.isEmpty()) {
            onEditTextViewClick(categories.get(0));
        }

        saveButtonCategories = findViewById(R.id.buttonEditAdaptersCategoriesSave);
        saveButtonCategories.setOnClickListener(v -> {
            String temp = jsonStrings.getJSONString();
            editor.putString("STORED_CATEGORIES", temp);
            editor.commit();
            Snackbar.make(v, "Changes Successful", Snackbar.LENGTH_SHORT).show();
        });

        saveButtonMedium = findViewById(R.id.buttonEditAdaptersModeSave);
        saveButtonMedium.setOnClickListener(v -> {
            String temp = jsonStrings1.getJSONString();
            editor.putString("STORED_MEDIUM", temp);
            editor.commit();
            Snackbar.make(v, "Changes Successful", Snackbar.LENGTH_SHORT).show();
        });
    }

    private void showAddDialog(String title, String hint, OnAddTextListener listener) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle(title);

        final EditText input = new EditText(this);
        input.setHint(hint);
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT);

        int paddingPx = (int) (16 * getResources().getDisplayMetrics().density);
        FrameLayout container = new FrameLayout(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = paddingPx;
        params.rightMargin = paddingPx;
        input.setLayoutParams(params);
        container.addView(input);

        builder.setView(container);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String text = input.getText().toString().trim();
            if (!text.isEmpty()) {
                listener.onTextAdded(text);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private interface OnAddTextListener {
        void onTextAdded(String text);
    }

    @Override
    public void onEditTextViewClick(String category) {
        currentCategoryHelper = category;
        if (tvSubcategoriesTitle != null) {
            tvSubcategoriesTitle.setText("Sub-Categories of " + category);
        }
        subcategory = jsonStrings.getList(category);
        adapter3 = new EditAdapterSubCategory(context, subcategory, this);
        recyclerViewSubCategories.setAdapter(adapter3);
    }

    @Override
    public void onEditCrossViewClick(String item) {
        categories.remove(item);
        jsonStrings.removeKey(item);
        jsonStrings.updateElementList("_elementlist", categories);
        adapter.notifyDataSetChanged();
        
        // If the currently displayed subcategories belonged to the deleted category, clear or re-select
        if (item.equals(currentCategoryHelper)) {
            if (!categories.isEmpty()) {
                onEditTextViewClick(categories.get(0));
            } else {
                currentCategoryHelper = "";
                if (tvSubcategoriesTitle != null) {
                    tvSubcategoriesTitle.setText("Sub-Categories");
                }
                if (subcategory != null) {
                    subcategory.clear();
                    if (adapter3 != null) {
                        adapter3.notifyDataSetChanged();
                    }
                }
            }
        }
    }

    @Override
    public void addItem() {
        showAddDialog("Add New Category", "Category Name", text -> {
            categories.add(text);
            adapter.notifyDataSetChanged();
            jsonStrings.updateElementList("_elementlist", categories);
            List<String> element = new ArrayList<>();
            element.add("Filler");
            jsonStrings.updateElementList(text, element);
            
            // Auto-select the newly added category
            onEditTextViewClick(text);
        });
    }

    @Override
    public void onEditSubCrossViewClick(String item) {
        subcategory.remove(item);
        jsonStrings.updateElementList(currentCategoryHelper, subcategory);
        if (adapter3 != null) {
            adapter3.notifyDataSetChanged();
        }
    }

    @Override
    public void addSubItem() {
        if (currentCategoryHelper == null || currentCategoryHelper.isEmpty()) {
            Snackbar.make(findViewById(android.R.id.content), "Please select a Category first", Snackbar.LENGTH_SHORT).show();
            return;
        }
        showAddDialog("Add Sub-Category for " + currentCategoryHelper, "Sub-Category Name", text -> {
            subcategory.add(text);
            if (adapter3 != null) {
                adapter3.notifyDataSetChanged();
            }
            jsonStrings.updateElementList(currentCategoryHelper, subcategory);
        });
    }

    @Override
    public void onEditMediumCrossViewClick(String item) {
        medium.remove(item);
        jsonStrings1.updateElementList("_list_medium", medium);
        adapter2.notifyDataSetChanged();
    }

    @Override
    public void addMediumItem() {
        showAddDialog("Add Payment Mode", "Mode Name", text -> {
            medium.add(text);
            adapter2.notifyDataSetChanged();
            jsonStrings1.updateElementList("_list_medium", medium);
        });
    }
}
