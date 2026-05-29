package com.financeiramente.android.ui.gastos;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.financeiramente.android.R;
import com.google.android.material.card.MaterialCardView;
import com.financeiramente.android.viewmodel.GastosCategoriaViewModel;
import com.financeiramente.core.domain.vo.TipoCategoria;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class GastosCategoriaAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_SECTION = 0;
    private static final int TYPE_CATEGORIA = 1;

    private final Context context;
    private final NumberFormat currencyFormat;
    private final Map<String, Boolean> expandedState = new HashMap<>();
    private final Map<String, Boolean> sectionExpandedState = new HashMap<>();

    private final List<Item> items = new ArrayList<>();
    private List<GastosCategoriaViewModel.SecaoTipoCategoria> secoes = new ArrayList<>();

    private static class Item {
        final int type;
        final String tituloSecao;
        final String secaoId;
        final TipoCategoria tipoSecao;
        final boolean investimentos;
        final double valorSecao;
        final double valorTotalSecao;
        final GastosCategoriaViewModel.GastoCategoriaNode categoria;
        final int depth;

        private Item(String tituloSecao,
                     String secaoId,
                     TipoCategoria tipoSecao,
                     boolean investimentos,
                     double valorSecao,
                     double valorTotalSecao) {
            this.type = TYPE_SECTION;
            this.tituloSecao = tituloSecao;
            this.secaoId = secaoId;
            this.tipoSecao = tipoSecao;
            this.investimentos = investimentos;
            this.valorSecao = valorSecao;
            this.valorTotalSecao = valorTotalSecao;
            this.categoria = null;
            this.depth = 0;
        }

        private Item(GastosCategoriaViewModel.GastoCategoriaNode categoria, int depth) {
            this.type = TYPE_CATEGORIA;
            this.tituloSecao = null;
            this.secaoId = null;
            this.tipoSecao = null;
            this.investimentos = false;
            this.valorSecao = 0d;
            this.valorTotalSecao = 0d;
            this.categoria = categoria;
            this.depth = depth;
        }
    }

    public GastosCategoriaAdapter(Context context) {
        this.context = context;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    }

    public void submitData(List<GastosCategoriaViewModel.SecaoTipoCategoria> secoes) {
        this.secoes = secoes != null ? secoes : new ArrayList<>();
        rebuildItems();
    }

    private void rebuildItems() {
        items.clear();
        for (GastosCategoriaViewModel.SecaoTipoCategoria secao : secoes) {
            if (secao.getCategorias().isEmpty()) {
                continue;
            }
            items.add(new Item(
                    getTituloSecao(secao),
                    secao.getId(),
                    secao.getTipo(),
                    secao.isInvestimentos(),
                    secao.getValorUtilizadoSecao(),
                    secao.getValorTotalSecao()));
            if (isSectionExpanded(secao.getId())) {
                for (GastosCategoriaViewModel.GastoCategoriaNode categoria : secao.getCategorias()) {
                    appendCategoria(categoria, 0);
                }
            }
        }
        notifyDataSetChanged();
    }

    private boolean isSectionExpanded(String secaoId) {
        return Boolean.TRUE.equals(sectionExpandedState.get(secaoId));
    }

    private void appendCategoria(GastosCategoriaViewModel.GastoCategoriaNode categoria, int depth) {
        items.add(new Item(categoria, depth));
        if (!categoria.possuiFilhas() || !Boolean.TRUE.equals(expandedState.get(categoria.getId()))) {
            return;
        }
        for (GastosCategoriaViewModel.GastoCategoriaNode filha : categoria.getFilhas()) {
            appendCategoria(filha, depth + 1);
        }
    }

    private String getTituloSecao(GastosCategoriaViewModel.SecaoTipoCategoria secao) {
        if (secao.getTitulo() != null && !secao.getTitulo().trim().isEmpty()) {
            return secao.getTitulo();
        }
        TipoCategoria tipo = secao.getTipo();
        if (tipo == TipoCategoria.ESSENCIAL) {
            return context.getString(R.string.gastos_secao_essenciais);
        }
        if (tipo == TipoCategoria.NAO_ESSENCIAL) {
            return context.getString(R.string.gastos_secao_nao_essenciais);
        }
        if (tipo == TipoCategoria.SEM_TIPO) {
            return context.getString(R.string.gastos_secao_sem_categoria);
        }
        return context.getString(R.string.gastos_secao_receitas);
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == TYPE_SECTION) {
            View view = inflater.inflate(R.layout.item_gasto_secao, parent, false);
            return new SectionViewHolder(view);
        }

        View view = inflater.inflate(R.layout.item_gasto_categoria, parent, false);
        return new CategoriaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Item item = items.get(position);
        if (item.type == TYPE_SECTION) {
            bindSection((SectionViewHolder) holder, item);
            return;
        }

        bindCategoria((CategoriaViewHolder) holder, item);
    }

    private void bindSection(SectionViewHolder holder, Item item) {
        boolean expanded = isSectionExpanded(item.secaoId);
        holder.tvTitulo.setText(item.tituloSecao);

        if (item.investimentos) {
            holder.tvResumo.setText(context.getString(
                R.string.gastos_secao_resumo_investimentos,
                currencyFormat.format(item.valorSecao)));
            holder.progressSecao.setVisibility(View.GONE);
            aplicarEstiloSecao(holder, true);
        } else {
            int progresso;
            if (item.valorTotalSecao > 0d) {
            progresso = (int) Math.min(100,
                Math.round((item.valorSecao / item.valorTotalSecao) * 100));
            holder.tvResumo.setText(context.getString(
                R.string.gastos_secao_resumo,
                currencyFormat.format(item.valorSecao),
                currencyFormat.format(item.valorTotalSecao),
                progresso));
            } else {
            progresso = item.valorSecao > 0d ? 100 : 0;
            holder.tvResumo.setText(context.getString(
                R.string.gastos_secao_resumo_sem_limite,
                currencyFormat.format(item.valorSecao)));
            }

            holder.progressSecao.setVisibility(View.VISIBLE);
            holder.progressSecao.setProgress(progresso);
            aplicarEstiloSecao(holder, false);
        }
        holder.ivArrow.setRotation(expanded ? 180f : 0f);
        holder.itemView.setOnClickListener(v -> {
            sectionExpandedState.put(item.secaoId, !expanded);
            rebuildItems();
        });
    }

        private void aplicarEstiloSecao(SectionViewHolder holder, boolean investimentos) {
        MaterialCardView cardView = (MaterialCardView) holder.itemView;
        int strokeColor = ContextCompat.getColor(
            context,
            investimentos ? R.color.investimento_destaque : R.color.azul_primary);
        int backgroundColor = ContextCompat.getColor(
            context,
            investimentos ? R.color.investimento_container : R.color.azul_primary_container);
        int textColor = ContextCompat.getColor(
            context,
            investimentos ? R.color.investimento_destaque : R.color.azul_primary_dark);
        cardView.setStrokeColor(strokeColor);
        holder.container.setBackgroundColor(backgroundColor);
        holder.tvTitulo.setTextColor(textColor);
        holder.tvResumo.setTextColor(textColor);
        holder.ivArrow.setColorFilter(textColor);
        }

    private void bindCategoria(CategoriaViewHolder holder, Item item) {
        GastosCategoriaViewModel.GastoCategoriaNode categoria = item.categoria;

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams();
        int marginStart = dpToPx(12 + (item.depth * 16));
        params.setMarginStart(marginStart);
        params.setMarginEnd(dpToPx(12));
        params.topMargin = dpToPx(6);
        holder.itemView.setLayoutParams(params);

        holder.tvIcone.setText(categoria.getIcone());
        try {
            int color = Color.parseColor(categoria.getCor());
            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.OVAL);
            bg.setColor(color);
            holder.flIconeContainer.setBackground(bg);
        } catch (IllegalArgumentException ignored) {
            holder.flIconeContainer.setBackgroundResource(R.drawable.bg_category_icon);
        }

        holder.tvNome.setText(categoria.getNome());
        if (categoria.isInvestimento()) {
            holder.tvResumoValores.setText(context.getString(
                    R.string.gastos_resumo_meta_mes,
                    currencyFormat.format(categoria.getValorUtilizado())));
            holder.tvInfo.setVisibility(View.VISIBLE);
            if (categoria.getValorTotal() > 0d) {
                holder.tvInfo.setText(context.getString(
                        R.string.gastos_resumo_meta_total,
                        currencyFormat.format(categoria.getValorAcumulado()),
                        currencyFormat.format(categoria.getValorTotal())));
                holder.progressUso.setVisibility(View.VISIBLE);
                int progressoMeta = (int) Math.min(100,
                        Math.round((categoria.getValorAcumulado() / categoria.getValorTotal()) * 100));
                int corInvestimento = ContextCompat.getColor(context, R.color.investimento_destaque);
                int corTrilho = ContextCompat.getColor(context, R.color.cinza_divider);
                holder.progressUso.setProgressTintList(ColorStateList.valueOf(corInvestimento));
                holder.progressUso.setSecondaryProgressTintList(ColorStateList.valueOf(corInvestimento));
                holder.progressUso.setProgressBackgroundTintList(ColorStateList.valueOf(corTrilho));
                holder.progressUso.setSecondaryProgress(0);
                holder.progressUso.setProgress(progressoMeta);
            } else {
                holder.tvInfo.setText(context.getString(
                        R.string.gastos_resumo_meta_total_sem_objetivo,
                        currencyFormat.format(categoria.getValorAcumulado())));
                holder.progressUso.setVisibility(View.GONE);
            }
            holder.layoutLegenda.setVisibility(View.GONE);
            holder.tvResumoValores.setTextColor(ContextCompat.getColor(context, R.color.investimento_destaque));
        } else {
            holder.layoutLegenda.setVisibility(View.VISIBLE);
            holder.progressUso.setVisibility(View.VISIBLE);
            if (categoria.getValorTotal() > 0) {
                holder.tvResumoValores.setText(context.getString(
                        R.string.gastos_resumo_valores,
                        currencyFormat.format(categoria.getValorUtilizado()),
                        currencyFormat.format(categoria.getValorTotal())));
            } else {
                holder.tvResumoValores.setText(currencyFormat.format(categoria.getValorUtilizado()));
            }

            holder.tvInfo.setVisibility(View.VISIBLE);
            holder.tvInfo.setText(context.getString(
                    R.string.gastos_resumo_credito_debito,
                    currencyFormat.format(categoria.getValorCreditoUtilizado()),
                    currencyFormat.format(categoria.getValorNaoCreditoUtilizado())));

            int progressoTotal;
            int progressoCredito;
            int progressoParaTom;
            double valorUtilizado = categoria.getValorUtilizado();
            double valorCredito = Math.max(0d, categoria.getValorCreditoUtilizado());
            if (categoria.getValorTotal() > 0) {
                progressoParaTom = (int) Math.round((categoria.getValorUtilizado() / categoria.getValorTotal()) * 100);
                progressoTotal = Math.min(100, progressoParaTom);
                progressoCredito = (int) Math.round((valorCredito / categoria.getValorTotal()) * 100);
                progressoCredito = Math.min(progressoTotal, Math.max(0, progressoCredito));
            } else {
                progressoParaTom = categoria.getValorUtilizado() > 0 ? 100 : 0;
                progressoTotal = progressoParaTom;
                if (valorUtilizado > 0d) {
                    progressoCredito = (int) Math.round((valorCredito / valorUtilizado) * 100);
                } else {
                    progressoCredito = 0;
                }
                progressoCredito = Math.min(progressoTotal, Math.max(0, progressoCredito));
            }

            int corCredito = ContextCompat.getColor(context, R.color.relatorio_credito);
            int corNaoCredito = ContextCompat.getColor(context, R.color.relatorio_debito);
            int corTrilho = ContextCompat.getColor(context, R.color.cinza_divider);
            holder.progressUso.setProgressTintList(ColorStateList.valueOf(corCredito));
            holder.progressUso.setSecondaryProgressTintList(ColorStateList.valueOf(corNaoCredito));
            holder.progressUso.setProgressBackgroundTintList(ColorStateList.valueOf(corTrilho));
            holder.progressUso.setSecondaryProgress(progressoTotal);
            holder.progressUso.setProgress(progressoCredito);
            bindLegend(holder);
            aplicarTomVisualDeUso(holder, progressoParaTom);
        }

        if (!categoria.isInvestimento() && categoria.possuiFilhas()) {
            holder.ivExpandArrow.setVisibility(View.VISIBLE);
            holder.ivExpandArrow.setRotation(Boolean.TRUE.equals(expandedState.get(categoria.getId())) ? 180f : 0f);
            holder.itemView.setOnClickListener(v -> {
                boolean expandido = Boolean.TRUE.equals(expandedState.get(categoria.getId()));
                expandedState.put(categoria.getId(), !expandido);
                rebuildItems();
            });
        } else {
            holder.ivExpandArrow.setVisibility(View.INVISIBLE);
            holder.itemView.setOnClickListener(null);
        }

        String descricaoAcessivel = context.getString(
        categoria.isInvestimento()
            ? R.string.gastos_acessibilidade_meta
            : R.string.gastos_acessibilidade_categoria,
                categoria.getNome(),
                currencyFormat.format(categoria.getValorUtilizado()),
                currencyFormat.format(categoria.getValorTotal()));
        holder.itemView.setContentDescription(descricaoAcessivel);
    }

    private void aplicarTomVisualDeUso(CategoriaViewHolder holder, int progresso) {
        int tom;
        if (progresso > 100) {
            tom = ContextCompat.getColor(context, R.color.vermelho_error);
        } else if (progresso >= 80) {
            tom = ContextCompat.getColor(context, android.R.color.holo_orange_dark);
        } else {
            tom = ContextCompat.getColor(context, R.color.verde_success);
        }
        holder.tvResumoValores.setTextColor(tom);
    }

    private void bindLegend(CategoriaViewHolder holder) {
        holder.tvCreditoDot.setTextColor(ContextCompat.getColor(context, R.color.relatorio_credito));
        holder.tvCreditoLabel.setTextColor(ContextCompat.getColor(context, R.color.cinza_secondary));
        holder.tvDebitoDot.setTextColor(ContextCompat.getColor(context, R.color.relatorio_debito));
        holder.tvDebitoLabel.setTextColor(ContextCompat.getColor(context, R.color.cinza_secondary));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private int dpToPx(int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }

    static class SectionViewHolder extends RecyclerView.ViewHolder {
        final View container;
        final TextView tvTitulo;
        final TextView tvResumo;
        final ProgressBar progressSecao;
        final ImageView ivArrow;

        SectionViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.layout_gasto_secao_container);
            tvTitulo = itemView.findViewById(R.id.tv_gasto_secao_titulo);
            tvResumo = itemView.findViewById(R.id.tv_gasto_secao_resumo);
            progressSecao = itemView.findViewById(R.id.pb_gasto_secao);
            ivArrow = itemView.findViewById(R.id.iv_gasto_secao_arrow);
        }
    }

    static class CategoriaViewHolder extends RecyclerView.ViewHolder {
        final FrameLayout flIconeContainer;
        final TextView tvIcone;
        final TextView tvNome;
        final TextView tvInfo;
        final View layoutLegenda;
        final TextView tvCreditoDot;
        final TextView tvCreditoLabel;
        final TextView tvDebitoDot;
        final TextView tvDebitoLabel;
        final TextView tvResumoValores;
        final ProgressBar progressUso;
        final ImageView ivExpandArrow;

        CategoriaViewHolder(@NonNull View itemView) {
            super(itemView);
            flIconeContainer = itemView.findViewById(R.id.fl_icone_container);
            tvIcone = itemView.findViewById(R.id.tv_icone);
            tvNome = itemView.findViewById(R.id.tv_categoria_nome);
            tvInfo = itemView.findViewById(R.id.tv_categoria_info);
            layoutLegenda = itemView.findViewById(R.id.layout_categoria_legenda);
            tvCreditoDot = itemView.findViewById(R.id.tv_credito_dot);
            tvCreditoLabel = itemView.findViewById(R.id.tv_credito_label);
            tvDebitoDot = itemView.findViewById(R.id.tv_debito_dot);
            tvDebitoLabel = itemView.findViewById(R.id.tv_debito_label);
            tvResumoValores = itemView.findViewById(R.id.tv_resumo_valores);
            progressUso = itemView.findViewById(R.id.pb_uso_categoria);
            ivExpandArrow = itemView.findViewById(R.id.iv_expand_arrow);
        }
    }
}
