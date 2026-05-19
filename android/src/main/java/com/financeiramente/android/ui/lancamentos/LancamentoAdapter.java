package com.financeiramente.android.ui.lancamentos;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.financeiramente.android.R;
import com.financeiramente.core.domain.entity.Lancamento;
import com.financeiramente.core.domain.vo.TipoLancamento;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LancamentoAdapter extends RecyclerView.Adapter<LancamentoAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onClick(Lancamento lancamento);
    }

    public interface OnItemLongClickListener {
        void onLongClick(Lancamento lancamento);
    }

    private List<Lancamento> items = new ArrayList<>();
    private OnItemClickListener clickListener;
    private OnItemLongClickListener longClickListener;

    public LancamentoAdapter(OnItemClickListener clickListener,
                              OnItemLongClickListener longClickListener) {
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    public void setItems(List<Lancamento> lancamentos) {
        this.items = lancamentos != null ? lancamentos : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lancamento, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Lancamento lancamento = items.get(position);
        holder.tvDescricao.setText(lancamento.getDescricao());
        holder.tvCategoriaData.setText(lancamento.getData());

        String valorFormatado = String.format(Locale.getDefault(), "R$ %.2f", lancamento.getValor());
        holder.tvValor.setText(valorFormatado);

        if (lancamento.getTipo() == TipoLancamento.RECEITA) {
            holder.tvValor.setTextColor(0xFF2E7D32); // verde
        } else {
            holder.tvValor.setTextColor(0xFFC62828); // vermelho
        }

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onClick(lancamento);
        });
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) longClickListener.onLongClick(lancamento);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDescricao;
        TextView tvCategoriaData;
        TextView tvValor;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDescricao = itemView.findViewById(R.id.tv_descricao);
            tvCategoriaData = itemView.findViewById(R.id.tv_categoria_data);
            tvValor = itemView.findViewById(R.id.tv_valor);
        }
    }
}
