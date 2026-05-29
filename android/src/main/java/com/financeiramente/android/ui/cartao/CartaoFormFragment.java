package com.financeiramente.android.ui.cartao;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.financeiramente.android.R;
import com.financeiramente.android.app.AppContext;
import com.financeiramente.android.viewmodel.CartaoFormViewModel;
import com.financeiramente.android.viewmodel.CartaoFormViewModelFactory;
import com.financeiramente.core.domain.entity.CartaoCredito;
import com.financeiramente.core.domain.vo.BandeiraCartao;
import java.math.BigDecimal;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

public class CartaoFormFragment extends BottomSheetDialogFragment {

    private CartaoFormViewModel viewModel;
    private String editandoId;

    private TextInputEditText etNome;
    private ChipGroup cgBandeiras;
    private TextInputEditText etDiaVencimento;
    private TextInputEditText etDiasFechamento;
    private TextInputEditText etLimite;
    private View cardPreviewDatas;
    private TextView tvPreviewFechamento;
    private TextView tvPreviewVencimento;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cartao_form, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AppContext ctx = AppContext.get(requireContext());
        CartaoFormViewModelFactory factory = new CartaoFormViewModelFactory(
                ctx.getCriarCartaoCreditoUseCase(),
                ctx.getEditarCartaoCreditoUseCase(),
                ctx.getCoreServices().getCartaoRepository());
        viewModel = new ViewModelProvider(this, factory).get(CartaoFormViewModel.class);

        etNome = view.findViewById(R.id.et_cartao_nome);
        cgBandeiras = view.findViewById(R.id.cg_bandeiras);
        etDiaVencimento = view.findViewById(R.id.et_dia_vencimento);
        etDiasFechamento = view.findViewById(R.id.et_dias_fechamento);
        etLimite = view.findViewById(R.id.et_limite);
        cardPreviewDatas = view.findViewById(R.id.card_preview_datas);
        tvPreviewFechamento = view.findViewById(R.id.tv_preview_fechamento);
        tvPreviewVencimento = view.findViewById(R.id.tv_preview_vencimento);

        view.findViewById(R.id.btn_fechar_cartao_form).setOnClickListener(v -> dismiss());
        view.findViewById(R.id.btn_salvar_cartao).setOnClickListener(v -> salvar());

        if (getArguments() != null) {
            editandoId = getArguments().getString("cartaoId");
            if (editandoId != null) {
                ((TextView) view.findViewById(R.id.tv_titulo_cartao_form))
                        .setText(R.string.cartao_editar);
                viewModel.carregarCartao(editandoId);
            }
        }

        // Preview de datas em tempo real
        TextWatcher previewWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) { atualizarPreview(); }
        };
        etDiaVencimento.addTextChangedListener(previewWatcher);
        etDiasFechamento.addTextChangedListener(previewWatcher);

        observarViewModel(view);
    }

    private void atualizarPreview() {
        try {
            int diaVenc = Integer.parseInt(etDiaVencimento.getText().toString().trim());
            int diasFech = Integer.parseInt(etDiasFechamento.getText().toString().trim());
            if (diaVenc >= 1 && diaVenc <= 28 && diasFech >= 1 && diasFech <= 27) {
                viewModel.atualizarPreviewDatas(diaVenc, diasFech);
            }
        } catch (NumberFormatException ignored) {}
    }

    private void salvar() {
        String nome = etNome.getText() != null ? etNome.getText().toString().trim() : "";
        String diaVencStr = etDiaVencimento.getText() != null ? etDiaVencimento.getText().toString().trim() : "";
        String diasFechStr = etDiasFechamento.getText() != null ? etDiasFechamento.getText().toString().trim() : "";
        String limiteStr = etLimite.getText() != null ? etLimite.getText().toString().trim() : "";

        if (nome.isEmpty()) {
            etNome.setError(getString(R.string.erro_nome_obrigatorio));
            return;
        }

        int diaVencimento;
        int diasFechamento;
        try {
            diaVencimento = Integer.parseInt(diaVencStr);
            diasFechamento = Integer.parseInt(diasFechStr.isEmpty() ? "10" : diasFechStr);
        } catch (NumberFormatException e) {
            etDiaVencimento.setError(getString(R.string.cartao_erro_dia_invalido));
            return;
        }

        BigDecimal limite = null;
        if (!limiteStr.isEmpty()) {
            try { limite = new BigDecimal(limiteStr.replace(",", ".")); }
            catch (NumberFormatException ignored) {}
        }

        BandeiraCartao bandeira = getBandeiraSelecionada();

        if (editandoId != null) {
            viewModel.editar(editandoId, nome, diaVencimento, diasFechamento, limite, bandeira, null, null);
        } else {
            viewModel.criar(nome, diaVencimento, diasFechamento, limite, bandeira, null, null);
        }
    }

    @Nullable
    private BandeiraCartao getBandeiraSelecionada() {
        int checkedId = cgBandeiras.getCheckedChipId();
        if (checkedId == R.id.chip_visa) return BandeiraCartao.VISA;
        if (checkedId == R.id.chip_mastercard) return BandeiraCartao.MASTERCARD;
        if (checkedId == R.id.chip_elo) return BandeiraCartao.ELO;
        if (checkedId == R.id.chip_amex) return BandeiraCartao.AMEX;
        if (checkedId == R.id.chip_hipercard) return BandeiraCartao.HIPERCARD;
        if (checkedId == R.id.chip_outro) return BandeiraCartao.OUTRO;
        return null;
    }

    private void observarViewModel(View view) {
        viewModel.getCartaoEditando().observe(getViewLifecycleOwner(), cartao -> {
            if (cartao != null) preencherFormulario(cartao);
        });

        viewModel.getPreviewFechamento().observe(getViewLifecycleOwner(), data -> {
            if (data != null) {
                tvPreviewFechamento.setText(getString(R.string.cartao_preview_fechamento, data));
                cardPreviewDatas.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getPreviewVencimento().observe(getViewLifecycleOwner(), data -> {
            if (data != null) {
                tvPreviewVencimento.setText(getString(R.string.cartao_preview_vencimento, data));
                cardPreviewDatas.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getSucesso().observe(getViewLifecycleOwner(), ok -> {
            if (Boolean.TRUE.equals(ok)) {
                Bundle result = new Bundle();
                result.putBoolean("saved", true);
                if (editandoId != null) {
                    result.putString("cartaoId", editandoId);
                }
                getParentFragmentManager().setFragmentResult("cartao_form_saved", result);
                dismiss();
            }
        });

        viewModel.getErro().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) Snackbar.make(view, msg, Snackbar.LENGTH_LONG).show();
        });
    }

    private void preencherFormulario(CartaoCredito cartao) {
        etNome.setText(cartao.getNome());
        etDiaVencimento.setText(String.valueOf(cartao.getDiaVencimento()));
        etDiasFechamento.setText(String.valueOf(cartao.getDiasParaFechamento()));
        if (cartao.getLimite() != null) {
            etLimite.setText(String.format("%.2f", cartao.getLimite()));
        }
        if (cartao.getBandeira() != null) {
            switch (cartao.getBandeira()) {
                case VISA: cgBandeiras.check(R.id.chip_visa); break;
                case MASTERCARD: cgBandeiras.check(R.id.chip_mastercard); break;
                case ELO: cgBandeiras.check(R.id.chip_elo); break;
                case AMEX: cgBandeiras.check(R.id.chip_amex); break;
                case HIPERCARD: cgBandeiras.check(R.id.chip_hipercard); break;
                case OUTRO: cgBandeiras.check(R.id.chip_outro); break;
            }
        }
    }
}
