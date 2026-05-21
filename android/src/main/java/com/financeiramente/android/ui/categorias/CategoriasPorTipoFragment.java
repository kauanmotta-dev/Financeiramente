package com.financeiramente.android.ui.categorias;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.financeiramente.android.R;
import com.financeiramente.android.viewmodel.CategoriasViewModel;
import com.financeiramente.core.domain.entity.Categoria;
import com.financeiramente.core.domain.vo.TipoCategoria;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for one tab (Despesas or Receitas) in the categorias screen.
 * Position 0 = Despesas (ESSENCIAL + NAO_ESSENCIAL)
 * Position 1 = Receitas (RECEITA)
 */
public class CategoriasPorTipoFragment extends Fragment
        implements CategoriaRecyclerAdapter.OnItemActionListener {

    private static final String ARG_TAB_POSITION = "tabPosition";

    private int tabPosition;
    private CategoriaRecyclerAdapter adapter;
    private View emptyState;

    public static CategoriasPorTipoFragment newInstance(int tabPosition) {
        CategoriasPorTipoFragment fragment = new CategoriasPorTipoFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TAB_POSITION, tabPosition);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tabPosition = getArguments() != null ? getArguments().getInt(ARG_TAB_POSITION, 0) : 0;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_categorias_por_tipo, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        emptyState = view.findViewById(R.id.layout_empty_state);
        ((ImageView) emptyState.findViewById(R.id.iv_empty_illustration))
                .setImageResource(R.drawable.ic_empty_categories);
        ((TextView) emptyState.findViewById(R.id.tv_empty_title))
                .setText(R.string.empty_categorias_title);
        ((TextView) emptyState.findViewById(R.id.tv_empty_subtitle))
                .setText(R.string.empty_categorias_subtitle);
        com.google.android.material.button.MaterialButton btnCta =
                emptyState.findViewById(R.id.btn_empty_cta);
        btnCta.setText(R.string.empty_categorias_cta);
        btnCta.setVisibility(View.VISIBLE);
        btnCta.setOnClickListener(v -> navigateToForm(null, null));

        RecyclerView recyclerView = view.findViewById(R.id.rv_categorias);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new CategoriaRecyclerAdapter(requireContext(), this);
        recyclerView.setAdapter(adapter);

        observeViewModel();
    }

    @Override
    public void onResume() {
        super.onResume();
        getParentViewModel().carregarCategorias();
    }

    private void observeViewModel() {
        CategoriasViewModel vm = getParentViewModel();
        if (tabPosition == 0) {
            vm.getCategoriasDespesasRaiz().observe(getViewLifecycleOwner(), this::updateList);
        } else {
            vm.getCategoriasReceitasRaiz().observe(getViewLifecycleOwner(), this::updateList);
        }
    }

    private void updateList(List<Categoria> raizes) {
        if (raizes == null || raizes.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
        } else {
            emptyState.setVisibility(View.GONE);
        }

        List<Categoria> filhasMap = new ArrayList<>();
        for (Categoria raiz : raizes != null ? raizes : new ArrayList<Categoria>()) {
            filhasMap.addAll(getParentViewModel().listarFilhas(raiz.getId()));
        }
        adapter.submitData(raizes != null ? raizes : new ArrayList<>(), getParentViewModel());
    }

    @Override
    public void onHeaderClick(Categoria categoria) {
        // toggle expand in adapter — handled internally
    }

    @Override
    public void onChildClick(Categoria categoria) {
        navigateToForm(categoria.getId(), categoria.getPaiId());
    }

    @Override
    public void onHeaderLongClick(Categoria categoria) {
        showGroupOptions(categoria);
    }

    @Override
    public void onChildLongClick(Categoria categoria) {
        showChildOptions(categoria);
    }

    private void showGroupOptions(Categoria raiz) {
        new AlertDialog.Builder(requireContext())
                .setTitle(raiz.getNome())
                .setItems(new String[]{
                        getString(R.string.editar),
                        getString(R.string.nova_subcategoria),
                        getString(R.string.excluir)},
                        (dialog, which) -> {
                            switch (which) {
                                case 0: navigateToForm(raiz.getId(), raiz.getPaiId()); break;
                                case 1: navigateToForm(null, raiz.getId()); break;
                                case 2: confirmarExclusao(raiz); break;
                            }
                        })
                .show();
    }

    private void showChildOptions(Categoria filha) {
        new AlertDialog.Builder(requireContext())
                .setTitle(filha.getNome())
                .setItems(new String[]{getString(R.string.editar), getString(R.string.excluir)},
                        (dialog, which) -> {
                            if (which == 0) navigateToForm(filha.getId(), filha.getPaiId());
                            else confirmarExclusao(filha);
                        })
                .show();
    }

    private void confirmarExclusao(Categoria categoria) {
        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.excluir_categoria))
                .setMessage(getString(R.string.confirmar_exclusao_categoria, categoria.getNome()))
                .setPositiveButton(android.R.string.ok, (d, w) -> {
                    getParentViewModel().deletarCategoria(categoria.getId());
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void navigateToForm(@Nullable String categoriaId, @Nullable String paiId) {
        Fragment parent = requireParentFragment();
        if (parent instanceof CategoriasFragment) {
            ((CategoriasFragment) parent).navigateToForm(categoriaId, paiId);
        }
    }

    private CategoriasViewModel getParentViewModel() {
        Fragment parent = requireParentFragment();
        if (parent instanceof CategoriasFragment) {
            return ((CategoriasFragment) parent).getViewModel();
        }
        throw new IllegalStateException("Parent must be CategoriasFragment");
    }

    private static boolean isDespesa(TipoCategoria tipo) {
        return tipo == TipoCategoria.ESSENCIAL || tipo == TipoCategoria.NAO_ESSENCIAL;
    }
}
