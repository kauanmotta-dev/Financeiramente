package com.financeiramente.android.ui.cartao;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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
import com.financeiramente.android.viewmodel.FaturaListViewModel;
import com.financeiramente.android.viewmodel.FaturaListViewModelFactory;
import com.financeiramente.core.domain.entity.CompraCartao;
import com.financeiramente.core.domain.entity.Fatura;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class FaturaListFragment extends Fragment {

    private FaturaListViewModel viewModel;
    private FaturaAdapter adapter;
    private TextView tvVazio;
    private TextView tvRecorrentesVazio;
    private LinearLayout layoutRecorrentes;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_fatura_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AppContext ctx = AppContext.get(requireContext());
        FaturaListViewModelFactory factory = new FaturaListViewModelFactory(
                ctx.getCoreServices().getFaturaRepository(),
            ctx.getAtualizarStatusFaturasUseCase(),
            ctx.getListarComprasCartaoUseCase(),
            ctx.getCancelarRecorrenciaCartaoUseCase());
        viewModel = new ViewModelProvider(this, factory).get(FaturaListViewModel.class);

        String cartaoId = getArguments() != null ? getArguments().getString("cartaoId") : null;
        tvVazio = view.findViewById(R.id.tv_faturas_vazio);
        tvRecorrentesVazio = view.findViewById(R.id.tv_recorrentes_vazio);
        layoutRecorrentes = view.findViewById(R.id.layout_recorrentes_lista);
        view.findViewById(R.id.btn_voltar_fatura_list).setOnClickListener(
                v -> Navigation.findNavController(view).navigateUp());

        adapter = new FaturaAdapter(fatura -> abrirDetalhe(view, fatura));
        RecyclerView rv = view.findViewById(R.id.rv_faturas);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        observarViewModel(view);

        if (cartaoId != null) {
            viewModel.init(cartaoId);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) viewModel.carregar();
    }

    private void abrirDetalhe(View view, Fatura fatura) {
        Bundle args = new Bundle();
        args.putString("faturaId", fatura.getId());
        Navigation.findNavController(view)
                .navigate(R.id.action_faturaListFragment_to_faturaDetalheFragment, args);
    }

    private void observarViewModel(View view) {
        viewModel.getFaturas().observe(getViewLifecycleOwner(), faturas -> {
            adapter.submitList(faturas);
            tvVazio.setVisibility(faturas == null || faturas.isEmpty() ? View.VISIBLE : View.GONE);
        });

        viewModel.getCobrancasRecorrentes().observe(getViewLifecycleOwner(), this::renderRecorrentes);

        viewModel.getErro().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) Snackbar.make(view, msg, Snackbar.LENGTH_LONG).show();
        });
    }

    private void renderRecorrentes(List<CompraCartao> recorrentes) {
        if (layoutRecorrentes == null || tvRecorrentesVazio == null) {
            return;
        }
        layoutRecorrentes.removeAllViews();
        if (recorrentes == null || recorrentes.isEmpty()) {
            tvRecorrentesVazio.setVisibility(View.VISIBLE);
            return;
        }

        tvRecorrentesVazio.setVisibility(View.GONE);
        for (CompraCartao compra : recorrentes) {
            View item = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_recorrencia_cartao, layoutRecorrentes, false);

            TextView tvDescricao = item.findViewById(R.id.tv_recorrencia_descricao);
            TextView tvDetalhes = item.findViewById(R.id.tv_recorrencia_detalhes);
            MaterialButton btnCancelar = item.findViewById(R.id.btn_cancelar_recorrencia);

            tvDescricao.setText(compra.getDescricao());
            String detalhes = getString(
                    R.string.recorrencia_cartao_detalhes,
                    compra.getDiaRecorrencia(),
                    String.format(java.util.Locale.getDefault(), "R$ %.2f", compra.getValorTotal()));
            tvDetalhes.setText(detalhes);

            btnCancelar.setOnClickListener(v -> confirmarCancelamento(compra));
            layoutRecorrentes.addView(item);
        }
    }

    private void confirmarCancelamento(CompraCartao compra) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.cancelar_recorrencia_cartao_titulo)
                .setMessage(getString(R.string.cancelar_recorrencia_cartao_mensagem, compra.getDescricao()))
                .setPositiveButton(R.string.confirmar,
                        (dialog, which) -> viewModel.cancelarRecorrencia(compra.getId()))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}
