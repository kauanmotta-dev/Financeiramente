package com.financeiramente.android.ui.metas;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.financeiramente.android.R;
import com.financeiramente.android.app.AppContext;
import com.financeiramente.android.viewmodel.MetasViewModel;
import com.financeiramente.android.viewmodel.MetasViewModelFactory;
import com.financeiramente.core.domain.entity.Meta;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.math.BigDecimal;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class MetasFragment extends Fragment {

    // Filter constants
    private static final int FILTRO_TODAS = 0;
    private static final int FILTRO_EM_ANDAMENTO = 1;
    private static final int FILTRO_CONCLUIDAS = 2;

    private MetasViewModel viewModel;
    private MetaAdapter adapter;
    private View emptyState;
    private Button btnEmptyCta;
    private TextView tvTotalMetas;
    private TextView tvProgressoMedio;
    private LinearProgressIndicator pbProgressoMedio;

    private int filtroAtual = FILTRO_TODAS;
    private List<Meta> todasMetas = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_metas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AppContext ctx = AppContext.get(requireContext());
        MetasViewModelFactory factory = new MetasViewModelFactory(
                ctx.getCriarMetaUseCase(),
                ctx.getEditarMetaUseCase(),
                ctx.getDeletarMetaUseCase(),
                ctx.getCoreServices().getMetaRepository());
        viewModel = new ViewModelProvider(this, factory).get(MetasViewModel.class);

        // Views
        emptyState = view.findViewById(R.id.layout_empty_state);
        ((ImageView) emptyState.findViewById(R.id.iv_empty_illustration))
                .setImageResource(R.drawable.ic_empty_goals);
        ((TextView) emptyState.findViewById(R.id.tv_empty_title))
                .setText(R.string.empty_metas_title);
        ((TextView) emptyState.findViewById(R.id.tv_empty_subtitle))
                .setText(R.string.empty_metas_subtitle);
        btnEmptyCta = emptyState.findViewById(R.id.btn_empty_cta);
        btnEmptyCta.setText(R.string.empty_metas_cta);
        btnEmptyCta.setVisibility(View.VISIBLE);
        btnEmptyCta.setOnClickListener(v -> mostrarDialogoNovaMeta(view));

        tvTotalMetas = view.findViewById(R.id.tv_total_metas);
        tvProgressoMedio = view.findViewById(R.id.tv_progresso_medio);
        pbProgressoMedio = view.findViewById(R.id.pb_progresso_medio);

        RecyclerView rv = view.findViewById(R.id.rv_metas);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new MetaAdapter(
                meta -> {
                    Bundle args = new Bundle();
                    args.putString("metaId", meta.getId());
                    Navigation.findNavController(view)
                            .navigate(R.id.action_metasFragment_to_metaDetalheFragment, args);
                },
                meta -> {
                    new AlertDialog.Builder(requireContext())
                        .setTitle(R.string.excluir_meta)
                        .setMessage(getString(R.string.confirmar_excluir_meta, meta.getNome()))
                        .setPositiveButton(R.string.excluir, (d, w) -> viewModel.excluir(meta.getId()))
                            .setNegativeButton(android.R.string.cancel, null)
                            .show();
                }
        );

        adapter.setOnAporteClickListener(meta -> {
            Bundle args = new Bundle();
            args.putString("metaId", meta.getId());
            Navigation.findNavController(view)
                    .navigate(R.id.action_metasFragment_to_metaDetalheFragment, args);
        });

        rv.setAdapter(adapter);

        MaterialButtonToggleGroup toggleGroup = view.findViewById(R.id.toggle_group_filtro);
        toggleGroup.check(R.id.chip_todas);
        toggleGroup.addOnButtonCheckedListener((group, id, isChecked) -> {
            if (!isChecked) return;
            if (id == R.id.chip_todas) filtroAtual = FILTRO_TODAS;
            else if (id == R.id.chip_em_andamento) filtroAtual = FILTRO_EM_ANDAMENTO;
            else if (id == R.id.chip_concluidas) filtroAtual = FILTRO_CONCLUIDAS;
            aplicarFiltro();
        });

        viewModel.getMetas().observe(getViewLifecycleOwner(), lista -> {
            todasMetas = lista != null ? lista : new ArrayList<>();
            atualizarResumo(todasMetas);
            aplicarFiltro();
        });

        viewModel.getErro().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
        });

        view.findViewById(R.id.btn_nova_meta).setOnClickListener(v -> mostrarDialogoNovaMeta(view));
    }

    private void aplicarFiltro() {
        List<Meta> filtradas;
        switch (filtroAtual) {
            case FILTRO_EM_ANDAMENTO:
                filtradas = todasMetas.stream()
                        .filter(m -> m.getValorAtual().add(m.getValorInicial()).compareTo(m.getValorObjetivo()) < 0
                                && !estaAtrasada(m))
                        .collect(Collectors.toList());
                break;
            case FILTRO_CONCLUIDAS:
                filtradas = todasMetas.stream()
                        .filter(m -> m.getValorAtual().add(m.getValorInicial()).compareTo(m.getValorObjetivo()) >= 0)
                        .collect(Collectors.toList());
                break;
            default: // FILTRO_TODAS
                filtradas = todasMetas;
                break;
        }

        adapter.setItems(filtradas);
        emptyState.setVisibility(filtradas.isEmpty() ? View.VISIBLE : View.GONE);
        if (filtroAtual == FILTRO_CONCLUIDAS) {
            btnEmptyCta.setVisibility(View.GONE);
        } else {
            btnEmptyCta.setVisibility(filtradas.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    private void atualizarResumo(List<Meta> lista) {
        int total = lista.size();
        if (total == 0) {
            tvTotalMetas.setText(getString(R.string.metas_resumo_vazio));
            tvProgressoMedio.setText("");
            pbProgressoMedio.setProgress(0);
            return;
        }

        double somaProgresso = 0.0;
        for (Meta m : lista) {
            if (m.getValorObjetivo().compareTo(BigDecimal.ZERO) > 0) {
                double totalSalvo = m.getValorAtual().add(m.getValorInicial()).doubleValue();
                somaProgresso += Math.min((totalSalvo / m.getValorObjetivo().doubleValue()) * 100.0, 100.0);
            }
        }
        int progressoMedio = (int) (somaProgresso / total);

        tvTotalMetas.setText(getResources().getQuantityString(
                R.plurals.metas_resumo_total, total, total));
        tvProgressoMedio.setText(String.format(Locale.getDefault(), "%d%%", progressoMedio));
        pbProgressoMedio.setProgressCompat(progressoMedio, true);
    }

    /** Returns true if the meta has a deadline that has already passed. */
    private boolean estaAtrasada(Meta meta) {
        if (meta.getDataAlvo() == null || meta.getDataAlvo().isEmpty()) return false;
        try {
            return LocalDate.parse(meta.getDataAlvo()).isBefore(LocalDate.now());
        } catch (Exception e) {
            return false;
        }
    }

    private void mostrarDialogoNovaMeta(View navView) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.fragment_meta_form, null);
        EditText etNome = dialogView.findViewById(R.id.et_meta_nome);
        EditText etValorObjetivo = dialogView.findViewById(R.id.et_meta_valor_objetivo);
        EditText etValorInicial = dialogView.findViewById(R.id.et_meta_valor_inicial);
        EditText etDataAlvo = dialogView.findViewById(R.id.et_meta_data_alvo);
        EditText etDescricao = dialogView.findViewById(R.id.et_meta_descricao);

        // Date picker para dataAlvo
        etDataAlvo.setOnClickListener(v -> {
            LocalDate hoje = LocalDate.now();
            new DatePickerDialog(requireContext(),
                    (dp, y, m, d) -> etDataAlvo.setText(
                            String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d)),
                    hoje.getYear(), hoje.getMonthValue() - 1, hoje.getDayOfMonth())
                    .show();
        });

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.add_meta)
                .setView(dialogView)
                .setPositiveButton(R.string.salvar, (d, w) -> {
                    String nome = etNome.getText().toString().trim();
                    String valorStr = etValorObjetivo.getText().toString().trim();
                    if (nome.isEmpty() || valorStr.isEmpty()) {
                        Toast.makeText(requireContext(), R.string.erro_nome_obrigatorio, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        double valor = Double.parseDouble(valorStr.replace(',', '.'));
                        String valorInicialStr = etValorInicial.getText() != null ? etValorInicial.getText().toString().trim() : "";
                        double valorInicial = 0.0;
                        if (!valorInicialStr.isEmpty()) {
                            try { valorInicial = Double.parseDouble(valorInicialStr.replace(',', '.')); } catch (NumberFormatException ignored) { }
                        }
                        String dataAlvo = etDataAlvo.getText().toString().trim();
                        String descricao = etDescricao.getText().toString().trim();
                        viewModel.criar(nome, valor, valorInicial,
                                dataAlvo.isEmpty() ? null : dataAlvo,
                                descricao.isEmpty() ? null : descricao);
                    } catch (NumberFormatException e) {
                        Toast.makeText(requireContext(), R.string.erro_valor_invalido, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.carregar();
    }
}

