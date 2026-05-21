package com.financeiramente.android.ui.gastos;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.financeiramente.android.R;
import com.financeiramente.android.app.AppContext;
import com.financeiramente.android.viewmodel.GastosCategoriaViewModel;
import com.financeiramente.android.viewmodel.GastosCategoriaViewModelFactory;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.color.MaterialColors;

import java.text.NumberFormat;
import java.util.Locale;

public class GastosCategoriaFragment extends Fragment {

    private GastosCategoriaViewModel viewModel;
    private GastosCategoriaAdapter adapter;
    private TextView tvCompetencia;
    private TextView tvTotalRecebido;
    private TextView tvTotalGasto;
    private View emptyState;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gastos_categoria, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AppContext appContext = AppContext.get(requireContext());
        GastosCategoriaViewModelFactory factory = new GastosCategoriaViewModelFactory(
                appContext.getCategoriaRepository(),
                appContext.getLancamentoRepository());
        viewModel = new ViewModelProvider(this, factory).get(GastosCategoriaViewModel.class);

        tvCompetencia = view.findViewById(R.id.tv_competencia);
        tvTotalRecebido = view.findViewById(R.id.tv_total_recebido);
        tvTotalGasto = view.findViewById(R.id.tv_total_gasto);
        emptyState = view.findViewById(R.id.layout_empty_state);
        configurarEmptyState();

        MaterialButton btnMesAnterior = view.findViewById(R.id.btn_mes_anterior);
        MaterialButton btnProximoMes = view.findViewById(R.id.btn_mes_proximo);
        MaterialButton btnAbrirGraficos = view.findViewById(R.id.btn_abrir_graficos);
        MaterialButton btnAbrirLancamentos = view.findViewById(R.id.btn_abrir_lancamentos);

        btnMesAnterior.setOnClickListener(v -> viewModel.mesAnterior());
        btnProximoMes.setOnClickListener(v -> viewModel.proximoMes());
        btnAbrirGraficos.setOnClickListener(v ->
            Navigation.findNavController(view).navigate(R.id.nav_relatorios));
        btnAbrirLancamentos.setOnClickListener(v ->
            Navigation.findNavController(view).navigate(R.id.lancamentosListFragment));

        MaterialButtonToggleGroup toggleModo = view.findViewById(R.id.toggle_modo);
        MaterialButton btnModoRecebido = view.findViewById(R.id.btn_modo_recebido);
        MaterialButton btnModoGastos = view.findViewById(R.id.btn_modo_gastos);
        toggleModo.check(R.id.btn_modo_gastos);
        atualizarEstiloModo(btnModoRecebido, btnModoGastos, R.id.btn_modo_gastos);
        toggleModo.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) {
                return;
            }
            atualizarEstiloModo(btnModoRecebido, btnModoGastos, checkedId);
            if (checkedId == R.id.btn_modo_recebido) {
                viewModel.setModoExibicao(GastosCategoriaViewModel.ModoExibicao.RECEBIDOS);
            } else {
                viewModel.setModoExibicao(GastosCategoriaViewModel.ModoExibicao.GASTOS);
            }
        });

        RecyclerView recyclerView = view.findViewById(R.id.rv_gastos_categoria);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new GastosCategoriaAdapter(requireContext());
        recyclerView.setAdapter(adapter);

        observarViewModel();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.carregar();
        }
    }

    private void observarViewModel() {
        viewModel.getCompetenciaLabel().observe(getViewLifecycleOwner(),
                label -> tvCompetencia.setText(label));

        viewModel.getSecoes().observe(getViewLifecycleOwner(), secoes -> {
            adapter.submitData(secoes);
            boolean vazio = secoes == null || secoes.isEmpty();
            emptyState.setVisibility(vazio ? View.VISIBLE : View.GONE);
        });

        viewModel.getTotalRecebido().observe(getViewLifecycleOwner(), valor -> {
            double total = valor != null ? valor : 0d;
            tvTotalRecebido.setText(currencyFormat.format(total));
        });

        viewModel.getTotalGasto().observe(getViewLifecycleOwner(), valor -> {
            double total = valor != null ? valor : 0d;
            tvTotalGasto.setText(currencyFormat.format(total));
        });

        viewModel.getErro().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.trim().isEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void configurarEmptyState() {
        ((android.widget.ImageView) emptyState.findViewById(R.id.iv_empty_illustration))
                .setImageResource(R.drawable.ic_empty_categories);
        ((TextView) emptyState.findViewById(R.id.tv_empty_title))
                .setText(R.string.empty_gastos_title);
        ((TextView) emptyState.findViewById(R.id.tv_empty_subtitle))
                .setText(R.string.empty_gastos_subtitle);
        emptyState.findViewById(R.id.btn_empty_cta).setVisibility(View.GONE);
    }

    private void atualizarEstiloModo(MaterialButton btnRecebido,
                                     MaterialButton btnGastos,
                                     int checkedId) {
        int secondary = MaterialColors.getColor(btnRecebido, com.google.android.material.R.attr.colorSecondary);
        int secondaryContainer = MaterialColors.getColor(btnRecebido, com.google.android.material.R.attr.colorSecondaryContainer);
        int error = MaterialColors.getColor(btnGastos, com.google.android.material.R.attr.colorError);
        int errorContainer = MaterialColors.getColor(btnGastos, com.google.android.material.R.attr.colorErrorContainer);
        int onSurfaceVariant = MaterialColors.getColor(btnRecebido, com.google.android.material.R.attr.colorOnSurfaceVariant);
        int surface = MaterialColors.getColor(btnRecebido, com.google.android.material.R.attr.colorSurface);

        if (checkedId == R.id.btn_modo_recebido) {
            estilizarBotaoSelecionado(btnRecebido, secondary, secondaryContainer);
            estilizarBotaoNaoSelecionado(btnGastos, onSurfaceVariant, surface);
        } else {
            estilizarBotaoSelecionado(btnGastos, error, errorContainer);
            estilizarBotaoNaoSelecionado(btnRecebido, onSurfaceVariant, surface);
        }
    }

    private void estilizarBotaoSelecionado(MaterialButton button, int strokeColor, int backgroundColor) {
        button.setStrokeColor(ColorStateList.valueOf(strokeColor));
        button.setBackgroundTintList(ColorStateList.valueOf(backgroundColor));
        button.setTextColor(strokeColor);
    }

    private void estilizarBotaoNaoSelecionado(MaterialButton button, int strokeColor, int backgroundColor) {
        button.setStrokeColor(ColorStateList.valueOf(strokeColor));
        button.setBackgroundTintList(ColorStateList.valueOf(backgroundColor));
        button.setTextColor(strokeColor);
    }
}
