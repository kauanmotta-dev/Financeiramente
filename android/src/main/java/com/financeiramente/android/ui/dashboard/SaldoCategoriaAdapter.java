package com.financeiramente.android.ui.dashboard;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.financeiramente.android.R;
import com.financeiramente.core.usecase.SaldoCategoria;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SaldoCategoriaAdapter extends RecyclerView.Adapter<SaldoCategoriaAdapter.ViewHolder> {

    private List<SaldoCategoria> items = new ArrayList<>();

    public void setItems(List<SaldoCategoria> items) {
        this.items = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_saldo_categoria, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SaldoCategoria item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final View viewStatusIndicator;
        private final TextView tvNome;
        private final TextView tvSaldo;
        private final TextView tvGasto;
        private final TextView tvLimite;
        private final ProgressBar pbCategoria;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            viewStatusIndicator = itemView.findViewById(R.id.view_status_indicator);
            tvNome              = itemView.findViewById(R.id.tv_categoria_nome);
            tvSaldo             = itemView.findViewById(R.id.tv_saldo_categoria);
            tvGasto             = itemView.findViewById(R.id.tv_gasto_categoria);
            tvLimite            = itemView.findViewById(R.id.tv_limite_categoria);
            pbCategoria         = itemView.findViewById(R.id.pb_categoria);
        }

        void bind(SaldoCategoria item) {
            tvNome.setText(item.getCategoriaNome());
            tvSaldo.setText(String.format(Locale.getDefault(), "R$ %.2f", item.getSaldo()));
            tvGasto.setText(String.format(Locale.getDefault(), "Gasto: R$ %.2f", item.getGastoRealizado()));
            tvLimite.setText(String.format(Locale.getDefault(), "Limite: R$ %.2f", item.getLimite()));

            int progressoPercent = 0;
            if (item.getLimite() > 0) {
                progressoPercent = (int) Math.min(100.0, (item.getGastoRealizado() / item.getLimite()) * 100.0);
            }
            pbCategoria.setProgress(progressoPercent);

            int statusColor = resolverCor(item.getLimite(), item.getGastoRealizado());
            viewStatusIndicator.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(statusColor));
            tvSaldo.setTextColor(item.getSaldo() >= 0 ? statusColor : Color.RED);
        }

        /** Cor baseada na proporção gasto/limite (sem enum explícito). */
        private int resolverCor(double limite, double gasto) {
            if (limite <= 0) {
                return gasto > 0 ? Color.parseColor("#F44336") : Color.parseColor("#4CAF50");
            }
            double percentual = gasto / limite;
            if (percentual < 0.75)  return Color.parseColor("#4CAF50");  // verde
            if (percentual <= 1.00) return Color.parseColor("#FF9800");  // amarelo
            return Color.parseColor("#F44336");                           // vermelho
        }
    }
}
