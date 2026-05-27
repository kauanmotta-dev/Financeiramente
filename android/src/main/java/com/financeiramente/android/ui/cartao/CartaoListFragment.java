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
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.financeiramente.android.R;
import com.financeiramente.android.app.AppContext;
import com.financeiramente.android.viewmodel.CartaoListViewModel;
import com.financeiramente.android.viewmodel.CartaoListViewModelFactory;
import com.financeiramente.core.domain.entity.CartaoCredito;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

public class CartaoListFragment extends Fragment {

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
        FloatingActionButton fab = view.findViewById(R.id.fab_novo_cartao);

        view.findViewById(R.id.btn_back_cartao_list).setOnClickListener(v -> {
            if (!Navigation.findNavController(view).navigateUp()) {
                Navigation.findNavController(view).navigate(R.id.nav_dashboard);
            }
        });

        adapter = new CartaoAdapter(new CartaoAdapter.OnCartaoClickListener() {
            @Override
            public void onCartaoClick(CartaoCredito cartao) {
                Bundle args = new Bundle();
                args.putString("cartaoId", cartao.getId());
                Navigation.findNavController(view)
                        .navigate(R.id.action_cartaoListFragment_to_faturaListFragment, args);
            }

            @Override
            public void onCartaoLongClick(CartaoCredito cartao) {
                mostrarMenuContextual(view, cartao);
            }
        });

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        fab.setOnClickListener(v ->
                Navigation.findNavController(view)
                        .navigate(R.id.action_cartaoListFragment_to_cartaoFormFragment));

        observarViewModel(view);
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
}
