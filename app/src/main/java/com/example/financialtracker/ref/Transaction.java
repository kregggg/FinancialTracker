package com.example.financialtracker.ref;

public class Transaction {
    String description;
    String category;
    double amount;
    TransacType type;

    public Transaction(){}

    public Transaction(String description, String category, double amount, TransacType type){
        this.description = description;
        this.category = category;
        this.amount = amount;
        this.type = type;
    }

    public void modifyExpense (String description, String category, double amount){
        if (!description.isEmpty()){
            this.description = description;
        }

        if (!category.isEmpty()){
            this.category = category;
        }

        if (amount != 0){
            this.amount = amount;
        }
    }

    public void receiveAllowance (double amount){
        this.description = "Allowance";
        this.category = "Allowance";
        this.amount = amount;
        this.type = TransacType.INCOME;
    }

    public enum TransacType{
        INCOME,
        EXPENSE
    }

}
