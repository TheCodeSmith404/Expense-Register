package com.tcssol.expensetracker.Data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.tcssol.expensetracker.Model.CategoryConfig;

import java.util.List;

@Dao
public interface CategoryConfigDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdateCategoryConfig(CategoryConfig config);

    @Query("SELECT * FROM category_config_table")
    LiveData<List<CategoryConfig>> getAllCategoryConfigs();

    @Query("SELECT * FROM category_config_table WHERE category_name = :name LIMIT 1")
    CategoryConfig getCategoryConfigSync(String name);

    @Query("SELECT * FROM category_config_table WHERE category_name = :name LIMIT 1")
    LiveData<CategoryConfig> getCategoryConfig(String name);
}
