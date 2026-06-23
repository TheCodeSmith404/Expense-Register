package com.tcssol.expensetracker.Data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.tcssol.expensetracker.Model.PersonExp
import com.tcssol.expensetracker.Utils.Converters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [PersonExp::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class PersonExpDatabase : RoomDatabase() {

    abstract fun personExpDao(): PersonExpDao

    companion object {
        const val DATABASE_NAME = "person_exp_database2"

        @Volatile
        private var INSTANCE: PersonExpDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE person_expenses ADD COLUMN note TEXT")
            }
        }

        private val sRoomDatabaseCallback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        database.personExpDao().deleteAll()
                    }
                }
            }
        }

        fun getDatabase(context: Context): PersonExpDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PersonExpDatabase::class.java,
                    DATABASE_NAME
                )
                    .addCallback(sRoomDatabaseCallback)
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
