package com.financeiramente.android.ui.planejamento;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.financeiramente.android.R;
import com.financeiramente.android.app.AppContext;
import com.financeiramente.android.viewmodel.PlanejamentoViewModel;
import com.financeiramente.android.viewmodel.PlanejamentoViewModelFactory;
import com.financeiramente.core.domain.entity.Categoria;
import com.financeiramente.core.domain.entity.PlanejamentoCategoria;
import com.financeiramente.core.domain.entity.PlanejamentoMensal;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
public class PlanejamentoFragment extends Fragment {

    private PlanejamentoViewModel viewModel;

    private TextView tvTitulo;
    private TextView tvBadgeConfirmado;
    private TextInputEditText etReceitaEsperada;
    private TextInputEditText etReservaImprevisto;
    private ListView lvCategoriasPlano;

    private List<Categoria> categoriasList = new ArrayList<>();
    private List<PlanejamentoCategoria> itensList = new ArrayList<>();
    private final Map<String, Double> limitesEditados = new HashMap<>();

    private PlanejamentoCategoriaAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_planejamento, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AppContext ctx = AppContext.get(requireContext());
        PlanejamentoViewModelFactory factory = new PlanejamentoViewModelFactory(
                ctx.getCriarPlanejamentoMensalUseCase(),
                ctx.getConfirmarPlanejamentoUseCase(),
                ctx.getDefinirLimiteCategoriaUseCase(),
                ctx.getDefinirComoPlanosPadraoUseCase(),
                ctx.getPlanejamentoRepository(),
                ctx.getCategoriaRepository());
        viewModel = new ViewModelProvider(this, factory).get(PlanejamentoViewModel.class);

        tvTitulo             = view.findViewById(R.id.tv_titulo_planejamento);
        tvBadgeConfirmado    = view.findViewById(R.id.tv_badge_confirmado);
        etReceitaEsperada    = view.findViewById(R.id.et_receita_esperada);
        etReservaImprevisto  = view.findViewById(R.id.et_reserva_imprevisto);
        lvCategoriasPlano    = view.findViewById(R.id.lv_categorias_plano);

        adapter = new PlanejamentoCategoriaAdapter(
                requireContext(), categoriasList, itensList, limitesEditados,
                (categoriaId, limite) -> viewModel.definirLimiteCategoria(categoriaId, limite));
        lvCategoriasPlano.setAdapter(adapter);

        view.findViewById(R.id.btn_salvar_cabecalho).setOnClickListener(v -> salvarCabecalho());
        view.findViewById(R.id.btn_confirmar_plano).setOnClickListener(v -> viewModel.confirmarPlano());
        view.findViewById(R.id.btn_definir_padrao).setOnClickListener(v -> viewModel.definirComoPadrao());

        viewModel.getPlanoAtual().observe(getViewLifecycleOwner(), this::preencherCabecalho);
        viewModel.getItensCategoria().observe(getViewLifecycleOwner(), itens -> {
            itensList.clear();
            itensList.addAll(itens);
            adapter.notifyDataSetChanged();
        });
        viewModel.getTodasCategorias().observe(getViewLifecycleOwner(), categorias -> {
            categoriasList.clear();
            categoriasList.addAll(categorias);
            adapter.notifyDataSetChanged();
        });
        viewModel.getErro().observe(getViewLifecycleOwner(), erro -> {
            if (erro != null) Toast.makeText(requireContext(), erro, Toast.LENGTH_LONG).show();
        });
        viewModel.getSucesso().observe(getViewLifecycleOwner(), ok -> {
            if (Boolean.TRUE.equals(ok)) {
                Toast.makeText(requireContext(),
                        R.string.planejamento_salvo_com_sucesso, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void preencherCabecalho(PlanejamentoMensal plano) {
        if (plano == null) return;

        String[] meses = {"Janeiro","Fevereiro","Março","Abril","Maio","Junho",
                "Julho","Agosto","Setembro","Outubro","Novembro","Dezembro"};
        String nomeMes = meses[plano.getMes() - 1];
        tvTitulo.setText(getString(R.string.planejamento_titulo_mes, nomeMes, plano.getAno()));

        if (plano.isConfirmado()) {
            tvBadgeConfirmado.setVisibility(View.VISIBLE);
        } else {
            tvBadgeConfirmado.setVisibility(View.GONE);
        }

        if (plano.getReceitaEsperada() > 0) {
            etReceitaEsperada.setText(
                    String.format(Locale.getDefault(), "%.2f", plano.getReceitaEsperada()));
        }
        if (plano.getReservaImprevisto() > 0) {
            etReservaImprevisto.setText(
                    String.format(Locale.getDefault(), "%.2f", plano.getReservaImprevisto()));
        }
    }

    private void salvarCabecalho() {
        double receita = parseDouble(etReceitaEsperada);
        double reserva = parseDouble(etReservaImprevisto);
        viewModel.atualizarCabecalho(receita, reserva);
        Toast.makeText(requireContext(),
                R.string.planejamento_salvo_com_sucesso, Toast.LENGTH_SHORT).show();
    }

    private double parseDouble(TextInputEditText et) {
        try {
            String txt = et.getText() != null ? et.getText().toString().replace(",", ".") : "0";
            return Double.parseDouble(txt);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        limitesEditados.clear();
        viewModel.carregarPlano();
    }
}
