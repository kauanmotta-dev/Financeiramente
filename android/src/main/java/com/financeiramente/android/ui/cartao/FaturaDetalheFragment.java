package com.financeiramente.android.ui.cartao;

import android.content.Context;
import android.graphics.Color;
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
import com.financeiramente.android.ui.lancamentos.LancamentoAdapter;
import com.financeiramente.android.viewmodel.FaturaDetalheViewModel;
import com.financeiramente.android.viewmodel.FaturaDetalheViewModelFactory;
import com.financeiramente.core.domain.entity.Fatura;
import com.financeiramente.core.domain.entity.Lancamento;
import com.financeiramente.core.domain.vo.StatusFatura;
import com.financeiramente.core.usecase.fatura.FaturaDetalheResult;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Locale;
import java.math.BigDecimal;

public class FaturaDetalheFragment extends Fragment {

    private FaturaDetalheViewModel viewModel;
    private LancamentoAdapter adapter;

    private TextView tvTitulo;
    private TextView tvTotal;
    private TextView tvStatusBadge;
    private TextView tvFechamento;
    private TextView tvVencimento;
    private MaterialButton btnPagar;
    private String faturaId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_fatura_detalhe, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        faturaId = getArguments() != null ? getArguments().getString("faturaId") : null;

        AppContext ctx = AppContext.get(requireContext());
        FaturaDetalheViewModelFactory factory = new FaturaDetalheViewModelFactory(
                ctx.getCoreServices().getFaturaRepository(),
                ctx.getCoreServices().getLancamentoRepository(),
                ctx.getCoreServices().getCategoriaRepository(),
                ctx.getPagarFaturaUseCase(),
                ctx.getAtualizarStatusFaturasUseCase());
        viewModel = new ViewModelProvider(this, factory).get(FaturaDetalheViewModel.class);

        tvTitulo = view.findViewById(R.id.tv_fatura_detalhe_titulo);
        tvTotal = view.findViewById(R.id.tv_detalhe_total);
        tvStatusBadge = view.findViewById(R.id.tv_detalhe_status_badge);
        tvFechamento = view.findViewById(R.id.tv_detalhe_fechamento);
        tvVencimento = view.findViewById(R.id.tv_detalhe_vencimento);
        btnPagar = view.findViewById(R.id.btn_pagar_fatura);

        view.findViewById(R.id.btn_voltar_fatura_detalhe).setOnClickListener(
                v -> Navigation.findNavController(view).navigateUp());

        btnPagar.setOnClickListener(v -> abrirPagarFatura());

        adapter = new LancamentoAdapter(
            lancamento -> { },
            this::mostrarOpcoesExclusaoLancamento);
        RecyclerView rv = view.findViewById(R.id.rv_lancamentos_fatura);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        observarViewModel(view);

