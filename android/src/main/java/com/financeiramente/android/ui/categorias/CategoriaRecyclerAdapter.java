package com.financeiramente.android.ui.categorias;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.financeiramente.android.R;
import com.financeiramente.android.ui.common.CategoriaVisualFallback;
import com.financeiramente.android.viewmodel.CategoriasViewModel;
import com.financeiramente.core.domain.entity.Categoria;
import com.financeiramente.core.domain.vo.TipoCategoria;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Expandable RecyclerView adapter for categories.
 * Uses two view types: TYPE_HEADER (root) and TYPE_CHILD (subcategory).
 */
public class CategoriaRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnItemActionListener {
        void onHeaderClick(Categoria categoria);
        void onChildClick(Categoria categoria);
        void onHeaderLongClick(Categoria categoria);
        void onChildLongClick(Categoria categoria);
    }

    static final int TYPE_HEADER = 0;
    static final int TYPE_CHILD  = 1;
    static final int TYPE_SECTION = 2;
    private static final int SECTION_ESSENCIAIS = 0;
    private static final int SECTION_NAO_ESSENCIAIS = 1;

    private static class Item {
        final int type;
        final Categoria categoria;
        final String sectionTitle;
        final int sectionType;
        boolean expanded;
        int childCount;

        Item(int type, Categoria categoria, boolean expanded, int childCount) {
            this.type = type;
            this.categoria = categoria;
            this.sectionTitle = null;
            this.sectionType = -1;
            this.expanded = expanded;
            this.childCount = childCount;
        }

        Item(String sectionTitle, int sectionType, boolean expanded) {
            this.type = TYPE_SECTION;
            this.categoria = null;
            this.sectionTitle = sectionTitle;
            this.sectionType = sectionType;
            this.expanded = expanded;
            this.childCount = 0;
        }
    }

    private final Context context;
    private final OnItemActionListener listener;
    private List<Item> items = new ArrayList<>();
    private List<Categoria> rootCategories = new ArrayList<>();
    private CategoriasViewModel viewModel;
    private boolean agruparDespesasPorTipo;
    private boolean secaoEssenciaisExpandida = true;
    private boolean secaoNaoEssenciaisExpandida = true;
    private final Map<String, Boolean> expandedStateById = new LinkedHashMap<>();

    public CategoriaRecyclerAdapter(Context context, OnItemActionListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void submitData(List<Categoria> roots, CategoriasViewModel vm, boolean agruparDespesasPorTipo) {
        this.rootCategories = roots != null ? roots : new ArrayList<>();
        this.viewModel = vm;
        this.agruparDespesasPorTipo = agruparDespesasPorTipo;
        if (!agruparDespesasPorTipo) {
            secaoEssenciaisExpandida = true;
            secaoNaoEssenciaisExpandida = true;
        }
        rebuildItems();
    }

    public void expandRootCategoria(String rootId) {
        if (rootId == null || rootId.isEmpty()) {
            return;
        }
        for (Categoria root : rootCategories) {
            if (rootId.equals(root.getId())) {
                expandedStateById.put(rootId, true);
                if (agruparDespesasPorTipo) {
                    if (root.getTipo() == TipoCategoria.NAO_ESSENCIAL) {
                        secaoNaoEssenciaisExpandida = true;
                    } else {
                        secaoEssenciaisExpandida = true;
                    }
                }
                rebuildItems();
                return;
            }
        }
    }

    private void rebuildItems() {
        for (Item existing : items) {
            if (existing.type == TYPE_HEADER && existing.categoria != null) {
                expandedStateById.put(existing.categoria.getId(), existing.expanded);
            }
        }

        List<Item> newItems = new ArrayList<>();
        List<Categoria> essenciais = new ArrayList<>();
        List<Categoria> naoEssenciais = new ArrayList<>();

        for (Categoria root : rootCategories) {
            if (agruparDespesasPorTipo && root.getTipo() == TipoCategoria.NAO_ESSENCIAL) {
                naoEssenciais.add(root);
            } else {
                essenciais.add(root);
            }
        }

        if (agruparDespesasPorTipo) {
            if (!essenciais.isEmpty()) {
                newItems.add(new Item(
                        context.getString(R.string.categorias_secao_essenciais),
                        SECTION_ESSENCIAIS,
                        secaoEssenciaisExpandida
                ));
                if (secaoEssenciaisExpandida) {
                    appendHeadersAndExpandedChildren(newItems, essenciais, expandedStateById);
                }
            }
            if (!naoEssenciais.isEmpty()) {
                newItems.add(new Item(
                        context.getString(R.string.categorias_secao_nao_essenciais),
                        SECTION_NAO_ESSENCIAIS,
                        secaoNaoEssenciaisExpandida
                ));
                if (secaoNaoEssenciaisExpandida) {
                    appendHeadersAndExpandedChildren(newItems, naoEssenciais, expandedStateById);
                }
            }
        } else {
            appendHeadersAndExpandedChildren(newItems, rootCategories, expandedStateById);
        }

        items = newItems;
        notifyDataSetChanged();
    }

    private void appendHeadersAndExpandedChildren(List<Item> target,
                                                  List<Categoria> roots,
                                                  Map<String, Boolean> expandedStateById) {
        for (Categoria root : roots) {
            List<Categoria> children = viewModel != null
                    ? viewModel.listarFilhas(root.getId())
                    : new ArrayList<>();

            boolean wasExpanded = Boolean.TRUE.equals(expandedStateById.get(root.getId()));

            Item header = new Item(TYPE_HEADER, root, wasExpanded, children.size());
            target.add(header);

            if (wasExpanded) {
                for (Categoria child : children) {
                    target.add(new Item(TYPE_CHILD, child, false, 0));
                }
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == TYPE_HEADER) {
            View view = inflater.inflate(R.layout.item_categoria_raiz, parent, false);
            return new HeaderViewHolder(view);
        } else if (viewType == TYPE_SECTION) {
            View view = inflater.inflate(R.layout.item_categoria_secao, parent, false);
            return new SectionViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_categoria_filha, parent, false);
            return new ChildViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Item item = items.get(position);
        if (item.type == TYPE_HEADER) {
            bindHeader((HeaderViewHolder) holder, item);
        } else if (item.type == TYPE_SECTION) {
            bindSection((SectionViewHolder) holder, item);
        } else {
            bindChild((ChildViewHolder) holder, item);
        }
    }

    private void bindSection(SectionViewHolder holder, Item item) {
        holder.tvTitulo.setText(item.sectionTitle);
        holder.ivArrow.setRotation(item.expanded ? 180f : 0f);
        holder.itemView.setOnClickListener(v -> toggleSection(item.sectionType));
    }

    private void bindHeader(HeaderViewHolder holder, Item item) {
        Categoria cat = item.categoria;

        // Icon + color
        String icone = CategoriaVisualFallback.icone(cat, null);
        String cor = CategoriaVisualFallback.cor(cat, null);
        holder.tvIcone.setText(icone);
        try {
            int color = Color.parseColor(cor);
            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.OVAL);
            bg.setColor(color);
            bg.setSize(dpToPx(44), dpToPx(44));
            holder.flIconeContainer.setBackground(bg);
        } catch (IllegalArgumentException ignored) {
            holder.flIconeContainer.setBackgroundResource(R.drawable.bg_category_icon);
        }

        holder.tvNome.setText(cat.getNome());

        if (cat.getLimiteMensal() != null) {
            holder.tvInfo.setText(String.format("R$ %.2f/mês", cat.getLimiteMensal()));
            holder.tvInfo.setVisibility(View.VISIBLE);
        } else {
            holder.tvInfo.setVisibility(View.GONE);
        }

        // Expand arrow rotation
        float rotation = item.expanded ? 180f : 0f;
        holder.ivArrow.setRotation(rotation);

        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            toggleExpand(pos);
            listener.onHeaderClick(items.get(pos).categoria);
        });

        holder.itemView.setOnLongClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return false;
            listener.onHeaderLongClick(items.get(pos).categoria);
            return true;
        });

        holder.btnEditar.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            listener.onHeaderLongClick(items.get(pos).categoria);
        });
    }

    private void bindChild(ChildViewHolder holder, Item item) {
        Categoria cat = item.categoria;
        Categoria categoriaVisual = resolveCategoriaVisualParaFilha(cat);
        String icone = CategoriaVisualFallback.icone(categoriaVisual, null);
        String cor = CategoriaVisualFallback.cor(categoriaVisual, null);

        holder.tvIcone.setText(icone);
        try {
            int color = Color.parseColor(cor);
            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.OVAL);
            bg.setColor(color);
            bg.setSize(dpToPx(32), dpToPx(32));
            holder.flIconeContainer.setBackground(bg);
        } catch (IllegalArgumentException ignored) {
            holder.flIconeContainer.setBackgroundResource(R.drawable.bg_category_icon);
        }

        holder.tvNome.setText(cat.getNome());
        if (cat.getLimiteMensal() != null) {
            holder.tvLimite.setText(String.format("R$ %.2f/mês", cat.getLimiteMensal()));
            holder.tvLimite.setVisibility(View.VISIBLE);
        } else {
            holder.tvLimite.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) listener.onChildClick(items.get(pos).categoria);
        });

        holder.itemView.setOnLongClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return false;
            listener.onChildLongClick(items.get(pos).categoria);
            return true;
        });
    }

    private Categoria resolveCategoriaVisualParaFilha(Categoria filha) {
        if (filha == null || filha.getPaiId() == null) {
            return filha;
        }
        for (Categoria root : rootCategories) {
            if (filha.getPaiId().equals(root.getId())) {
                return root;
            }
        }
        return filha;
    }

    private void toggleExpand(int headerPos) {
        Item header = items.get(headerPos);
        if (header.type != TYPE_HEADER) return;

        if (header.expanded) {
            // Collapse: remove children
            int childrenToRemove = 0;
            int pos = headerPos + 1;
            while (pos < items.size() && items.get(pos).type == TYPE_CHILD) {
                pos++;
                childrenToRemove++;
            }
            for (int i = 0; i < childrenToRemove; i++) {
                items.remove(headerPos + 1);
            }
            header.expanded = false;
            expandedStateById.put(header.categoria.getId(), false);
            notifyItemChanged(headerPos);
            notifyItemRangeRemoved(headerPos + 1, childrenToRemove);
        } else {
            // Expand: insert children
            List<Categoria> children = viewModel != null
                    ? viewModel.listarFilhas(header.categoria.getId())
                    : new ArrayList<>();
            header.expanded = true;
            int insertPos = headerPos + 1;
            for (int i = 0; i < children.size(); i++) {
                items.add(insertPos + i, new Item(TYPE_CHILD, children.get(i), false, 0));
            }
            expandedStateById.put(header.categoria.getId(), true);
            notifyItemChanged(headerPos);
            notifyItemRangeInserted(insertPos, children.size());
        }
    }

    private void toggleSection(int sectionType) {
        if (sectionType == SECTION_ESSENCIAIS) {
            secaoEssenciaisExpandida = !secaoEssenciaisExpandida;
        } else if (sectionType == SECTION_NAO_ESSENCIAIS) {
            secaoNaoEssenciaisExpandida = !secaoNaoEssenciaisExpandida;
        }
        rebuildItems();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private int dpToPx(int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }

    // ─── ViewHolders ──────────────────────────────────────────────────────────

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        final FrameLayout flIconeContainer;
        final TextView tvIcone;
        final TextView tvNome;
        final TextView tvInfo;
        final ImageView ivArrow;
        final ImageView btnEditar;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            flIconeContainer = itemView.findViewById(R.id.fl_icone_container);
            tvIcone = itemView.findViewById(R.id.tv_icone);
            tvNome  = itemView.findViewById(R.id.tv_categoria_nome);
            tvInfo  = itemView.findViewById(R.id.tv_categoria_info);
            ivArrow = itemView.findViewById(R.id.iv_expand_arrow);
            btnEditar = itemView.findViewById(R.id.btn_edit_categoria);
        }
    }

    static class ChildViewHolder extends RecyclerView.ViewHolder {
        final FrameLayout flIconeContainer;
        final TextView tvIcone;
        final TextView tvNome;
        final TextView tvLimite;

        ChildViewHolder(@NonNull View itemView) {
            super(itemView);
            flIconeContainer = itemView.findViewById(R.id.fl_subcategoria_icone_container);
            tvIcone = itemView.findViewById(R.id.tv_subcategoria_icone);
            tvNome   = itemView.findViewById(R.id.tv_subcategoria_nome);
            tvLimite = itemView.findViewById(R.id.tv_subcategoria_limite);
        }
    }

    static class SectionViewHolder extends RecyclerView.ViewHolder {
        final TextView tvTitulo;
        final ImageView ivArrow;

        SectionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tv_categoria_secao_titulo);
            ivArrow = itemView.findViewById(R.id.iv_categoria_secao_arrow);
        }
    }
}
