package com.example.financialtracker.ref;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.financialtracker.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WeeklyRecordAdapter extends RecyclerView.Adapter<WeeklyRecordAdapter.WeekViewHolder> {

    public interface OnWeekClickListener {
        void onWeekClicked(WeekRecord week);
    }

    private final List<WeekRecord> weeks;
    private final OnWeekClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.US);

    public WeeklyRecordAdapter(List<WeekRecord> weeks, OnWeekClickListener listener) {
        this.weeks = weeks;
        this.listener = listener;
    }

    @NonNull
    @Override
    public WeekViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.weekly_record_card, parent, false);
        return new WeekViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeekViewHolder holder, int position) {
        WeekRecord week = weeks.get(position);

        holder.tvWeekTitle.setText("Week " + week.weekNumber);
        holder.tvDateRange.setText(
                dateFormat.format(new Date(week.weekStartMillis)) + " - " +
                        dateFormat.format(new Date(week.weekEndMillis))
        );

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onWeekClicked(week);
        });
    }

    @Override
    public int getItemCount() {
        return weeks != null ? weeks.size() : 0;
    }

    static class WeekViewHolder extends RecyclerView.ViewHolder {
        TextView tvWeekTitle, tvDateRange;

        WeekViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWeekTitle = itemView.findViewById(R.id.tvWeekTitle);
            tvDateRange = itemView.findViewById(R.id.tvDateRange);
        }
    }
}