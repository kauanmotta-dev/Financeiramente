package com.financeiramente.android.ui.lancamentos;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.financeiramente.android.R;
import com.financeiramente.android.ui.common.CategoriaVisualFallback;
import com.financeiramente.core.domain.entity.Categoria;
import com.financeiramente.core.domain.entity.Lancamento;
import com.financeiramente.core.domain.entity.Tag;
import com.financeiramente.core.domain.vo.TipoLancamento;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LancamentoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int VIEW_TYPE_HEADER = 0;
    public static final int VIEW_TYPE_ITEM   = 1;

    // ─── Item model ──────────────────────────────────────────────────────────

    public static abstract class ListItem {
        public abstract int getType();
    }

    public static class DateHeaderItem extends ListItem {
        final String dataIso;
        final String dataFormatada;
        double dayBalance;

        DateHeaderItem(String dataIso, String dataFormatada, double dayBalance) {
            this.dataIso = dataIso;
            this.dataFormatada = dataFormatada;
            this.dayBalance = dayBalance;
        }

        @Override public int getType() { return VIEW_TYPE_HEADER; }
    }

    public static class TransactionItem extends ListItem {
        final Lancamento lancamento;
        final Categoria categoria;
        final List<Tag> tags;

        TransactionItem(Lancamento lancamento, Categoria categoria, List<Tag> tags) {
            this.lancamento = lancamento;
            this.categoria = categoria;
            this.tags = tags != null ? tags : Collections.emptyList();
        }

        @Override public int getType() { return VIEW_TYPE_ITEM; }
    }

    // ─── Listeners ───────────────────────────────────────────────────────────

    public interface OnItemClickListener { void onClick(Lancamento lancamento); }

    // ─── State ───────────────────────────────────────────────────────────────

    private List<Lancamento> allLancamentos = new ArrayList<>();
    private Map<String, Categoria> categoriaMap = new LinkedHashMap<>();
    private Map<String, List<Tag>> tagsMap = new LinkedHashMap<>();
    private String currentQuery = "";

    /** Flat list rendered by the adapter (headers + transactions) */
    private List<ListItem> displayList = new ArrayList<>();

    private OnItemClickListener clickListener;

    /** Tracks the last position that received an entry animation. */
    private int lastAnimatedPosition = -1;

    public LancamentoAdapter(OnItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    // ─── Public API ──────────────────────────────────────────────────────────

    public void setData(List<Lancamento> lancamentos,
                        Map<String, Categoria> cats,
                        Map<String, List<Tag>> tags) {
        this.allLancamentos = lancamentos != null ? lancamentos : new ArrayList<>();
        this.categoriaMap   = cats != null ? cats : new LinkedHashMap<>();
        this.tagsMap        = tags != null ? tags : new LinkedHashMap<>();
        lastAnimatedPosition = -1;
        applyFilter(currentQuery);
    }

    public void filter(String query) {
        currentQuery = query != null ? query.trim().toLowerCase(Locale.getDefault()) : "";
        applyFilter(currentQuery);
    }

    /**
     * Returns the Lancamento at the given adapter position, or null if it's a header.
     */
    public Lancamento getLancamentoAt(int position) {
        ListItem item = displayList.get(position);
        if (item instanceof TransactionItem) {
            return ((TransactionItem) item).lancamento;
        }
        return null;
    }

    /** Returns true if the item at this position is a date header (not swipeable). */
    public boolean isHeader(int position) {
        return displayList.get(position).getType() == VIEW_TYPE_HEADER;
    }

    // ─── Private helpers ─────────────────────────────────────────────────────

    private void applyFilter(String query) {
        List<Lancamento> filtered = new ArrayList<>();
        for (Lancamento l : allLancamentos) {
            if (query.isEmpty()) {
                filtered.add(l);
            } else {
                String desc = l.getDescricao() != null ? l.getDescricao().toLowerCase(Locale.getDefault()) : "";
                Categoria cat = categoriaMap.get(l.getCategoriaId());
                String catNome = cat != null && cat.getNome() != null
                        ? cat.getNome().toLowerCase(Locale.getDefault()) : "";
                String valor = String.format(Locale.getDefault(), "%.2f", l.getValor());
                if (desc.contains(query) || catNome.contains(query) || valor.contains(query)) {
                    filtered.add(l);
                }
            }
        }
        displayList = buildDisplayList(filtered);
        notifyDataSetChanged();
    }

    private List<ListItem> buildDisplayList(List<Lancamento> lancamentos) {
        // Sort by date descending
        List<Lancamento> sorted = new ArrayList<>(lancamentos);
        sorted.sort((a, b) -> compareDataDesc(a.getData(), b.getData()));

        // Group by date (LinkedHashMap preserves insertion order)
        LinkedHashMap<String, List<Lancamento>> byDate = new LinkedHashMap<>();
        for (Lancamento l : sorted) {
            String date = l.getData() != null ? l.getData() : "";
            byDate.computeIfAbsent(date, k -> new ArrayList<>()).add(l);
        }

        List<ListItem> result = new ArrayList<>();
        for (Map.Entry<String, List<Lancamento>> entry : byDate.entrySet()) {
            String dataIso = entry.getKey();
            List<Lancamento> group = entry.getValue();

            double balance = 0;
            for (Lancamento l : group) {
                balance += l.getTipo() == TipoLancamento.RECEITA ? l.getValor() : -l.getValor();
            }

            result.add(new DateHeaderItem(dataIso, formatDate(dataIso), balance));
            for (Lancamento l : group) {
                Categoria cat = categoriaMap.get(l.getCategoriaId());
                List<Tag> tags = tagsMap.getOrDefault(l.getId(), Collections.emptyList());
                result.add(new TransactionItem(l, cat, tags));
            }
        }
        return result;
    }

    private static int compareDataDesc(String a, String b) {
        if (a == null) a = "";
        if (b == null) b = "";
        return b.compareTo(a); // lexicographic descending works for ISO dates
    }

    private String formatDate(String isoDate) {
        try {
            LocalDate date = LocalDate.parse(isoDate, DateTimeFormatter.ISO_LOCAL_DATE);
            LocalDate today = LocalDate.now();
            if (date.equals(today)) return "HOJE";
            if (date.equals(today.minusDays(1))) return "ONTEM";
            String dayOfWeek = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, new Locale("pt", "BR"));
            String month = date.getMonth().getDisplayName(TextStyle.SHORT, new Locale("pt", "BR"));
            return (dayOfWeek + ", " + date.getDayOfMonth() + " " + month).toUpperCase(new Locale("pt", "BR"));
        } catch (Exception e) {
            return isoDate;
        }
    }

    // ─── RecyclerView.Adapter ────────────────────────────────────────────────

    @Override public int getItemViewType(int position) { return displayList.get(position).getType(); }
    @Override public int getItemCount() { return displayList.size(); }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_HEADER) {
            View v = inf.inflate(R.layout.item_date_header, parent, false);
            return new HeaderViewHolder(v);
        } else {
            View v = inf.inflate(R.layout.item_lancamento, parent, false);
            return new TransactionViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ListItem item = displayList.get(position);
        if (item instanceof DateHeaderItem) {
            bindHeader((HeaderViewHolder) holder, (DateHeaderItem) item);
        } else {
            bindTransaction((TransactionViewHolder) holder, (TransactionItem) item);
        }
        // Fade + translateY entry animation for newly appearing items
        if (position > lastAnimatedPosition) {
            lastAnimatedPosition = position;
            holder.itemView.setAlpha(0f);
            float translateY = holder.itemView.getContext().getResources().getDisplayMetrics().density * 32;
            holder.itemView.setTranslationY(translateY);
            long delay = Math.min(position, 8) * 50L;
            holder.itemView.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(280)
                    .setStartDelay(delay)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        } else {
            holder.itemView.setAlpha(1f);
            holder.itemView.setTranslationY(0f);
        }
    }

    // ─── Bind methods ────────────────────────────────────────────────────────

    private void bindHeader(HeaderViewHolder h, DateHeaderItem item) {
        h.tvDataHeader.setText(item.dataFormatada);

        Context ctx = h.itemView.getContext();
        String valorFormatado = formatValor(Math.abs(item.dayBalance));
        String saldoText = (item.dayBalance >= 0 ? "+" : "-") + "R$ " + valorFormatado;
        h.tvSaldoDia.setText(ctx.getString(R.string.saldo_dia, saldoText));

        int cor = item.dayBalance >= 0
                ? ContextCompat.getColor(ctx, R.color.verde_success)
                : ContextCompat.getColor(ctx, R.color.vermelho_error);
        h.tvSaldoDia.setTextColor(cor);
    }

    private void bindTransaction(TransactionViewHolder h, TransactionItem item) {
        Context ctx = h.itemView.getContext();
        Lancamento l = item.lancamento;
        Categoria cat = item.categoria;
        String nomePai = null;
        if (cat != null && cat.getPaiId() != null) {
            Categoria pai = categoriaMap.get(cat.getPaiId());
            nomePai = pai != null ? pai.getNome() : null;
        }

        // Ícone circular com cor da categoria
        String icone = CategoriaVisualFallback.icone(cat, nomePai);
        String hexCor = CategoriaVisualFallback.cor(cat, nomePai);
        h.tvIconeCategoria.setText(icone);
        applyCircleBackground(h.tvIconeCategoria, hexCor);

        // Descrição
        h.tvDescricao.setText(l.getDescricao());

        // Categoria • data
        String catNome = cat != null ? cat.getNome() : l.getCategoriaId();
        h.tvCategoriaData.setText(catNome + " • " + formatDisplayDate(l.getData()));

        // Tags como chips
        h.cgpTags.removeAllViews();
        if (!item.tags.isEmpty()) {
            h.cgpTags.setVisibility(View.VISIBLE);
            for (Tag tag : item.tags) {
                Chip chip = new Chip(ctx);
                chip.setText(tag.getEmoji() + " " + tag.getNome());
                chip.setTextSize(10f);
                float density = ctx.getResources().getDisplayMetrics().density;
                chip.setChipMinHeight(24f * density);
                chip.setClickable(false);
                chip.setCheckable(false);
                aplicarEstiloTagChip(chip, tag.getCor());
                h.cgpTags.addView(chip);
            }
        } else {
            h.cgpTags.setVisibility(View.GONE);
        }

        // Valor formatado em verde/vermelho
        double val = l.getValor();
        String sinal = l.getTipo() == TipoLancamento.RECEITA ? "+" : "-";
        h.tvValor.setText(sinal + "R$ " + formatValor(val));
        int corValor = l.getTipo() == TipoLancamento.RECEITA
                ? ContextCompat.getColor(ctx, R.color.verde_success)
                : ContextCompat.getColor(ctx, R.color.vermelho_error);
        h.tvValor.setTextColor(corValor);

        h.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onClick(l);
        });
    }

    private void applyCircleBackground(TextView tv, String hexColor) {
        try {
            int color = Color.parseColor(hexColor);
            // Lighten: blend 80% white
            int r = (Color.red(color) * 20 + 255 * 80) / 100;
            int g = (Color.green(color) * 20 + 255 * 80) / 100;
            int b = (Color.blue(color) * 20 + 255 * 80) / 100;
            int lightColor = Color.rgb(r, g, b);

            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.OVAL);
            bg.setColor(lightColor);
            tv.setBackground(bg);
            tv.setTextColor(color);
        } catch (Exception ignored) {
            // fallback: no background change
        }
    }

    private String formatValor(double valor) {
        return String.format(Locale.getDefault(), "%.2f", valor);
    }

    private String formatDisplayDate(String isoDate) {
        try {
            LocalDate date = LocalDate.parse(isoDate, DateTimeFormatter.ISO_LOCAL_DATE);
            return date.getDayOfMonth() + "/" + date.getMonthValue() + "/" + date.getYear();
        } catch (Exception e) {
            return isoDate != null ? isoDate : "";
        }
    }

    private void aplicarEstiloTagChip(Chip chip, String corHex) {
        try {
            int corSolida = Color.parseColor(corHex);
            int corFundo = Color.argb(36, Color.red(corSolida), Color.green(corSolida), Color.blue(corSolida));
            chip.setChipBackgroundColor(ColorStateList.valueOf(corFundo));
            chip.setChipStrokeColor(ColorStateList.valueOf(corSolida));
            chip.setChipStrokeWidth(1f * chip.getResources().getDisplayMetrics().density);
            chip.setTextColor(corSolida);
        } catch (Exception ignored) {
            // Keep default style when color is invalid.
        }
    }

    // ─── ViewHolders ─────────────────────────────────────────────────────────

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvDataHeader;
        TextView tvSaldoDia;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDataHeader = itemView.findViewById(R.id.tv_data_header);
            tvSaldoDia   = itemView.findViewById(R.id.tv_saldo_dia);
        }
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView tvIconeCategoria;
        TextView tvDescricao;
        TextView tvCategoriaData;
        ChipGroup cgpTags;
        TextView tvValor;

        TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIconeCategoria = itemView.findViewById(R.id.tv_icone_categoria);
            tvDescricao      = itemView.findViewById(R.id.tv_descricao);
            tvCategoriaData  = itemView.findViewById(R.id.tv_categoria_data);
            cgpTags          = itemView.findViewById(R.id.cgp_tags);
            tvValor          = itemView.findViewById(R.id.tv_valor);
        }
    }
}

