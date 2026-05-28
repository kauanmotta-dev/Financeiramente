package com.financeiramente.android.ui.relatorios;

import android.app.DatePickerDialog;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.financeiramente.android.R;
import com.financeiramente.android.app.AppContext;
import com.financeiramente.android.ui.common.CategoriaVisualFallback;
import com.financeiramente.android.viewmodel.RelatoriosViewModel;
import com.financeiramente.android.viewmodel.RelatoriosViewModelFactory;
import com.financeiramente.core.domain.entity.Categoria;
import com.financeiramente.core.domain.entity.Lancamento;
import com.financeiramente.core.domain.entity.Tag;
import com.financeiramente.core.domain.vo.TipoCategoria;
import com.financeiramente.core.domain.vo.TipoLancamento;
import com.financeiramente.core.usecase.relatorio.FiltroRelatorio;
import com.financeiramente.core.usecase.relatorio.RelatorioResult;
import com.financeiramente.core.util.FinanceCalculator;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class RelatoriosFragment extends Fragment {

    private static final int MODO_MES    = 0;
    private static final int MODO_SEMANA = 1;
    private static final int MODO_CUSTOM = 2;
    private static final int MODO_ANO    = 3;

    // Chart slice/bar colors aligned with the design system cat_* tokens
    private static final int[] PIE_COLORS = {
        0xFF6366F1, 0xFF0EA5E9, 0xFFF97316, 0xFF10B981,
        0xFF8B5CF6, 0xFFEC4899, 0xFFF59E0B, 0xFFEF4444,
        0xFF64748B, 0xFF14B8A6
    };

    private RelatoriosViewModel viewModel;
    private ChipGroup chipGroupPeriodo;
    private View layoutDatasCustom;
    private TextInputEditText etDataInicio, etDataFim;

    // filter state — maintained across bottom sheet opens
    private final Set<String> filtrosCategoriasSelecionadas = new LinkedHashSet<>();
    private final Set<String> filtrosTagsSelecionadas = new LinkedHashSet<>();
    private final Set<TipoCategoria> filtrosTiposCategoriaSelecionados = new LinkedHashSet<>();

    private TextView tvTotalReceitas, tvTotalDespesas, tvSaldo, tvResumoPercentual;
    private LinearProgressIndicator pbResumoPercentual;
    private LinearLayout layoutCategorias;

    private PieChart pieChart;
    private HorizontalBarChart horizontalBarChart;
    private BarChart barChart;
    private LineChart lineChart;
    private LinearLayout layoutPieLegenda;
    private MaterialButtonToggleGroup toggleChartTipo;

    private Map<String, BigDecimal> ultimoTotalPorCategoria = null;

    private RelatorioResult ultimoResultadoBruto = null;
    private List<Categoria> listaCategorias = new ArrayList<>();
    private List<Tag> listaTags = new ArrayList<>();
    private Map<String, Categoria> categoriaPorId = new LinkedHashMap<>();
    private Map<String, List<Tag>> tagsPorLancamento = new LinkedHashMap<>();
    private int modoAtual = MODO_MES;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_relatorios, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AppContext ctx = AppContext.get(requireContext());
        RelatoriosViewModelFactory factory = new RelatoriosViewModelFactory(
                ctx.getGerarRelatorioUseCase(),
                ctx.getCoreServices().getCategoriaRepository(),
                ctx.getCoreServices().getTagRepository(),
                ctx.getCoreServices().getLancamentoRepository());
        viewModel = new ViewModelProvider(this, factory).get(RelatoriosViewModel.class);

        bindViews(view);
        MaterialToolbar toolbarRelatorios = view.findViewById(R.id.toolbar_relatorios);
        toolbarRelatorios.setNavigationOnClickListener(v -> {
            if (!Navigation.findNavController(view).navigateUp()) {
                Navigation.findNavController(view).navigate(R.id.nav_dashboard);
            }
        });
        configurarChipsPeriodo();
        configurarBotaoFiltrar(view);
        setupPieChart();
        setupHorizontalBarChart();
        setupBarChart();
        setupLineChart();
        configurarToggleChart();
        observarViewModel();
    }

    private void bindViews(View view) {
        chipGroupPeriodo     = view.findViewById(R.id.chip_group_periodo);
        layoutDatasCustom    = view.findViewById(R.id.layout_datas_custom);
        etDataInicio         = view.findViewById(R.id.et_data_inicio);
        etDataFim            = view.findViewById(R.id.et_data_fim);
        tvTotalReceitas      = view.findViewById(R.id.tv_total_receitas);
        tvTotalDespesas      = view.findViewById(R.id.tv_total_despesas);
        tvSaldo              = view.findViewById(R.id.tv_saldo);
        tvResumoPercentual   = view.findViewById(R.id.tv_resumo_percentual);
        pbResumoPercentual   = view.findViewById(R.id.pb_resumo_percentual);
        layoutCategorias     = view.findViewById(R.id.layout_categorias);
        pieChart             = view.findViewById(R.id.pie_chart);
        horizontalBarChart   = view.findViewById(R.id.horizontal_bar_chart);
        barChart             = view.findViewById(R.id.bar_chart);
        lineChart            = view.findViewById(R.id.line_chart);
        layoutPieLegenda     = view.findViewById(R.id.layout_pie_legenda);
        toggleChartTipo      = view.findViewById(R.id.toggle_chart_tipo);
    }

    private void configurarChipsPeriodo() {
        LocalDate hoje = LocalDate.now();
        etDataInicio.setText(String.format(Locale.US, "%04d-%02d-01",
                hoje.getYear(), hoje.getMonthValue()));
        etDataFim.setText(hoje.toString());
        etDataInicio.setOnClickListener(v -> mostrarDatePicker(etDataInicio));
        etDataFim.setOnClickListener(v -> mostrarDatePicker(etDataFim));

        chipGroupPeriodo.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chip_semana) {
                modoAtual = MODO_SEMANA;
                layoutDatasCustom.setVisibility(View.GONE);
                viewModel.filtrarSemanaAtual();
            } else if (id == R.id.chip_mes) {
                modoAtual = MODO_MES;
                layoutDatasCustom.setVisibility(View.GONE);
                viewModel.filtrarMesAtual();
            } else if (id == R.id.chip_ano) {
                modoAtual = MODO_ANO;
                layoutDatasCustom.setVisibility(View.GONE);
                viewModel.filtrarAnoAtual();
            } else if (id == R.id.chip_personalizado) {
                modoAtual = MODO_CUSTOM;
                layoutDatasCustom.setVisibility(View.VISIBLE);
            }
        });
    }

    private void configurarBotaoFiltrar(View view) {
        view.findViewById(R.id.btn_abrir_filtros).setOnClickListener(v -> mostrarFiltrosSheet());
        // Observe category/tag lists so they're ready when the sheet opens
        viewModel.getCategorias().observe(getViewLifecycleOwner(), cats ->
                listaCategorias = cats != null ? cats : new ArrayList<>());
        viewModel.getTags().observe(getViewLifecycleOwner(), tgs ->
                listaTags = tgs != null ? tgs : new ArrayList<>());
    }

    /** 3.7 — opens the filter Bottom Sheet */
    private void mostrarFiltrosSheet() {
        BottomSheetDialog sheet = new BottomSheetDialog(requireContext());
        View sheetView = LayoutInflater.from(requireContext())
                .inflate(R.layout.bottom_sheet_relatorios_filtros, null);
        sheet.setContentView(sheetView);

        ChipGroup chipGroupTipo = sheetView.findViewById(R.id.chip_group_tipo);
        ChipGroup chipGroupCategorias = sheetView.findViewById(R.id.chip_group_categorias);
        ChipGroup chipGroupTags = sheetView.findViewById(R.id.chip_group_tags);

        ((Chip) sheetView.findViewById(R.id.chip_tipo_todos))
            .setChecked(filtrosTiposCategoriaSelecionados.isEmpty());
        ((Chip) sheetView.findViewById(R.id.chip_tipo_essenciais))
            .setChecked(filtrosTiposCategoriaSelecionados.contains(TipoCategoria.ESSENCIAL));
        ((Chip) sheetView.findViewById(R.id.chip_tipo_nao_essenciais))
            .setChecked(filtrosTiposCategoriaSelecionados.contains(TipoCategoria.NAO_ESSENCIAL));
        ((Chip) sheetView.findViewById(R.id.chip_tipo_receitas))
            .setChecked(filtrosTiposCategoriaSelecionados.contains(TipoCategoria.RECEITA));

        chipGroupCategorias.removeAllViews();
        for (Categoria categoria : categoriasRaizOrdenadas()) {
            Chip chip = new Chip(requireContext());
            chip.setText(CategoriaVisualFallback.icone(categoria, null) + " " + categoria.getNome());
            chip.setTag(categoria.getId());
            chip.setCheckable(true);
            chip.setChecked(filtrosCategoriasSelecionadas.contains(categoria.getId()));
            chipGroupCategorias.addView(chip);
        }

        chipGroupTags.removeAllViews();
        for (Tag tag : listaTags) {
            Chip chip = new Chip(requireContext());
            chip.setText(tag.getEmoji() + " " + tag.getNome());
            chip.setTag(tag.getId());
            chip.setCheckable(true);
            chip.setChecked(filtrosTagsSelecionadas.contains(tag.getId()));
            chipGroupTags.addView(chip);
        }

        sheetView.findViewById(R.id.btn_limpar).setOnClickListener(v -> {
            filtrosCategoriasSelecionadas.clear();
            filtrosTagsSelecionadas.clear();
            filtrosTiposCategoriaSelecionados.clear();
            sheet.dismiss();
            aplicarFiltro();
        });

        sheetView.findViewById(R.id.btn_aplicar).setOnClickListener(v -> {
            filtrosCategoriasSelecionadas.clear();
            for (int i = 0; i < chipGroupCategorias.getChildCount(); i++) {
                View child = chipGroupCategorias.getChildAt(i);
                if (child instanceof Chip) {
                    Chip chip = (Chip) child;
                    if (chip.isChecked() && chip.getTag() instanceof String) {
                        filtrosCategoriasSelecionadas.add((String) chip.getTag());
                    }
                }
            }

            filtrosTagsSelecionadas.clear();
            for (int i = 0; i < chipGroupTags.getChildCount(); i++) {
                View child = chipGroupTags.getChildAt(i);
                if (child instanceof Chip) {
                    Chip chip = (Chip) child;
                    if (chip.isChecked() && chip.getTag() instanceof String) {
                        filtrosTagsSelecionadas.add((String) chip.getTag());
                    }
                }
            }

            filtrosTiposCategoriaSelecionados.clear();
            for (int i = 0; i < chipGroupTipo.getChildCount(); i++) {
                View child = chipGroupTipo.getChildAt(i);
                if (!(child instanceof Chip)) continue;
                Chip chip = (Chip) child;
                if (!chip.isChecked()) continue;
                int id = chip.getId();
                if (id == R.id.chip_tipo_essenciais) {
                    filtrosTiposCategoriaSelecionados.add(TipoCategoria.ESSENCIAL);
                } else if (id == R.id.chip_tipo_nao_essenciais) {
                    filtrosTiposCategoriaSelecionados.add(TipoCategoria.NAO_ESSENCIAL);
                } else if (id == R.id.chip_tipo_receitas) {
                    filtrosTiposCategoriaSelecionados.add(TipoCategoria.RECEITA);
                }
            }

            sheet.dismiss();
            aplicarFiltro();
        });

        sheet.show();
    }

    // ─── Chart Setup ─────────────────────────────────────────────────────────

    /** 3.6 — configure chart type toggle */
    private void configurarToggleChart() {
        toggleChartTipo.check(R.id.btn_chart_pizza);
        toggleChartTipo.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            if (checkedId == R.id.btn_chart_pizza) {
                pieChart.setVisibility(View.VISIBLE);
                horizontalBarChart.setVisibility(View.GONE);
            } else {
                pieChart.setVisibility(View.GONE);
                horizontalBarChart.setVisibility(View.VISIBLE);
                if (ultimoTotalPorCategoria != null) {
                    atualizarHorizontalBarChart(ultimoTotalPorCategoria);
                }
            }
        });
    }

    private void setupPieChart() {
        pieChart.getDescription().setEnabled(false);
        pieChart.setHoleRadius(55f);
        pieChart.setTransparentCircleRadius(60f);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setDrawCenterText(true);
        pieChart.setCenterText("Gastos\npor categoria");
        pieChart.setCenterTextSize(11f);
        pieChart.getLegend().setEnabled(false);
        pieChart.setEntryLabelTextSize(0f);
        pieChart.setRotationEnabled(false);
        pieChart.setNoDataText(getString(R.string.lancamentos_vazio));
    }

    private void setupHorizontalBarChart() {
        horizontalBarChart.getDescription().setEnabled(false);
        horizontalBarChart.getLegend().setEnabled(false);
        horizontalBarChart.setDrawGridBackground(false);
        horizontalBarChart.getAxisRight().setEnabled(false);
        horizontalBarChart.setExtraLeftOffset(6f);
        horizontalBarChart.setExtraRightOffset(12f);
        horizontalBarChart.getAxisLeft().setAxisMinimum(0f);
        horizontalBarChart.getAxisLeft().setDrawGridLines(true);
        horizontalBarChart.getAxisLeft().setGridColor(0x33000000);
        horizontalBarChart.getXAxis().setDrawGridLines(false);
        horizontalBarChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        horizontalBarChart.getXAxis().setGranularity(1f);
        horizontalBarChart.getXAxis().setTextSize(11f);
        horizontalBarChart.setNoDataText(getString(R.string.lancamentos_vazio));
        horizontalBarChart.setFitBars(true);
    }

    private void setupBarChart() {
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.setFitBars(true);
        barChart.setDrawGridBackground(false);
        barChart.getAxisRight().setEnabled(false);
        XAxis xAxisBar = barChart.getXAxis();
        xAxisBar.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxisBar.setDrawGridLines(false);
        xAxisBar.setGranularity(1f);
        xAxisBar.setCenterAxisLabels(true);
        barChart.getAxisLeft().setAxisMinimum(0f);
        barChart.getAxisLeft().setGridColor(0x33000000);
        barChart.setNoDataText(getString(R.string.lancamentos_vazio));
    }

    private void setupLineChart() {
        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.setDrawGridBackground(false);
        lineChart.getAxisRight().setEnabled(false);
        XAxis xAxisLine = lineChart.getXAxis();
        xAxisLine.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxisLine.setDrawGridLines(false);
        xAxisLine.setGranularity(1f);
        lineChart.getAxisLeft().setGridColor(0x33000000);
        lineChart.setNoDataText(getString(R.string.lancamentos_vazio));
    }

    // ─── Chart Population ────────────────────────────────────────────────────

    private void atualizarHorizontalBarChart(Map<String, BigDecimal> totalPorCategoria) {
        if (totalPorCategoria == null || totalPorCategoria.isEmpty()) {
            horizontalBarChart.clear();
            horizontalBarChart.invalidate();
            return;
        }

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels    = new ArrayList<>();
        List<Integer> colors   = new ArrayList<>();
        int idx = 0;
        for (Map.Entry<String, BigDecimal> entry : totalPorCategoria.entrySet()) {
            entries.add(new BarEntry(idx, entry.getValue().floatValue()));
            labels.add(entry.getKey());
            colors.add(PIE_COLORS[idx % PIE_COLORS.length]);
            idx++;
        }

        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setDrawValues(true);
        dataSet.setValueTextSize(10f);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.6f);
        horizontalBarChart.setData(data);
        horizontalBarChart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = Math.round(value);
                if (index < 0 || index >= labels.size()) return "";
                String label = labels.get(index);
                return label.length() > 18 ? label.substring(0, 18) + "…" : label;
            }
        });
        horizontalBarChart.getXAxis().setLabelCount(labels.size());
        horizontalBarChart.animateX(500);
        horizontalBarChart.invalidate();
    }

    private void atualizarPieChart(Map<String, BigDecimal> totalPorCategoria) {
        ultimoTotalPorCategoria = totalPorCategoria;
        layoutPieLegenda.removeAllViews();
        if (totalPorCategoria == null || totalPorCategoria.isEmpty()) {
            pieChart.clear();
            pieChart.invalidate();
            horizontalBarChart.clear();
            horizontalBarChart.invalidate();
            return;
        }

        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        int idx = 0;

        for (Map.Entry<String, BigDecimal> entry : totalPorCategoria.entrySet()) {
            entries.add(new PieEntry(entry.getValue().floatValue(), ""));
            int color = PIE_COLORS[idx % PIE_COLORS.length];
            colors.add(color);

            View row = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_pie_legenda, layoutPieLegenda, false);
            setOvalBackground(row.findViewById(R.id.view_dot), color);
            ((TextView) row.findViewById(R.id.tv_legenda_nome)).setText(entry.getKey());
            ((TextView) row.findViewById(R.id.tv_legenda_percentual)).setText(
                    String.format(Locale.getDefault(), "R$ %.2f", entry.getValue()));
            layoutPieLegenda.addView(row);
            idx++;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setSliceSpace(2f);
        dataSet.setSelectionShift(5f);
        dataSet.setDrawValues(false);

        pieChart.setData(new PieData(dataSet));
        pieChart.animateY(800, Easing.EaseInOutQuad);
        pieChart.invalidate();

        // also update the horizontal bar chart (pre-populate for when user toggles)
        atualizarHorizontalBarChart(totalPorCategoria);
    }

    private void atualizarBarChart(float[] receitas, float[] despesas, String[] labels) {
        List<BarEntry> receitaEntries = new ArrayList<>();
        List<BarEntry> despesaEntries = new ArrayList<>();
        for (int i = 0; i < receitas.length; i++) {
            receitaEntries.add(new BarEntry(i, receitas[i]));
            despesaEntries.add(new BarEntry(i, despesas[i]));
        }

        BarDataSet receitaSet = new BarDataSet(receitaEntries, "Receita");
        receitaSet.setColor(ContextCompat.getColor(requireContext(), R.color.verde_success));
        receitaSet.setDrawValues(false);

        BarDataSet despesaSet = new BarDataSet(despesaEntries, "Despesa");
        despesaSet.setColor(ContextCompat.getColor(requireContext(), R.color.vermelho_error));
        despesaSet.setDrawValues(false);

        float groupSpace = 0.3f;
        float barSpace   = 0.05f;
        float barWidth   = 0.3f;

        BarData data = new BarData(receitaSet, despesaSet);
        data.setBarWidth(barWidth);

        barChart.setData(data);
        barChart.groupBars(0f, groupSpace, barSpace);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChart.getXAxis().setAxisMinimum(0f);
        barChart.getXAxis().setAxisMaximum(data.getGroupWidth(groupSpace, barSpace) * labels.length);
        barChart.animateXY(600, 600);
        barChart.invalidate();
    }

    private void atualizarLineChart(float[] receitas, float[] despesas, String[] labels) {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < receitas.length; i++) {
            entries.add(new Entry(i, receitas[i] - despesas[i]));
        }

        int primaryColor = ContextCompat.getColor(requireContext(), R.color.azul_primary);
        LineDataSet dataSet = new LineDataSet(entries, "Resultado");
        dataSet.setColor(primaryColor);
        dataSet.setLineWidth(2f);
        dataSet.setCircleColor(primaryColor);
        dataSet.setCircleRadius(4f);
        dataSet.setCircleHoleColor(ContextCompat.getColor(requireContext(), R.color.white));
        dataSet.setCircleHoleRadius(2f);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setCubicIntensity(0.2f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(primaryColor);
        dataSet.setFillAlpha(30);
        dataSet.setDrawValues(false);

        lineChart.setData(new LineData(dataSet));
        lineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        lineChart.animateX(600);
        lineChart.invalidate();
    }

    // ─── Observers & Filter ──────────────────────────────────────────────────

    private void observarViewModel() {
        viewModel.getResultado().observe(getViewLifecycleOwner(), res -> {
            ultimoResultadoBruto = res;
            renderResultadoFiltrado();
        });
        viewModel.getCategoriasMap().observe(getViewLifecycleOwner(), map -> {
            categoriaPorId = map != null ? map : new LinkedHashMap<>();
            renderResultadoFiltrado();
        });
        viewModel.getTagsPorLancamento().observe(getViewLifecycleOwner(), map -> {
            tagsPorLancamento = map != null ? map : new LinkedHashMap<>();
            renderResultadoFiltrado();
        });
        viewModel.getErro().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
        });
        viewModel.getReceitasMensais().observe(getViewLifecycleOwner(), r -> atualizarChartsHistorico());
        viewModel.getDespesasMensais().observe(getViewLifecycleOwner(), d -> atualizarChartsHistorico());
        viewModel.getLabelsMeses().observe(getViewLifecycleOwner(), l -> atualizarChartsHistorico());
    }

    private void atualizarChartsHistorico() {
        float[]  rec    = viewModel.getReceitasMensais().getValue();
        float[]  desp   = viewModel.getDespesasMensais().getValue();
        String[] labels = viewModel.getLabelsMeses().getValue();
        if (rec != null && desp != null && labels != null) {
            atualizarBarChart(rec, desp, labels);
            atualizarLineChart(rec, desp, labels);
        }
    }

    private void aplicarFiltro() {
        String dataInicio, dataFim;
        LocalDate hoje = LocalDate.now();
        if (modoAtual == MODO_MES) {
            dataInicio = String.format(Locale.US, "%04d-%02d-01",
                    hoje.getYear(), hoje.getMonthValue());
            dataFim = hoje.toString();
        } else if (modoAtual == MODO_SEMANA) {
            dataInicio = hoje.minusDays(6).toString();
            dataFim = hoje.toString();
        } else if (modoAtual == MODO_ANO) {
            dataInicio = String.format(Locale.US, "%04d-01-01", hoje.getYear());
            dataFim = hoje.toString();
        } else {
            dataInicio = etDataInicio.getText() != null
                    ? etDataInicio.getText().toString().trim() : "";
            dataFim = etDataFim.getText() != null
                    ? etDataFim.getText().toString().trim() : "";
            if (dataInicio.isEmpty() || dataFim.isEmpty()) {
                Toast.makeText(requireContext(),
                        R.string.relatorio_datas_obrigatorias, Toast.LENGTH_SHORT).show();
                return;
            }
        }

        viewModel.filtrar(new FiltroRelatorio(dataInicio, dataFim,
            null, null, null));
    }

    private void atualizarUI(RelatorioResult res) {
        if (res == null) return;

        BigDecimal totalReceitas = res.getTotalReceitas();
        BigDecimal totalDespesas = res.getTotalDespesas();

        tvTotalReceitas.setText(getString(R.string.relatorio_resumo_recebido,
            String.format(Locale.getDefault(), "R$ %.2f", totalReceitas)));
        tvTotalDespesas.setText(getString(R.string.relatorio_resumo_gastos,
            String.format(Locale.getDefault(), "R$ %.2f", totalDespesas)));

        int percentual = FinanceCalculator.calcularPercentualLimitado(totalDespesas, totalReceitas);
        tvResumoPercentual.setText(getString(R.string.relatorio_resumo_percentual, percentual));
        pbResumoPercentual.setProgressCompat(percentual, true);

        BigDecimal saldo = res.getSaldo();
        tvSaldo.setText(String.format(Locale.getDefault(), "R$ %.2f", saldo));
        tvSaldo.setTextColor(saldo.compareTo(BigDecimal.ZERO) >= 0 ? 0xFF2E7D32 : 0xFFC62828);

        atualizarPieChart(res.getTotalPorCategoria());

        layoutCategorias.removeAllViews();
        for (Map.Entry<String, BigDecimal> entry : res.getTotalPorCategoria().entrySet()) {
            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            rowParams.setMargins(0, 2, 0, 2);
            row.setLayoutParams(rowParams);

            TextView tvNome = new TextView(requireContext());
            tvNome.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            tvNome.setText(entry.getKey());
                tvNome.setTextColor(ContextCompat.getColor(requireContext(), R.color.cinza_on_surface));

            TextView tvValor = new TextView(requireContext());
            tvValor.setText(String.format(Locale.getDefault(), "R$ %.2f", entry.getValue()));
            tvValor.setTextColor(0xFFC62828);

            row.addView(tvNome);
            row.addView(tvValor);
            layoutCategorias.addView(row);
        }

    }

    private void renderResultadoFiltrado() {
        if (ultimoResultadoBruto == null) return;
        atualizarUI(aplicarFiltrosLocais(ultimoResultadoBruto));
    }

    private RelatorioResult aplicarFiltrosLocais(RelatorioResult original) {
        List<Lancamento> filtrados = new ArrayList<>();
        for (Lancamento lancamento : original.getLancamentos()) {
            if (!matchesTipoCategoriaSelecionado(lancamento)) continue;
            if (!matchesCategoriasSelecionadas(lancamento)) continue;
            if (!matchesTagsSelecionadas(lancamento)) continue;
            filtrados.add(lancamento);
        }

        BigDecimal receitas = BigDecimal.ZERO;
        BigDecimal despesas = BigDecimal.ZERO;
        Map<String, BigDecimal> totalPorCategoria = new LinkedHashMap<>();
        for (Lancamento lancamento : filtrados) {
            if (lancamento.getTipo() == TipoLancamento.RECEITA) {
                receitas = receitas.add(lancamento.getValor());
                continue;
            }

            despesas = despesas.add(lancamento.getValor());
            Categoria categoriaRaiz = encontrarCategoriaRaiz(lancamento.getCategoriaId());
            String nomeCategoria;
            if (categoriaRaiz != null) {
                nomeCategoria = CategoriaVisualFallback.icone(categoriaRaiz, null) + " " + categoriaRaiz.getNome();
            } else {
                nomeCategoria = lancamento.getCategoriaId();
            }
            totalPorCategoria.merge(nomeCategoria, lancamento.getValor(), BigDecimal::add);
        }

        return new RelatorioResult(filtrados, receitas, despesas, totalPorCategoria);
    }

    private boolean matchesCategoriasSelecionadas(Lancamento lancamento) {
        if (filtrosCategoriasSelecionadas.isEmpty()) return true;
        Categoria raiz = encontrarCategoriaRaiz(lancamento.getCategoriaId());
        return raiz != null && filtrosCategoriasSelecionadas.contains(raiz.getId());
    }

    private boolean matchesTipoCategoriaSelecionado(Lancamento lancamento) {
        if (filtrosTiposCategoriaSelecionados.isEmpty()) return true;
        Categoria raiz = encontrarCategoriaRaiz(lancamento.getCategoriaId());
        return raiz != null && filtrosTiposCategoriaSelecionados.contains(raiz.getTipo());
    }

    private boolean matchesTagsSelecionadas(Lancamento lancamento) {
        if (filtrosTagsSelecionadas.isEmpty()) return true;
        List<Tag> tags = tagsPorLancamento.get(lancamento.getId());
        if (tags == null || tags.isEmpty()) return false;
        for (Tag tag : tags) {
            if (filtrosTagsSelecionadas.contains(tag.getId())) return true;
        }
        return false;
    }

    private Categoria encontrarCategoriaRaiz(String categoriaId) {
        Categoria atual = categoriaPorId.get(categoriaId);
        int guard = 0;
        while (atual != null && atual.getPaiId() != null && guard++ < 8) {
            Categoria pai = categoriaPorId.get(atual.getPaiId());
            if (pai == null) break;
            atual = pai;
        }
        return atual;
    }

    private List<Categoria> categoriasRaizOrdenadas() {
        List<Categoria> raizes = new ArrayList<>();
        for (Categoria categoria : listaCategorias) {
            if (categoria.getPaiId() == null) {
                raizes.add(categoria);
            }
        }
        raizes.sort((a, b) -> a.getNome().compareToIgnoreCase(b.getNome()));
        return raizes;
    }

    private void mostrarDatePicker(TextInputEditText campo) {
        LocalDate hoje = LocalDate.now();
        new DatePickerDialog(requireContext(),
                (dp, y, m, d) -> campo.setText(
                        String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d)),
                hoje.getYear(), hoje.getMonthValue() - 1, hoje.getDayOfMonth())
                .show();
    }

    /** Programa um fundo oval colorido na view (usado para os dots da legenda). */
    private static void setOvalBackground(View view, int color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(color);
        view.setBackground(drawable);
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.carregarFiltros();
    }
}
