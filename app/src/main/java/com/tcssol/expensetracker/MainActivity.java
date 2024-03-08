package com.tcssol.expensetracker;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.HorizontalScrollView;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.tcssol.expensetracker.Adapters.PagerAdapter;
import com.tcssol.expensetracker.Data.PersonExpRepository;
import com.tcssol.expensetracker.Model.ExpenseViewModel;
import com.tcssol.expensetracker.Model.Expenses;
import com.tcssol.expensetracker.Model.PersonExp;
import com.tcssol.expensetracker.Model.PersonExpViewModel;
import com.tcssol.expensetracker.Model.SharedExpenseViewModel;
import com.tcssol.expensetracker.Utils.DataBaseExporter;
import com.tcssol.expensetracker.Utils.Wrapped;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private FloatingActionButton fab;
    private ExpenseViewModel expenseViewModel;
    private Toolbar toolbar;
    private HorizontalScrollView scrollView;
    private static final String[] MonthList={"January","February","March","April","May","June","July","August","September","October","November","December"};
    private static final Integer[] Years={2023,2024,2025,2026,2027,2028,2029,2030,2031,2032,2033,2034};
    PagerAdapter pagerAdapter;
    private Spinner selectMonth;
    private Spinner selectYear;
    private Chip all;
    private Chip thisMonth;
    private Chip previousMonth;
/*
    private Chip thisYear;
    private Chip previousYear;
*/
private PersonExpViewModel personExpViewModel;
    private int month= LocalDate.now().getMonthValue();
    private int year=LocalDate.now().getYear();
    private SharedExpenseViewModel sharedExpenseViewModel;

    private Boolean  userSelectCategoryflag=false;
    private Boolean userSelectSubCategoryflag=false;

    private ActivityResultLauncher<Intent> someActivityResultLauncher;
    private int tableId=-1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Log.d("SEVM","Create mi");

        setContentView(R.layout.activity_main);
        toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        someActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                    }
                }
        );
        tabLayout=findViewById(R.id.tabLayout);
        scrollView=findViewById(R.id.scrollPeriodSelector);
        viewPager=findViewById(R.id.viewpager2);
        fab=findViewById(R.id.fab);

        selectMonth=findViewById(R.id.spinnerSelectMonth);
        selectYear=findViewById(R.id.spinnerSelectYear);
        all=findViewById(R.id.chipAll);
        thisMonth=findViewById(R.id.chipThisMonth);
        previousMonth=findViewById(R.id.chipPreviousMonth);


        Log.d("Chip","Before setting:"+String.valueOf(all.isChecked()));
        Log.d("Chip","After Setting: "+String.valueOf(all.isChecked()));

        all.setOnClickListener(this);
        all.setSelected(true);
        thisMonth.setOnClickListener(this);
        previousMonth.setOnClickListener(this);
