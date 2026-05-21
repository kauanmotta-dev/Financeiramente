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
import com.financeiramente.android.viewmodel.CategoriasViewModel;
import com.financeiramente.core.domain.entity.Categoria;

import java.util.ArrayList;
import java.util.List;

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

    private static class Item {
        final int type;
        final Categoria categoria;
        boolean expanded;
        int childCount;

        Item(int type, Categoria categoria, boolean expanded, int childCount) {
            this.type = type;
            this.categoria = categoria;
            this.expanded = expanded;
            this.childCount = childCount;
        }
    }

    private final Context context;
    private final OnItemActionListener listener;
    private List<Item> items = new ArrayList<>();
    private List<Categoria> rootCategories = new ArrayList<>();
    private CategoriasViewModel viewModel;

    public CategoriaRecyclerAdapter(Context context, OnItemActionListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void submitData(List<Categoria> roots, CategoriasViewModel vm) {
        this.rootCategories = roots != null ? roots : new ArrayList<>();
        this.viewModel = vm;
        rebuildItems();
    }

    private void rebuildItems() {
        List<Item> newItems = new ArrayList<>();
        for (Categoria root : rootCategories) {
            List<Categoria> children = viewModel != null
                    ? viewModel.listarFilhas(root.getId())
                    : new ArrayList<>();

            // Find existing expanded state
            boolean wasExpanded = false;
            for (Item existing : items) {
                if (existing.type == TYPE_HEADER && existing.categoria.getId().equals(root.getId())) {
                    wasExpanded = existing.expanded;
                    break;
                }
            }

            Item header = new Item(TYPE_HEADER, root, wasExpanded, children.size());
            newItems.add(header);

            if (wasExpanded) {
                for (Categoria child : children) {
                    newItems.add(new Item(TYPE_CHILD, child, false, 0));
                }
            }
        }
        items = newItems;
        notifyDataSetChanged();
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
        } else {
            bindChild((ChildViewHolder) holder, item);
        }
    }

    private void bindHeader(HeaderViewHolder holder, Item item) {
        Categoria cat = item.categoria;

        // Icon + color
        holder.tvIcone.setText(cat.getIcone());
        try {
            int color = Color.parseColor(cat.getCor());
            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.OVAL);
            bg.setColor(color);
            bg.setSize(dpToPx(44), dpToPx(44));
            holder.flIconeContainer.setBackground(bg);
        } catch (IllegalArgumentException ignored) {
            holder.flIconeContainer.setBackgroundResource(R.drawable.bg_category_icon);
        }

        holder.tvNome.setText(cat.getNome());

        // Subtitle: "X subcategorias • R$ Y/mês" or "X subcategorias"
        StringBuilder info = new StringBuilder();
        if (item.childCount == 1) {
            info.append("1 subcategoria");
        } else if (item.childCount > 1) {
            info.append(item.childCount).append(" subcategorias");
        }
        if (cat.getLimiteMensal() != null) {
            if (info.length() > 0) info.append("  •  ");
            info.append(String.format("R$ %.0f/mês", cat.getLimiteMensal()));
        }
        holder.tvInfo.setText(info.toString());

        // Expand arrow rotation
        float rotation = item.expanded ? 180f : 0f;
        holder.ivArrow.setRotation(rotation);

        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_ID) return;
            toggleExpand(pos);
            listener.onHeaderClick(items.get(pos).categoria);
        });

        holder.itemView.setOnLongClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_ID) return false;
            listener.onHeaderLongClick(items.get(pos).categoria);
            return true;
        });
    }

    private void bindChild(ChildViewHolder holder, Item item) {
        Categoria cat = item.categoria;
        holder.tvNome.setText(cat.getNome());
        if (cat.getLimiteMensal() != null) {
            holder.tvLimite.setText(String.format("R$ %.2f/mês", cat.getLimiteMensal()));
            holder.tvLimite.setVisibility(View.VISIBLE);
        } else {
            holder.tvLimite.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_ID) listener.onChildClick(items.get(pos).categoria);
        });

        holder.itemView.setOnLongClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_ID) return false;
            listener.onChildLongClick(items.get(pos).categoria);
            return true;
        });
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
            notifyItemChanged(headerPos);
            notifyItemRangeInserted(insertPos, children.size());
        }
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

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            flIconeContainer = itemView.findViewById(R.id.fl_icone_container);
            tvIcone = itemView.findViewById(R.id.tv_icone);
            tvNome  = itemView.findViewById(R.id.tv_categoria_nome);
            tvInfo  = itemView.findViewById(R.id.tv_categoria_info);
            ivArrow = itemView.findViewById(R.id.iv_expand_arrow);
        }
    }

    static class ChildViewHolder extends RecyclerView.ViewHolder {
        final TextView tvNome;
        final TextView tvLimite;

        ChildViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNome   = itemView.findViewById(R.id.tv_subcategoria_nome);
            tvLimite = itemView.findViewById(R.id.tv_subcategoria_limite);
        }
    }
}