        if (faturaId != null) {
            viewModel.init(faturaId);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null && faturaId != null) viewModel.carregar();
    }

    private void observarViewModel(View view) {
        viewModel.getDetalhe().observe(getViewLifecycleOwner(), result -> {
            if (result == null) return;
            Fatura fatura = result.getFatura();

            // Formatar mês: "YYYY-MM" → "Janeiro 2024"
            String tituloMes = fatura.getMes();
            try {
                java.time.YearMonth ym = java.time.YearMonth.parse(fatura.getMes());
                String nomeMes = ym.getMonth().getDisplayName(java.time.format.TextStyle.FULL, new java.util.Locale("pt", "BR"));
                tituloMes = nomeMes.substring(0, 1).toUpperCase() + nomeMes.substring(1) + "/" + ym.getYear();
            } catch (Exception ignored) { }
            tvTitulo.setText(getString(R.string.fatura_detalhe_titulo, tituloMes));

            tvFechamento.setText(getString(R.string.fatura_fecha_em, fatura.getDataFechamento()));
            tvVencimento.setText(getString(R.string.fatura_vence_em, fatura.getDataVencimento()));

            applyStatusBadge(requireContext(), fatura.getStatus());

            boolean podePagar = fatura.getStatus() == StatusFatura.ABERTO
                    || fatura.getStatus() == StatusFatura.FECHADO;
            btnPagar.setVisibility(podePagar ? View.VISIBLE : View.GONE);

            java.util.Map<String, com.financeiramente.core.domain.entity.Categoria> catsMap =
                    viewModel.getCategoriasMap().getValue();
            adapter.setData(result.getLancamentos(),
                    catsMap != null ? catsMap : new java.util.LinkedHashMap<>(),
                    new java.util.LinkedHashMap<>());
        });

        viewModel.getCategoriasMap().observe(getViewLifecycleOwner(), catsMap -> {
            com.financeiramente.core.usecase.fatura.FaturaDetalheResult result =
                    viewModel.getDetalhe().getValue();
            if (result != null && catsMap != null) {
                adapter.setData(result.getLancamentos(), catsMap, new java.util.LinkedHashMap<>());
            }
        });

        viewModel.getTotalFatura().observe(getViewLifecycleOwner(), total -> {
            tvTotal.setText(String.format(Locale.getDefault(), "R$ %.2f", total));
        });

        viewModel.getResultadoPagamento().observe(getViewLifecycleOwner(), result -> {
            if (result != null) {
                String msg;
                if (result.getSaldoDevedor().compareTo(BigDecimal.ZERO) > 0) {
                    msg = getString(R.string.fatura_paga_parcial,
                            String.format(Locale.getDefault(), "R$ %.2f", result.getSaldoDevedor().doubleValue()));
                } else {
                    msg = getString(R.string.fatura_paga_total);
                }
                Snackbar.make(requireView(), msg, Snackbar.LENGTH_LONG).show();
            }
        });

        viewModel.getErro().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) Snackbar.make(view, msg, Snackbar.LENGTH_LONG).show();
        });
    }

    private void abrirPagarFatura() {
        Double total = viewModel.getTotalFatura().getValue();
        Bundle args = new Bundle();
        args.putString("faturaId", faturaId);
        args.putDouble("totalSugerido", total != null ? total : 0.0);
        PagarFaturaBottomSheet sheet = new PagarFaturaBottomSheet();
        sheet.setArguments(args);
        sheet.setOnPagoListener((valorFatura, valorPago) ->
                viewModel.pagar(valorFatura, valorPago));
        sheet.show(getChildFragmentManager(), "pagar_fatura");
    }

    private void applyStatusBadge(Context ctx, StatusFatura status) {
        String label;
        int bgColor;
        int textColor = Color.WHITE;
        switch (status) {
            case ABERTO:
                label = ctx.getString(R.string.fatura_status_aberto);
                bgColor = ctx.getColor(R.color.azul_primary);
                break;
            case FECHADO:
                label = ctx.getString(R.string.fatura_status_fechado);
                bgColor = ctx.getColor(R.color.laranja_warning);
                break;
            case PAGO:
                label = ctx.getString(R.string.fatura_status_pago);
                bgColor = ctx.getColor(R.color.verde_success);
                break;
            case PAGO_PARCIAL:
                label = ctx.getString(R.string.fatura_status_pago_parcial);
                bgColor = ctx.getColor(R.color.amarelo_warning);
                textColor = ctx.getColor(R.color.cinza_on_surface);
                break;
            default:
                label = status.name();
                bgColor = ctx.getColor(R.color.cinza_secondary);
        }
        tvStatusBadge.setText(label);
        tvStatusBadge.setTextColor(textColor);
        tvStatusBadge.getBackground().setTint(bgColor);
    }

    private boolean mostrarOpcoesExclusaoLancamento(Lancamento lancamento) {
        if (lancamento == null) {
            return false;
        }

        if (lancamento.getCompraCartaoId() == null || lancamento.getCompraCartaoId().trim().isEmpty()) {
            confirmarExclusaoApenasLancamento(lancamento);
            return true;
        }

        CharSequence[] opcoes = new CharSequence[] {
                getString(R.string.excluir_esta_parcela),
                getString(R.string.excluir_todas_parcelas)
        };

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.excluir_compra_cartao_titulo)
                .setMessage(R.string.excluir_compra_cartao_mensagem)
                .setItems(opcoes, (dialog, which) -> {
                    if (which == 0) {
                        confirmarExclusaoApenasLancamento(lancamento);
                    } else if (which == 1) {
                        confirmarExclusaoCompraInteira(lancamento.getCompraCartaoId());
                    }
                })
                .show();
        return true;
    }

    private void confirmarExclusaoApenasLancamento(Lancamento lancamento) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.excluir_lancamento)
                .setMessage(getString(R.string.confirmar_exclusao_lancamento, lancamento.getDescricao()))
                .setPositiveButton(R.string.excluir,
                        (dialog, which) -> excluirLancamento(lancamento.getId()))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void confirmarExclusaoCompraInteira(String compraCartaoId) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.excluir_compra_cartao_titulo)
                .setMessage(R.string.excluir_compra_cartao_mensagem)
                .setPositiveButton(R.string.excluir,
                        (dialog, which) -> excluirCompraCartao(compraCartaoId))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void excluirLancamento(String lancamentoId) {
        new Thread(() -> {
            try {
                AppContext.get(requireContext()).getDeletarLancamentoUseCase().executar(lancamentoId);
                if (getActivity() == null) {
                    return;
                }
                getActivity().runOnUiThread(() -> {
                    if (isAdded()) {
                        viewModel.carregar();
                        Snackbar.make(requireView(), R.string.lancamento_excluido, Snackbar.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                if (getActivity() == null) {
                    return;
                }
                getActivity().runOnUiThread(() -> {
                    if (isAdded()) {
                        Snackbar.make(requireView(), e.getMessage(), Snackbar.LENGTH_LONG).show();
                    }
                });
            }
        }).start();
    }

    private void excluirCompraCartao(String compraCartaoId) {
        new Thread(() -> {
            try {
                AppContext.get(requireContext()).getDeletarCompraCartaoUseCase().executar(compraCartaoId);
                if (getActivity() == null) {
                    return;
                }
                getActivity().runOnUiThread(() -> {
                    if (isAdded()) {
                        viewModel.carregar();
                        Snackbar.make(requireView(), R.string.compra_cartao_excluida, Snackbar.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                if (getActivity() == null) {
                    return;
                }
                getActivity().runOnUiThread(() -> {
                    if (isAdded()) {
                        Snackbar.make(requireView(), e.getMessage(), Snackbar.LENGTH_LONG).show();
                    }
                });
            }
        }).start();
    }
}
