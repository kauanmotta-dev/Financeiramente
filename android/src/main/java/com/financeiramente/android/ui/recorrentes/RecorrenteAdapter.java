package com.financeiramente.android.ui.recorrentes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.financeiramente.android.R;
import com.financeiramente.core.domain.entity.LancamentoRecorrente;
import com.financeiramente.core.domain.vo.TipoLancamento;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RecorrenteAdapter extends RecyclerView.Adapter<RecorrenteAdapter.ViewHolder> {

    public interface OnItemClick {
        void onClick(LancamentoRecorrente recorrente);
    }

    private final OnItemClick onEdit;
    private final OnItemClick onDesativar;
    private final List<LancamentoRecorrente> items = new ArrayList<>();
    private final Map<String, String> categoriaLabelPorId = new LinkedHashMap<>();

    public RecorrenteAdapter(OnItemClick onEdit, OnItemClick onDesativar) {
        this.onEdit = onEdit;
        this.onDesativar = onDesativar;
    }

    public void setItems(List<LancamentoRecorrente> lista) {
        items.clear();
        if (lista != null) items.addAll(lista);
        notifyDataSetChanged();
    }

    public void setCategoryLabels(Map<String, String> labels) {
        categoriaLabelPorId.clear();
        if (labels != null) {
            categoriaLabelPorId.putAll(labels);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recorrente, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LancamentoRecorrente rec = items.get(position);

        holder.tvDescricao.setText(rec.getDescricao());

        String sinal = rec.getTipo() == TipoLancamento.RECEITA ? "+" : "-";
        holder.tvValor.setText(String.format(Locale.getDefault(), "%sR$ %.2f", sinal, rec.getValor()));
        holder.tvValor.setTextColor(holder.itemView.getContext().getColor(
                rec.getTipo() == TipoLancamento.RECEITA
                        ? android.R.color.holo_green_dark
                        : android.R.color.holo_red_dark));

        holder.tvRecorrencia.setText(rec.getRecorrencia().name().toLowerCase());
        String categoriaLabel = categoriaLabelPorId.get(rec.getCategoriaId());
        holder.tvCategoria.setText(categoriaLabel != null ? categoriaLabel : "Categoria");

        holder.itemView.setOnClickListener(v -> onEdit.onClick(rec));
        holder.itemView.setOnLongClickListener(v -> {
            onDesativar.onClick(rec);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvDescricao;
        final TextView tvValor;
        final TextView tvRecorrencia;
        final TextView tvCategoria;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDescricao   = itemView.findViewById(R.id.tv_recorrente_descricao);
            tvValor       = itemView.findViewById(R.id.tv_recorrente_valor);
            tvRecorrencia = itemView.findViewById(R.id.tv_recorrente_recorrencia);
            tvCategoria   = itemView.findViewById(R.id.tv_recorrente_categoria);
        }
    }
}
