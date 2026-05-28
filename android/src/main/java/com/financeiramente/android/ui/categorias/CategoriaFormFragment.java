package com.financeiramente.android.ui.categorias;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.financeiramente.android.R;
import com.financeiramente.android.app.AppContext;
import com.financeiramente.core.domain.entity.Categoria;
import com.financeiramente.core.domain.vo.TipoCategoria;
import com.financeiramente.core.util.DomainException;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Optional;
import java.math.BigDecimal;

public class CategoriaFormFragment extends Fragment {

    private TextInputLayout tilNome;
    private TextInputEditText etNome;
    private TabLayout tabTipo;
    private TabLayout tabDespesaClassificacao;
    private TextInputEditText etLimite;
    private IconeSelectorAdapter iconeSelectorAdapter;
    private CorSelectorAdapter corSelectorAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_categoria_form, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.btn_back_categoria_form).setOnClickListener(v ->
            Navigation.findNavController(view).navigateUp());

        tilNome  = view.findViewById(R.id.til_nome);
        etNome   = view.findViewById(R.id.et_nome);
        tabTipo  = view.findViewById(R.id.tab_tipo);
        tabDespesaClassificacao = view.findViewById(R.id.tab_despesa_classificacao);
        etLimite = view.findViewById(R.id.et_limite);
        com.google.android.material.textfield.TextInputLayout tilLimite = view.findViewById(R.id.til_limite);
        MaterialButton btnSalvar = view.findViewById(R.id.btn_salvar);
        MaterialButton btnExcluirSubcategoria = view.findViewById(R.id.btn_excluir_subcategoria);
        View llTipoContainer = view.findViewById(R.id.ll_tipo_container);
        View llDespesaClassificacao = view.findViewById(R.id.ll_despesa_classificacao);
        View tvLabelCor = view.findViewById(R.id.tv_label_cor);
        View tvLabelIcone = view.findViewById(R.id.tv_label_icone);
        View tvSubcategoriaCorHint = view.findViewById(R.id.tv_subcategoria_cor_hint);
        RecyclerView rvIcones = view.findViewById(R.id.rv_icones);
        RecyclerView rvCores  = view.findViewById(R.id.rv_cores);

        String categoriaId = getArguments() != null ? getArguments().getString("categoriaId") : null;
        String paiId       = getArguments() != null ? getArguments().getString("paiId") : null;

        // Type selector: hidden for subcategories
        boolean isSubcategoria = paiId != null;
        if (isSubcategoria) {
            llTipoContainer.setVisibility(View.GONE);
            llDespesaClassificacao.setVisibility(View.GONE);
            rvCores.setVisibility(View.GONE);
            tvLabelCor.setVisibility(View.GONE);
            rvIcones.setVisibility(View.GONE);
            if (tvLabelIcone != null) tvLabelIcone.setVisibility(View.GONE);
            tvSubcategoriaCorHint.setVisibility(View.VISIBLE);
        } else {
            llDespesaClassificacao.setVisibility(View.VISIBLE);
            tvSubcategoriaCorHint.setVisibility(View.GONE);
        }

        // Default selections
        final String[] initialIcone = { IconeSelectorAdapter.ICONES.get(0) };
        final String[] initialCor   = { CorSelectorAdapter.CORES.get(0) };

        // Load existing values if editing
        AppContext ctx = AppContext.get(requireContext());
        if (categoriaId != null) {
            Optional<Categoria> opt = ctx.getCoreServices().getCategoriaRepository().buscarPorId(categoriaId);
            if (opt.isPresent()) {
                Categoria categoria = opt.get();
                etNome.setText(categoria.getNome());
                if (categoria.getLimiteMensal() != null) {
                    etLimite.setText(String.valueOf(categoria.getLimiteMensal()));
                }
                // Select tab based on tipo
                int tabIndex = (categoria.getTipo() == TipoCategoria.RECEITA) ? 1 : 0;
                if (tabTipo.getTabAt(tabIndex) != null) {
                    tabTipo.selectTab(tabTipo.getTabAt(tabIndex));
                }
                if (categoria.getTipo() == TipoCategoria.NAO_ESSENCIAL
                        && tabDespesaClassificacao.getTabAt(1) != null) {
                    tabDespesaClassificacao.selectTab(tabDespesaClassificacao.getTabAt(1));
                } else if (tabDespesaClassificacao.getTabAt(0) != null) {
                    tabDespesaClassificacao.selectTab(tabDespesaClassificacao.getTabAt(0));
                }
                initialIcone[0] = categoria.getIcone();
                initialCor[0] = categoria.getCor();
            }
        }

        tabTipo.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                boolean despesaSelecionada = tab != null && tab.getPosition() == 0;
                if (!isSubcategoria) {
                    llDespesaClassificacao.setVisibility(despesaSelecionada ? View.VISIBLE : View.GONE);
                }
                tilLimite.setHint(getString(despesaSelecionada
                        ? R.string.categoria_limite_mensal
                        : R.string.categoria_previsao_receita));
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        if (!isSubcategoria) {
            llDespesaClassificacao.setVisibility(
                    tabTipo.getSelectedTabPosition() == 0 ? View.VISIBLE : View.GONE);
        }
        // Set initial hint if the tab listener hasn't fired (default state)
        tilLimite.setHint(getString(tabTipo.getSelectedTabPosition() == 1
                ? R.string.categoria_previsao_receita
                : R.string.categoria_limite_mensal));

        // Icon grid
        iconeSelectorAdapter = new IconeSelectorAdapter(
                requireContext(), initialIcone[0], icone -> { /* selection tracked in adapter */ });
        rvIcones.setLayoutManager(new GridLayoutManager(requireContext(), 6));
        rvIcones.setAdapter(iconeSelectorAdapter);

        // Color grid
        corSelectorAdapter = new CorSelectorAdapter(
            requireContext(), initialCor[0], cor -> { /* selection tracked in adapter */ });
        rvCores.setLayoutManager(new GridLayoutManager(requireContext(), 8));
        rvCores.setAdapter(corSelectorAdapter);

        if (categoriaId != null && isSubcategoria) {
            btnExcluirSubcategoria.setVisibility(View.VISIBLE);
            btnExcluirSubcategoria.setOnClickListener(v -> confirmarExclusaoSubcategoria(categoriaId, view, ctx));
        }

        final String finalCategoriaId = categoriaId;
        final String finalPaiId       = paiId;
        btnSalvar.setOnClickListener(v -> salvar(finalCategoriaId, finalPaiId, ctx, view));
    }

    private void confirmarExclusaoSubcategoria(String categoriaId, View view, AppContext ctx) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.excluir_subcategoria)
                .setMessage(R.string.confirmar_excluir_subcategoria)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    try {
                        ctx.getDeletarCategoriaUseCase().executar(categoriaId);
                        Navigation.findNavController(view).popBackStack();
                    } catch (DomainException e) {
                        Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void salvar(String categoriaId, String paiId, AppContext ctx, View view) {
        String nome = etNome.getText() != null ? etNome.getText().toString().trim() : "";
        if (nome.isEmpty()) {
            tilNome.setError(getString(R.string.erro_nome_obrigatorio));
            return;
        }
        tilNome.setError(null);

        // Resolve tipo from tab (ignored for subcategories — use case handles it)
        TipoCategoria tipo;
        if (paiId != null) {
            tipo = TipoCategoria.ESSENCIAL; // will be overridden by use case with parent's tipo
        } else {
            int selectedTab = tabTipo.getSelectedTabPosition();
            if (selectedTab == 1) {
                tipo = TipoCategoria.RECEITA;
            } else {
                tipo = tabDespesaClassificacao.getSelectedTabPosition() == 1
                        ? TipoCategoria.NAO_ESSENCIAL
                        : TipoCategoria.ESSENCIAL;
            }
        }

        BigDecimal limiteMensal = null;
        String limiteStr = etLimite.getText() != null ? etLimite.getText().toString().trim() : "";
        if (!limiteStr.isEmpty()) {
            try {
                limiteMensal = new BigDecimal(limiteStr);
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), getString(R.string.erro_limite_invalido),
                        Toast.LENGTH_SHORT).show();
                return;
            }
        }

        String icone = paiId != null
                ? buscarIconeCategoriaPai(ctx, paiId, iconeSelectorAdapter.getSelectedIcone())
                : iconeSelectorAdapter.getSelectedIcone();
        String cor = paiId != null
                ? buscarCorCategoriaPai(ctx, paiId, corSelectorAdapter.getSelectedCor())
                : corSelectorAdapter.getSelectedCor();

        try {
            if (categoriaId != null) {
                ctx.getEditarCategoriaUseCase().executar(categoriaId, nome, tipo, limiteMensal, icone, cor);
            } else {
                ctx.getCriarCategoriaUseCase().executar(nome, tipo, paiId, limiteMensal, icone, cor);
            }

            String expandRootId = paiId != null ? paiId : categoriaId;
            if (expandRootId != null) {
                Bundle result = new Bundle();
                result.putString("expandRootId", expandRootId);
                getParentFragmentManager().setFragmentResult("categoria_form_saved", result);
            }
            Navigation.findNavController(view).popBackStack();
        } catch (DomainException e) {
            Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private String buscarIconeCategoriaPai(AppContext ctx, String paiId, String fallbackIcone) {
        return ctx.getCoreServices().getCategoriaRepository()
                .buscarPorId(paiId)
                .map(Categoria::getIcone)
                .orElse(fallbackIcone);
    }

    private String buscarCorCategoriaPai(AppContext ctx, String paiId, String fallbackCor) {
        return ctx.getCoreServices().getCategoriaRepository()
                .buscarPorId(paiId)
                .map(Categoria::getCor)
                .orElse(fallbackCor);
    }
}
