package com.financeiramente.android.ui.metas;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.financeiramente.android.R;
import com.financeiramente.core.domain.entity.AporteMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AporteAdapter extends RecyclerView.Adapter<AporteAdapter.ViewHolder> {

    private static final double THOUSAND = 1_000d;
    private static final double MILLION = 1_000_000d;
    private static final double BILLION = 1_000_000_000d;
    private static final double TRILLION = 1_000_000_000_000d;

    public interface OnAporteDeleteClickListener {
        void onDeleteClick(AporteMeta aporte);
    }

    private List<AporteMeta> items = new ArrayList<>();
    private final OnAporteDeleteClickListener onDeleteClick;

    public AporteAdapter(OnAporteDeleteClickListener onDeleteClick) {
        this.onDeleteClick = onDeleteClick;
    }

    public void setItems(List<AporteMeta> lista) {
        this.items = lista != null ? lista : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_aporte, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        AporteMeta aporte = items.get(position);
        h.tvData.setText(aporte.getData());
        h.tvValor.setText(formatCurrencyCompact(aporte.getValor().doubleValue()));
        String desc = aporte.getDescricao();
        h.tvDescricao.setText(desc != null && !desc.isEmpty() ? desc : "—");
        h.btnExcluir.setOnClickListener(v -> onDeleteClick.onDeleteClick(aporte));
    }

    private String formatCurrencyCompact(double value) {
        Locale locale = Locale.getDefault();
        double abs = Math.abs(value);
        if (abs >= TRILLION) {
            return String.format(locale, "R$ %.2f tri", value / TRILLION);
        }
        if (abs >= BILLION) {
            return String.format(locale, "R$ %.2f bi", value / BILLION);
        }
        if (abs >= MILLION) {
            return String.format(locale, "R$ %.2f mi", value / MILLION);
        }
        if (abs >= THOUSAND) {
            return String.format(locale, "R$ %.2f mil", value / THOUSAND);
        }
        return String.format(locale, "R$ %.2f", value);
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvData;
        TextView tvValor;
        TextView tvDescricao;
        ImageButton btnExcluir;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvData = itemView.findViewById(R.id.tv_aporte_data);
            tvValor = itemView.findViewById(R.id.tv_aporte_valor);
            tvDescricao = itemView.findViewById(R.id.tv_aporte_descricao);
            btnExcluir = itemView.findViewById(R.id.btn_aporte_excluir);
        }
    }
}
