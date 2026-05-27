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
import com.financeiramente.core.domain.entity.Fatura;
import com.financeiramente.core.domain.vo.StatusFatura;

import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FaturaAdapter extends RecyclerView.Adapter<FaturaAdapter.ViewHolder> {

    public interface OnFaturaClickListener {
        void onFaturaClick(Fatura fatura);
    }

    private List<Fatura> faturas = new ArrayList<>();
    private final OnFaturaClickListener listener;

    public FaturaAdapter(OnFaturaClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<Fatura> list) {
        this.faturas = list != null ? new ArrayList<>(list) : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_fatura, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(faturas.get(position));
    }

    @Override
    public int getItemCount() { return faturas.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvMes;
        private final TextView tvVencimento;
        private final TextView tvTotal;
        private final TextView tvStatusBadge;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMes = itemView.findViewById(R.id.tv_fatura_mes);
            tvVencimento = itemView.findViewById(R.id.tv_fatura_vencimento);
            tvTotal = itemView.findViewById(R.id.tv_fatura_total);
            tvStatusBadge = itemView.findViewById(R.id.tv_fatura_status_badge);
        }

        void bind(Fatura fatura) {
            try {
                YearMonth ym = YearMonth.parse(fatura.getMes());
                String nomeMes = ym.getMonth().getDisplayName(TextStyle.FULL, new Locale("pt", "BR"));
                tvMes.setText(nomeMes.substring(0, 1).toUpperCase() + nomeMes.substring(1) + "/" + ym.getYear());
            } catch (Exception e) {
                tvMes.setText(fatura.getMes());
            }
            tvVencimento.setText(itemView.getContext().getString(
                    R.string.fatura_vence_em, fatura.getDataVencimento()));

            applyStatusBadge(itemView.getContext(), fatura.getStatus());
            itemView.setOnClickListener(v -> listener.onFaturaClick(fatura));
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
            tvStatusBadge.setText(label);
            tvStatusBadge.setTextColor(textColor);
            tvStatusBadge.getBackground().setTint(bgColor);
        }
    }
}
