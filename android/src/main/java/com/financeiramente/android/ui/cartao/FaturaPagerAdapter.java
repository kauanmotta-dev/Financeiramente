package com.financeiramente.android.ui.cartao;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.financeiramente.android.R;
import com.financeiramente.android.viewmodel.FaturaListViewModel;
import com.financeiramente.core.domain.entity.Fatura;
import com.financeiramente.core.domain.vo.StatusFatura;
import com.google.android.material.button.MaterialButton;

import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FaturaPagerAdapter extends RecyclerView.Adapter<FaturaPagerAdapter.ViewHolder> {

    public interface OnFaturaClickListener {
        void onFaturaClick(FaturaListViewModel.FaturaResumo fatura);
    }

    private List<FaturaListViewModel.FaturaResumo> faturas = new ArrayList<>();
    private final OnFaturaClickListener listener;

    public FaturaPagerAdapter(OnFaturaClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<FaturaListViewModel.FaturaResumo> list) {
        this.faturas = list != null ? new ArrayList<>(list) : new ArrayList<>();
        notifyDataSetChanged();
    }

    /** Retorna o índice da fatura cujo mês é o mês atual, ou -1 se não encontrar. */
    public int indexOfCurrentMonth() {
        YearMonth mesAtual = YearMonth.now();
        for (int i = 0; i < faturas.size(); i++) {
            try {
                if (YearMonth.parse(faturas.get(i).getFatura().getMes()).equals(mesAtual)) return i;
            } catch (Exception ignored) {}
        }
        return -1;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_fatura_page, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(faturas.get(position));
    }

    @Override
    public int getItemCount() {
        return faturas.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvMes;
        private final TextView tvVencimento;
        private final TextView tvTotal;
        private final TextView tvPago;
        private final TextView tvStatus;
        private final MaterialButton btnDetalhe;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMes = itemView.findViewById(R.id.tv_page_fatura_mes);
            tvVencimento = itemView.findViewById(R.id.tv_page_fatura_vencimento);
            tvTotal = itemView.findViewById(R.id.tv_page_fatura_total);
            tvPago = itemView.findViewById(R.id.tv_page_fatura_pago);
            tvStatus = itemView.findViewById(R.id.tv_page_fatura_status);
            btnDetalhe = itemView.findViewById(R.id.btn_page_fatura_detalhe);
        }

        void bind(FaturaListViewModel.FaturaResumo resumo) {
            Fatura fatura = resumo.getFatura();
            try {
                YearMonth ym = YearMonth.parse(fatura.getMes());
                String nomeMes = ym.getMonth().getDisplayName(TextStyle.FULL, new Locale("pt", "BR"));
                tvMes.setText(nomeMes.substring(0, 1).toUpperCase() + nomeMes.substring(1)
                        + " / " + ym.getYear());
            } catch (Exception e) {
                tvMes.setText(fatura.getMes());
            }

            if (fatura.getDataVencimento() != null && !fatura.getDataVencimento().isEmpty()) {
                tvVencimento.setText(itemView.getContext().getString(
                        R.string.fatura_vence_em, fatura.getDataVencimento()));
                tvVencimento.setVisibility(View.VISIBLE);
            } else {
                tvVencimento.setVisibility(View.GONE);
            }

            double valorTotal = resumo.getValorTotal();
            tvTotal.setText(String.format(new Locale("pt", "BR"), "R$ %.2f", valorTotal));

            double valorPago = fatura.getValorPago() != null ? fatura.getValorPago().doubleValue() : 0d;
            if (fatura.getStatus() == StatusFatura.PAGO_PARCIAL && valorPago > 0) {
                tvPago.setVisibility(View.VISIBLE);
                tvPago.setText(ctx().getString(
                        R.string.fatura_valor_pago_label,
                        String.format(new Locale("pt", "BR"), "R$ %.2f", valorPago)));
            } else {
                tvPago.setVisibility(View.GONE);
            }

            applyStatusBadge(itemView.getContext(), fatura.getStatus());

            btnDetalhe.setOnClickListener(v -> listener.onFaturaClick(resumo));
            itemView.setOnClickListener(v -> listener.onFaturaClick(resumo));
        }

        private void applyStatusBadge(Context ctx, StatusFatura status) {
            String label;
            int bgColor;
            int textColor = Color.WHITE;
            switch (status) {
                case ABERTO:
                    label = ctx.getString(R.string.fatura_status_aberto);
                    bgColor = ctx.getColor(R.color.azul_primary);
                    break;
                case FECHADO:
                    label = ctx.getString(R.string.fatura_status_fechado);
                    bgColor = ctx.getColor(R.color.laranja_warning);
                    break;
                case PAGO:
                    label = ctx.getString(R.string.fatura_status_pago);
                    bgColor = ctx.getColor(R.color.verde_success);
                    break;
                case PAGO_PARCIAL:
                    label = ctx.getString(R.string.fatura_status_pago_parcial);
                    bgColor = ctx.getColor(R.color.amarelo_warning);
                    textColor = ctx.getColor(R.color.cinza_on_surface);
                    break;
                default:
                    label = status.name();
                    bgColor = ctx.getColor(R.color.cinza_secondary);
            }
            tvStatus.setText(label);
            tvStatus.setTextColor(textColor);
            tvStatus.getBackground().setTint(bgColor);
        }

        private Context ctx() {
            return itemView.getContext();
        }
    }
}
