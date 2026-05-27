package com.financeiramente.android.ui.metas;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.financeiramente.android.R;
import com.financeiramente.core.domain.entity.Meta;
import com.google.android.material.button.MaterialButton;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MetaAdapter extends RecyclerView.Adapter<MetaAdapter.ViewHolder> {

    public interface OnMetaClickListener {
        void onClick(Meta meta);
    }

    public interface OnMetaDeleteClickListener {
        void onDeleteClick(Meta meta);
    }

    public interface OnAporteClickListener {
        void onAporteClick(Meta meta);
    }

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter MONTH_FMT =
            DateTimeFormatter.ofPattern("MMM/yyyy", new Locale("pt", "BR"));

    private List<Meta> items = new ArrayList<>();
    private final OnMetaClickListener onClick;
    private final OnMetaDeleteClickListener onDeleteClick;
    private OnAporteClickListener onAporteClick;

    public MetaAdapter(OnMetaClickListener onClick, OnMetaDeleteClickListener onDeleteClick) {
        this.onClick = onClick;
        this.onDeleteClick = onDeleteClick;
    }

    public void setOnAporteClickListener(OnAporteClickListener listener) {
        this.onAporteClick = listener;
    }

    public void setItems(List<Meta> lista) {
        this.items = lista != null ? lista : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_meta, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Meta meta = items.get(position);
        h.tvNome.setText(meta.getNome());

        double totalSalvo = meta.getValorAtual() + meta.getValorInicial();
        double percentual = meta.getValorObjetivo() > 0
                ? (totalSalvo / meta.getValorObjetivo()) * 100.0
                : 0.0;
        h.cpvProgresso.setProgress((float) Math.min(percentual, 100.0));

        h.tvValores.setText(String.format(Locale.getDefault(),
                "R$ %.2f / R$ %.2f", totalSalvo, meta.getValorObjetivo()));

        if (meta.getDescricao() != null && !meta.getDescricao().trim().isEmpty()) {
            h.tvDescricao.setText(meta.getDescricao().trim());
            h.tvDescricao.setVisibility(View.VISIBLE);
        } else {
            h.tvDescricao.setVisibility(View.GONE);
        }

        int statusColor;
        if (totalSalvo >= meta.getValorObjetivo()) {
            h.tvStatus.setText(R.string.meta_status_concluida);
            statusColor = ContextCompat.getColor(h.itemView.getContext(), R.color.verde_success);
        } else if (estaAtrasada(meta)) {
            h.tvStatus.setText(R.string.meta_status_em_risco);
            statusColor = ContextCompat.getColor(h.itemView.getContext(), R.color.vermelho_error);
        } else {
            h.tvStatus.setText(R.string.meta_status_em_progresso);
            statusColor = ContextCompat.getColor(h.itemView.getContext(), R.color.azul_primary);
        }
        h.tvStatus.getBackground().setTint(statusColor);

        // Data alvo
        if (meta.getDataAlvo() != null && !meta.getDataAlvo().isEmpty()) {
            try {
                LocalDate alvo = LocalDate.parse(meta.getDataAlvo());
                h.tvDataAlvo.setText(h.itemView.getContext()
                        .getString(R.string.metas_data_alvo, alvo.format(DATE_FMT)));
                h.tvDataAlvo.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                h.tvDataAlvo.setVisibility(View.GONE);
            }
        } else {
            h.tvDataAlvo.setVisibility(View.GONE);
        }

        // Projeção de conclusão
        String projecao = calcularProjecao(meta);
        if (projecao != null) {
            h.tvProjecao.setText(projecao);
            h.tvProjecao.setVisibility(View.VISIBLE);
        } else {
            h.tvProjecao.setVisibility(View.GONE);
        }

        h.itemView.setOnClickListener(v -> onClick.onClick(meta));
        h.btnExcluir.setOnClickListener(v -> onDeleteClick.onDeleteClick(meta));

        h.btnAporte.setOnClickListener(v -> {
            if (onAporteClick != null) onAporteClick.onAporteClick(meta);
        });
    }

    private boolean estaAtrasada(Meta meta) {
        if (meta.getDataAlvo() == null || meta.getDataAlvo().isEmpty()) return false;
        try {
            return LocalDate.parse(meta.getDataAlvo()).isBefore(LocalDate.now());
        } catch (Exception ignored) {
            return false;
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * Calculates a projected completion month based on the average monthly rate
     * since the meta was created (criadoEm timestamp → months elapsed).
     * Returns null when there is insufficient data (no progress, or already completed).
     */
    private String calcularProjecao(Meta meta) {
        double restante = meta.getValorEfetivo() - meta.getValorAtual();
        if (restante <= 0) {
            return null; // already met
        }
        if (meta.getValorAtual() <= 0 || meta.getCriadoEm() <= 0) {
            return null; // no data
        }

        LocalDate criacao = LocalDate.ofEpochDay(meta.getCriadoEm() / 86_400_000L);
        LocalDate hoje = LocalDate.now();
        long mesesDecorridos = ChronoUnit.MONTHS.between(criacao, hoje);
        if (mesesDecorridos <= 0) {
            return null;
        }

        double mediaMensal = meta.getValorAtual() / (double) mesesDecorridos;
        if (mediaMensal <= 0) {
            return null;
        }

        long mesesRestantes = (long) Math.ceil(restante / mediaMensal);
        LocalDate projecaoData = hoje.plusMonths(mesesRestantes);
        return meta.getValorAtual() > 0
                ? String.format(Locale.getDefault(), "📈 +R$ %.0f/mês · %s",
                        mediaMensal, projecaoData.format(MONTH_FMT))
                : null;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CircularProgressView cpvProgresso;
        TextView tvStatus;
        TextView tvNome;
        TextView tvValores;
        TextView tvDescricao;
        TextView tvDataAlvo;
        TextView tvProjecao;
        MaterialButton btnExcluir;
        MaterialButton btnAporte;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cpvProgresso = itemView.findViewById(R.id.cpv_meta_progresso);
            tvStatus = itemView.findViewById(R.id.tv_meta_status);
            tvNome = itemView.findViewById(R.id.tv_meta_nome);
            tvValores = itemView.findViewById(R.id.tv_meta_valores);
            tvDescricao = itemView.findViewById(R.id.tv_meta_descricao);
            tvDataAlvo = itemView.findViewById(R.id.tv_meta_data_alvo);
            tvProjecao = itemView.findViewById(R.id.tv_meta_projecao);
            btnExcluir = itemView.findViewById(R.id.btn_excluir_meta);
            btnAporte = itemView.findViewById(R.id.btn_registrar_aporte);
        }
    }
}

