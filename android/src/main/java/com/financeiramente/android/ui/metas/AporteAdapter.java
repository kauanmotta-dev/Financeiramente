package com.financeiramente.android.ui.metas;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.financeiramente.android.R;
import com.financeiramente.core.domain.entity.AporteMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AporteAdapter extends RecyclerView.Adapter<AporteAdapter.ViewHolder> {

    public interface OnAporteLongClickListener {
        boolean onLongClick(AporteMeta aporte);
    }

    private List<AporteMeta> items = new ArrayList<>();
    private final OnAporteLongClickListener onLongClick;

    public AporteAdapter(OnAporteLongClickListener onLongClick) {
        this.onLongClick = onLongClick;
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
        h.tvValor.setText(String.format(Locale.getDefault(), "R$ %.2f", aporte.getValor()));
        String desc = aporte.getDescricao();
        h.tvDescricao.setText(desc != null && !desc.isEmpty() ? desc : "—");
        h.itemView.setOnLongClickListener(v -> onLongClick.onLongClick(aporte));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvData;
        TextView tvValor;
        TextView tvDescricao;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvData = itemView.findViewById(R.id.tv_aporte_data);
            tvValor = itemView.findViewById(R.id.tv_aporte_valor);
            tvDescricao = itemView.findViewById(R.id.tv_aporte_descricao);
        }
    }
}
