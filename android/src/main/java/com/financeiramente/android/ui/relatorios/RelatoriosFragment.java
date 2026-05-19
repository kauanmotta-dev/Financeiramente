package com.financeiramente.android.ui.relatorios;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.android.material.textfield.TextInputEditText;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RelatoriosFragment extends Fragment {

    private static final int MODO_MES = 0;
    private static final int MODO_SEMANA = 1;
    private static final int MODO_CUSTOM = 2;

    private RelatoriosViewModel viewModel;
    private LancamentoAdapter lancamentoAdapter;

    private Button btnSemana, btnMes, btnPersonalizado;
    private View layoutDatasCustom;
    private TextInputEditText etDataInicio, etDataFim;
    private Spinner spinnerCategoria, spinnerTag;
    private RadioGroup rgTipo;
    private RadioButton rbTodos, rbReceitas, rbDespesas;
    private TextView tvTotalReceitas, tvTotalDespesas, tvSaldo, tvVazio;
    private LinearLayout layoutCategorias;

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
                ctx.getTagRepository());
        viewModel = new ViewModelProvider(this, factory).get(RelatoriosViewModel.class);

        bindViews(view);
        configurarBotoesPeriodo();
        configurarSpinners();
        configurarBotaoAplicar(view);
        configurarRecyclerLancamentos(view);
        observarViewModel();
    }

    private void bindViews(View view) {
        btnSemana         = view.findViewById(R.id.btn_semana);
        btnMes            = view.findViewById(R.id.btn_mes);
        btnPersonalizado  = view.findViewById(R.id.btn_personalizado);
        layoutDatasCustom = view.findViewById(R.id.layout_datas_custom);
        etDataInicio      = view.findViewById(R.id.et_data_inicio);
        etDataFim         = view.findViewById(R.id.et_data_fim);
        spinnerCategoria  = view.findViewById(R.id.spinner_categoria);
        spinnerTag        = view.findViewById(R.id.spinner_tag);
        rgTipo            = view.findViewById(R.id.rg_tipo);
        rbTodos           = view.findViewById(R.id.rb_todos);
        rbReceitas        = view.findViewById(R.id.rb_receitas);
        rbDespesas        = view.findViewById(R.id.rb_despesas);
        tvTotalReceitas   = view.findViewById(R.id.tv_total_receitas);
        tvTotalDespesas   = view.findViewById(R.id.tv_total_despesas);
        tvSaldo           = view.findViewById(R.id.tv_saldo);
        tvVazio           = view.findViewById(R.id.tv_vazio);
        layoutCategorias  = view.findViewById(R.id.layout_categorias);
    }

    private void configurarBotoesPeriodo() {
        btnMes.setOnClickListener(v -> {
            modoAtual = MODO_MES;
            layoutDatasCustom.setVisibility(View.GONE);
            viewModel.filtrarMesAtual();
        });
        btnSemana.setOnClickListener(v -> {
            modoAtual = MODO_SEMANA;
            layoutDatasCustom.setVisibility(View.GONE);
            viewModel.filtrarSemanaAtual();
        });
        btnPersonalizado.setOnClickListener(v -> {
            modoAtual = MODO_CUSTOM;
            layoutDatasCustom.setVisibility(View.VISIBLE);
        });

        LocalDate hoje = LocalDate.now();
        String inicioMes = String.format(Locale.US, "%04d-%02d-01", hoje.getYear(), hoje.getMonthValue());
        etDataInicio.setText(inicioMes);
        etDataFim.setText(hoje.toString());

        etDataInicio.setOnClickListener(v -> mostrarDatePicker(etDataInicio));
        etDataFim.setOnClickListener(v -> mostrarDatePicker(etDataFim));
    }

    private void configurarSpinners() {
        viewModel.getCategorias().observe(getViewLifecycleOwner(), cats -> {
            listaCategorias = cats != null ? cats : new ArrayList<>();
            List<String> nomes = new ArrayList<>();
            nomes.add(getString(R.string.relatorio_todas_categorias));
            for (Categoria c : listaCategorias) nomes.add(c.getNome());
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_item, nomes);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCategoria.setAdapter(adapter);
        });

        viewModel.getTags().observe(getViewLifecycleOwner(), tgs -> {
            listaTags = tgs != null ? tgs : new ArrayList<>();
            List<String> nomes = new ArrayList<>();
            nomes.add(getString(R.string.relatorio_todas_tags));
            for (Tag t : listaTags) nomes.add(t.getNome());
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_item, nomes);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerTag.setAdapter(adapter);
        });
    }

    private void configurarBotaoAplicar(View view) {
        Button btnAplicar = view.findViewById(R.id.btn_aplicar);
        btnAplicar.setOnClickListener(v -> aplicarFiltro());
    }

    private void configurarRecyclerLancamentos(View view) {
        RecyclerView rv = view.findViewById(R.id.rv_lancamentos);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setNestedScrollingEnabled(false);
        lancamentoAdapter = new LancamentoAdapter(l -> {}, l -> {});
        rv.setAdapter(lancamentoAdapter);
    }

    private void observarViewModel() {
        viewModel.getResultado().observe(getViewLifecycleOwner(), this::atualizarUI);
        viewModel.getErro().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
        });
    }

    private void aplicarFiltro() {
        String dataInicio, dataFim;
        if (modoAtual == MODO_MES) {
            LocalDate hoje = LocalDate.now();
            dataInicio = String.format(Locale.US, "%04d-%02d-01", hoje.getYear(), hoje.getMonthValue());
            dataFim = hoje.toString();
        } else if (modoAtual == MODO_SEMANA) {
            LocalDate hoje = LocalDate.now();
            dataInicio = hoje.minusDays(6).toString();
            dataFim = hoje.toString();
        } else {
            dataInicio = etDataInicio.getText() != null ? etDataInicio.getText().toString().trim() : "";
            dataFim = etDataFim.getText() != null ? etDataFim.getText().toString().trim() : "";
            if (dataInicio.isEmpty() || dataFim.isEmpty()) {
                Toast.makeText(requireContext(), R.string.relatorio_datas_obrigatorias, Toast.LENGTH_SHORT).show();
                return;
            }
        }

        String categoriaId = null;
        int catPos = spinnerCategoria.getSelectedItemPosition();
        if (catPos > 0 && catPos - 1 < listaCategorias.size()) {
            categoriaId = listaCategorias.get(catPos - 1).getId();
        }

        String tagId = null;
        int tagPos = spinnerTag.getSelectedItemPosition();
        if (tagPos > 0 && tagPos - 1 < listaTags.size()) {
            tagId = listaTags.get(tagPos - 1).getId();
        }

        TipoLancamento tipo = null;
        int checkedId = rgTipo.getCheckedRadioButtonId();
        if (checkedId == R.id.rb_receitas) tipo = TipoLancamento.RECEITA;
        else if (checkedId == R.id.rb_despesas) tipo = TipoLancamento.DESPESA;

        viewModel.filtrar(new FiltroRelatorio(dataInicio, dataFim, categoriaId, tagId, tipo));
    }

    private void atualizarUI(RelatorioResult res) {
        if (res == null) return;

        tvTotalReceitas.setText(String.format(Locale.getDefault(), "R$ %.2f", res.getTotalReceitas()));
        tvTotalDespesas.setText(String.format(Locale.getDefault(), "R$ %.2f", res.getTotalDespesas()));
        double saldo = res.getSaldo();
        tvSaldo.setText(String.format(Locale.getDefault(), "R$ %.2f", saldo));
        tvSaldo.setTextColor(saldo >= 0 ? 0xFF2E7D32 : 0xFFC62828);

        // Total por categoria
        layoutCategorias.removeAllViews();
        for (Map.Entry<String, Double> entry : res.getTotalPorCategoria().entrySet()) {
            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            rowParams.setMargins(0, 2, 0, 2);
            row.setLayoutParams(rowParams);

            TextView tvNome = new TextView(requireContext());
            LinearLayout.LayoutParams nomeParams = new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            tvNome.setLayoutParams(nomeParams);
            tvNome.setText(entry.getKey());

            TextView tvValor = new TextView(requireContext());
            tvValor.setText(String.format(Locale.getDefault(), "R$ %.2f", entry.getValue()));
            tvValor.setTextColor(0xFFC62828);

            row.addView(tvNome);
            row.addView(tvValor);
            layoutCategorias.addView(row);
        }

        // Lista de lançamentos
        lancamentoAdapter.setItems(res.getLancamentos());
        tvVazio.setVisibility(res.getLancamentos().isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void mostrarDatePicker(TextInputEditText campo) {
        LocalDate hoje = LocalDate.now();
        new DatePickerDialog(requireContext(),
                (dp, y, m, d) -> campo.setText(
                        String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d)),
                hoje.getYear(), hoje.getMonthValue() - 1, hoje.getDayOfMonth())
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.carregarFiltros();
    }
}
