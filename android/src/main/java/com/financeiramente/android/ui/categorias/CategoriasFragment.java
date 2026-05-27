package com.financeiramente.android.ui.categorias;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.viewpager2.widget.ViewPager2;

import com.financeiramente.android.R;
import com.financeiramente.android.app.AppContext;
import com.financeiramente.android.viewmodel.CategoriasViewModel;
import com.financeiramente.android.viewmodel.CategoriasViewModelFactory;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class CategoriasFragment extends Fragment {

    private CategoriasViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_categorias, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.btn_back_categorias).setOnClickListener(v ->
            Navigation.findNavController(view).navigateUp());

        AppContext ctx = AppContext.get(requireContext());
        CategoriasViewModelFactory factory = new CategoriasViewModelFactory(
                ctx.getCoreServices().getCategoriaRepository(),
                ctx.getCriarCategoriaUseCase(),
                ctx.getEditarCategoriaUseCase(),
                ctx.getDeletarCategoriaUseCase(),
                ctx.getReordenarCategoriasUseCase());
        viewModel = new ViewModelProvider(this, factory).get(CategoriasViewModel.class);

        ViewPager2 viewPager = view.findViewById(R.id.vp_categorias);
        TabLayout tabLayout = view.findViewById(R.id.tab_layout_categorias);

        CategoriasTabPagerAdapter pagerAdapter = new CategoriasTabPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(position == 0
                    ? getString(R.string.tab_despesas)
                    : getString(R.string.tab_receitas));
        }).attach();

        view.findViewById(R.id.btn_add_categoria).setOnClickListener(v -> navigateToForm(null, null));

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

    /** Exposed for child tab fragments */
    public CategoriasViewModel getViewModel() {
        return viewModel;
    }

    /** Exposed for child tab fragments to trigger navigation */
    public void navigateToForm(@Nullable String categoriaId, @Nullable String paiId) {
        Bundle args = new Bundle();
        args.putString("categoriaId", categoriaId);
        args.putString("paiId", paiId);
        Navigation.findNavController(requireView())
                .navigate(R.id.action_categoriasFragment_to_categoriaFormFragment, args);
    }
}
