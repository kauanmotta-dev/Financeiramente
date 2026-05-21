package com.financeiramente.android.ui.relatorios;

import android.app.DatePickerDialog;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButtonToggleGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.financeiramente.android.R;
import com.financeiramente.android.app.AppContext;
import com.financeiramente.android.ui.lancamentos.LancamentoAdapter;
import com.financeiramente.android.viewmodel.RelatoriosViewModel;
import com.financeiramente.android.viewmodel.RelatoriosViewModelFactory;
import com.financeiramente.core.domain.entity.Categoria;
import com.financeiramente.core.domain.entity.Tag;
import com.financeiramente.core.domain.vo.TipoLancamento;
import com.financeiramente.core.usecase.FiltroRelatorio;
import com.financeiramente.core.usecase.RelatorioResult;
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
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
    private LancamentoAdapter lancamentoAdapter;

    private ChipGroup chipGroupPeriodo;
    private View layoutDatasCustom;
    private TextInputEditText etDataInicio, etDataFim;

    // filter state — maintained across bottom sheet opens
    private String filtroCategoriaSelecionadaId  = null;
    private String filtroTagSelecionadaId        = null;
    private TipoLancamento filtroTipoSelecionado = null;

    private TextView tvTotalReceitas, tvTotalDespesas, tvSaldo;
    private View emptyState;
    private LinearLayout layoutCategorias;

    private PieChart pieChart;
    private HorizontalBarChart horizontalBarChart;
    private BarChart barChart;
    private LineChart lineChart;
    private LinearLayout layoutPieLegenda;
    private MaterialButtonToggleGroup toggleChartTipo;

    private Map<String, Double> ultimoTotalPorCategoria = null;

    private List<Categoria> listaCategorias = new ArrayList<>();
    private List<Tag> listaTags = new ArrayList<>();
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
                ctx.getCategoriaRepository(),
                ctx.getTagRepository(),
                ctx.getLancamentoRepository());
        viewModel = new ViewModelProvider(this, factory).get(RelatoriosViewModel.class);

        bindViews(view);
        configurarEmptyState();
        configurarChipsPeriodo();
        configurarBotaoFiltrar(view);
        configurarRecyclerLancamentos(view);
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
        emptyState           = view.findViewById(R.id.layout_empty_state);
        layoutCategorias     = view.findViewById(R.id.layout_categorias);
        pieChart             = view.findViewById(R.id.pie_chart);
        horizontalBarChart   = view.findViewById(R.id.horizontal_bar_chart);
        barChart             = view.findViewById(R.id.bar_chart);
        lineChart            = view.findViewById(R.id.line_chart);
        layoutPieLegenda     = view.findViewById(R.id.layout_pie_legenda);
        toggleChartTipo      = view.findViewById(R.id.toggle_chart_tipo);
    }

    private void configurarEmptyState() {
        ((android.widget.ImageView) emptyState.findViewById(R.id.iv_empty_illustration))
                .setImageResource(R.drawable.ic_empty_reports);
        ((android.widget.TextView) emptyState.findViewById(R.id.tv_empty_title))
                .setText(R.string.empty_relatorios_title);
        ((android.widget.TextView) emptyState.findViewById(R.id.tv_empty_subtitle))
                .setText(R.string.empty_relatorios_subtitle);
        com.google.android.material.button.MaterialButton btnCta =
                emptyState.findViewById(R.id.btn_empty_cta);
        btnCta.setText(R.string.empty_relatorios_cta);
        btnCta.setVisibility(View.VISIBLE);
        btnCta.setOnClickListener(v -> {
            try {
                androidx.navigation.Navigation.findNavController(requireView())
                        .navigate(R.id.lancamentosListFragment);
            } catch (Exception ignored) {}
        });
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

        Spinner spCategoria = sheetView.findViewById(R.id.spinner_categoria);
        Spinner spTag       = sheetView.findViewById(R.id.spinner_tag);
        RadioGroup rgTipo   = sheetView.findViewById(R.id.rg_tipo);

        // Populate categoria spinner
        List<String> catNomes = new ArrayList<>();
        catNomes.add(getString(R.string.relatorio_todas_categorias));
        for (Categoria c : listaCategorias) catNomes.add(c.getNome());
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, catNomes);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategoria.setAdapter(catAdapter);

        // Restore previous selection
        if (filtroCategoriaSelecionadaId != null) {
            for (int i = 0; i < listaCategorias.size(); i++) {
                if (listaCategorias.get(i).getId().equals(filtroCategoriaSelecionadaId)) {
                    spCategoria.setSelection(i + 1);
                    break;
                }
            }
        }

        // Populate tag spinner
        List<String> tagNomes = new ArrayList<>();
        tagNomes.add(getString(R.string.relatorio_todas_tags));
        for (Tag t : listaTags) tagNomes.add(t.getNome());
        ArrayAdapter<String> tagAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, tagNomes);
        tagAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTag.setAdapter(tagAdapter);

        // Restore previous tag selection
        if (filtroTagSelecionadaId != null) {
            for (int i = 0; i < listaTags.size(); i++) {
                if (listaTags.get(i).getId().equals(filtroTagSelecionadaId)) {
                    spTag.setSelection(i + 1);
                    break;
                }
            }
        }

        // Restore tipo selection
        if (filtroTipoSelecionado == TipoLancamento.RECEITA) {
            ((RadioButton) sheetView.findViewById(R.id.rb_receitas)).setChecked(true);
        } else if (filtroTipoSelecionado == TipoLancamento.DESPESA) {
            ((RadioButton) sheetView.findViewById(R.id.rb_despesas)).setChecked(true);
        }

        sheetView.findViewById(R.id.btn_aplicar).setOnClickListener(v -> {
            int catPos = spCategoria.getSelectedItemPosition();
            filtroCategoriaSelecionadaId = (catPos > 0 && catPos - 1 < listaCategorias.size())
                    ? listaCategorias.get(catPos - 1).getId() : null;

            int tagPos = spTag.getSelectedItemPosition();
            filtroTagSelecionadaId = (tagPos > 0 && tagPos - 1 < listaTags.size())
                    ? listaTags.get(tagPos - 1).getId() : null;

            int checkedId = rgTipo.getCheckedRadioButtonId();
            if (checkedId == R.id.rb_receitas) filtroTipoSelecionado = TipoLancamento.RECEITA;
            else if (checkedId == R.id.rb_despesas) filtroTipoSelecionado = TipoLancamento.DESPESA;
            else filtroTipoSelecionado = null;

            sheet.dismiss();
            aplicarFiltro();
        });

        sheet.show();
    }

    private void configurarRecyclerLancamentos(View view) {
        RecyclerView rv = view.findViewById(R.id.rv_lancamentos);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setNestedScrollingEnabled(false);
        lancamentoAdapter = new LancamentoAdapter(l -> {});
        rv.setAdapter(lancamentoAdapter);
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
        horizontalBarChart.getAxisLeft().setAxisMinimum(0f);
        horizontalBarChart.getAxisLeft().setDrawGridLines(true);
        horizontalBarChart.getAxisLeft().setGridColor(0x33000000);
        horizontalBarChart.getXAxis().setDrawGridLines(false);
        horizontalBarChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM_INSIDE);
        horizontalBarChart.getXAxis().setGranularity(1f);
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

    private void atualizarHorizontalBarChart(Map<String, Double> totalPorCategoria) {
        if (totalPorCategoria == null || totalPorCategoria.isEmpty()) {
            horizontalBarChart.clear();
            horizontalBarChart.invalidate();
            return;
        }

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels    = new ArrayList<>();
        List<Integer> colors   = new ArrayList<>();
        int idx = 0;
        for (Map.Entry<String, Double> entry : totalPorCategoria.entrySet()) {
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
        horizontalBarChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        horizontalBarChart.getXAxis().setLabelCount(labels.size());
        horizontalBarChart.animateX(500);
        horizontalBarChart.invalidate();
    }

    private void atualizarPieChart(Map<String, Double> totalPorCategoria) {
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

        for (Map.Entry<String, Double> entry : totalPorCategoria.entrySet()) {
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
        viewModel.getResultado().observe(getViewLifecycleOwner(), this::atualizarUI);
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
                filtroCategoriaSelecionadaId, filtroTagSelecionadaId, filtroTipoSelecionado));
    }

    private void atualizarUI(RelatorioResult res) {
        if (res == null) return;

        tvTotalReceitas.setText(String.format(Locale.getDefault(),
                "R$ %.2f", res.getTotalReceitas()));
        tvTotalDespesas.setText(String.format(Locale.getDefault(),
                "R$ %.2f", res.getTotalDespesas()));
        double saldo = res.getSaldo();
        tvSaldo.setText(String.format(Locale.getDefault(), "R$ %.2f", saldo));
        tvSaldo.setTextColor(saldo >= 0 ? 0xFF2E7D32 : 0xFFC62828);

        atualizarPieChart(res.getTotalPorCategoria());

        layoutCategorias.removeAllViews();
        for (Map.Entry<String, Double> entry : res.getTotalPorCategoria().entrySet()) {
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

            TextView tvValor = new TextView(requireContext());
            tvValor.setText(String.format(Locale.getDefault(), "R$ %.2f", entry.getValue()));
            tvValor.setTextColor(0xFFC62828);

            row.addView(tvNome);
            row.addView(tvValor);
            layoutCategorias.addView(row);
        }

        lancamentoAdapter.setData(res.getLancamentos(), null, null);
        emptyState.setVisibility(res.getLancamentos().isEmpty() ? View.VISIBLE : View.GONE);
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
