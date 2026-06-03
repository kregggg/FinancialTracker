package com.example.financialtracker.ref;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "transactions_table")
public class Transaction {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String description;
    private double amount;
    private String category;
    private long timestamp;
    private String transactionType; // Will hold "EXPENSE" or "INCOME"

    public Transaction(String description, double amount, String category, long timestamp, String transactionType) {
        this.description = description;
        this.amount = amount;
        this.category = category;
        this.timestamp = timestamp;
        this.transactionType = transactionType;
    }

    // --- Getters and Setters (Required by Room) ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
}