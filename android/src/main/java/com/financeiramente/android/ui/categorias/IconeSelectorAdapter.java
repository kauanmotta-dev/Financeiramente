package com.financeiramente.android.ui.categorias;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.financeiramente.android.R;

import java.util.Arrays;
import java.util.List;

public class IconeSelectorAdapter extends RecyclerView.Adapter<IconeSelectorAdapter.ViewHolder> {

    public interface OnIconeSelectedListener {
        void onIconeSelected(String icone);
    }

    public static final List<String> ICONES = Arrays.asList(
            "🏠", "🚗", "🍔", "❤️", "📚", "🎮", "🛍️", "✈️",
            "💰", "💳", "🏋️", "🎵", "🍕", "⚡", "🐕", "🎁",
            "🎨", "🏆", "💼", "🏥", "📱", "🎯", "🌿", "➕"
    );

    private final Context context;
    private final OnIconeSelectedListener listener;
    private String selectedIcone;

    public IconeSelectorAdapter(Context context, String initialIcone,
                                OnIconeSelectedListener listener) {
        this.context = context;
        this.selectedIcone = initialIcone != null ? initialIcone : ICONES.get(0);
        this.listener = listener;
    }

    public String getSelectedIcone() {
        return selectedIcone;
    }

    public void setSelectedIcone(String icone) {
        String old = this.selectedIcone;
        this.selectedIcone = icone;
        int oldPos = ICONES.indexOf(old);
        int newPos = ICONES.indexOf(icone);
        if (oldPos >= 0) notifyItemChanged(oldPos);
        if (newPos >= 0) notifyItemChanged(newPos);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_icone_selector, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String icone = ICONES.get(position);
        holder.tvIcone.setText(icone);

        boolean selected = icone.equals(selectedIcone);
        holder.vSelectionRing.setVisibility(selected ? View.VISIBLE : View.GONE);
        holder.flIconeBg.setAlpha(selected ? 1.0f : 0.7f);

        holder.itemView.setOnClickListener(v -> {
            String newIcone = ICONES.get(holder.getAdapterPosition());
            setSelectedIcone(newIcone);
            listener.onIconeSelected(newIcone);
        });
    }

    @Override
    public int getItemCount() {
        return ICONES.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final FrameLayout flIconeBg;
        final TextView tvIcone;
        final View vSelectionRing;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            flIconeBg     = itemView.findViewById(R.id.fl_icone_bg);
            tvIcone       = itemView.findViewById(R.id.tv_icone);
            vSelectionRing = itemView.findViewById(R.id.v_selection_ring);
        }
    }
}
