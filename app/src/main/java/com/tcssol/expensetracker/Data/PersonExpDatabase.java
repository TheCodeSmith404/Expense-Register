package com.tcssol.expensetracker.Data;


import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.tcssol.expensetracker.Model.PersonExp;
import com.tcssol.expensetracker.Utils.Converters;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
@Database(entities = {PersonExp.class},version = 1,exportSchema = false)
@TypeConverters({Converters.class})
public abstract class PersonExpDatabase extends RoomDatabase {
    public static final int NUMBER_OF_THREADS=4;
    public static final String DATABASE_NAME="person_exp_database2";
    private static volatile PersonExpDatabase INSTANCE;
    public static final ExecutorService databaseWriterExecutor
            = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    public static final RoomDatabase.Callback sRoomDatabaseCallback=
            new RoomDatabase.Callback(){
                @Override
                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                    super.onCreate(db);
                    databaseWriterExecutor.execute(()->{
                        //invoke Dao, and write
                        PersonExpDao expenseDao=INSTANCE.personExpDao();
                        expenseDao.deleteAll();// clean slate

                        //writing to our table
                    });
                }
            };

    public static PersonExpDatabase getDatabase(final Context context){
        if(INSTANCE==null){
            synchronized (PersonExpDatabase.class){
                if(INSTANCE==null){
                    INSTANCE= Room.databaseBuilder(context.getApplicationContext(),
                                    PersonExpDatabase.class,DATABASE_NAME)
                            .addCallback(sRoomDatabaseCallback)
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
    public abstract PersonExpDao personExpDao();

}
