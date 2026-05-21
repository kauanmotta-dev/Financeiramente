package com.financeiramente.android.ui.dashboard;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.financeiramente.android.R;
import com.financeiramente.core.usecase.SaldoCategoria;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SaldoCategoriaAdapter extends RecyclerView.Adapter<SaldoCategoriaAdapter.ViewHolder> {

    /** Paleta categÃ³rica cÃ­clica â€” atribuÃ­da por hash do nome da categoria. */
    private static final int[] CAT_COLORS = {
            0xFF6366F1, // Ã­ndigo
            0xFF0EA5E9, // azul cÃ©u
            0xFFF97316, // laranja
            0xFF10B981, // verde
            0xFF8B5CF6, // roxo
            0xFFEC4899, // rosa
            0xFFF59E0B, // Ã¢mbar
            0xFFEF4444, // vermelho
            0xFF64748B, // cinza
            0xFF14B8A6, // teal
    };

    private List<SaldoCategoria> items = new ArrayList<>();

    /** Tracks the last position that received an entry animation. */
    private int lastAnimatedPosition = -1;

    public void setItems(List<SaldoCategoria> items) {
        this.items = items != null ? items : new ArrayList<>();
        lastAnimatedPosition = -1;
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
        holder.bind(items.get(position));
        // Fade + translateY entry animation
        if (position > lastAnimatedPosition) {
            lastAnimatedPosition = position;
            holder.itemView.setAlpha(0f);
            float translateY = holder.itemView.getContext().getResources().getDisplayMetrics().density * 32;
            holder.itemView.setTranslationY(translateY);
            long delay = Math.min(position, 6) * 60L;
            holder.itemView.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(300)
                    .setStartDelay(delay)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        } else {
            holder.itemView.setAlpha(1f);
            holder.itemView.setTranslationY(0f);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final ImageView ivCategoryIcon;
        private final TextView tvNome;
        private final TextView tvSaldo;
        private final TextView tvGasto;
        private final TextView tvLimite;
        private final LinearProgressIndicator pbCategoria;
        private final TextView tvPercentual;
        private final ImageView ivAlerta;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCategoryIcon = itemView.findViewById(R.id.iv_category_icon);
            tvNome         = itemView.findViewById(R.id.tv_categoria_nome);
            tvSaldo        = itemView.findViewById(R.id.tv_saldo_categoria);
            tvGasto        = itemView.findViewById(R.id.tv_gasto_categoria);
            tvLimite       = itemView.findViewById(R.id.tv_limite_categoria);
            pbCategoria    = itemView.findViewById(R.id.pb_categoria);
            tvPercentual   = itemView.findViewById(R.id.tv_percentual);
            ivAlerta       = itemView.findViewById(R.id.iv_alerta);
        }

        void bind(SaldoCategoria item) {
            tvNome.setText(item.getCategoriaNome());
            tvSaldo.setText(String.format(Locale.getDefault(), "R$ %.2f", item.getSaldo()));
            tvGasto.setText(String.format(Locale.getDefault(), "R$ %.2f", item.getGastoRealizado()));
            tvLimite.setText(String.format(Locale.getDefault(), "R$ %.2f", item.getLimite()));

            // Cor categÃ³rica baseada no nome â€” consistente por categoria
            int catColorIndex = Math.abs(item.getCategoriaNome().hashCode()) % CAT_COLORS.length;
            int catColor = CAT_COLORS[catColorIndex];
            ViewCompat.setBackgroundTintList(ivCategoryIcon, ColorStateList.valueOf(catColor));

            // Progresso e cor de status
            int progressoPercent = 0;
            if (item.getLimite() > 0) {
                progressoPercent = (int) Math.min(100.0, (item.getGastoRealizado() / item.getLimite()) * 100.0);
            }
            pbCategoria.setProgress(progressoPercent);

            int statusColor = resolverCorStatus(item.getLimite(), item.getGastoRealizado());
            pbCategoria.setIndicatorColor(statusColor);
            tvSaldo.setTextColor(statusColor);

            // Badge de percentual
            if (item.getLimite() > 0) {
                tvPercentual.setText(String.format(Locale.getDefault(), "%d%%", progressoPercent));
                tvPercentual.setVisibility(View.VISIBLE);
            } else {
                tvPercentual.setVisibility(View.GONE);
            }

            // Ãcone de alerta quando >85%
            boolean emAlerta = item.getLimite() > 0
                    && (item.getGastoRealizado() / item.getLimite()) > 0.85;
            ivAlerta.setVisibility(emAlerta ? View.VISIBLE : View.GONE);
        }

        private int resolverCorStatus(double limite, double gasto) {
            if (limite <= 0) {
                return gasto > 0 ? 0xFFEF4444 : 0xFF10B981;
            }
            double pct = gasto / limite;
            if (pct < 0.60)  return 0xFF10B981; // verde â€” status_ok
            if (pct <= 0.85) return 0xFFF59E0B; // amarelo â€” status_warning
            return 0xFFEF4444;                   // vermelho â€” status_danger
        }
    }
}
