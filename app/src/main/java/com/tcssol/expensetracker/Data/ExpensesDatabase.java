package com.tcssol.expensetracker.Data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.tcssol.expensetracker.Model.Expenses;
import com.tcssol.expensetracker.Utils.Converters;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
@Database(entities={Expenses.class},version=1,exportSchema = false)
@TypeConverters({Converters.class})
//@TypeConverters({Converter.class})
public abstract class ExpensesDatabase extends RoomDatabase{
    public static final int NUMBER_OF_THREADS=4;
    public static final String DATABASE_NAME="expenses_database1";
    private static volatile ExpensesDatabase INSTANCE;
    public static final ExecutorService databaseWriterExecutor
            = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static final RoomDatabase.Callback sRoomDatabaseCallback=
            new RoomDatabase.Callback(){
                @Override
                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                    super.onCreate(db);
                    databaseWriterExecutor.execute(()->{
                        //invoke Dao, and write
                        ExpenseDao expenseDao=INSTANCE.expenseDao();
                        expenseDao.deleteAll();// clean slate

                        //writing to our table
                    });
                }
            };

    public static ExpensesDatabase getDatabase(final Context context){
        if(INSTANCE==null){
            synchronized (ExpensesDatabase.class){
                if(INSTANCE==null){
                    INSTANCE= Room.databaseBuilder(context.getApplicationContext(),
                                    ExpensesDatabase.class,DATABASE_NAME)
                            .addCallback(sRoomDatabaseCallback)
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
    public abstract ExpenseDao expenseDao();

}
