package com.financeiramente.android.ui.categorias;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.financeiramente.android.R;
import com.financeiramente.android.app.AppContext;
import com.financeiramente.android.viewmodel.CategoriasViewModel;
import com.financeiramente.android.viewmodel.CategoriasViewModelFactory;
import com.financeiramente.core.domain.entity.Categoria;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class CategoriasFragment extends Fragment {

    private CategoriasViewModel viewModel;
    private ExpandableListView expandableListView;
    private CategoriaExpandableAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_categorias, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AppContext ctx = AppContext.get(requireContext());
        CategoriasViewModelFactory factory = new CategoriasViewModelFactory(
                ctx.getCategoriaRepository(),
                ctx.getCriarCategoriaUseCase(),
                ctx.getEditarCategoriaUseCase(),
                ctx.getDeletarCategoriaUseCase(),
                ctx.getReordenarCategoriasUseCase());
        viewModel = new ViewModelProvider(this, factory).get(CategoriasViewModel.class);

        expandableListView = view.findViewById(R.id.elv_categorias);
        FloatingActionButton fab = view.findViewById(R.id.fab_add_categoria);

        adapter = new CategoriaExpandableAdapter(requireContext(), viewModel);
        expandableListView.setAdapter(adapter);

        // Single-click on child → edit subcategory
        expandableListView.setOnChildClickListener((parent, v, groupPos, childPos, id) -> {
            Categoria filha = adapter.getChild(groupPos, childPos);
            navigateToForm(view, filha.getId(), filha.getPaiId());
            return true;
        });

        // Long-press on any item → options dialog
        expandableListView.setOnItemLongClickListener((parent, v, flatPosition, id) -> {
            long packedPos = expandableListView.getExpandableListPosition(flatPosition);
            int type = ExpandableListView.getPackedPositionType(packedPos);
            int groupPos = ExpandableListView.getPackedPositionGroup(packedPos);
            int childPos = ExpandableListView.getPackedPositionChild(packedPos);

            if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
                Categoria raiz = adapter.getGroup(groupPos);
                showGroupOptions(raiz, view);
            } else if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                Categoria filha = adapter.getChild(groupPos, childPos);
                showChildOptions(filha, view);
            }
            return true;
        });

        // FAB → new root category
        fab.setOnClickListener(v -> navigateToForm(view, null, null));

        viewModel.getCategoriasRaiz().observe(getViewLifecycleOwner(), categorias -> {
            adapter.notifyDataSetChanged();
            for (int i = 0; i < adapter.getGroupCount(); i++) {
                expandableListView.expandGroup(i);
            }
        });

        viewModel.getErro().observe(getViewLifecycleOwner(), erro -> {
            if (erro != null) {
                Toast.makeText(requireContext(), erro, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.carregarCategorias();
    }

    private void navigateToForm(View view, @Nullable String categoriaId, @Nullable String paiId) {
        Bundle args = new Bundle();
        args.putString("categoriaId", categoriaId);
        args.putString("paiId", paiId);
        Navigation.findNavController(view)
                .navigate(R.id.action_categoriasFragment_to_categoriaFormFragment, args);
    }

    private void showGroupOptions(Categoria raiz, View view) {
        new AlertDialog.Builder(requireContext())
                .setTitle(raiz.getNome())
                .setItems(new String[]{"Editar", "Nova subcategoria", "Excluir"},
                        (dialog, which) -> {
                            switch (which) {
                                case 0:
                                    navigateToForm(view, raiz.getId(), raiz.getPaiId());
                                    break;
                                case 1:
                                    navigateToForm(view, null, raiz.getId());
                                    break;
                                case 2:
                                    confirmarExclusao(raiz);
                                    break;
                            }
                        })
                .show();
    }

    private void showChildOptions(Categoria filha, View view) {
        new AlertDialog.Builder(requireContext())
                .setTitle(filha.getNome())
                .setItems(new String[]{"Editar", "Excluir"},
                        (dialog, which) -> {
                            if (which == 0) {
                                navigateToForm(view, filha.getId(), filha.getPaiId());
                            } else {
                                confirmarExclusao(filha);
                            }
                        })
                .show();
    }

    private void confirmarExclusao(Categoria categoria) {
        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.excluir_categoria))
                .setMessage(getString(R.string.confirmar_exclusao_categoria, categoria.getNome()))
                .setPositiveButton(android.R.string.ok, (d, w) ->
                        viewModel.deletarCategoria(categoria.getId()))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}
