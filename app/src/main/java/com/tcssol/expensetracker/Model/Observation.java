package com.tcssol.expensetracker.Model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * A money transaction detected from an incoming bank/UPI SMS, waiting for the
 * user to categorize it into a real expense. Deduplicated on (timeMillis, amount).
 */
@Entity(tableName = "observations_table",
        indices = {@Index(value = {"time_millis", "amount"}, unique = true)})
public class Observation {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "amount")
    private double amount;

    /** true = money in (credited), false = money out (debited) */
    @ColumnInfo(name = "credit")
    private boolean credit;

    @ColumnInfo(name = "time_millis")
    private long timeMillis;

    @ColumnInfo(name = "sender")
    private String sender;

    @ColumnInfo(name = "body")
    private String body;

    public Observation(double amount, boolean credit, long timeMillis, String sender, String body) {
        this.amount = amount;
        this.credit = credit;
        this.timeMillis = timeMillis;
        this.sender = sender;
        this.body = body;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public boolean isCredit() {
        return credit;
    }

    public void setCredit(boolean credit) {
        this.credit = credit;
    }

    public long getTimeMillis() {
        return timeMillis;
    }

    public void setTimeMillis(long timeMillis) {
        this.timeMillis = timeMillis;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
