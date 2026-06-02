package com.example.financialtracker.ref;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class User {
    // Base information
    String username;
    String allowanceType;
    int daysWithClasses;
    double allowance;
    double savings;

    // Expenses
    Map<String, Date[]> dateMap = new HashMap<>(); // <Week number, Dates within that week>
    Map<Date, DaySummary> daySummaryMap = new HashMap<>(); // the date  from the dateMap then its corresponding summary
}
