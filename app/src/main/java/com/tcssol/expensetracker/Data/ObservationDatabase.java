package com.tcssol.expensetracker.Data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.tcssol.expensetracker.Model.Observation;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Standalone database for SMS-detected transactions. Kept separate from the
 * expense/person databases so it can evolve without migrations touching user data.
 */
@Database(entities = {Observation.class}, version = 1, exportSchema = false)
public abstract class ObservationDatabase extends RoomDatabase {
    public static final String DATABASE_NAME = "observations_database";
    private static volatile ObservationDatabase INSTANCE;
    public static final ExecutorService databaseWriterExecutor
            = Executors.newFixedThreadPool(2);

    public static ObservationDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (ObservationDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    ObservationDatabase.class, DATABASE_NAME)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public abstract ObservationDao observationDao();
}
