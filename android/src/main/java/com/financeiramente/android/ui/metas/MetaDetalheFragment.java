package com.financeiramente.android.ui.metas;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
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
import com.financeiramente.android.viewmodel.MetaDetalheViewModel;
import com.financeiramente.android.viewmodel.MetaDetalheViewModelFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;

public class MetaDetalheFragment extends Fragment {

    private MetaDetalheViewModel viewModel;
    private AporteAdapter aporteAdapter;
    private String metaId;

    private ProgressBar progressBar;
    private TextView tvPercentual;
    private TextView tvValores;
    private TextView tvProjecao;
    private TextView tvVazioAportes;
    private TextView tvTituloMeta;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_meta_detalhe, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.btn_back_meta_detalhe).setOnClickListener(v ->
            Navigation.findNavController(view).navigateUp());

        if (getArguments() != null) {
            metaId = getArguments().getString("metaId");
        }

        AppContext ctx = AppContext.get(requireContext());
        MetaDetalheViewModelFactory factory = new MetaDetalheViewModelFactory(
                ctx.getCoreServices().getMetaRepository(),
                ctx.getCoreServices().getAporteMetaRepository(),
                ctx.getRegistrarAporteMetaUseCase(),
                ctx.getDeletarAporteMetaUseCase(),
                ctx.getCalcularProjecaoMetaUseCase());
        viewModel = new ViewModelProvider(this, factory).get(MetaDetalheViewModel.class);

        progressBar = view.findViewById(R.id.pb_detalhe_progresso);
        tvPercentual = view.findViewById(R.id.tv_detalhe_percentual);
        tvValores = view.findViewById(R.id.tv_detalhe_valores);
        tvProjecao = view.findViewById(R.id.tv_detalhe_projecao);
        tvVazioAportes = view.findViewById(R.id.tv_vazio_aportes);
        tvTituloMeta = view.findViewById(R.id.tv_titulo_meta_detalhe);

        RecyclerView rvAportes = view.findViewById(R.id.rv_aportes);
        rvAportes.setLayoutManager(new LinearLayoutManager(requireContext()));
        aporteAdapter = new AporteAdapter(aporte -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle(R.string.deletar_aporte)
                    .setMessage(R.string.confirmar_deletar_aporte)
                    .setPositiveButton(android.R.string.ok,
                            (d, w) -> viewModel.deletarAporte(aporte.getId(), metaId))
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        });
        rvAportes.setAdapter(aporteAdapter);

        viewModel.getMeta().observe(getViewLifecycleOwner(), meta -> {
            if (meta == null) return;
            double totalSalvo = meta.getValorAtual().add(meta.getValorInicial()).doubleValue();
            double pct = meta.getValorObjetivo().compareTo(BigDecimal.ZERO) > 0
                    ? (totalSalvo / meta.getValorObjetivo().doubleValue()) * 100 : 0;
            progressBar.setMax(100);
            progressBar.setProgress((int) Math.min(pct, 100));
            tvPercentual.setText(String.format(Locale.getDefault(), "%.1f%%", Math.min(pct, 100)));
            tvValores.setText(String.format(Locale.getDefault(),
                    "R$ %.2f / R$ %.2f", totalSalvo, meta.getValorObjetivo()));
                tvTituloMeta.setText(meta.getNome());
        });

        viewModel.getAportes().observe(getViewLifecycleOwner(), lista -> {
            aporteAdapter.setItems(lista);
            tvVazioAportes.setVisibility(lista == null || lista.isEmpty() ? View.VISIBLE : View.GONE);
        });

        viewModel.getProjecao().observe(getViewLifecycleOwner(), data -> {
            if (data != null) {
                tvProjecao.setText(getString(R.string.meta_projecao, data.toString()));
            } else {
                tvProjecao.setText(R.string.meta_projecao_sem_dados);
            }
        });

        viewModel.getErro().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
        });

        view.findViewById(R.id.btn_novo_aporte).setOnClickListener(v -> mostrarDialogoNovoAporte());

        if (metaId != null) {
            viewModel.carregar(metaId);
        }
    }

    private void mostrarDialogoNovoAporte() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.fragment_aporte_form, null);
        EditText etValor = dialogView.findViewById(R.id.et_aporte_valor);
        EditText etData = dialogView.findViewById(R.id.et_aporte_data);
        EditText etDescricao = dialogView.findViewById(R.id.et_aporte_descricao);

        // Preenche data com hoje
        etData.setText(LocalDate.now().toString());

        etData.setOnClickListener(v -> {
            LocalDate hoje = LocalDate.now();
            new DatePickerDialog(requireContext(),
                    (dp, y, m, d) -> etData.setText(
                            String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d)),
                    hoje.getYear(), hoje.getMonthValue() - 1, hoje.getDayOfMonth())
                    .show();
        });

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.add_aporte)
                .setView(dialogView)
                .setPositiveButton(R.string.salvar, (d, w) -> {
                    String valorStr = etValor.getText().toString().trim();
                    String data = etData.getText().toString().trim();
                    if (valorStr.isEmpty() || data.isEmpty()) {
                        Toast.makeText(requireContext(), R.string.erro_valor_obrigatorio, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        double valor = Double.parseDouble(valorStr.replace(',', '.'));
                        String descricao = etDescricao.getText().toString().trim();
                        viewModel.registrarAporte(metaId, valor, data,
                                descricao.isEmpty() ? null : descricao);
                    } catch (NumberFormatException e) {
                        Toast.makeText(requireContext(), R.string.erro_valor_invalido, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}
