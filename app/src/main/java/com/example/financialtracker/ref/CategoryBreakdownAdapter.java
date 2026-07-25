package com.example.financialtracker.ref;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.financialtracker.R;

import java.util.List;
import java.util.Locale;

public class CategoryBreakdownAdapter extends RecyclerView.Adapter<CategoryBreakdownAdapter.RowViewHolder> {

    /** Plain data holder for one row: a category and its total + share of the whole. */
    public static class CategoryTotal {
        public final String category;
        public final double amount;
        public final double percentage;

        public CategoryTotal(String category, double amount, double percentage) {
            this.category = category;
            this.amount = amount;
            this.percentage = percentage;
        }
    }

    private final List<CategoryTotal> rows;

    public CategoryBreakdownAdapter(List<CategoryTotal> rows) {
        this.rows = rows;
    }

    @NonNull
    @Override
    public RowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_summary_row, parent, false);
        return new RowViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RowViewHolder holder, int position) {
        CategoryTotal row = rows.get(position);
        holder.tvCategoryName.setText(row.category);
        holder.tvPercentage.setText(String.format(Locale.US, "%.1f%%", row.percentage));
        holder.tvAmount.setText(String.format(Locale.US, "P%.2f", row.amount));
    }

    @Override
    public int getItemCount() {
        return rows != null ? rows.size() : 0;
    }

    static class RowViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryName, tvPercentage, tvAmount;

        RowViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            tvPercentage = itemView.findViewById(R.id.tvPercentage);
            tvAmount = itemView.findViewById(R.id.tvAmount);
        }
    }
}