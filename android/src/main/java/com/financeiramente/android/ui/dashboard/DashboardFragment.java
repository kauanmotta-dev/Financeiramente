package com.financeiramente.android.ui.dashboard;

import android.animation.ValueAnimator;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import com.financeiramente.core.usecase.saldo.SaldoDashboardResult;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

public class DashboardFragment extends Fragment {

    private DashboardViewModel viewModel;

    private TextView tvMesAtual;
    private TextView tvSaldoConta;
    private TextView tvSaldoEssenciais;
    private TextView tvSaldoNaoEssenciais;
    private TextView tvGastoConta;
    private TextView tvReceitaConta;
    private LinearProgressIndicator pbSaldoConta;
    private LinearLayout layoutCartoesLimites;
    private ImageButton btnToggleSaldo;
    private View contentDashboard;
    private ShimmerFrameLayout shimmerDashboard;

    private MaterialCardView cardAvisoDashboard;
    private TextView tvAvisoEssenciais;
    private TextView tvAvisoMetas;
    private View viewAvisoEssenciaisStatus;
    private View viewAvisoMetasStatus;

    private boolean saldoOculto = false;
    private double ultimoSaldoConta = 0.0;
    private double ultimoSaldoEssenciais = 0.0;
    private double ultimoSaldoNaoEssenciais = 0.0;
    private double ultimoPercentualMetas = 0.0;
    private boolean carregamentoInicialConcluido = false;

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

        tvMesAtual = view.findViewById(R.id.tv_mes_atual);
        tvSaldoConta = view.findViewById(R.id.tv_saldo_conta);
        tvSaldoEssenciais = view.findViewById(R.id.tv_saldo_essenciais);
        tvSaldoNaoEssenciais = view.findViewById(R.id.tv_saldo_nao_essenciais);
        tvGastoConta = view.findViewById(R.id.tv_gasto_conta);
        tvReceitaConta = view.findViewById(R.id.tv_receita_conta);
        pbSaldoConta = view.findViewById(R.id.pb_saldo_conta);
        layoutCartoesLimites = view.findViewById(R.id.layout_cartoes_limites);
        btnToggleSaldo = view.findViewById(R.id.btn_toggle_saldo);
        contentDashboard = view.findViewById(R.id.content_dashboard);
        shimmerDashboard = view.findViewById(R.id.shimmer_dashboard);
        mostrarLoadingInicial(true);

        cardAvisoDashboard = view.findViewById(R.id.card_aviso_dashboard);
        tvAvisoEssenciais = view.findViewById(R.id.tv_aviso_essenciais);
        tvAvisoMetas = view.findViewById(R.id.tv_aviso_metas);
        viewAvisoEssenciaisStatus = view.findViewById(R.id.view_aviso_essenciais_status);
        viewAvisoMetasStatus = view.findViewById(R.id.view_aviso_metas_status);

        AppContext ctx = AppContext.get(requireContext());
        DashboardViewModelFactory factory = new DashboardViewModelFactory(
                ctx.getCalcularSaldoDashboardUseCase(),
                ctx.getDeletarLancamentoUseCase(),
            ctx.getCalcularTotalAportesMesUseCase(),
            ctx.getCoreServices().getCartaoRepository(),
            ctx.getCoreServices().getFaturaRepository());
        viewModel = new ViewModelProvider(this, factory).get(DashboardViewModel.class);

        viewModel.getSaldoDashboard().observe(getViewLifecycleOwner(), resultado -> {
            atualizarTopo(resultado);
            atualizarAvisos(resultado);
            carregamentoInicialConcluido = true;
            mostrarLoadingInicial(false);
        });
        viewModel.getCartoesLimite().observe(getViewLifecycleOwner(), this::renderizarCartoesLimite);
        viewModel.getPercentualMetas().observe(getViewLifecycleOwner(), percentual -> {
            ultimoPercentualMetas = percentual != null ? percentual : 0.0;
            SaldoDashboardResult atual = viewModel.getSaldoDashboard().getValue();
            if (atual != null) {
                atualizarAvisos(atual);
            }
        });

