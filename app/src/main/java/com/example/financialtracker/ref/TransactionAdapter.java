package com.example.financialtracker.ref;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.financialtracker.R;
import com.example.financialtracker.ref.Transaction;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Transaction> transactionList;

    public TransactionAdapter(List<Transaction> transactionList) {
        this.transactionList = transactionList;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.transaction_row_activity, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);

        holder.tvDescription.setText(transaction.getDescription());
        holder.tvCategory.setText(transaction.getCategory());

        // Add minus sign for expenses visually if desired, otherwise just show the price
        if ("EXPENSE".equalsIgnoreCase(transaction.getTransactionType())) {
            holder.tvPrice.setText(String.format(Locale.US, "-P%.2f", transaction.getAmount()));
            holder.tvPrice.setTextColor(0xFFD32F2F); // Red color for expenses
        } else {
            holder.tvPrice.setText(String.format(Locale.US, "P%.2f", transaction.getAmount()));
            holder.tvPrice.setTextColor(0xFF388E3C); // Green color for income
        }
    }

    @Override
    public int getItemCount() {
        return transactionList != null ? transactionList.size() : 0;
    }

    /**
     * Replaces the old dataset with a updated list and refreshes the layout view.
     */
    public void updateData(List<Transaction> newList) {
        this.transactionList = newList;
        notifyDataSetChanged();
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView tvDescription, tvCategory, tvPrice;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDescription = itemView.findViewById(R.id.tvRowDescription);
            tvCategory = itemView.findViewById(R.id.tvRowCategory);
            tvPrice = itemView.findViewById(R.id.tvRowPrice);
        }
    }
}