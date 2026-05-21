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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Optional;

public class CategoriaFormFragment extends Fragment {

    private TextInputLayout tilNome;
    private TextInputEditText etNome;
    private TabLayout tabTipo;
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

        tilNome  = view.findViewById(R.id.til_nome);
        etNome   = view.findViewById(R.id.et_nome);
        tabTipo  = view.findViewById(R.id.tab_tipo);
        etLimite = view.findViewById(R.id.et_limite);
        MaterialButton btnSalvar = view.findViewById(R.id.btn_salvar);
        View llTipoContainer = view.findViewById(R.id.ll_tipo_container);
        RecyclerView rvIcones = view.findViewById(R.id.rv_icones);
        RecyclerView rvCores  = view.findViewById(R.id.rv_cores);

        String categoriaId = getArguments() != null ? getArguments().getString("categoriaId") : null;
        String paiId       = getArguments() != null ? getArguments().getString("paiId") : null;

        // Type selector: hidden for subcategories
        if (paiId != null) {
            llTipoContainer.setVisibility(View.GONE);
        }

        // Default selections
        String initialIcone = IconeSelectorAdapter.ICONES.get(0);
        String initialCor   = CorSelectorAdapter.CORES.get(0);

        // Load existing values if editing
        AppContext ctx = AppContext.get(requireContext());
        if (categoriaId != null) {
            Optional<Categoria> opt = ctx.getCategoriaRepository().buscarPorId(categoriaId);
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
                initialIcone = categoria.getIcone();
                initialCor   = categoria.getCor();
            }
        }

        // Icon grid
        iconeSelectorAdapter = new IconeSelectorAdapter(
                requireContext(), initialIcone, icone -> { /* selection tracked in adapter */ });
        rvIcones.setLayoutManager(new GridLayoutManager(requireContext(), 6));
        rvIcones.setAdapter(iconeSelectorAdapter);

        // Color grid
        corSelectorAdapter = new CorSelectorAdapter(
                requireContext(), initialCor, cor -> { /* selection tracked in adapter */ });
        rvCores.setLayoutManager(new GridLayoutManager(requireContext(), 8));
        rvCores.setAdapter(corSelectorAdapter);

        final String finalCategoriaId = categoriaId;
        final String finalPaiId       = paiId;
        btnSalvar.setOnClickListener(v -> salvar(finalCategoriaId, finalPaiId, ctx, view));
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
            tipo = (selectedTab == 1) ? TipoCategoria.RECEITA : TipoCategoria.ESSENCIAL;
        }

        Double limiteMensal = null;
        String limiteStr = etLimite.getText() != null ? etLimite.getText().toString().trim() : "";
        if (!limiteStr.isEmpty()) {
            try {
                limiteMensal = Double.parseDouble(limiteStr);
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), getString(R.string.erro_limite_invalido),
                        Toast.LENGTH_SHORT).show();
                return;
            }
        }

        String icone = iconeSelectorAdapter.getSelectedIcone();
        String cor   = corSelectorAdapter.getSelectedCor();

        try {
            if (categoriaId != null) {
                ctx.getEditarCategoriaUseCase().executar(categoriaId, nome, tipo, limiteMensal, icone, cor);
            } else {
                ctx.getCriarCategoriaUseCase().executar(nome, tipo, paiId, limiteMensal, icone, cor);
            }
            Navigation.findNavController(view).popBackStack();
        } catch (DomainException e) {
            Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
