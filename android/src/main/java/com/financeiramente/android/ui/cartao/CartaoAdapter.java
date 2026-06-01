package com.financeiramente.android.ui.cartao;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.financeiramente.android.R;
import com.financeiramente.android.viewmodel.CartaoListViewModel.CartaoComUtilizacao;
import com.financeiramente.core.domain.entity.CartaoCredito;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartaoAdapter extends RecyclerView.Adapter<CartaoAdapter.ViewHolder> {

    public interface OnCartaoClickListener {
        void onCartaoClick(CartaoComUtilizacao item);
        void onCartaoLongClick(CartaoComUtilizacao item);
    }

    private List<CartaoComUtilizacao> cartoes = new ArrayList<>();
    private final OnCartaoClickListener listener;

    public CartaoAdapter(OnCartaoClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<CartaoComUtilizacao> list) {
        this.cartoes = list != null ? new ArrayList<>(list) : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cartao, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartaoComUtilizacao item = cartoes.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() { return cartoes.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvIcone;
        private final TextView tvNome;
        private final TextView tvBandeira;
        private final TextView tvLimiteDisponivel;
        private final LinearProgressIndicator pbLimite;
        private final TextView tvUtilizado;
        private final TextView tvLimiteTotal;
        private final TextView tvVencimento;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIcone = itemView.findViewById(R.id.tv_cartao_icone);
            tvNome = itemView.findViewById(R.id.tv_cartao_nome);
            tvBandeira = itemView.findViewById(R.id.tv_cartao_bandeira);
            tvLimiteDisponivel = itemView.findViewById(R.id.tv_cartao_limite_disponivel);
            pbLimite = itemView.findViewById(R.id.pb_limite);
            tvUtilizado = itemView.findViewById(R.id.tv_cartao_utilizado);
            tvLimiteTotal = itemView.findViewById(R.id.tv_cartao_limite_total);
            tvVencimento = itemView.findViewById(R.id.tv_cartao_vencimento);
        }

        void bind(CartaoComUtilizacao item) {
            CartaoCredito cartao = item.getCartao();
            tvIcone.setText(cartao.getIcone());
            tvNome.setText(cartao.getNome());
            tvBandeira.setText(cartao.getBandeira() != null ? cartao.getBandeira().name() : "");
            tvVencimento.setText(String.format(Locale.getDefault(),
                    "Vencimento: dia %d", cartao.getDiaVencimento()));

            if (cartao.getLimite() != null && cartao.getLimite().doubleValue() > 0) {
                double limite = cartao.getLimite().doubleValue();
                double utilizado = item.getUtilizado();
                int pct = (int) Math.min(100.0, item.getPercentualUso());

                tvLimiteTotal.setText(String.format(Locale.getDefault(), "Limite: R$ %.2f", limite));
                tvUtilizado.setText(String.format(Locale.getDefault(), "Utilizado: R$ %.2f", utilizado));
                tvLimiteTotal.setVisibility(View.VISIBLE);
                tvUtilizado.setVisibility(View.VISIBLE);
                pbLimite.setVisibility(View.VISIBLE);

                // Cor dinâmica: verde < 50%, laranja 50-75%, vermelho > 75%
                int corIndicador;
                if (pct < 50) {
                    corIndicador = ContextCompat.getColor(itemView.getContext(), R.color.verde_success);
                } else if (pct <= 75) {
                    corIndicador = ContextCompat.getColor(itemView.getContext(), R.color.laranja_warning);
                } else {
                    corIndicador = ContextCompat.getColor(itemView.getContext(), R.color.vermelho_error);
                }
                pbLimite.setIndicatorColor(corIndicador);
                pbLimite.setTrackColor(ContextCompat.getColor(itemView.getContext(), R.color.cinza_divider));
                pbLimite.setProgressCompat(pct, false);

                double disponivel = limite - utilizado;
                tvLimiteDisponivel.setText(String.format(Locale.getDefault(), "R$ %.2f", disponivel));
                tvLimiteDisponivel.setTextColor(
                        pct >= 75
                        ? ContextCompat.getColor(itemView.getContext(), R.color.vermelho_error)
                        : ContextCompat.getColor(itemView.getContext(), R.color.verde_success));
                tvLimiteDisponivel.setVisibility(View.VISIBLE);
            } else {
                tvLimiteTotal.setVisibility(View.GONE);
                tvUtilizado.setVisibility(View.GONE);
                pbLimite.setVisibility(View.GONE);
                tvLimiteDisponivel.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> listener.onCartaoClick(item));
            itemView.setOnLongClickListener(v -> {
                listener.onCartaoLongClick(item);
                return true;
            });
        }
    }
}
