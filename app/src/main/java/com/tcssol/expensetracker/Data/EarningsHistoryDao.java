package com.tcssol.expensetracker.Data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.tcssol.expensetracker.Model.EarningsHistory;

import java.util.List;

@Dao
public interface EarningsHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertEarningsHistory(EarningsHistory history);

    @Query("SELECT * FROM earnings_history_table ORDER BY timestamp ASC")
    LiveData<List<EarningsHistory>> getAllEarningsHistory();

    @Query("SELECT * FROM earnings_history_table ORDER BY timestamp ASC")
    List<EarningsHistory> getAllEarningsHistorySync();

    @Query("DELETE FROM earnings_history_table")
    void deleteAll();
}
