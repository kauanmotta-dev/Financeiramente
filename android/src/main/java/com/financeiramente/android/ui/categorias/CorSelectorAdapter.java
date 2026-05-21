package com.financeiramente.android.ui.categorias;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.financeiramente.android.R;

import java.util.Arrays;
import java.util.List;

public class CorSelectorAdapter extends RecyclerView.Adapter<CorSelectorAdapter.ViewHolder> {

    public interface OnCorSelectedListener {
        void onCorSelected(String cor);
    }

    public static final List<String> CORES = Arrays.asList(
            "#6366F1", "#0EA5E9", "#F97316", "#10B981",
            "#8B5CF6", "#EC4899", "#F59E0B", "#EF4444",
            "#64748B", "#14B8A6", "#A855F7", "#06B6D4",
            "#84CC16", "#F43F5E", "#D97706", "#7C3AED"
    );

    private final Context context;
    private final OnCorSelectedListener listener;
    private String selectedCor;

    public CorSelectorAdapter(Context context, String initialCor,
                              OnCorSelectedListener listener) {
        this.context = context;
        this.selectedCor = initialCor != null ? initialCor : CORES.get(0);
        this.listener = listener;
    }

    public String getSelectedCor() {
        return selectedCor;
    }

    public void setSelectedCor(String cor) {
        String old = this.selectedCor;
        this.selectedCor = cor;
        int oldPos = CORES.indexOf(old);
        int newPos = CORES.indexOf(cor);
        if (oldPos >= 0) notifyItemChanged(oldPos);
        if (newPos >= 0) notifyItemChanged(newPos);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_cor_selector, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String cor = CORES.get(position);

        // Set circle color
        try {
            int colorInt = Color.parseColor(cor);
            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.OVAL);
            bg.setColor(colorInt);
            holder.vCorCircle.setBackground(bg);
        } catch (IllegalArgumentException ignored) {
            holder.vCorCircle.setBackgroundResource(R.drawable.bg_category_icon);
        }

        boolean selected = cor.equals(selectedCor);
        holder.ivCheck.setVisibility(selected ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            String newCor = CORES.get(holder.getAdapterPosition());
            setSelectedCor(newCor);
            listener.onCorSelected(newCor);
        });
    }

    @Override
    public int getItemCount() {
        return CORES.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final View vCorCircle;
        final ImageView ivCheck;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            vCorCircle = itemView.findViewById(R.id.v_cor_circle);
            ivCheck    = itemView.findViewById(R.id.iv_check);
        }
    }
}
