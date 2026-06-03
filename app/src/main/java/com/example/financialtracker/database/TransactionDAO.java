package com.example.financialtracker.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.financialtracker.ref.Transaction;

import java.util.List;

@Dao
public interface TransactionDAO {

    @Insert
    void insertTransaction(Transaction transaction);

    // CRITICAL FOR MODIFY OVERLAYS: Overwrites an existing row based on its ID
    @Update
    void updateTransaction(Transaction transaction);

    // CRITICAL FOR MODIFY OVERLAYS: Deletes a specific row permanently
    @Delete
    void deleteTransaction(Transaction transaction);

    @Query("SELECT * FROM transactions_table ORDER BY timestamp DESC")
    List<Transaction> getAllTransactions();

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions_table WHERE transactionType = :type AND timestamp >= :startTime")
    double getTotalAmountByTypeSince(String type, long startTime);
}
