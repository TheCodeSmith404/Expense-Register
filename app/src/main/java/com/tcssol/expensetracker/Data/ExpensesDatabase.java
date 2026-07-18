package com.tcssol.expensetracker.Data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.tcssol.expensetracker.Model.CategoryConfig;
import com.tcssol.expensetracker.Model.Expenses;
import com.tcssol.expensetracker.Model.EarningsHistory;
import com.tcssol.expensetracker.Utils.Converters;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Expenses.class, CategoryConfig.class, EarningsHistory.class}, version = 5, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class ExpensesDatabase extends RoomDatabase {
    public static final int NUMBER_OF_THREADS = 4;
    public static final String DATABASE_NAME = "expenses_database1";
    private static volatile ExpensesDatabase INSTANCE;
    public static final ExecutorService databaseWriterExecutor
            = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    /**
     * Migration from v1 to v2: adds the optional 'note' text column.
     */
    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE expenses_table ADD COLUMN note TEXT");
        }
    };

    /**
     * Migration from v2 to v3: adds the category_config_table.
     */
    public static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `category_config_table` (`category_name` TEXT NOT NULL, `is_fixed` INTEGER NOT NULL, PRIMARY KEY(`category_name`))");
        }
    };

    /**
     * Migration from v3 to v4: adds the budget column to category_config_table.
     */
    public static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE category_config_table ADD COLUMN budget REAL NOT NULL DEFAULT 0.0");
        }
    };

    /**
     * Migration from v4 to v5: adds the earnings_history_table.
     */
    public static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `earnings_history_table` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `timestamp` INTEGER NOT NULL, `monthly_earnings` REAL NOT NULL, `days_worked` INTEGER NOT NULL, `hours_worked` REAL NOT NULL, `hourly_rate` REAL NOT NULL)");
        }
    };

    public static final RoomDatabase.Callback sRoomDatabaseCallback =
            new RoomDatabase.Callback() {
                @Override
                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                    super.onCreate(db);
                    databaseWriterExecutor.execute(() -> {
                        ExpenseDao expenseDao = INSTANCE.expenseDao();
                        expenseDao.deleteAll();
                    });
                }
            };

    public static ExpensesDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (ExpensesDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    ExpensesDatabase.class,
                                    DATABASE_NAME)
                            .addCallback(sRoomDatabaseCallback)
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                            .build(); // allowMainThreadQueries() removed – use Executor/LiveData
                }
            }
        }
        return INSTANCE;
    }

    public abstract ExpenseDao expenseDao();
    public abstract CategoryConfigDao categoryConfigDao();
    public abstract EarningsHistoryDao earningsHistoryDao();
}
