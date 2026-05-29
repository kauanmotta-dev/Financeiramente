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

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.appbar.MaterialToolbar;
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
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

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

    private PieChart pieChartTipoCategoria;
    private PieChart pieChartCategorias;
    private PieChart pieChartTags;
    private LineChart lineChartEvolucao;
    private LinearLayout layoutPieLegendaTipoCategoria;
    private LinearLayout layoutPieLegendaCategorias;
    private LinearLayout layoutPieLegendaTags;
    private Chip chipEvolucaoGastos;
    private Chip chipEvolucaoReceitas;

    private RelatorioResult ultimoResultadoBruto = null;
    private List<Categoria> listaCategorias = new ArrayList<>();
    private List<Tag> listaTags = new ArrayList<>();
    private Map<String, Categoria> categoriaPorId = new LinkedHashMap<>();
    private Map<String, List<Tag>> tagsPorLancamento = new LinkedHashMap<>();
    private List<Lancamento> ultimoLancamentosFiltrados = new ArrayList<>();
    private BigDecimal totalInvestidoPeriodo = BigDecimal.ZERO;
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
            ctx.getCoreServices().getLancamentoRepository(),
            ctx.getCoreServices().getMetaRepository(),
            ctx.getCoreServices().getAporteMetaRepository());
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
        setupPieChart(pieChartTipoCategoria, getString(R.string.relatorio_distribuicao_tipo_categoria));
        setupPieChart(pieChartCategorias, getString(R.string.relatorio_distribuicao_categorias));
        setupPieChart(pieChartTags, getString(R.string.relatorio_distribuicao_tags));
        setupLineChart();
        configurarFiltrosEvolucao();
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
        pieChartTipoCategoria = view.findViewById(R.id.pie_chart_tipo_categoria);
        pieChartCategorias    = view.findViewById(R.id.pie_chart_categorias);
        pieChartTags          = view.findViewById(R.id.pie_chart_tags);
        lineChartEvolucao     = view.findViewById(R.id.line_chart_evolucao);
        layoutPieLegendaTipoCategoria = view.findViewById(R.id.layout_pie_legenda_tipo_categoria);
        layoutPieLegendaCategorias    = view.findViewById(R.id.layout_pie_legenda_categorias);
        layoutPieLegendaTags          = view.findViewById(R.id.layout_pie_legenda_tags);
        chipEvolucaoGastos    = view.findViewById(R.id.chip_evolucao_gastos);
        chipEvolucaoReceitas  = view.findViewById(R.id.chip_evolucao_receitas);
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

    private void configurarFiltrosEvolucao() {
        View.OnClickListener refresh = v -> {
            if (!chipEvolucaoGastos.isChecked() && !chipEvolucaoReceitas.isChecked()) {
                if (v == chipEvolucaoGastos) {
                    chipEvolucaoGastos.setChecked(true);
                } else {
                    chipEvolucaoReceitas.setChecked(true);
                }
                return;
            }
            atualizarLineChartEvolucao(ultimoLancamentosFiltrados);
        };
        chipEvolucaoGastos.setOnClickListener(refresh);
        chipEvolucaoReceitas.setOnClickListener(refresh);
    }

    private void setupPieChart(PieChart chart, String centerText) {
        chart.getDescription().setEnabled(false);
        chart.setHoleRadius(55f);
        chart.setTransparentCircleRadius(60f);
        chart.setDrawHoleEnabled(true);
        chart.setDrawCenterText(true);
        chart.setCenterText(centerText);
        chart.setCenterTextSize(10f);
        chart.getLegend().setEnabled(false);
        chart.setEntryLabelTextSize(0f);
        chart.setRotationEnabled(false);
        chart.setNoDataText(getString(R.string.lancamentos_vazio));
    }

    private void setupLineChart() {
        lineChartEvolucao.getDescription().setEnabled(false);
        lineChartEvolucao.getLegend().setEnabled(true);
        lineChartEvolucao.setDrawGridBackground(false);
        lineChartEvolucao.getAxisRight().setEnabled(false);
        XAxis xAxisLine = lineChartEvolucao.getXAxis();
        xAxisLine.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxisLine.setDrawGridLines(false);
        xAxisLine.setGranularity(1f);
        xAxisLine.setLabelRotationAngle(-30f);
        lineChartEvolucao.getAxisLeft().setAxisMinimum(0f);
        lineChartEvolucao.getAxisLeft().setGridColor(0x33000000);
        lineChartEvolucao.setNoDataText(getString(R.string.lancamentos_vazio));
    }

    // ─── Chart Population ────────────────────────────────────────────────────

    private void atualizarPieChart(PieChart chart,
                                   LinearLayout legenda,
                                   Map<String, BigDecimal> distribuicao,
                                   String centerText) {
        legenda.removeAllViews();
        if (distribuicao == null || distribuicao.isEmpty()) {
            chart.clear();
            chart.invalidate();
            return;
        }

        chart.setCenterText(centerText);

        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        int idx = 0;
        for (Map.Entry<String, BigDecimal> entry : distribuicao.entrySet()) {
            entries.add(new PieEntry(entry.getValue().floatValue(), ""));
            int color = PIE_COLORS[idx % PIE_COLORS.length];
            colors.add(color);

            View row = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_pie_legenda, legenda, false);
            setOvalBackground(row.findViewById(R.id.view_dot), color);
            ((TextView) row.findViewById(R.id.tv_legenda_nome)).setText(entry.getKey());
            ((TextView) row.findViewById(R.id.tv_legenda_percentual)).setText(
                    formatarMoeda(entry.getValue()));
            legenda.addView(row);
            idx++;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setSliceSpace(2f);
        dataSet.setSelectionShift(5f);
        dataSet.setDrawValues(false);

        chart.setData(new PieData(dataSet));
        chart.animateY(800, Easing.EaseInOutQuad);
        chart.invalidate();
    }

    private void atualizarLineChartEvolucao(List<Lancamento> lancamentos) {
        if (lancamentos == null || lancamentos.isEmpty()) {
            lineChartEvolucao.clear();
            lineChartEvolucao.invalidate();
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM", Locale.getDefault());
        Map<LocalDate, BigDecimal> receitasPorDia = new TreeMap<>();
        Map<LocalDate, BigDecimal> despesasPorDia = new TreeMap<>();
        for (Lancamento lancamento : lancamentos) {
            LocalDate data = parseDataLancamento(lancamento.getData());
            if (data == null) continue;
            if (lancamento.getTipo() == TipoLancamento.RECEITA) {
                receitasPorDia.merge(data, lancamento.getValor(), BigDecimal::add);
            } else {
                despesasPorDia.merge(data, lancamento.getValor(), BigDecimal::add);
            }
        }

        List<LocalDate> datas = new ArrayList<>();
        datas.addAll(receitasPorDia.keySet());
        for (LocalDate data : despesasPorDia.keySet()) {
            if (!datas.contains(data)) datas.add(data);
        }
        datas.sort(LocalDate::compareTo);

        if (datas.isEmpty()) {
            lineChartEvolucao.clear();
            lineChartEvolucao.invalidate();
            return;
        }

        List<Entry> entradasReceitas = new ArrayList<>();
        List<Entry> entradasDespesas = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        for (int i = 0; i < datas.size(); i++) {
            LocalDate data = datas.get(i);
            labels.add(data.format(formatter));
            entradasReceitas.add(new Entry(i,
                    receitasPorDia.getOrDefault(data, BigDecimal.ZERO).floatValue()));
            entradasDespesas.add(new Entry(i,
                    despesasPorDia.getOrDefault(data, BigDecimal.ZERO).floatValue()));
        }

        List<ILineDataSet> dataSets = new ArrayList<>();
        if (chipEvolucaoReceitas.isChecked()) {
            LineDataSet receitasSet = new LineDataSet(entradasReceitas, getString(R.string.relatorio_receitas));
            receitasSet.setColor(ContextCompat.getColor(requireContext(), R.color.verde_success));
            receitasSet.setCircleColor(ContextCompat.getColor(requireContext(), R.color.verde_success));
            receitasSet.setLineWidth(2f);
            receitasSet.setDrawValues(false);
            receitasSet.setCircleRadius(3f);
            dataSets.add(receitasSet);
        }

        if (chipEvolucaoGastos.isChecked()) {
            LineDataSet despesasSet = new LineDataSet(entradasDespesas, getString(R.string.relatorio_gastos));
            despesasSet.setColor(ContextCompat.getColor(requireContext(), R.color.vermelho_error));
            despesasSet.setCircleColor(ContextCompat.getColor(requireContext(), R.color.vermelho_error));
            despesasSet.setLineWidth(2f);
            despesasSet.setDrawValues(false);
            despesasSet.setCircleRadius(3f);
            dataSets.add(despesasSet);
        }

        if (dataSets.isEmpty()) {
            lineChartEvolucao.clear();
            lineChartEvolucao.invalidate();
            return;
        }

        lineChartEvolucao.setData(new LineData(dataSets));
        lineChartEvolucao.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        lineChartEvolucao.getXAxis().setLabelCount(Math.min(labels.size(), 6), true);
        lineChartEvolucao.animateX(500);
        lineChartEvolucao.invalidate();
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
        viewModel.getTotalInvestidoPeriodo().observe(getViewLifecycleOwner(), total -> {
            totalInvestidoPeriodo = total != null ? total : BigDecimal.ZERO;
            renderResultadoFiltrado();
        });
        viewModel.getErro().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
        });
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
        tvSaldo.setText(formatarMoeda(saldo));
        tvSaldo.setTextColor(saldo.compareTo(BigDecimal.ZERO) >= 0 ? 0xFF2E7D32 : 0xFFC62828);

        ultimoLancamentosFiltrados = res.getLancamentos() != null
            ? res.getLancamentos() : new ArrayList<>();

        atualizarPieChart(
            pieChartTipoCategoria,
            layoutPieLegendaTipoCategoria,
            criarDistribuicaoTipoCategoria(ultimoLancamentosFiltrados),
            "Essenciais x Não essenciais");

        atualizarPieChart(
            pieChartCategorias,
            layoutPieLegendaCategorias,
            res.getTotalPorCategoria(),
            "Categorias");

        atualizarPieChart(
            pieChartTags,
            layoutPieLegendaTags,
            criarDistribuicaoTags(ultimoLancamentosFiltrados),
            "Tags");

        atualizarLineChartEvolucao(ultimoLancamentosFiltrados);
    }

    private void renderResultadoFiltrado() {
        if (ultimoResultadoBruto == null) return;
        atualizarUI(aplicarFiltrosLocais(ultimoResultadoBruto));
    }

    private RelatorioResult aplicarFiltrosLocais(RelatorioResult original) {
        List<Lancamento> filtrados = new ArrayList<>();
        for (Lancamento lancamento : original.getLancamentos()) {
            if (ehLancamentoDerivadoDeFatura(lancamento)) continue;
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

    private Map<String, BigDecimal> criarDistribuicaoTipoCategoria(List<Lancamento> lancamentos) {
        Map<String, BigDecimal> distribuicao = new LinkedHashMap<>();
        distribuicao.put(getString(R.string.categoria_tipo_essencial), BigDecimal.ZERO);
        distribuicao.put(getString(R.string.categoria_tipo_nao_essencial), BigDecimal.ZERO);
        distribuicao.put(getString(R.string.relatorio_sem_categoria), BigDecimal.ZERO);

        for (Lancamento lancamento : lancamentos) {
            if (lancamento.getTipo() != TipoLancamento.DESPESA) continue;
            if (ehLancamentoDerivadoDeFatura(lancamento)) continue;
            Categoria raiz = encontrarCategoriaRaiz(lancamento.getCategoriaId());
            if (raiz == null || raiz.getTipo() == null || raiz.getTipo() == TipoCategoria.RECEITA
                    || raiz.getTipo() == TipoCategoria.SEM_TIPO) {
                distribuicao.merge(getString(R.string.relatorio_sem_categoria), lancamento.getValor(), BigDecimal::add);
            } else if (raiz.getTipo() == TipoCategoria.ESSENCIAL) {
                distribuicao.merge(getString(R.string.categoria_tipo_essencial), lancamento.getValor(), BigDecimal::add);
            } else {
                distribuicao.merge(getString(R.string.categoria_tipo_nao_essencial), lancamento.getValor(), BigDecimal::add);
            }
        }

        if (totalInvestidoPeriodo.compareTo(BigDecimal.ZERO) > 0) {
            distribuicao.put(getString(R.string.relatorio_investido), totalInvestidoPeriodo);
        }

        distribuicao.entrySet().removeIf(entry -> entry.getValue().compareTo(BigDecimal.ZERO) <= 0);
        return distribuicao;
    }

    private boolean ehLancamentoDerivadoDeFatura(Lancamento lancamento) {
        String descricao = lancamento.getDescricao();
        if (descricao == null) {
            return false;
        }

        String normalizada = descricao.trim().toLowerCase(Locale.ROOT);
        return normalizada.startsWith("pagamento ")
            || "ajuste de fatura".equals(normalizada)
            || normalizada.startsWith("saldo anterior (");
    }

    private Map<String, BigDecimal> criarDistribuicaoTags(List<Lancamento> lancamentos) {
        Map<String, BigDecimal> distribuicao = new LinkedHashMap<>();
        for (Lancamento lancamento : lancamentos) {
            if (lancamento.getTipo() != TipoLancamento.DESPESA) continue;
            List<Tag> tags = tagsPorLancamento.get(lancamento.getId());
            if (tags == null || tags.isEmpty()) {
                distribuicao.merge(getString(R.string.relatorio_sem_tag), lancamento.getValor(), BigDecimal::add);
                continue;
            }
            BigDecimal valorRateado = lancamento.getValor().divide(
                    BigDecimal.valueOf(tags.size()),
                    2,
                    RoundingMode.HALF_UP);
            for (Tag tag : tags) {
                String nomeTag = tag.getEmoji() + " " + tag.getNome();
                distribuicao.merge(nomeTag, valorRateado, BigDecimal::add);
            }
        }

        distribuicao.entrySet().removeIf(entry -> entry.getValue().compareTo(BigDecimal.ZERO) <= 0);
        return distribuicao;
    }

    private String formatarMoeda(BigDecimal valor) {
        return String.format(Locale.getDefault(), "R$ %.2f", valor);
    }

    private LocalDate parseDataLancamento(String data) {
        if (data == null || data.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(data.trim());
        } catch (Exception ignored) {
            return null;
        }
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
