package com.financeiramente.android.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.animation.ValueAnimator;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.financeiramente.android.R;
import com.financeiramente.android.app.AppContext;
import com.financeiramente.android.viewmodel.DashboardViewModel;
import com.financeiramente.android.viewmodel.DashboardViewModelFactory;
import com.financeiramente.core.usecase.SaldoCategoria;
import com.financeiramente.core.usecase.SaldoMensalResult;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class DashboardFragment extends Fragment {

    private static final int[] CAT_COLORS = {
            0xFF6366F1, 0xFF0EA5E9, 0xFFF97316, 0xFF10B981, 0xFF8B5CF6,
            0xFFEC4899, 0xFFF59E0B, 0xFFEF4444, 0xFF64748B, 0xFF14B8A6,
    };

    private DashboardViewModel viewModel;
    private SaldoCategoriaAdapter adapter;
    private PieLegendAdapter pieLegendAdapter;

    private TextView tvMesAtual;
    private TextView tvSaldoDisponivel;
    private TextView tvGastoTotal;
    private TextView tvReceitaTotal;
    private LinearProgressIndicator pbGlobal;
    private boolean saldoOculto = false;
    private double ultimoSaldo = 0;
    private TextView tvMiniDespesas;
    private TextView tvMiniReceitas;
    private TextView tvMiniSaldo;
    private TextView tvMiniUso;
    private PieChart pieChart;
    private TextView tvChartVazio;
    private View cardAlerta;
    private TextView tvAlerta;
    private TextView tvCategoriasVazio;
    private SwipeRefreshLayout swipeRefresh;
    private ExtendedFloatingActionButton fab;

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
        tvSaldoDisponivel = view.findViewById(R.id.tv_saldo_disponivel);
        tvGastoTotal      = view.findViewById(R.id.tv_gasto_total);
        tvReceitaTotal    = view.findViewById(R.id.tv_receita_total);
        pbGlobal          = view.findViewById(R.id.pb_global);

        // Mini-cards
        tvMiniDespesas = view.findViewById(R.id.tv_mini_despesas);
        tvMiniReceitas = view.findViewById(R.id.tv_mini_receitas);
        tvMiniSaldo    = view.findViewById(R.id.tv_mini_saldo);
        tvMiniUso      = view.findViewById(R.id.tv_mini_uso);

        // PieChart
        pieChart     = view.findViewById(R.id.pie_chart);
        tvChartVazio = view.findViewById(R.id.tv_chart_vazio);
        configurarPieChart();

        // RecyclerView da legenda do pie
        RecyclerView rvPieLegenda = view.findViewById(R.id.rv_pie_legenda);
        pieLegendAdapter = new PieLegendAdapter();
        rvPieLegenda.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvPieLegenda.setAdapter(pieLegendAdapter);

        // Alerta + categorias
        cardAlerta        = view.findViewById(R.id.card_alerta);
        tvAlerta          = view.findViewById(R.id.tv_alerta);
        tvCategoriasVazio = view.findViewById(R.id.tv_categorias_vazio);

        // RecyclerView de categorias
        RecyclerView rvCategorias = view.findViewById(R.id.rv_categorias);
        adapter = new SaldoCategoriaAdapter();
        rvCategorias.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvCategorias.setAdapter(adapter);

        // ViewModel
        AppContext ctx = AppContext.get(requireContext());
        DashboardViewModelFactory factory = new DashboardViewModelFactory(
                ctx.getCalcularSaldoMensalUseCase(),
                ctx.getDeletarLancamentoUseCase());
        viewModel = new ViewModelProvider(this, factory).get(DashboardViewModel.class);

        // SwipeRefreshLayout
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        swipeRefresh.setOnRefreshListener(() -> viewModel.carregarDashboard());

        viewModel.getSaldoMensal().observe(getViewLifecycleOwner(), resultado -> {
            swipeRefresh.setRefreshing(false);
            atualizarUI(resultado);
        });
        // Botão olho — ocultar/mostrar saldo
        view.findViewById(R.id.btn_toggle_saldo).setOnClickListener(v -> {
            saldoOculto = !saldoOculto;
            if (saldoOculto) {
                tvSaldoDisponivel.setText("R$ ••••••");
            } else {
                tvSaldoDisponivel.setText(
                        String.format(Locale.getDefault(), "R$ %.2f", ultimoSaldo));
            }
        });
        // FAB estendido: field-level (ícone + texto), colapsa ao scrollar
        fab = view.findViewById(R.id.fab_novo_lancamento);
        fab.setOnClickListener(v ->
                Navigation.findNavController(view)
                        .navigate(R.id.action_dashboardFragment_to_lancamentoFormFragment));

        // FAB colapsa ao scrollar para baixo, expande ao scrollar para cima
        NestedScrollView nestedScrollView = view.findViewById(R.id.nested_scroll);
        nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener)
                (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                    if (scrollY > oldScrollY && scrollY > 0) {
                        fab.shrink();
                    } else if (scrollY <= oldScrollY) {
                        fab.extend();
                    }
                });

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

    private void configurarPieChart() {
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(android.graphics.Color.TRANSPARENT);
        pieChart.setHoleRadius(60f);
        pieChart.setTransparentCircleRadius(65f);
        pieChart.setDrawCenterText(false);
        pieChart.setRotationEnabled(false);
        pieChart.setHighlightPerTapEnabled(false);
        pieChart.getLegend().setEnabled(false);
        pieChart.setDrawEntryLabels(false);
        pieChart.setExtraOffsets(4f, 4f, 4f, 4f);
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

        // Mini-cards
        tvMiniDespesas.setText(
                String.format(Locale.getDefault(), "R$ %.2f", resultado.getTotalGasto()));
        tvMiniReceitas.setText(
                String.format(Locale.getDefault(), "R$ %.2f", resultado.getReceitaRealizada()));
        tvMiniSaldo.setText(
                String.format(Locale.getDefault(), "R$ %.2f", resultado.getSaldoDisponivel()));
        tvMiniUso.setText(String.format(Locale.getDefault(), "%d%%", progressoGlobal));

        // Lista de categorias
        List<SaldoCategoria> saldos = resultado.getSaldosPorCategoria();
        adapter.setItems(saldos);
        tvCategoriasVazio.setVisibility(saldos.isEmpty() ? View.VISIBLE : View.GONE);

        // Pie chart â€” top 5 por gasto
        atualizarPieChart(saldos);

        // Alerta de categorias no limite ou estouradas
        List<SaldoCategoria> emAlerta = saldos.stream()
                .filter(s -> s.getLimite() > 0 && s.getGastoRealizado() / s.getLimite() >= 0.75)
                .collect(Collectors.toList());

        if (emAlerta.isEmpty()) {
            cardAlerta.setVisibility(View.GONE);
        } else {
            cardAlerta.setVisibility(View.VISIBLE);
            String nomes = emAlerta.stream()
                    .map(SaldoCategoria::getCategoriaNome)
                    .collect(Collectors.joining(", "));
            boolean temEstourada = emAlerta.stream()
                    .anyMatch(s -> s.getGastoRealizado() > s.getLimite());
            if (temEstourada) {
                tvAlerta.setText(getString(R.string.dashboard_alerta_estourado, nomes));
            } else {
                tvAlerta.setText(getString(R.string.dashboard_alerta_limite, nomes));
            }
        }
    }

    private void atualizarPieChart(List<SaldoCategoria> saldos) {
        List<SaldoCategoria> comGasto = saldos.stream()
                .filter(s -> s.getGastoRealizado() > 0)
                .sorted(Comparator.comparingDouble(SaldoCategoria::getGastoRealizado).reversed())
                .limit(5)
                .collect(Collectors.toList());

        if (comGasto.isEmpty()) {
            pieChart.setVisibility(View.GONE);
            tvChartVazio.setVisibility(View.VISIBLE);
            pieLegendAdapter.setItems(new ArrayList<>());
            return;
        }

        pieChart.setVisibility(View.VISIBLE);
        tvChartVazio.setVisibility(View.GONE);

        double totalGasto = comGasto.stream()
                .mapToDouble(SaldoCategoria::getGastoRealizado).sum();

        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors  = new ArrayList<>();
        List<PieLegendAdapter.LegendItem> legendItems = new ArrayList<>();

        for (int i = 0; i < comGasto.size(); i++) {
            SaldoCategoria s = comGasto.get(i);
            entries.add(new PieEntry((float) s.getGastoRealizado()));
            int color = CAT_COLORS[Math.abs(s.getCategoriaNome().hashCode()) % CAT_COLORS.length];
            colors.add(color);
            float pct = totalGasto > 0 ? (float) (s.getGastoRealizado() / totalGasto * 100f) : 0f;
            legendItems.add(new PieLegendAdapter.LegendItem(s.getCategoriaNome(), pct, color));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setSliceSpace(2f);
        dataSet.setSelectionShift(4f);
        dataSet.setDrawValues(false);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.animateY(800, Easing.EaseInOutQuad);
        pieChart.invalidate();

        pieLegendAdapter.setItems(legendItems);
    }
}
