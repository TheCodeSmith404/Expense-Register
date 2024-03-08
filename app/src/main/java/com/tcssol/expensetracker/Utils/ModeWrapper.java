package com.tcssol.expensetracker.Utils;

import androidx.room.ColumnInfo;
import androidx.room.Entity;

@Entity(tableName = "mode_wrapper")
public class ModeWrapper {
    @ColumnInfo(name="Mode")
    String name;
    @ColumnInfo(name="Count")
    Double perentage;
    public ModeWrapper(String name, Double perentage) {
        this.name = name;
        this.perentage = perentage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPerentage() {
        return perentage;
    }

    public void setPerentage(Double perentage) {
        this.perentage = perentage;
    }


}
