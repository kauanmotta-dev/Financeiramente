package com.financeiramente.android.ui.cartao;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.financeiramente.android.R;
import com.financeiramente.android.app.AppContext;
import com.financeiramente.android.viewmodel.FaturaListViewModel;
import com.financeiramente.android.viewmodel.FaturaListViewModelFactory;
import com.financeiramente.core.domain.entity.CompraCartao;
import com.financeiramente.core.domain.vo.TipoCompraCartao;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CompraCartaoListFragment extends Fragment {

    private static final int FILTRO_RECORRENTES = R.id.btn_tipo_recorrentes;
    private static final int FILTRO_PARCELADAS = R.id.btn_tipo_parceladas;

    private FaturaListViewModel viewModel;
    private TextView tvVazio;
    private TextView tvResumo;
    private TextView tvResumoSecundario;
    private LinearLayout layoutCompras;
    private MaterialButtonToggleGroup toggleTipoCompra;
    private List<CompraCartao> comprasRecorrentes = new ArrayList<>();
    private List<CompraCartao> comprasParceladas = new ArrayList<>();
    private int filtroAtual = FILTRO_RECORRENTES;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_compra_cartao_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AppContext ctx = AppContext.get(requireContext());
        FaturaListViewModelFactory factory = new FaturaListViewModelFactory(
                ctx.getCoreServices().getFaturaRepository(),
                ctx.getCoreServices().getLancamentoRepository(),
                ctx.getAtualizarStatusFaturasUseCase(),
                ctx.getAlterarStatusFaturaUseCase(),
                ctx.getListarComprasCartaoUseCase(),
                ctx.getCancelarRecorrenciaCartaoUseCase(),
                ctx.getDeletarCompraCartaoUseCase());
        viewModel = new ViewModelProvider(this, factory).get(FaturaListViewModel.class);

        tvVazio = view.findViewById(R.id.tv_compras_cartao_vazio);
        tvResumo = view.findViewById(R.id.tv_compras_cartao_resumo);
        tvResumoSecundario = view.findViewById(R.id.tv_compras_cartao_resumo_secundario);
        layoutCompras = view.findViewById(R.id.layout_compras_cartao_lista);
        toggleTipoCompra = view.findViewById(R.id.toggle_tipo_compra);

        view.findViewById(R.id.btn_voltar_compra_cartao_list)
                .setOnClickListener(v -> Navigation.findNavController(view).navigateUp());

        toggleTipoCompra.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked || checkedId == filtroAtual) {
                return;
            }
            filtroAtual = checkedId;
            renderCompras();
        });

        String cartaoId = getArguments() != null ? getArguments().getString("cartaoId") : null;
        if (cartaoId != null) {
            viewModel.init(cartaoId);
        }

        observarViewModel(view);

        getParentFragmentManager().setFragmentResultListener(
            "lancamento_salvo", getViewLifecycleOwner(),
            (requestKey, bundle) -> viewModel.carregar());

        toggleTipoCompra.check(FILTRO_RECORRENTES);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.carregar();
        }
    }

    private void observarViewModel(View view) {
        viewModel.getComprasRecorrentes().observe(getViewLifecycleOwner(), compras -> {
            comprasRecorrentes = compras != null ? compras : new ArrayList<>();
            renderCompras();
        });
        viewModel.getComprasParceladas().observe(getViewLifecycleOwner(), compras -> {
            comprasParceladas = compras != null ? compras : new ArrayList<>();
            renderCompras();
        });
        viewModel.getErro().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) {
                Snackbar.make(view, msg, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void renderCompras() {
        layoutCompras.removeAllViews();
        boolean modoParceladas = filtroAtual == FILTRO_PARCELADAS;
        List<CompraCartao> comprasAtuais = modoParceladas ? comprasParceladas : comprasRecorrentes;
        int total = comprasAtuais != null ? comprasAtuais.size() : 0;

        String titulo = getString(modoParceladas
                ? R.string.compras_cartao_secao_parceladas
                : R.string.compras_cartao_secao_recorrentes);
        tvResumo.setText(getString(R.string.compras_cartao_resumo_formatado, total, titulo.toLowerCase(Locale.getDefault())));
        tvResumoSecundario.setText(modoParceladas
                ? R.string.compras_cartao_resumo_parceladas
                : R.string.compras_cartao_resumo_recorrentes);

        if (total == 0) {
            tvVazio.setVisibility(View.VISIBLE);
            tvVazio.setText(modoParceladas
                    ? R.string.compras_cartao_vazio_parceladas
                    : R.string.compras_cartao_vazio_recorrentes);
            return;
        }

        tvVazio.setVisibility(View.GONE);
        for (CompraCartao compra : comprasAtuais) {
            layoutCompras.addView(criarItemCompra(compra));
        }
    }

    private View criarItemCompra(CompraCartao compra) {
        View item = LayoutInflater.from(requireContext())
            .inflate(R.layout.item_recorrencia_cartao, layoutCompras, false);

        Chip chipTipo = item.findViewById(R.id.chip_tipo_compra);
        TextView tvDescricao = item.findViewById(R.id.tv_recorrencia_descricao);
        TextView tvDetalhes = item.findViewById(R.id.tv_recorrencia_detalhes);
        MaterialButton btnExcluir = item.findViewById(R.id.btn_excluir_compra);

        configurarChipTipo(chipTipo, compra.getTipo());
        tvDescricao.setText(compra.getDescricao());
        tvDetalhes.setText(formatarDetalhesCompra(compra));

        btnExcluir.setOnClickListener(v -> confirmarExclusao(compra));
        item.setOnLongClickListener(v -> {
            mostrarAcoesCompra(compra);
            return true;
        });

        return item;
    }

    private void configurarChipTipo(Chip chip, TipoCompraCartao tipo) {
        if (chip == null) {
            return;
        }
        boolean recorrente = tipo == TipoCompraCartao.RECORRENTE;
        chip.setText(recorrente
                ? R.string.compras_cartao_secao_recorrentes
                : R.string.compras_cartao_secao_parceladas);
        int cor = ContextCompat.getColor(requireContext(), recorrente
                ? R.color.verde_success
                : R.color.azul_primary);
        int fundo = ContextCompat.getColor(requireContext(), recorrente
                ? R.color.verde_success_container
                : R.color.azul_primary_container);
        chip.setChipBackgroundColor(ColorStateList.valueOf(fundo));
        chip.setChipStrokeColor(ColorStateList.valueOf(cor));
        chip.setTextColor(cor);
    }

    private String formatarDetalhesCompra(CompraCartao compra) {
        String dataCompra = formatarData(compra.getDataCompra());
        if (compra.getTipo() == TipoCompraCartao.PARCELADO) {
            return String.format(
                    Locale.getDefault(),
                    "%s • %d parcelas • R$ %.2f",
                    dataCompra,
                    compra.getTotalParcelas(),
                    compra.getValorTotal());
        }
        if (compra.getTipo() == TipoCompraCartao.RECORRENTE) {
            return getString(
                    R.string.recorrencia_cartao_detalhes_com_data,
                    dataCompra,
                    compra.getDiaRecorrencia(),
                    String.format(Locale.getDefault(), "R$ %.2f", compra.getValorTotal()));
        }
        return String.format(Locale.getDefault(), "%s • R$ %.2f • À vista", dataCompra, compra.getValorTotal());
    }

    private String formatarData(String dataIso) {
        if (dataIso == null || dataIso.trim().isEmpty()) {
            return "Sem data";
        }
        try {
            LocalDate data = LocalDate.parse(dataIso, DateTimeFormatter.ISO_LOCAL_DATE);
            return data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e) {
            return dataIso;
        }
    }

    private void mostrarAcoesCompra(CompraCartao compra) {
        CharSequence[] opcoes = new CharSequence[] {
                getString(R.string.editar),
                getString(R.string.excluir)
        };
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(compra.getDescricao())
                .setItems(opcoes, (dialog, which) -> {
                    if (which == 0) {
                        editarCompra(compra);
                    } else if (which == 1) {
                        confirmarExclusao(compra);
                    }
                })
                .show();
    }

    private void editarCompra(CompraCartao compra) {
        if (!isAdded()) {
            return;
        }
        Bundle args = new Bundle();
        args.putString("compraCartaoId", compra.getId());
        args.putInt("sourceTab", R.id.nav_dashboard);
        Navigation.findNavController(requireView()).navigate(R.id.lancamentoFormFragment, args);
    }

    private void confirmarExclusao(CompraCartao compra) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.excluir_compra_cartao_titulo)
                .setMessage(R.string.excluir_compra_cartao_mensagem)
                .setPositiveButton(R.string.excluir,
                        (dialog, which) -> viewModel.excluirCompraCartao(compra.getId()))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}