        btnToggleSaldo.setOnClickListener(v -> {
            saldoOculto = !saldoOculto;
            atualizarTextosSaldo();
        });

        view.findViewById(R.id.btn_registrar_lancamento).setOnClickListener(v ->
                Navigation.findNavController(view)
                        .navigate(R.id.action_dashboardFragment_to_lancamentoFormFragment));
        view.findViewById(R.id.btn_abrir_configuracoes).setOnClickListener(v ->
                Navigation.findNavController(view)
                        .navigate(R.id.nav_configuracoes));

        getParentFragmentManager().setFragmentResultListener(
                "lancamento_salvo", getViewLifecycleOwner(),
                (requestKey, bundle) -> {
                    String lancId = bundle.getString("lancamentoId");
                    Snackbar snackbar = Snackbar.make(view,
                            R.string.lancamento_salvo, Snackbar.LENGTH_LONG);
                    if (lancId != null) {
                        snackbar.setAction(R.string.desfazer,
                                v -> confirmarExclusaoLancamento(lancId));
                    }
                    snackbar.show();
                viewModel.carregarDashboard();
                });

        getParentFragmentManager().setFragmentResultListener(
            "cartao_form_saved", getViewLifecycleOwner(),
            (requestKey, bundle) -> viewModel.carregarDashboard());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            if (!carregamentoInicialConcluido) {
                mostrarLoadingInicial(true);
            }
            viewModel.carregarDashboard();
        }
    }

    private void mostrarLoadingInicial(boolean loading) {
        if (contentDashboard != null) {
            contentDashboard.setVisibility(loading ? View.INVISIBLE : View.VISIBLE);
        }
        if (shimmerDashboard != null) {
            shimmerDashboard.setVisibility(loading ? View.VISIBLE : View.GONE);
            if (loading) {
                shimmerDashboard.startShimmer();
            } else {
                shimmerDashboard.stopShimmer();
            }
        }
    }

    private void atualizarTopo(SaldoDashboardResult resultado) {
        Month month = Month.of(viewModel.getMes());
        String mesNome = month.getDisplayName(TextStyle.FULL, new Locale("pt", "BR"));
        String mesCapitalized = mesNome.substring(0, 1).toUpperCase(new Locale("pt", "BR"))
                + mesNome.substring(1);
        tvMesAtual.setText(mesCapitalized + " " + viewModel.getAno());

        ultimoSaldoConta = resultado.getSaldoConta();
        ultimoSaldoEssenciais = resultado.getSaldoEssenciais();
        ultimoSaldoNaoEssenciais = resultado.getSaldoNaoEssenciais();

        tvGastoConta.setText(String.format(Locale.getDefault(), "Gasto: R$ %.2f", resultado.getTotalGastoConta()));
        tvReceitaConta.setText(String.format(Locale.getDefault(), "Receita: R$ %.2f", resultado.getTotalReceita()));

        int usoPercentual = calcularPercentualUso(resultado.getTotalReceita(), resultado.getTotalGastoConta());
        aplicarCoresProgresso(pbSaldoConta, usoPercentual);
        pbSaldoConta.setProgressCompat(usoPercentual, true);

        atualizarTextosSaldo();
    }

    private void atualizarTextosSaldo() {
        if (saldoOculto) {
            tvSaldoConta.setText("R$ ••••••");
            tvSaldoEssenciais.setText("R$ ••••••");
            tvSaldoNaoEssenciais.setText("R$ ••••••");
            btnToggleSaldo.setImageResource(R.drawable.ic_visibility_off);
            btnToggleSaldo.setContentDescription(getString(R.string.dashboard_mostrar_saldo));
            return;
        }

        animarTexto(tvSaldoConta, ultimoSaldoConta);
        animarTexto(tvSaldoEssenciais, ultimoSaldoEssenciais);
        animarTexto(tvSaldoNaoEssenciais, ultimoSaldoNaoEssenciais);
        aplicarCorSaldo(tvSaldoConta, ultimoSaldoConta);
        aplicarCorSaldo(tvSaldoEssenciais, ultimoSaldoEssenciais);
        aplicarCorSaldo(tvSaldoNaoEssenciais, ultimoSaldoNaoEssenciais);
        btnToggleSaldo.setImageResource(R.drawable.ic_visibility);
        btnToggleSaldo.setContentDescription(getString(R.string.dashboard_ocultar_saldo));
    }

    private void animarTexto(TextView textView, double valor) {
        ValueAnimator animator = ValueAnimator.ofFloat(0f, (float) valor);
        animator.setDuration(600);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(anim -> {
            float animatedValue = (float) anim.getAnimatedValue();
            textView.setText(String.format(Locale.getDefault(), "R$ %.2f", animatedValue));
        });
        animator.start();
    }

    private void atualizarAvisos(SaldoDashboardResult resultado) {
        double limiteEssenciais = resultado.getLimiteEssenciais();

        String statusEssenciais;
        NivelAviso nivelEssenciais;

        if (limiteEssenciais <= 0) {
            nivelEssenciais = NivelAviso.CINZA;
            statusEssenciais = "Essenciais sem limite planejado";
        } else {
            double receita = resultado.getTotalReceita();
            double percentualEssenciais = receita > 0
                    ? (limiteEssenciais / receita) * 100.0
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

        String statusMetas = "Metas: " + formatarPercentual(ultimoPercentualMetas) + " da receita";
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

        cardAvisoDashboard.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.dashboard_aviso_card_bg));
        cardAvisoDashboard.setStrokeColor(ContextCompat.getColor(requireContext(), R.color.dashboard_aviso_card_stroke));
        tvAvisoEssenciais.setTextColor(ContextCompat.getColor(requireContext(), R.color.dashboard_aviso_text));
        tvAvisoMetas.setTextColor(ContextCompat.getColor(requireContext(), R.color.dashboard_aviso_text));

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
                dotColor = ContextCompat.getColor(requireContext(), R.color.dashboard_aviso_dot_preto);
                break;
            case CINZA:
            default:
                dotColor = ContextCompat.getColor(requireContext(), R.color.dashboard_aviso_dot_cinza);
                break;
        }

        if (dotView.getBackground() instanceof GradientDrawable) {
            ((GradientDrawable) dotView.getBackground().mutate()).setColor(dotColor);
        }
    }

    private String formatarPercentual(double valor) {
        return String.format(Locale.getDefault(), "%.0f%%", valor);
    }

    private int calcularPercentualUso(double totalBase, double valorUsado) {
        if (totalBase <= 0.0) {
            return valorUsado > 0.0 ? 100 : 0;
        }
        return (int) Math.min(100.0, Math.max(0.0, (valorUsado / totalBase) * 100.0));
    }

    private void aplicarCoresProgresso(LinearProgressIndicator indicador, int usoPercentual) {
        int corIndicador;
        if (usoPercentual < 50) {
            corIndicador = ContextCompat.getColor(requireContext(), R.color.verde_success);
        } else if (usoPercentual <= 75) {
            corIndicador = ContextCompat.getColor(requireContext(), R.color.laranja_warning);
        } else {
            corIndicador = ContextCompat.getColor(requireContext(), R.color.vermelho_error);
        }
        indicador.setIndicatorColor(corIndicador);
        indicador.setTrackColor(ContextCompat.getColor(requireContext(), R.color.verde_success));
    }

    private void aplicarCorSaldo(TextView textView, double valor) {
        int cor = valor >= 0
                ? ContextCompat.getColor(requireContext(), R.color.verde_success)
                : ContextCompat.getColor(requireContext(), R.color.vermelho_error);
        textView.setTextColor(cor);
    }

    private void confirmarExclusaoLancamento(String lancamentoId) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.excluir_lancamento)
                .setMessage(R.string.confirmar_exclusao_lancamento_generico)
                .setPositiveButton(R.string.excluir,
                        (dialog, which) -> viewModel.deletarLancamento(lancamentoId))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void renderizarCartoesLimite(java.util.List<DashboardViewModel.CartaoLimiteResumo> cartoes) {
        layoutCartoesLimites.removeAllViews();

        if (cartoes == null || cartoes.isEmpty()) {
            TextView vazio = new TextView(requireContext());
            vazio.setText(R.string.dashboard_cartoes_limites_vazio);
            vazio.setTextColor(ContextCompat.getColor(requireContext(), R.color.cinza_secondary));
            vazio.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
            layoutCartoesLimites.addView(vazio);
            return;
        }

        for (DashboardViewModel.CartaoLimiteResumo cartao : cartoes) {
            MaterialCardView card = new MaterialCardView(requireContext());
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(dp(172), ViewGroup.LayoutParams.WRAP_CONTENT);
            cardParams.setMarginEnd(dp(10));
            card.setLayoutParams(cardParams);
            card.setCardElevation(0f);
            card.setRadius(dp(10));
            card.setStrokeWidth(dp(1));
            card.setStrokeColor(ContextCompat.getColor(requireContext(), R.color.cinza_divider));
            card.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.cinza_surface));
            card.setClickable(true);
            card.setFocusable(true);
            card.setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putInt("sourceScreen", R.id.nav_dashboard);
                Navigation.findNavController(requireView()).navigate(R.id.nav_cartoes, args);
            });

            LinearLayout conteudo = new LinearLayout(requireContext());
            conteudo.setOrientation(LinearLayout.VERTICAL);
            conteudo.setPadding(dp(12), dp(10), dp(12), dp(10));

            TextView tvNome = new TextView(requireContext());
            tvNome.setText(cartao.getNome());
            tvNome.setTextColor(ContextCompat.getColor(requireContext(), R.color.cinza_on_surface));
            tvNome.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
            tvNome.setMaxLines(1);

            TextView tvResumo = new TextView(requireContext());
            String usado = String.format(Locale.getDefault(), "R$ %.2f", cartao.getUtilizado());
            String limite = String.format(Locale.getDefault(), "R$ %.2f", cartao.getLimite());
            tvResumo.setText(getString(R.string.dashboard_cartoes_limites_resumo, usado, limite));
            tvResumo.setTextColor(ContextCompat.getColor(requireContext(), R.color.cinza_secondary));
            tvResumo.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);

            LinearProgressIndicator pbUso = new LinearProgressIndicator(requireContext());
            pbUso.setTrackThickness(dp(6));
            pbUso.setTrackCornerRadius(dp(3));
            pbUso.setTrackColor(ContextCompat.getColor(requireContext(), R.color.cinza_divider));
            int pctUso = (int) Math.min(100.0, Math.max(0.0, cartao.getPercentualUso()));
            aplicarCoresProgresso(pbUso, pctUso);
            pbUso.setProgress(pctUso);
            LinearLayout.LayoutParams pbParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            pbParams.topMargin = dp(8);
            pbUso.setLayoutParams(pbParams);

            TextView tvPercentual = new TextView(requireContext());
            tvPercentual.setText(String.format(Locale.getDefault(), "%.0f%% usado", cartao.getPercentualUso()));
            int corPercentual;
            if (pctUso < 50) {
                corPercentual = ContextCompat.getColor(requireContext(), R.color.verde_success);
            } else if (pctUso <= 75) {
                corPercentual = ContextCompat.getColor(requireContext(), R.color.laranja_warning);
            } else {
                corPercentual = ContextCompat.getColor(requireContext(), R.color.vermelho_error);
            }
            tvPercentual.setTextColor(corPercentual);
            tvPercentual.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
            LinearLayout.LayoutParams percentualParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            percentualParams.topMargin = dp(4);
            tvPercentual.setLayoutParams(percentualParams);

            conteudo.addView(tvNome);
            conteudo.addView(tvResumo);
            conteudo.addView(pbUso);
            conteudo.addView(tvPercentual);
            card.addView(conteudo);

            layoutCartoesLimites.addView(card);
        }
    }

    private int dp(int value) {
        float density = requireContext().getResources().getDisplayMetrics().density;
        return (int) (value * density);
    }
}
