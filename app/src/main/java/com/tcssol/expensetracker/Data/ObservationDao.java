package com.tcssol.expensetracker.Data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.tcssol.expensetracker.Model.Observation;

import java.util.List;

@Dao
public interface ObservationDao {

    /** Returns -1 when an observation with the same time and amount already exists. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(Observation observation);

    @Query("SELECT * FROM observations_table ORDER BY time_millis DESC")
    LiveData<List<Observation>> getAll();

    @Query("DELETE FROM observations_table WHERE id=:id")
    void deleteById(long id);

    @Query("DELETE FROM observations_table")
    void deleteAll();
}
