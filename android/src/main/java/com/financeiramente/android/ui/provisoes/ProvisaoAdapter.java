package com.financeiramente.android.ui.provisoes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.financeiramente.android.R;
import com.financeiramente.core.domain.entity.Provisao;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProvisaoAdapter extends RecyclerView.Adapter<ProvisaoAdapter.ViewHolder> {

    public interface OnEditListener {
        void onEdit(Provisao provisao);
    }

    public interface OnDesativarListener {
        void onDesativar(Provisao provisao);
    }

    private List<Provisao> items = new ArrayList<>();
    private final OnEditListener editListener;
    private final OnDesativarListener desativarListener;

    public ProvisaoAdapter(OnEditListener editListener, OnDesativarListener desativarListener) {
        this.editListener = editListener;
        this.desativarListener = desativarListener;
    }

    public void setItems(List<Provisao> items) {
        this.items = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_provisao, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Provisao p = items.get(position);
        holder.tvNome.setText(p.getNome());
        holder.tvSaldo.setText(String.format(Locale.getDefault(),
                "Saldo: R$ %.2f", p.getSaldoAcumulado()));
        holder.tvValorMensal.setText(String.format(Locale.getDefault(),
                "R$ %.2f/mês  |  Total anual: R$ %.2f", p.getValorMensal(), p.getTotalAnual()));
        holder.itemView.setOnClickListener(v -> editListener.onEdit(p));
        holder.itemView.setOnLongClickListener(v -> {
            desativarListener.onDesativar(p);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNome;
        TextView tvSaldo;
        TextView tvValorMensal;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNome = itemView.findViewById(R.id.tv_provisao_nome);
            tvSaldo = itemView.findViewById(R.id.tv_provisao_saldo);
            tvValorMensal = itemView.findViewById(R.id.tv_provisao_valor_mensal);
        }
    }
}
