package com.example.financialtracker.ref;

import java.text.DateFormatSymbols;
import java.util.Date;

public class DaySummary {
    Date date;
    double income;
    double expenses;

    Transaction[] transactions;

    public DaySummary(Date date, double income, double  expenses, Transaction[] transactions){
        this.date = date;
        this.income = income;
        this.expenses = expenses;
        this.transactions = transactions;
    }
}
