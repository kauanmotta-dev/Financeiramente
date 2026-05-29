package com.financeiramente.android.ui.cartao;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.financeiramente.android.R;
import com.financeiramente.android.app.AppContext;
import com.financeiramente.android.viewmodel.CartaoListViewModel;
import com.financeiramente.android.viewmodel.CartaoListViewModelFactory;
import com.financeiramente.android.viewmodel.CartaoListViewModel.CartaoComUtilizacao;
import com.financeiramente.core.domain.entity.CartaoCredito;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

public class CartaoListFragment extends Fragment {

    private static final String ARG_SOURCE_SCREEN = "sourceScreen";
    private static final String ARG_SOURCE_TAB = "sourceTab";

    private CartaoListViewModel viewModel;
    private CartaoAdapter adapter;
    private TextView tvVazio;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cartao_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AppContext ctx = AppContext.get(requireContext());
        CartaoListViewModelFactory factory = new CartaoListViewModelFactory(
                ctx.getCoreServices().getCartaoRepository(),
                ctx.getCoreServices().getFaturaRepository(),
                ctx.getDesativarCartaoCreditoUseCase());
        viewModel = new ViewModelProvider(this, factory).get(CartaoListViewModel.class);

        RecyclerView rv = view.findViewById(R.id.rv_cartoes);
        tvVazio = view.findViewById(R.id.tv_cartoes_vazio);
        MaterialButton btnNovoCartao = view.findViewById(R.id.btn_novo_cartao);

        view.findViewById(R.id.btn_back_cartao_list).setOnClickListener(v -> voltarParaOrigem(view));

        adapter = new CartaoAdapter(new CartaoAdapter.OnCartaoClickListener() {
            @Override
            public void onCartaoClick(CartaoComUtilizacao item) {
                Bundle args = new Bundle();
                args.putString("cartaoId", item.getCartao().getId());
                Navigation.findNavController(view)
                        .navigate(R.id.action_cartaoListFragment_to_faturaListFragment, args);
            }

            @Override
            public void onCartaoLongClick(CartaoComUtilizacao item) {
                mostrarMenuContextual(view, item.getCartao());
            }
        });

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        btnNovoCartao.setOnClickListener(v ->
                Navigation.findNavController(view)
                        .navigate(R.id.action_cartaoListFragment_to_cartaoFormFragment));

        observarViewModel(view);

        getParentFragmentManager().setFragmentResultListener(
            "cartao_form_saved",
            getViewLifecycleOwner(),
            (requestKey, result) -> viewModel.carregar());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) viewModel.carregar();
    }

    private void observarViewModel(View view) {
        viewModel.getCartoes().observe(getViewLifecycleOwner(), cartoes -> {
            adapter.submitList(cartoes);
            tvVazio.setVisibility(cartoes == null || cartoes.isEmpty() ? View.VISIBLE : View.GONE);
        });

        viewModel.getErro().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) Snackbar.make(view, msg, Snackbar.LENGTH_LONG).show();
        });
    }

    private void mostrarMenuContextual(View view, CartaoCredito cartao) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(cartao.getNome())
                .setItems(new CharSequence[]{
                        getString(R.string.editar),
                        getString(R.string.cartao_desativar)
                }, (dialog, which) -> {
                    if (which == 0) {
                        Bundle args = new Bundle();
                        args.putString("cartaoId", cartao.getId());
                        Navigation.findNavController(view)
                                .navigate(R.id.action_cartaoListFragment_to_cartaoFormFragment, args);
                    } else {
                        confirmarDesativacao(view, cartao);
                    }
                })
                .show();
    }

    private void confirmarDesativacao(View view, CartaoCredito cartao) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.cartao_desativar)
                .setMessage(getString(R.string.cartao_confirmar_desativar, cartao.getNome()))
                .setPositiveButton(R.string.confirmar, (d, w) -> viewModel.desativar(cartao.getId()))
                .setNegativeButton(R.string.cancelar, null)
                .show();
    }

    private void voltarParaOrigem(View view) {
        NavController navController = Navigation.findNavController(view);

        // Prioriza retorno para a tela imediatamente anterior no back stack.
        if (navController.navigateUp()) {
            return;
        }

        int sourceScreen = getArguments() != null
                ? getArguments().getInt(ARG_SOURCE_SCREEN, R.id.nav_dashboard)
                : R.id.nav_dashboard;

        if (sourceScreen == R.id.lancamentosListFragment) {
            Bundle args = new Bundle();
            int sourceTab = getArguments() != null
                    ? getArguments().getInt(ARG_SOURCE_TAB, R.id.nav_dashboard)
                    : R.id.nav_dashboard;
            args.putInt(ARG_SOURCE_TAB,
                    sourceTab == R.id.nav_gastos ? R.id.nav_gastos : R.id.nav_dashboard);
            navController.navigate(
                    R.id.lancamentosListFragment,
                    args,
                    new NavOptions.Builder()
                            .setLaunchSingleTop(true)
                            .setPopUpTo(R.id.nav_graph, false)
                            .build());
            return;
        }

        if (sourceScreen == R.id.nav_dashboard || sourceScreen == R.id.nav_configuracoes) {
            navController.navigate(
                    sourceScreen,
                    null,
                    new NavOptions.Builder()
                            .setLaunchSingleTop(true)
                            .setPopUpTo(R.id.nav_graph, false)
                            .build());
            return;
        }

        navController.navigate(
                R.id.nav_dashboard,
                null,
                new NavOptions.Builder()
                        .setLaunchSingleTop(true)
                        .setPopUpTo(R.id.nav_graph, false)
                        .build());
    }
}
