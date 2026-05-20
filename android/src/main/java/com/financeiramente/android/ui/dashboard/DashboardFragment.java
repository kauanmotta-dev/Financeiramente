package com.financeiramente.android.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.financeiramente.android.R;
import com.financeiramente.android.app.AppContext;
import com.financeiramente.android.viewmodel.DashboardViewModel;
import com.financeiramente.android.viewmodel.DashboardViewModelFactory;
import com.financeiramente.core.usecase.SaldoCategoria;
import com.financeiramente.core.usecase.SaldoMensalResult;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class DashboardFragment extends Fragment {

    private DashboardViewModel viewModel;
    private SaldoCategoriaAdapter adapter;

    private TextView tvSaldoDisponivel;
    private TextView tvGastoTotal;
    private TextView tvReceitaTotal;
    private ProgressBar pbGlobal;
    private View cardAlerta;
    private TextView tvAlerta;
    private TextView tvCategoriasVazio;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Vincular views
        tvSaldoDisponivel = view.findViewById(R.id.tv_saldo_disponivel);
        tvGastoTotal      = view.findViewById(R.id.tv_gasto_total);
        tvReceitaTotal    = view.findViewById(R.id.tv_receita_total);
        pbGlobal          = view.findViewById(R.id.pb_global);
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
                ctx.getCalcularSaldoMensalUseCase());
        viewModel = new ViewModelProvider(this, factory).get(DashboardViewModel.class);

        viewModel.getSaldoMensal().observe(getViewLifecycleOwner(), this::atualizarUI);

        // FAB → novo lançamento
        FloatingActionButton fab = view.findViewById(R.id.fab_novo_lancamento);
        fab.setOnClickListener(v ->
                Navigation.findNavController(view)
                        .navigate(R.id.action_dashboardFragment_to_lancamentoFormFragment));
    }

    @Override
    public void onResume() {
        super.onResume();
        // Recalcula ao voltar à tela
        if (viewModel != null) {
            viewModel.carregarDashboard();
        }
    }

    private void atualizarUI(SaldoMensalResult resultado) {
        // Saldo disponível
        tvSaldoDisponivel.setText(
                String.format(Locale.getDefault(), "R$ %.2f", resultado.getSaldoDisponivel()));

        // Totais
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

        // Lista de categorias
        List<SaldoCategoria> saldos = resultado.getSaldosPorCategoria();
        adapter.setItems(saldos);
        tvCategoriasVazio.setVisibility(saldos.isEmpty() ? View.VISIBLE : View.GONE);

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
}

