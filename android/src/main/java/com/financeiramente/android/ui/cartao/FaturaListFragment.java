package com.financeiramente.android.ui.cartao;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.viewpager2.widget.ViewPager2;

import com.financeiramente.android.R;
import com.financeiramente.android.app.AppContext;
import com.financeiramente.android.viewmodel.FaturaListViewModel;
import com.financeiramente.android.viewmodel.FaturaListViewModelFactory;
import com.google.android.material.snackbar.Snackbar;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FaturaListFragment extends Fragment {

    private FaturaListViewModel viewModel;
    private FaturaPagerAdapter pagerAdapter;
    private ViewPager2 vpFaturas;
    private TextView tvVazio;
    private boolean posicionadoNoMesAtual = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_fatura_list, container, false);
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

        String cartaoId = getArguments() != null ? getArguments().getString("cartaoId") : null;
        tvVazio = view.findViewById(R.id.tv_faturas_vazio);

        view.findViewById(R.id.btn_voltar_fatura_list).setOnClickListener(
                v -> Navigation.findNavController(view).navigateUp());

        view.findViewById(R.id.btn_ir_compras_cartao).setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("cartaoId", cartaoId);
            Navigation.findNavController(view)
                    .navigate(R.id.action_faturaListFragment_to_compraCartaoListFragment, args);
        });

        pagerAdapter = new FaturaPagerAdapter(fatura -> abrirDetalhe(view, fatura));
        vpFaturas = view.findViewById(R.id.vp_faturas);
        vpFaturas.setAdapter(pagerAdapter);
        vpFaturas.setOffscreenPageLimit(2);

        observarViewModel(view);

        if (cartaoId != null) {
            posicionadoNoMesAtual = false;
            viewModel.init(cartaoId);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        posicionadoNoMesAtual = false;
        if (viewModel != null) viewModel.carregar();
    }

    private void abrirDetalhe(View view, FaturaListViewModel.FaturaResumo resumo) {
        Bundle args = new Bundle();
        args.putString("faturaId", resumo.getFatura().getId());
        Navigation.findNavController(view)
                .navigate(R.id.action_faturaListFragment_to_faturaDetalheFragment, args);
    }

    private void observarViewModel(View view) {
        viewModel.getFaturas().observe(getViewLifecycleOwner(), faturas -> {
            boolean vazio = faturas == null || faturas.isEmpty();
            tvVazio.setVisibility(vazio ? View.VISIBLE : View.GONE);
            vpFaturas.setVisibility(vazio ? View.GONE : View.VISIBLE);

            if (!vazio) {
                List<FaturaListViewModel.FaturaResumo> ordenadas = new ArrayList<>(faturas);
                ordenadas.sort(Comparator.comparing(this::toYearMonthOrMin));
                pagerAdapter.submitList(ordenadas);
                posicionarNoMesAtual(ordenadas);
            }
        });

        viewModel.getErro().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) Snackbar.make(view, msg, Snackbar.LENGTH_LONG).show();
        });
    }

    private void posicionarNoMesAtual(List<FaturaListViewModel.FaturaResumo> faturas) {
        if (posicionadoNoMesAtual) return;
        int idx = indexOfMesAtualOuProximo(faturas);
        if (idx < 0) {
            idx = pagerAdapter.indexOfCurrentMonth();
        }
        if (idx < 0) {
            idx = faturas.size() - 1;
        }
        vpFaturas.setCurrentItem(idx, false);
        posicionadoNoMesAtual = true;
    }

    private int indexOfMesAtualOuProximo(List<FaturaListViewModel.FaturaResumo> faturas) {
        YearMonth mesAtual = YearMonth.now();
        for (int i = 0; i < faturas.size(); i++) {
            try {
                YearMonth mesFatura = YearMonth.parse(faturas.get(i).getFatura().getMes());
                if (!mesFatura.isBefore(mesAtual)) {
                    return i;
                }
            } catch (Exception ignored) {
            }
        }
        return -1;
    }

    private YearMonth toYearMonthOrMin(FaturaListViewModel.FaturaResumo resumo) {
        try {
            return YearMonth.parse(resumo.getFatura().getMes());
        } catch (Exception e) {
            return YearMonth.of(1900, 1);
        }
    }
}
