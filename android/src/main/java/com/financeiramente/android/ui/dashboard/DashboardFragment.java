package com.financeiramente.android.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.animation.ValueAnimator;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import android.widget.TextView;
import android.graphics.drawable.GradientDrawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.financeiramente.android.R;
import com.financeiramente.android.app.AppContext;
import com.financeiramente.android.viewmodel.DashboardViewModel;
import com.financeiramente.android.viewmodel.DashboardViewModelFactory;
import com.financeiramente.core.usecase.SaldoCategoria;
import com.financeiramente.core.usecase.SaldoMensalResult;
import com.financeiramente.core.domain.vo.TipoCategoria;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class DashboardFragment extends Fragment {

    private DashboardViewModel viewModel;

    private TextView tvMesAtual;
    private TextView tvSaldoReal;
    private TextView tvSaldoDisponivel;
    private TextView tvGastoTotal;
    private TextView tvReceitaTotal;
    private LinearProgressIndicator pbGlobal;

    private MaterialCardView cardAvisoDashboard;
    private TextView tvAvisoEssenciais;
    private TextView tvAvisoMetas;
    private View viewAvisoEssenciaisStatus;
    private View viewAvisoMetasStatus;

    private boolean saldoOculto = false;
    private double ultimoSaldo = 0;
    private double ultimoSaldoReal = 0;
    private double ultimoPercentualMetas = 0.0;

    private enum NivelAviso {
        VERDE,
        AMARELO,
        VERMELHO,
        PRETO,
        CINZA
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Vincular views do hero card
        tvMesAtual        = view.findViewById(R.id.tv_mes_atual);
        tvSaldoReal       = view.findViewById(R.id.tv_saldo_real);
        tvSaldoDisponivel = view.findViewById(R.id.tv_saldo_disponivel);
        tvGastoTotal      = view.findViewById(R.id.tv_gasto_total);
        tvReceitaTotal    = view.findViewById(R.id.tv_receita_total);
        pbGlobal          = view.findViewById(R.id.pb_global);

        cardAvisoDashboard = view.findViewById(R.id.card_aviso_dashboard);
        tvAvisoEssenciais = view.findViewById(R.id.tv_aviso_essenciais);
        tvAvisoMetas = view.findViewById(R.id.tv_aviso_metas);
        viewAvisoEssenciaisStatus = view.findViewById(R.id.view_aviso_essenciais_status);
        viewAvisoMetasStatus = view.findViewById(R.id.view_aviso_metas_status);

        // ViewModel
        AppContext ctx = AppContext.get(requireContext());
        DashboardViewModelFactory factory = new DashboardViewModelFactory(
                ctx.getCalcularSaldoMensalUseCase(),
            ctx.getDeletarLancamentoUseCase(),
            ctx.getMetaRepository(),
            ctx.getAporteMetaRepository());
        viewModel = new ViewModelProvider(this, factory).get(DashboardViewModel.class);

        viewModel.getSaldoMensal().observe(getViewLifecycleOwner(), resultado -> {
            atualizarUI(resultado);
        });
        viewModel.getPercentualMetas().observe(getViewLifecycleOwner(), percentual -> {
            ultimoPercentualMetas = percentual != null ? percentual : 0.0;
            SaldoMensalResult atual = viewModel.getSaldoMensal().getValue();
            if (atual != null) {
                atualizarAvisos(atual);
            }
        });
        // Botão olho — ocultar/mostrar saldo
        view.findViewById(R.id.btn_toggle_saldo).setOnClickListener(v -> {
            saldoOculto = !saldoOculto;
            if (saldoOculto) {
                tvSaldoReal.setText("R$ ••••••");
                tvSaldoDisponivel.setText("R$ ••••••");
            } else {
                tvSaldoReal.setText(
                        String.format(Locale.getDefault(), "R$ %.2f", ultimoSaldoReal));
                tvSaldoDisponivel.setText(
                        String.format(Locale.getDefault(), "R$ %.2f", ultimoSaldo));
            }
        });
            view.findViewById(R.id.btn_registrar_lancamento).setOnClickListener(v ->
                Navigation.findNavController(view)
                    .navigate(R.id.action_dashboardFragment_to_lancamentoFormFragment));
            view.findViewById(R.id.btn_abrir_configuracoes).setOnClickListener(v ->
                Navigation.findNavController(view)
                    .navigate(R.id.nav_configuracoes));

        // Snackbar "Lançamento salvo" com ação Desfazer
        getParentFragmentManager().setFragmentResultListener(
                "lancamento_salvo", getViewLifecycleOwner(),
                (requestKey, bundle) -> {
                    String lancId = bundle.getString("lancamentoId");
                    Snackbar snackbar = Snackbar.make(view,
                            R.string.lancamento_salvo, Snackbar.LENGTH_LONG);
                    if (lancId != null) {
                        snackbar.setAction(R.string.desfazer, v ->
                                viewModel.deletarLancamento(lancId));
                    }
                    snackbar.show();
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.carregarDashboard();
        }
    }

    private void atualizarUI(SaldoMensalResult resultado) {
        // MÃªs atual
        Month month = Month.of(viewModel.getMes());
        String mesNome = month.getDisplayName(TextStyle.FULL, new Locale("pt", "BR"));
        String mesCapitalized = mesNome.substring(0, 1).toUpperCase(new Locale("pt", "BR"))
                + mesNome.substring(1);
        tvMesAtual.setText(mesCapitalized + " " + viewModel.getAno());

        // Saldo disponível — respeitar estado oculto; animar com ValueAnimator
        ultimoSaldo = resultado.getSaldoDisponivel();
        if (!saldoOculto) {
            ValueAnimator animator = ValueAnimator.ofFloat(0f, (float) ultimoSaldo);
            animator.setDuration(600);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.addUpdateListener(anim -> {
                float animatedValue = (float) anim.getAnimatedValue();
                tvSaldoDisponivel.setText(
                        String.format(Locale.getDefault(), "R$ %.2f", animatedValue));
            });
            animator.start();
        }

        // Totais no hero
        tvGastoTotal.setText(
                String.format(Locale.getDefault(), "Gasto: R$ %.2f", resultado.getTotalGasto()));
        tvReceitaTotal.setText(
                String.format(Locale.getDefault(), "Receita: R$ %.2f", resultado.getReceitaRealizada()));

        // Barra de progresso global
        double receita = resultado.getReceitaRealizada();
        int progressoGlobal = receita > 0
                ? (int) Math.min(100.0, (resultado.getTotalGasto() / receita) * 100.0)
                : 0;
        pbGlobal.setProgress(progressoGlobal);

        // Saldo real usa limite + excedente para mostrar um cenário mais conservador.
        List<SaldoCategoria> saldos = resultado.getSaldosPorCategoria();
        double totalLimitesCategorias = saldos.stream()
            .filter(s -> s.getLimite() > 0)
            .mapToDouble(SaldoCategoria::getLimite)
            .sum();

        double excedenteLimites = saldos.stream()
            .filter(s -> s.getLimite() > 0)
            .mapToDouble(s -> Math.max(0.0, s.getGastoRealizado() - s.getLimite()))
            .sum();

        double totalRecebido = resultado.getReceitaRealizada();
        double saldoAposDescontoLimites = totalRecebido - totalLimitesCategorias - excedenteLimites;
        ultimoSaldoReal = saldoAposDescontoLimites;

        if (!saldoOculto) {
            ValueAnimator saldoRealAnimator = ValueAnimator.ofFloat(0f, (float) ultimoSaldoReal);
            saldoRealAnimator.setDuration(700);
            saldoRealAnimator.setInterpolator(new DecelerateInterpolator());
            saldoRealAnimator.addUpdateListener(anim -> {
                float animatedValue = (float) anim.getAnimatedValue();
                tvSaldoReal.setText(
                        String.format(Locale.getDefault(), "R$ %.2f", animatedValue));
            });
            saldoRealAnimator.start();
        }

        atualizarAvisos(resultado);
    }

    private void atualizarAvisos(SaldoMensalResult resultado) {
        List<SaldoCategoria> saldos = resultado.getSaldosPorCategoria();
        List<SaldoCategoria> essenciaisComLimite = saldos.stream()
                .filter(s -> s.getTipoCategoria() == TipoCategoria.ESSENCIAL)
                .filter(s -> s.getLimite() > 0)
                .collect(Collectors.toList());

        String statusEssenciais;
        NivelAviso nivelEssenciais;

        if (essenciaisComLimite.isEmpty()) {
            nivelEssenciais = NivelAviso.CINZA;
            statusEssenciais = "Essenciais sem limite planejado";
        } else {
            double receitaEssenciais = resultado.getReceitaRealizada();
            double totalLimitesEssenciais = essenciaisComLimite.stream()
                    .mapToDouble(SaldoCategoria::getLimite)
                    .sum();
            double percentualEssenciais = receitaEssenciais > 0
                    ? (totalLimitesEssenciais / receitaEssenciais) * 100.0
                    : 999.0;

            if (percentualEssenciais <= 50.0) {
                nivelEssenciais = NivelAviso.VERDE;
            } else if (percentualEssenciais <= 75.0) {
                nivelEssenciais = NivelAviso.AMARELO;
            } else if (percentualEssenciais <= 100.0) {
                nivelEssenciais = NivelAviso.VERMELHO;
            } else {
                nivelEssenciais = NivelAviso.PRETO;
            }
            statusEssenciais = "Essenciais: " + formatarPercentual(percentualEssenciais) + " da receita";
        }

        String statusMetas = "Metas: " + formatarPercentual(ultimoPercentualMetas) + " concluído";
        NivelAviso nivelMetas;
        if (ultimoPercentualMetas == 0.0) {
            nivelMetas = NivelAviso.PRETO;
        } else if (ultimoPercentualMetas < 10.0) {
            nivelMetas = NivelAviso.VERMELHO;
        } else if (ultimoPercentualMetas < 20.0) {
            nivelMetas = NivelAviso.AMARELO;
        } else {
            nivelMetas = NivelAviso.VERDE;
        }

        cardAvisoDashboard.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.cinza_surface));
        cardAvisoDashboard.setStrokeColor(ContextCompat.getColor(requireContext(), R.color.cinza_divider));
        tvAvisoEssenciais.setTextColor(ContextCompat.getColor(requireContext(), R.color.cinza_secondary));
        tvAvisoMetas.setTextColor(ContextCompat.getColor(requireContext(), R.color.cinza_secondary));

        tvAvisoEssenciais.setText(statusEssenciais);
        tvAvisoMetas.setText(statusMetas);
        aplicarCorDot(viewAvisoEssenciaisStatus, nivelEssenciais);
        aplicarCorDot(viewAvisoMetasStatus, nivelMetas);
    }

    private void aplicarCorDot(View dotView, NivelAviso nivel) {
        int dotColor;

        switch (nivel) {
            case VERDE:
                dotColor = ContextCompat.getColor(requireContext(), R.color.status_ok);
                break;
            case AMARELO:
                dotColor = ContextCompat.getColor(requireContext(), R.color.status_warning);
                break;
            case VERMELHO:
                dotColor = ContextCompat.getColor(requireContext(), R.color.status_danger);
                break;
            case PRETO:
                dotColor = ContextCompat.getColor(requireContext(), R.color.black);
                break;
            case CINZA:
            default:
                dotColor = ContextCompat.getColor(requireContext(), R.color.cinza_secondary);
                break;
        }

        if (dotView.getBackground() instanceof GradientDrawable) {
            ((GradientDrawable) dotView.getBackground().mutate()).setColor(dotColor);
        }
    }

    private String formatarPercentual(double valor) {
        return String.format(Locale.getDefault(), "%.0f%%", valor);
    }
}
