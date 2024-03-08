package com.tcssol.expensetracker;

import static android.graphics.Color.WHITE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.Group;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BlendMode;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.snackbar.Snackbar;
import com.tcssol.expensetracker.Data.ExpensesRepository;
import com.tcssol.expensetracker.Model.ExpenseViewModel;
import com.tcssol.expensetracker.Model.Expenses;
import com.tcssol.expensetracker.Model.PersonExp;
import com.tcssol.expensetracker.Model.PersonExpViewModel;
import com.tcssol.expensetracker.Model.SharedExpenseViewModel;
import com.tcssol.expensetracker.Utils.WorkwithJSONStrings;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

/***
 * Add a feature to just add the contact from saved contacts
 */

public class CreateExpenses extends AppCompatActivity {
    private Context context;
    private Toolbar toolbar;
    private ExpensesRepository expensesRepository;
    private RadioGroup radioGroup;
    private TextView showCategory;
    private TextView showSubCategory;
    private TextView showMedium;

    private EditText editAmount;
    private Button saveExpenses;
    private String categoryString="General";
    private String subCategoryString="General";
    private int type;
    private Boolean exptype;
    private Boolean perexptype;
    private String storedCategories;
    private EditText msgTxtEditText;

    private Group sendMsgGrp;
    private Switch aSwitch;
    private Switch sendMsgSwitch;
    private CalendarView calendarView;
    private CheckBox received;
    private CheckBox given;

    private List<String> subcategories;

    private Boolean receivedGiven;
    private TextView name;
    private TextView number;

    private LocalDate targetDate;


    private String storedMedium;
    private String mediumText="Cash";
    private String msgSendTxt;
    private Boolean donotChange=false;