/*
        thisYear.setOnClickListener(this);
        previousYear.setOnClickListener(this);
*/
        Log.d("Chip","After Onclic: "+String.valueOf(all.isChecked()));

        all.setChecked(true);


        FragmentManager fm=getSupportFragmentManager();
        pagerAdapter=new PagerAdapter(fm,getLifecycle());
        viewPager.setAdapter(pagerAdapter);

        sharedExpenseViewModel = new ViewModelProvider.AndroidViewModelFactory(this.getApplication()).create(SharedExpenseViewModel.class);

        ArrayAdapter aa=new ArrayAdapter(getApplicationContext(), android.R.layout.simple_spinner_item, MonthList);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_item);
        ArrayAdapter ab=new ArrayAdapter(getApplicationContext(),android.R.layout.simple_spinner_item,Years);
        ab.setDropDownViewResource(android.R.layout.simple_spinner_item);
        selectMonth.setAdapter(aa);
        selectMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(userSelectCategoryflag) {

                    unSelectAll();
                    month = position + 1;
                    Wrapped wrap = new Wrapped(month, year);
                    sharedExpenseViewModel.setObject(wrap);
                }else{
                    userSelectCategoryflag=true;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        selectYear.setAdapter(ab);
        selectYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(userSelectSubCategoryflag) {
                    unSelectAll();
                    int year = Years[position];
                    Wrapped wrap = new Wrapped(month, year);
                    sharedExpenseViewModel.setObject(wrap);
                }else
                    userSelectSubCategoryflag=true;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                tabLayout.selectTab(tabLayout.getTabAt(position));

            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),CreateExpenses.class);
                intent.putExtra("Type",0);
                someActivityResultLauncher.launch(intent);
            }
        });
        expenseViewModel = new ViewModelProvider(this)
                .get(ExpenseViewModel.class);
        personExpViewModel = new ViewModelProvider(this)
                .get(PersonExpViewModel.class);
        Intent temp=getIntent();
        if(temp!=null&&temp.getComponent()!=null){
            tabLayout.selectTab(tabLayout.getTabAt(temp.getIntExtra("selected_tab",0)));
            viewPager.setCurrentItem(temp.getIntExtra("selected_tab_pager",0));
        }




    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
        if(id==R.id.menu_overflow){
            PopupMenu popupMenu = new PopupMenu((Context) MainActivity.this,toolbar, Gravity.RIGHT);
            popupMenu.getMenuInflater().inflate(R.menu.menu_menu_fetch, popupMenu.getMenu());
            popupMenu.show();
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    int id=item.getItemId();
                    if(id==R.id.menu_manage_categories){
                        Intent intent=new Intent(getApplicationContext(), EditAdapter.class);
                        intent.putExtra("calling_activity_class","com.tcssol.expensetracker.MainActivity");
                        intent.putExtra("selected_tab",tabLayout.getSelectedTabPosition());
                        intent.putExtra("selected_tab_pager",viewPager.getCurrentItem());
                        someActivityResultLauncher.launch(intent);
                    }else if(id==R.id.menu_About){
                        Intent intent = new Intent(getApplicationContext(), AboutActivity.class);
                        someActivityResultLauncher.launch(intent);
                    }else if(id==R.id.menu_settings){
                        Intent intent=new Intent(getApplicationContext(),Settings.class);
                        someActivityResultLauncher.launch(intent);
                    }else if(id==R.id.menu_export_data){
                        PopupWindow popupWindow=new PopupWindow();
                        View popupView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.export_data_popup, null);
                        popupWindow.setContentView(popupView);
                        popupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
                        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
                        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        popupWindow.setFocusable(true);
                        popupWindow.setOutsideTouchable(true);

                        ViewGroup root = (ViewGroup) getWindow().getDecorView().getRootView();
                        Drawable dim = new ColorDrawable(Color.LTGRAY);
                        dim.setBounds(0, 0, root.getWidth(), root.getHeight());
                        dim.setAlpha((int) (100));
                        root.getOverlay().add(dim);
                        Button exportDataCsv;
                        Button exportDataTxt;
                        RadioGroup selectTable;
                        exportDataCsv=popupView.findViewById(R.id.buttonExportCsvData);
                        exportDataTxt=popupView.findViewById(R.id.buttonExportTxtData);
                        selectTable=popupView.findViewById(R.id.radioGrpSelectTable);
                        popupWindow.showAtLocation(toolbar,Gravity.CENTER,0,0);
                        selectTable.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(RadioGroup group, int checkedId) {
                                if(checkedId==R.id.radioButtonTable1){
                                    tableId=1;
                                }else if(checkedId==R.id.radioButtonTable2){
                                    tableId=2;
                                }
                            }
                        });
                        View vv=root;
                        exportDataCsv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(tableId>0){
                                    if(tableId==1){
                                        List<Expenses> list= expenseViewModel.getAllExpensesList();
                                        popupWindow.dismiss();
                                        DataBaseExporter.exportCSVExpenses(getApplicationContext(),getWindow().getDecorView().getRootView(),list);
                                    }else if(tableId==2){
                                        List<PersonExp> list=personExpViewModel.getAllExpensesList();
                                        popupWindow.dismiss();
                                        DataBaseExporter.exportCsvPersonExpenses(getApplicationContext(),getWindow().getDecorView().getRootView(),list);
                                    }else{
                                        Snackbar.make(vv,"Unknown Error",Snackbar.LENGTH_SHORT).show();

                                    }
                                }else{
                                    Snackbar.make(vv,"Please Select Table to Export",Snackbar.LENGTH_SHORT).show();


                                }

                            }
                        });
                        exportDataTxt.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(tableId>0){
                                    if(tableId==1){
                                        List<Expenses> list= expenseViewModel.getAllExpensesList();
                                        popupWindow.dismiss();
                                        DataBaseExporter.exportTxtExpenses(getApplicationContext(),getWindow().getDecorView().getRootView(),list);
                                    }else if(tableId==2){
                                        List<PersonExp> list=personExpViewModel.getAllExpensesList();
                                        popupWindow.dismiss();
                                        DataBaseExporter.exportTxtPersonExpenses(getApplicationContext(),getWindow().getDecorView().getRootView(),list);
                                    }else{
                                        Snackbar.make(vv,"Please Select Table to Export",Snackbar.LENGTH_SHORT).show();
                                    }
                                }else{
                                    Snackbar.make(vv,"Please Select Table to Export",Snackbar.LENGTH_SHORT).show();
                                }
                            }
                        });


                        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                            @Override
                            public void onDismiss() {
                                root.getOverlay().clear();
                            }
                        });
                        popupView.setOnTouchListener(new View.OnTouchListener() {
                            @Override
                            public boolean onTouch(View v, MotionEvent event) {
                                if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                                    popupWindow.dismiss();
                                    return true;
                                }
                                return false;
                            }
                        });
                    }
                    return false;
                }
            });
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        Log.d("Chip","Listner used");
        unSelectAllChip(v.getId());
        int id=v.getId();
        if(id==R.id.chipAll){
            Wrapped wrap=new Wrapped(-12,-2024);
            sharedExpenseViewModel.setObject(wrap);
        }
        else if(id==R.id.chipThisMonth){
            month=LocalDate.now().getMonthValue();
            year=LocalDate.now().getYear();
            Wrapped wrapped=new Wrapped(month,year);
            sharedExpenseViewModel.setObject(wrapped);
            Log.d("SEVM","Inside This Month chip");
        }
        else if(id==R.id.chipPreviousMonth){
            LocalDate localDate=LocalDate.now().minusMonths(1);
            int tmonth=localDate.getMonthValue();
            int tyear=localDate.getYear();
            Wrapped wrapped=new Wrapped(tmonth,tyear);
            sharedExpenseViewModel.setObject(wrapped);
        }

    }
    public void unSelectAll(){
        Chip chip;
        Integer[] array={R.id.chipAll,R.id.chipPreviousMonth,R.id.chipThisMonth/*,R.id.chipThisYear,R.id.chipPreviousYear*/};
        for(int i:array){
            chip=findViewById(i);
            chip.setSelected(false);
        }
    }
    public void unSelectAllChip(int id){
        Integer[] array={R.id.chipAll,R.id.chipPreviousMonth,R.id.chipThisMonth,/*R.id.chipThisYear,R.id.chipPreviousYear*/};
        Chip v=findViewById(id);
        for(int i:array){
            if(id!=i){
                Chip vv=findViewById(i);
                vv.setSelected(false);
            }
        }
        v.setSelected(true);

    }
}