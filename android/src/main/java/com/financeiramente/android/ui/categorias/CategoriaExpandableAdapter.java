package com.financeiramente.android.ui.categorias;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.financeiramente.android.R;
import com.financeiramente.android.viewmodel.CategoriasViewModel;
import com.financeiramente.core.domain.entity.Categoria;

import java.util.Collections;
import java.util.List;

public class CategoriaExpandableAdapter extends BaseExpandableListAdapter {

    private final Context context;
    private final CategoriasViewModel viewModel;

    public CategoriaExpandableAdapter(Context context, CategoriasViewModel viewModel) {
        this.context = context;
        this.viewModel = viewModel;
    }

    private List<Categoria> getRaizes() {
        List<Categoria> raizes = viewModel.getCategoriasRaiz().getValue();
        return raizes != null ? raizes : Collections.emptyList();
    }

    @Override
    public int getGroupCount() {
        return getRaizes().size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return viewModel.listarFilhas(getGroup(groupPosition).getId()).size();
    }

    @Override
    public Categoria getGroup(int groupPosition) {
        return getRaizes().get(groupPosition);
    }

    @Override
    public Categoria getChild(int groupPosition, int childPosition) {
        return viewModel.listarFilhas(getGroup(groupPosition).getId()).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_categoria_raiz, parent, false);
        }
        Categoria categoria = getGroup(groupPosition);
        TextView tvNome = convertView.findViewById(R.id.tv_categoria_nome);
        tvNome.setText(categoria.getNome());
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                             View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_categoria_filha, parent, false);
        }
        Categoria categoria = getChild(groupPosition, childPosition);
        TextView tvNome = convertView.findViewById(R.id.tv_subcategoria_nome);
        TextView tvLimite = convertView.findViewById(R.id.tv_subcategoria_limite);
        tvNome.setText(categoria.getNome());
        if (categoria.getLimiteMensal() != null) {
            tvLimite.setText(String.format("R$ %.2f/mês", categoria.getLimiteMensal()));
            tvLimite.setVisibility(View.VISIBLE);
        } else {
            tvLimite.setVisibility(View.GONE);
        }
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
