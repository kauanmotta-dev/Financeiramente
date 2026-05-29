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
import java.time.YearMonth;

public class FaturaDetalheFragment extends Fragment {

    private FaturaDetalheViewModel viewModel;
    private LancamentoAdapter adapter;

    private TextView tvTitulo;
    private TextView tvTotal;
    private TextView tvStatusBadge;
    private TextView tvFechamento;
    private TextView tvVencimento;
    private MaterialButton btnPagar;
    private MaterialButton btnEditarFatura;
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
                ctx.getAtualizarStatusFaturasUseCase(),
                ctx.getAlterarStatusFaturaUseCase(),
                ctx.getAnteciparLancamentosUseCase());
        viewModel = new ViewModelProvider(this, factory).get(FaturaDetalheViewModel.class);

        tvTitulo = view.findViewById(R.id.tv_fatura_detalhe_titulo);
        tvTotal = view.findViewById(R.id.tv_detalhe_total);
        tvStatusBadge = view.findViewById(R.id.tv_detalhe_status_badge);
        tvFechamento = view.findViewById(R.id.tv_detalhe_fechamento);
        tvVencimento = view.findViewById(R.id.tv_detalhe_vencimento);
        btnPagar = view.findViewById(R.id.btn_pagar_fatura);
        btnEditarFatura = view.findViewById(R.id.btn_editar_fatura);

        view.findViewById(R.id.btn_voltar_fatura_detalhe).setOnClickListener(
                v -> Navigation.findNavController(view).navigateUp());

        // Listener set dynamically in observarViewModel based on fatura status

        adapter = new LancamentoAdapter(
            this::mostrarAcoesLancamento,
            this::mostrarAcoesLancamento);
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

            YearMonth mesFatura = null;
            try {
                mesFatura = YearMonth.parse(fatura.getMes());
            } catch (Exception ignored) { }

            YearMonth mesAtual = YearMonth.now();
            boolean isFuturo = mesFatura != null && mesFatura.isAfter(mesAtual);
            boolean isFaturaAtual = Boolean.TRUE.equals(viewModel.getEhFaturaAtual().getValue());

            boolean podePagar = !isFuturo && (fatura.getStatus() == StatusFatura.ABERTO
                    || fatura.getStatus() == StatusFatura.FECHADO);
            boolean podeReverter = fatura.getStatus() == StatusFatura.PAGO
                    || fatura.getStatus() == StatusFatura.PAGO_PARCIAL;

            if (isFaturaAtual && fatura.getStatus() == StatusFatura.ABERTO) {
                btnPagar.setVisibility(View.VISIBLE);
                btnPagar.setEnabled(true);
                btnPagar.setText(R.string.fatura_pagar);
                btnPagar.setOnClickListener(v -> abrirPagarFatura());
                btnEditarFatura.setVisibility(View.VISIBLE);
                btnEditarFatura.setText(R.string.fatura_fechar);
                btnEditarFatura.setOnClickListener(v -> confirmarAlteracaoStatus(StatusFatura.FECHADO));
            } else if (isFaturaAtual && fatura.getStatus() == StatusFatura.FECHADO) {
                btnPagar.setVisibility(View.VISIBLE);
                btnPagar.setEnabled(true);
                btnPagar.setText(R.string.fatura_pagar);
                btnPagar.setOnClickListener(v -> abrirPagarFatura());
                btnEditarFatura.setVisibility(View.VISIBLE);
                btnEditarFatura.setText(R.string.fatura_abrir);
                btnEditarFatura.setOnClickListener(v -> confirmarAlteracaoStatus(StatusFatura.ABERTO));
            } else if (isFuturo) {
                btnPagar.setVisibility(View.VISIBLE);
                btnPagar.setEnabled(true);
                btnPagar.setText(R.string.fatura_antecipar_lancamentos);
                btnPagar.setOnClickListener(v -> confirmarAntecipar());
                btnEditarFatura.setVisibility(View.VISIBLE);
                btnEditarFatura.setText(R.string.fatura_alterar_status);
                btnEditarFatura.setOnClickListener(v -> mostrarOpcoesAlterarStatus(fatura));
            } else if (podePagar) {
                btnPagar.setVisibility(View.VISIBLE);
                btnPagar.setEnabled(true);
                btnPagar.setText(R.string.fatura_pagar);
                btnPagar.setOnClickListener(v -> abrirPagarFatura());
                btnEditarFatura.setVisibility(View.VISIBLE);
                btnEditarFatura.setText(R.string.fatura_alterar_status);
                btnEditarFatura.setOnClickListener(v -> mostrarOpcoesAlterarStatus(fatura));
            } else if (podeReverter) {
                btnPagar.setVisibility(View.VISIBLE);
                btnPagar.setEnabled(false);
                btnPagar.setText(fatura.getStatus() == StatusFatura.PAGO
                        ? R.string.fatura_status_pago
                        : R.string.fatura_status_pago_parcial);
                btnPagar.setOnClickListener(null);
                btnEditarFatura.setVisibility(View.VISIBLE);
                btnEditarFatura.setText(R.string.fatura_alterar_status);
                btnEditarFatura.setOnClickListener(v -> mostrarOpcoesAlterarStatus(fatura));
            } else {
                btnPagar.setVisibility(View.GONE);
                btnEditarFatura.setVisibility(View.GONE);
            }

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
                viewModel.limparResultadoPagamento();
            }
        });

        viewModel.getFaturaAntecipadaId().observe(getViewLifecycleOwner(), destFaturaId -> {
            if (destFaturaId == null) return;
            Snackbar.make(requireView(), R.string.fatura_lancamentos_antecipados, Snackbar.LENGTH_SHORT).show();
            viewModel.carregar();
            viewModel.limparEventoFaturaAntecipada();
        });

        viewModel.getFaturaAntecipadaExcluida().observe(getViewLifecycleOwner(), excluida -> {
            if (excluida == null || !excluida) return;
            Snackbar.make(requireView(), R.string.fatura_lancamentos_antecipados, Snackbar.LENGTH_SHORT).show();
            Navigation.findNavController(requireView()).navigateUp();
            viewModel.limparEventoFaturaAntecipadaExcluida();
        });

        viewModel.getErro().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) Snackbar.make(view, msg, Snackbar.LENGTH_LONG).show();
        });

        getParentFragmentManager().setFragmentResultListener(
                "lancamento_salvo", getViewLifecycleOwner(),
                (requestKey, bundle) -> viewModel.carregar());
    }

    private void abrirPagarFatura() {
        Double total = viewModel.getTotalFatura().getValue();
        Bundle args = new Bundle();
        args.putString("faturaId", faturaId);
        args.putDouble("totalSugerido", total != null ? total : 0.0);
        PagarFaturaBottomSheet sheet = new PagarFaturaBottomSheet();
        sheet.setArguments(args);
        sheet.setOnPagoListener((valorFatura, valorPago) ->
                viewModel.alterarStatus(com.financeiramente.core.domain.vo.StatusFatura.PAGO, valorFatura, valorPago));
        sheet.show(getChildFragmentManager(), "pagar_fatura");
    }

    private void mostrarOpcoesAlterarStatus(Fatura fatura) {
        java.util.List<CharSequence> opcoes = new ArrayList<>();
        java.util.List<Runnable> acoes = new ArrayList<>();

        if (fatura.getStatus() == StatusFatura.ABERTO || fatura.getStatus() == StatusFatura.FECHADO) {
            opcoes.add(getString(R.string.fatura_marcar_como_paga));
            acoes.add(this::abrirPagarFatura);

            opcoes.add(getString(R.string.fatura_marcar_como_paga_parcialmente));
            acoes.add(this::abrirPagarFatura);
        }

        if (fatura.getStatus() != StatusFatura.ABERTO) {
            opcoes.add(getString(R.string.fatura_marcar_como_aberta));
            acoes.add(() -> confirmarAlteracaoStatus(StatusFatura.ABERTO));
        }

        if (fatura.getStatus() != StatusFatura.FECHADO) {
            opcoes.add(getString(R.string.fatura_marcar_como_fechada));
            acoes.add(() -> confirmarAlteracaoStatus(StatusFatura.FECHADO));
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.fatura_alterar_status)
                .setItems(opcoes.toArray(new CharSequence[0]), (dialog, which) -> {
                    if (which >= 0 && which < acoes.size()) {
                        acoes.get(which).run();
                    }
                })
                .show();
    }

    private void confirmarAlteracaoStatus(StatusFatura statusDestino) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.fatura_alterar_status)
                .setMessage(R.string.fatura_alterar_status_msg)
                .setPositiveButton(R.string.confirmar, (d, w) ->
                        viewModel.alterarStatus(statusDestino, 0, 0))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void confirmarAntecipar() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.fatura_antecipar_lancamentos)
                .setMessage(R.string.fatura_antecipar_msg)
                .setPositiveButton(R.string.confirmar, (d, w) -> viewModel.anteciparLancamentos())
                .setNegativeButton(android.R.string.cancel, null)
                .show();
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

    private boolean mostrarAcoesLancamento(Lancamento lancamento) {
        if (lancamento == null) {
            return false;
        }

        final boolean podeAdiantar = podeAnteciparLancamento();
        if (!podeAdiantar) {
            Snackbar.make(requireView(), R.string.fatura_lancamento_sem_edicao, Snackbar.LENGTH_LONG).show();
            return true;
        }

        CharSequence[] opcoes = new CharSequence[] {
                getString(R.string.fatura_acao_adiantar_lancamento)
        };

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.fatura_acao_titulo)
                .setItems(opcoes, (dialog, which) -> {
                    if (which == 0) {
                        confirmarAnteciparLancamento(lancamento);
                    }
                })
                .show();
        return true;
    }

    private void confirmarAnteciparLancamento(Lancamento lancamento) {
        if (!podeAnteciparLancamento()) {
            Snackbar.make(requireView(), R.string.fatura_acao_adiantar_indisponivel, Snackbar.LENGTH_LONG).show();
            return;
        }
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.fatura_acao_adiantar_lancamento)
                .setMessage(R.string.fatura_acao_adiantar_lancamento_msg)
                .setPositiveButton(R.string.confirmar, (d, w) -> viewModel.anteciparLancamento(lancamento.getId()))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private boolean podeAnteciparLancamento() {
        FaturaDetalheResult result = viewModel.getDetalhe().getValue();
        if (result == null || result.getFatura() == null) {
            return false;
        }
        Fatura fatura = result.getFatura();
        if (fatura.getStatus() == StatusFatura.PAGO || fatura.getStatus() == StatusFatura.PAGO_PARCIAL) {
            return false;
        }
        try {
            return YearMonth.parse(fatura.getMes()).isAfter(YearMonth.now());
        } catch (Exception ignored) {
            return false;
        }
    }

    private void abrirEdicaoLancamento(String lancamentoId) {
        if (!isAdded()) {
            return;
        }
        Bundle args = new Bundle();
        args.putString("lancamentoId", lancamentoId);
        args.putInt("sourceTab", R.id.nav_dashboard);
        Navigation.findNavController(requireView()).navigate(R.id.lancamentoFormFragment, args);
    }

}
