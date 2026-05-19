package com.financeiramente.android.ui.metas;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.financeiramente.android.R;
import com.financeiramente.core.domain.entity.Meta;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MetaAdapter extends RecyclerView.Adapter<MetaAdapter.ViewHolder> {

    public interface OnMetaClickListener {
        void onClick(Meta meta);
    }

    public interface OnMetaLongClickListener {
        boolean onLongClick(Meta meta);
    }

    private List<Meta> items = new ArrayList<>();
    private final OnMetaClickListener onClick;
    private final OnMetaLongClickListener onLongClick;

    public MetaAdapter(OnMetaClickListener onClick, OnMetaLongClickListener onLongClick) {
        this.onClick = onClick;
        this.onLongClick = onLongClick;
    }

    public void setItems(List<Meta> lista) {
        this.items = lista != null ? lista : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_meta, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Meta meta = items.get(position);
        h.tvNome.setText(meta.getNome());

        double percentual = meta.getValorObjetivo() > 0
                ? (meta.getValorAtual() / meta.getValorObjetivo()) * 100
                : 0;
        h.tvPercentual.setText(String.format(Locale.getDefault(), "%.1f%%", Math.min(percentual, 100)));
        h.tvValores.setText(String.format(Locale.getDefault(),
                "R$ %.2f / R$ %.2f", meta.getValorAtual(), meta.getValorObjetivo()));
        h.progressBar.setMax(100);
        h.progressBar.setProgress((int) Math.min(percentual, 100));

        h.itemView.setOnClickListener(v -> onClick.onClick(meta));
        h.itemView.setOnLongClickListener(v -> onLongClick.onLongClick(meta));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNome;
        TextView tvPercentual;
        TextView tvValores;
        ProgressBar progressBar;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNome = itemView.findViewById(R.id.tv_meta_nome);
            tvPercentual = itemView.findViewById(R.id.tv_meta_percentual);
            tvValores = itemView.findViewById(R.id.tv_meta_valores);
            progressBar = itemView.findViewById(R.id.pb_meta_progresso);
        }
    }
}