    private SharedPreferences sharedPreferences;
    SelectSpinner selectSpinner=new SelectSpinner();
    private final SharedExpenseViewModel viewModel=new SharedExpenseViewModel();
    BottomSheetBehavior<View> bottomSheetBehavior;
    WorkwithJSONStrings jsonStrings;
    WorkwithJSONStrings jsonStrings1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
        Setting App Action bar and getting the view
         */
        context=getApplicationContext();
        setContentView(R.layout.activity_create_expenses);
        toolbar = findViewById(R.id.addexp_toolbar);
        toolbar.setSubtitle("Add Expenditure");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        Drawable drawable = toolbar.getNavigationIcon();
        if (drawable != null) {
            drawable = DrawableCompat.wrap(drawable);
            DrawableCompat.setTint(drawable, Color.WHITE);
            toolbar.setNavigationIcon(drawable);

        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        showCategory=findViewById(R.id.selectCategory);
        showSubCategory=findViewById(R.id.selectSubCategory);
        showMedium=findViewById(R.id.selectMedium);
        /*
        Setting Up bottom sheet fragment, Setting its max height
         */
        View constraintLayout=findViewById(R.id.fragmentSelectSpinner);
        bottomSheetBehavior = BottomSheetBehavior.from(constraintLayout);
        bottomSheetBehavior.setPeekHeight(BottomSheetBehavior.STATE_HIDDEN);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenHeight = displayMetrics.heightPixels;
        int maxHeight = (int) (screenHeight * 0.50);
        bottomSheetBehavior.setMaxHeight(maxHeight);

        /*
        Initilizing View
         */
        msgTxtEditText=findViewById(R.id.editTextMsg);
        sendMsgGrp=findViewById(R.id.groupSendMsg);
        radioGroup=findViewById(R.id.radioGroup);
        radioGroup.check(R.id.radioButtonSpend);
        Group group1=findViewById(R.id.earned_spend);
        Group group2=findViewById(R.id.lendRecievedGrp);
        /*
        initializing switch to send text messages if it is checked
         */
        sendMsgSwitch=findViewById(R.id.switchOnMessage);
        sendMsgSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!number.getText().toString().trim().isEmpty()&&!name.getText().toString().trim().isEmpty()&&!editAmount.getText().toString().trim().isEmpty()) {
                    if(received.isChecked()) {
                        msgTxtEditText.setVisibility(msgTxtEditText.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                    /*
                    TODO use .format method for changes;
                     */
                        msgSendTxt = "Please Return " + Currency.getInstance(Locale.getDefault()).getSymbol() + editAmount.getText().toString().trim();
                        msgTxtEditText.setText(msgSendTxt);
                    }
                    else if(given.isChecked()) {
                        showSnackbar(getWindow().getDecorView(),"Messages Can only be send when giving Money!",Snackbar.LENGTH_LONG);
                        sendMsgSwitch.setChecked(false);
                    }else{
                        showSnackbar(getWindow().getDecorView(),"Please Select a CheckBox!",Snackbar.LENGTH_LONG);
                        sendMsgSwitch.setChecked(false);
                    }
                }else{
                    showSnackbar(getWindow().getDecorView(),"Please Make Sure that Name, Amount and Number are not Empty",Snackbar.LENGTH_LONG);
                    sendMsgSwitch.setChecked(false);
                }
            }
        });


        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId==R.id.radioButtonSpend){
                    type=0;
                    calendarView.setVisibility(View.GONE);
                    group2.setVisibility(View.GONE);
                    group1.setVisibility(View.VISIBLE);
                    Log.d("DEBUG TYPE","in radiogrp0 type="+type+" exptype="+exptype);
                }else if(checkedId==R.id.radioButtonEarned){
                    type=1;
                    calendarView.setVisibility(View.GONE);
                    group2.setVisibility(View.GONE);
                    group1.setVisibility(View.VISIBLE);
                    Log.d("DEBUG TYPE","in radiogrp1 type="+type+" exptype="+exptype);
                }else if(checkedId==R.id.radioButtonLendRecieve){
                    type=2;
                    aSwitch.setChecked(false);
                    group1.setVisibility(View.GONE);
                    group2.setVisibility(View.VISIBLE);
                    Log.d("DEBUG TYPE","in radiogrp2 type="+type+" exptype="+exptype);

                }
            }
        });


        /*
        calendar view was not displaying when set hidden in the xml file so had to do it of activity creation
         */
        calendarView=findViewById(R.id.addPendingDate);
        calendarView.setVisibility(View.GONE);
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                targetDate=LocalDate.of(year, month + 1, dayOfMonth);
            }
        });
        /*
        Switch for setting a date for reminder for pending transaction
         */
        aSwitch=findViewById(R.id.switchOnDate);
        aSwitch.setChecked(false);
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                calendarView.setVisibility(isChecked ? View.VISIBLE : View.GONE);

                if(received.isChecked()==true) {
                    sendMsgGrp.setVisibility(View.VISIBLE);
                }
                sendMsgGrp.setVisibility(isChecked?sendMsgGrp.getVisibility():View.GONE);
                if(isChecked==false){
                    sendMsgSwitch.setChecked(false);
                }
            }
        });
        /*
        Check box for peer to peer transactions to select whether we are lending or borrowing money and
            logic to handle their proper selection and prevent user from selecting both
         */

        received=findViewById(R.id.checkBoxRecieved);
        given=findViewById(R.id.checkBoxLend);
        received.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(given.isChecked()==true)
                    given.setChecked(false);
                receivedGiven=false;
                if(aSwitch.isChecked()==true)
                    sendMsgGrp.setVisibility(View.VISIBLE);
            }
        });
        given.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(received.isChecked()==true)
                    received.setChecked(false);
                receivedGiven=true;
                sendMsgGrp.setVisibility(View.GONE);

            }
        });
        /*
        Getting the stored categories, subcategories and payment modes using shared preferences and converting it into list using json libraries
         */
        sharedPreferences= context.getSharedPreferences("com.tcs.expensetracker.stored_categories",Context.MODE_PRIVATE);
        storedMedium=getResources().getString(R.string.transfer_medium);
        storedMedium=sharedPreferences.getString("STORED_MEDIUM",storedMedium);

        storedCategories= getResources().getString(R.string.category_subcategory);
        storedCategories=sharedPreferences.getString("STORED_CATEGORIES",storedCategories);

        Log.d("CheckingString",storedCategories);
        jsonStrings=new WorkwithJSONStrings(storedCategories);
        jsonStrings1=new WorkwithJSONStrings(storedMedium);
        List<String> medium=jsonStrings1.getList("_list_medium");
        List<String> categories=jsonStrings.getList("_elementlist");
        /*
        Setting listners for text view which are acting as Spinners
         */
        showCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle item=new Bundle();
                item.putString("type","Select Payment Category");
                Log.d("Spinner New",categories.toString());
                item.putStringArrayList("items", (ArrayList<String>) categories);
                item.putString("isEdit",showCategory.getText().toString());
                selectSpinner.setArguments(item);
                showSelectSpinner();
            }
        });
        showSubCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle item=new Bundle();
                item.putString("type","Select Payment Sub-Category");
                subcategories=jsonStrings.getList(showCategory.getText().toString());
                Log.d("Sub Categories Error",subcategories.toString());
                item.putStringArrayList("items", (ArrayList<String>) subcategories);
                item.putString("isEdit",showSubCategory.getText().toString());
                selectSpinner.setArguments(item);
                showSelectSpinner();
            }
        });
        showMedium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                Bundle item=new Bundle();
                item.putString("type","Select Payment Mode");
                item.putStringArrayList("items", (ArrayList<String>) medium);
                item.putString("isEdit",showMedium.getText().toString());
                selectSpinner.setArguments(item);
                showSelectSpinner();
            }
        });
        /*
        Setting Mutable live data to update any changes that are made to the categories, subcategories and
         */
        viewModel.getCategory().observe(this,data->{
            showCategory.setText(data);
            categoryString=data;
            subcategories=jsonStrings.getList(categoryString);
            showSubCategory.setText(subcategories.get(0));
        });
        viewModel.getSubCategory().observe(this,data->{
            Log.d("Sub Categories Error","Data changed");
            showSubCategory.setText(data);
            subCategoryString=data;
        });
        viewModel.getMedium().observe(this,data->{
            showMedium.setText(data);
            mediumText=data;
        });
        /*
        Initializing more views
         */
        name=findViewById(R.id.editTextGetName);
        number=findViewById(R.id.editTextMobileNumber);
        editAmount=findViewById(R.id.edit_text_number);
        saveExpenses=findViewById(R.id.button);
        saveExpenses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                Saving expenses and  implementing logic to prevent any crashe
                 */
                Log.d("DEBUG TYPE","in save button start type="+type+" exptype="+exptype);
                if(type==0){
                    exptype=false;
                }
                else if(type==1){
                    exptype=true;
                }
                else if(type==2){
                    exptype=receivedGiven;

                }
                categoryString=showCategory.getText().toString();
                subCategoryString=showSubCategory.getText().toString();
                mediumText=showMedium.getText().toString();
                if(!(editAmount.getText().toString().trim().isEmpty())&&(type==0||type==1)){
                    Log.d("1234","Inside if");
                    Double amount= Double.valueOf(String.valueOf(editAmount.getText()));
                    Expenses expense=new Expenses(LocalDate.now(),categoryString,subCategoryString,mediumText,amount,exptype);
                    ExpenseViewModel.insert(expense,getApplicationContext());
                    finish();
                } else if(type==2&&!(editAmount.getText().toString().trim().isEmpty())&&!(name.getText().toString().trim().isEmpty())){
                    Log.d("1234","Inside else if");
                    Double amount= Double.valueOf(String.valueOf(editAmount.getText()));
                    LocalDate localDate=LocalDate.now();
                    String getName= String.valueOf(name.getText());
                    String contactNumber=String.valueOf(number.getText());
                    String category=receivedGiven?"Money Received":"Money Given";
                    if(aSwitch.isChecked()==false){
                        PersonExp personExp=new PersonExp(localDate,receivedGiven,getName,contactNumber,mediumText,false,null,amount);
                        Expenses expense=new Expenses(localDate,category,getName,mediumText,amount,exptype);
                        PersonExpViewModel.insert(personExp);
                        ExpenseViewModel.insert(expense,getApplicationContext());
                        finish();
                    }else{
                        /*Setting One Time reminders for tasks if user wants using Alarm service
                         */
                        if(targetDate!=null){
                            PersonExp personExp=new PersonExp(localDate,receivedGiven,getName,contactNumber,mediumText,true,targetDate,amount);
                            Expenses expense=new Expenses(localDate,category,getName,mediumText,amount,exptype);
                            PersonExpViewModel.insert(personExp);
                            ExpenseViewModel.insert(expense,getApplicationContext());
                            if(sendMsgSwitch.isChecked()) {
                                if(!number.getText().toString().trim().isEmpty()) {
                                    if(!msgTxtEditText.getText().toString().trim().isEmpty()) {
                                        scheduleNotification(personExp, true,msgTxtEditText.getText().toString().trim());
                                    }else{
                                        showSnackbar(v,"Number CanNot be empty when sending a Msg.",Snackbar.LENGTH_LONG);
                                    }
                                }else{
                                    showSnackbar(v,"Number CanNot be empty when sending a Msg.",Snackbar.LENGTH_LONG);
                                }
                            }else {
                                scheduleNotification(personExp,false,"");
                            }
                            finish();
                        }else{
                            showSnackbar(findViewById(android.R.id.content), String.valueOf(R.string.invalid_date),Snackbar.LENGTH_SHORT);
                        }
                    }

                }else {
                    showSnackbar(findViewById(android.R.id.content), String.valueOf(R.string.invalid_expense),Snackbar.LENGTH_SHORT);
                }

            }
        });





    }
    /*
    If activity is started after clicking at item in recycle view to quickly save a existing type of expense then using intent to set up the create expense page
     */

    @Override
    protected void onResume() {
        super.onResume();
        /*
        Logic to properly receive data for correct type of task
        Using intent to getExtras to get teh needed elements
         */
        Intent intent2=getIntent();
        int typeOf=intent2.getIntExtra("Type",0);
        Log.d("Adv Select","Type"+type);
        if(typeOf==1){
            boolean typeExp=intent2.getBooleanExtra("TypeExpense",false);
            int intentchangeView=intent2.getIntExtra("ChangeView",0);
            if(intentchangeView==0) {
                if (typeExp == false) {
                    radioGroup.check(R.id.radioButtonSpend);
                } else {
                    radioGroup.check(R.id.radioButtonEarned);
                }
                String categoryReceived=intent2.getStringExtra("Category");
                showCategory.setText(categoryReceived);
                subcategories=jsonStrings.getList(intent2.getStringExtra("Category"));
                showSubCategory.setText(subcategories.get(0));
                categoryString=intent2.getStringExtra("Category");
            }else{
                radioGroup.check(R.id.radioButtonLendRecieve);
                if(typeExp==false)
                    received.setChecked(true);
                else
                    given.setChecked(true);

            }
        }else if(typeOf==2){
            boolean typeExp=intent2.getBooleanExtra("TypeExpense",false);
            radioGroup.check(R.id.radioButtonLendRecieve);
            if(typeExp==false)
                received.setChecked(true);
            else
                given.setChecked(true);
            name.setText(intent2.getStringExtra("Name"));
            number.setText(intent2.getStringExtra("ContactNumber"));
            showMedium.setText(intent2.getStringExtra("Medium"));
        }else if(typeOf==4){
            boolean typeExp=intent2.getBooleanExtra("TypeExpense",false);
            int intentchangeView=intent2.getIntExtra("ChangeView",0);
            if(intentchangeView==0) {
                if (typeExp == false) {
                    radioGroup.check(R.id.radioButtonSpend);
                } else {
                    radioGroup.check(R.id.radioButtonEarned);
                }
                showCategory.setText(intent2.getStringExtra("Category"));
                categoryString=intent2.getStringExtra("Category");

                subcategories=jsonStrings.getList(intent2.getStringExtra("Category"));

                Log.d("SubCategory issue", String.valueOf(subcategories.indexOf(intent2.getStringExtra("SubCategory"))));
                showMedium.setText(intent2.getStringExtra("Medium"));
                showSubCategory.setText(intent2.getStringExtra("SubCategory"));
                donotChange=true;
                Log.d("SubCategory issue", "Sub Category set");

            }else{
                radioGroup.check(R.id.radioButtonLendRecieve);
                if(typeExp==false)
                    received.setChecked(true);
                else
                    given.setChecked(true);
                name.setText(intent2.getStringExtra("Name"));
                showMedium.setText(intent2.getStringExtra("Medium"));
            }
        }
    }

    private void scheduleNotification(PersonExp personExp, boolean type, String msg) {
        /*
        Using pending intent to set up the alarm at the day user defined at current time + few seconds
         */
        /*
        TODO Option to save pending intent and give user option to delete or reschedule the notification.
         */
        Intent intent = new Intent(context, AlarmReceiver.class);
        if(type==false) {
            intent.putExtra("Person_Name", personExp.getName());
            intent.putExtra("Amount", String.valueOf(personExp.getAmount().intValue()));
            String typeString;
            if (personExp.getType()) {
                typeString = "Money Reminder: Payback";
            } else
                typeString = "Money Reminder: Collect";
            intent.putExtra("Type", typeString);
            intent.putExtra("SendMsg",false);
            intent.putExtra("Number","1234567890");
        }else{
            intent.putExtra("Person_Name", personExp.getName());
            intent.putExtra("Amount",String.valueOf(personExp.getAmount().intValue()));
            intent.putExtra("SendMsg",true);
            intent.putExtra("Number",personExp.getContactNumber());
            intent.putExtra("TextToSent",msg);

        }
        // Create a PendingIntent for the alarm
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                getUniqueNotificationId(),
                intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_MUTABLE
        );

        // Get the AlarmManager and schedule the alarm
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        LocalDateTime localDateTime=LocalDateTime.of(personExp.getPendingDate(), LocalTime.now().plusSeconds(10));

        // Calculate the notification time in milliseconds
        long notificationTime = localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        // Ensure that the notification time is in the future
        if (notificationTime > System.currentTimeMillis()) {
            String[] permissions = {Manifest.permission.SEND_SMS};
            List<String> permissionList = new ArrayList<>();
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionList.add(permission);
                }
            }
            if (!permissionList.isEmpty()) {
                ActivityCompat.requestPermissions(this, permissionList.toArray(new String[0]),
                        PackageManager.PERMISSION_GRANTED);
            }else {
                // Permission is already granted, proceed with sending SMS
                alarmManager.set(AlarmManager.RTC_WAKEUP, notificationTime, pendingIntent);
            }
        } else {
            // Handle the case where the selected time is in the past
            Toast.makeText(context, "Selected time is in the past", Toast.LENGTH_SHORT).show();
        }

    }

    // Method to generate a unique notification ID
    private int getUniqueNotificationId() {
        return (int) System.currentTimeMillis();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.add_expense_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        /*
        Using popUp menu to show options
        I do not the native menu as it hides the option button
         */
        int id=item.getItemId();
        if(id==R.id.expmenu_options){
            PopupMenu popupMenu = new PopupMenu((Context) CreateExpenses.this,toolbar, Gravity.RIGHT);
            popupMenu.getMenuInflater().inflate(R.menu.expense_menu_fetch, popupMenu.getMenu());
            popupMenu.show();
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    int id=item.getItemId();
                    Log.d("Menu",getResources().getResourceEntryName(id));
                    if(id==R.id.menu_action_search){
                        Intent intent=new Intent(context, EditAdapter.class);
                        intent.putExtra("calling_activity_class","com.tcssol.expensetracker.CreateExpenses");
                        startActivity(intent);
                    }
                    return false;
                }
            });
        }
        return super.onOptionsItemSelected(item);
    }
    private void showSelectSpinner() {
        selectSpinner.show(getSupportFragmentManager(),selectSpinner.getTag());
    }
    private void showSnackbar(View view,String message,int duration){
        Snackbar.make(view,message,duration).show();
    }

}