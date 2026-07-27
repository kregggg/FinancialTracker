package com.example.financialtracker.ref;

public class WeekRecord {
    public final int weekNumber;
    public final long weekStartMillis;
    public final long weekEndMillis;

    public WeekRecord(int weekNumber, long weekStartMillis, long weekEndMillis) {
        this.weekNumber = weekNumber;
        this.weekStartMillis = weekStartMillis;
        this.weekEndMillis = weekEndMillis;
    }
}