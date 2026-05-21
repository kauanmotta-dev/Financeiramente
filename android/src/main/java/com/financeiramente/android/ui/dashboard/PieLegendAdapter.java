package com.financeiramente.android.ui.dashboard;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.financeiramente.android.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PieLegendAdapter extends RecyclerView.Adapter<PieLegendAdapter.ViewHolder> {

    public static class LegendItem {
        public final String nome;
        public final float percentual;
        public final int color;

        public LegendItem(String nome, float percentual, int color) {
            this.nome = nome;
            this.percentual = percentual;
            this.color = color;
        }
    }

    private List<LegendItem> items = new ArrayList<>();

    public void setItems(List<LegendItem> items) {
        this.items = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pie_legenda, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final View viewDot;
        private final TextView tvNome;
        private final TextView tvPercentual;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            viewDot      = itemView.findViewById(R.id.view_dot);
            tvNome       = itemView.findViewById(R.id.tv_legenda_nome);
            tvPercentual = itemView.findViewById(R.id.tv_legenda_percentual);
        }

        void bind(LegendItem item) {
            viewDot.setBackgroundTintList(ColorStateList.valueOf(item.color));
            tvNome.setText(item.nome);
            tvPercentual.setText(String.format(Locale.getDefault(), "%.0f%%", item.percentual));
        }
    }
}
