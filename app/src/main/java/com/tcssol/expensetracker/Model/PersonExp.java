package com.tcssol.expensetracker.Model;

import androidx.room.Entity;
import androidx.room.ColumnInfo;
import androidx.room.PrimaryKey;

import com.tcssol.expensetracker.Utils.Converters;

import java.time.LocalDate;

@Entity(tableName = "person_expenses")
public class PersonExp {
    @PrimaryKey(autoGenerate = true)
    private long id;
    @ColumnInfo(name="date_created")
    private LocalDate dateCreated;
    private Boolean type;
    private String name;
    private Double amount;
    @ColumnInfo(name="contact_number")
    private String contactNumber;
    private String mode;
    @ColumnInfo(name="has_date")
    private Boolean hasDate;
    @ColumnInfo(name="pending_date")
    private LocalDate pendingDate;

    public PersonExp(LocalDate dateCreated, Boolean type, String name, String contactNumber,String mode, Boolean hasDate, LocalDate pendingDate,Double amount) {
        this.dateCreated = dateCreated;
        this.type = type;
        this.name = name;
        this.contactNumber = contactNumber;
        this.mode=mode;
        this.hasDate = hasDate;
        this.pendingDate = pendingDate;
        this.amount=amount;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }
    public LocalDate getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(LocalDate dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Boolean getType() {
        return type;
    }

    public void setType(Boolean type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public Boolean getHasDate() {
        return hasDate;
    }

    public void setHasDate(Boolean hasDate) {
        this.hasDate = hasDate;
    }

    public LocalDate getPendingDate() {
        return pendingDate;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public void setPendingDate(LocalDate pendingDate) {
        this.pendingDate = pendingDate;
    }
    public static String toCsvFormat(PersonExp data){
        String typeString=String.valueOf(data.getType());
        String amountString=String.valueOf(data.getAmount());
        String temp= String.format("%d,%s,%s,%s,%s,%s,%s,%s\n", data.getId(),Converters.toString(data.getDateCreated()),data.getName(),data.getContactNumber(), data.getMode(),Converters.toString(data.getPendingDate()),typeString,amountString);
        return temp;
    }
    public static String toTxtFormat(PersonExp data){
        String typeString=String.valueOf(data.getType());
        String amountString=String.valueOf(data.getAmount());
        String temp= String.format("%d\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n", data.getId(),Converters.toString(data.getDateCreated()),data.getName(),data.getContactNumber(),data.getMode(),Converters.toString(data.getPendingDate()),typeString,amountString);
        return temp;
    }
}
