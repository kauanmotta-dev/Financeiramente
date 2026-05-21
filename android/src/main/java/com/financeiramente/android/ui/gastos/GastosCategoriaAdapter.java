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
    private final Map<TipoCategoria, Boolean> sectionExpandedState = new HashMap<>();

    private final List<Item> items = new ArrayList<>();
    private List<GastosCategoriaViewModel.SecaoTipoCategoria> secoes = new ArrayList<>();

    private static class Item {
        final int type;
        final String tituloSecao;
        final TipoCategoria tipoSecao;
        final double valorSecao;
        final double valorTotalSecao;
        final GastosCategoriaViewModel.GastoCategoriaNode categoria;
        final int depth;

        private Item(String tituloSecao,
                     TipoCategoria tipoSecao,
                     double valorSecao,
                     double valorTotalSecao) {
            this.type = TYPE_SECTION;
            this.tituloSecao = tituloSecao;
            this.tipoSecao = tipoSecao;
            this.valorSecao = valorSecao;
            this.valorTotalSecao = valorTotalSecao;
            this.categoria = null;
            this.depth = 0;
        }

        private Item(GastosCategoriaViewModel.GastoCategoriaNode categoria, int depth) {
            this.type = TYPE_CATEGORIA;
            this.tituloSecao = null;
            this.tipoSecao = null;
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
                    getTituloSecao(secao.getTipo()),
                    secao.getTipo(),
                    secao.getValorUtilizadoSecao(),
                    secao.getValorTotalSecao()));
            if (isSectionExpanded(secao.getTipo())) {
                for (GastosCategoriaViewModel.GastoCategoriaNode categoria : secao.getCategorias()) {
                    appendCategoria(categoria, 0);
                }
            }
        }
        notifyDataSetChanged();
    }

    private boolean isSectionExpanded(TipoCategoria tipo) {
        return Boolean.TRUE.equals(sectionExpandedState.get(tipo));
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

    private String getTituloSecao(TipoCategoria tipo) {
        if (tipo == TipoCategoria.ESSENCIAL) {
            return context.getString(R.string.gastos_secao_essenciais);
        }
        if (tipo == TipoCategoria.NAO_ESSENCIAL) {
            return context.getString(R.string.gastos_secao_nao_essenciais);
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
        boolean expanded = isSectionExpanded(item.tipoSecao);
        holder.tvTitulo.setText(item.tituloSecao);

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

        holder.progressSecao.setProgress(progresso);
        holder.ivArrow.setRotation(expanded ? 180f : 0f);
        holder.itemView.setOnClickListener(v -> {
            sectionExpandedState.put(item.tipoSecao, !expanded);
            rebuildItems();
        });
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
        boolean isFilha = item.depth > 0;
        holder.tvInfo.setVisibility(View.GONE);
        if (isFilha) {
            if (categoria.getValorTotal() > 0) {
                holder.tvResumoValores.setText(String.format(
                        "%s / %s",
                        currencyFormat.format(categoria.getValorUtilizado()),
                        currencyFormat.format(categoria.getValorTotal())));
            } else {
                holder.tvResumoValores.setText(currencyFormat.format(categoria.getValorUtilizado()));
            }
        } else {
            if (categoria.getValorTotal() > 0) {
                holder.tvResumoValores.setText(String.format(
                        "%s / %s",
                        currencyFormat.format(categoria.getValorUtilizado()),
                        currencyFormat.format(categoria.getValorTotal())));
            } else {
                holder.tvResumoValores.setText(currencyFormat.format(categoria.getValorUtilizado()));
            }
        }

        int progresso;
        int progressoParaTom;
        if (categoria.getValorTotal() > 0) {
            progressoParaTom = (int) Math.round((categoria.getValorUtilizado() / categoria.getValorTotal()) * 100);
            progresso = Math.min(100, progressoParaTom);
        } else {
            progressoParaTom = categoria.getValorUtilizado() > 0 ? 100 : 0;
            progresso = progressoParaTom;
        }
        holder.progressUso.setProgress(progresso);
        aplicarTomVisualDeUso(holder, progressoParaTom);

        if (categoria.possuiFilhas()) {
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
                R.string.gastos_acessibilidade_categoria,
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
        holder.progressUso.setProgressTintList(ColorStateList.valueOf(tom));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private int dpToPx(int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }

    static class SectionViewHolder extends RecyclerView.ViewHolder {
        final TextView tvTitulo;
        final TextView tvResumo;
        final ProgressBar progressSecao;
        final ImageView ivArrow;

        SectionViewHolder(@NonNull View itemView) {
            super(itemView);
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
        final TextView tvResumoValores;
        final ProgressBar progressUso;
        final ImageView ivExpandArrow;

        CategoriaViewHolder(@NonNull View itemView) {
            super(itemView);
            flIconeContainer = itemView.findViewById(R.id.fl_icone_container);
            tvIcone = itemView.findViewById(R.id.tv_icone);
            tvNome = itemView.findViewById(R.id.tv_categoria_nome);
            tvInfo = itemView.findViewById(R.id.tv_categoria_info);
            tvResumoValores = itemView.findViewById(R.id.tv_resumo_valores);
            progressUso = itemView.findViewById(R.id.pb_uso_categoria);
            ivExpandArrow = itemView.findViewById(R.id.iv_expand_arrow);
        }
    }
}
