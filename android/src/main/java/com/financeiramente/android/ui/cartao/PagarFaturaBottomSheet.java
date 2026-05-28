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

import com.financeiramente.android.R;
import com.financeiramente.core.usecase.fatura.PagamentoFaturaResult;
import com.financeiramente.core.util.FinanceCalculator;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;

import java.math.BigDecimal;
import java.util.Locale;

public class PagarFaturaBottomSheet extends BottomSheetDialogFragment {

    public interface OnPagoListener {
        void onPago(double valorFatura, double valorPago);
    }

    private OnPagoListener onPagoListener;
    private TextInputEditText etValorExtrato;
    private TextInputEditText etValorPago;
    private TextView tvPreview;

    public void setOnPagoListener(OnPagoListener listener) {
        this.onPagoListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_pagar_fatura, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etValorExtrato = view.findViewById(R.id.et_valor_extrato);
        etValorPago = view.findViewById(R.id.et_valor_pago);
        tvPreview = view.findViewById(R.id.tv_preview_pagamento);

        double totalSugerido = getArguments() != null ? getArguments().getDouble("totalSugerido", 0.0) : 0.0;
        if (totalSugerido > 0) {
            etValorExtrato.setText(String.format(Locale.getDefault(), "%.2f", totalSugerido));
        }

        TextWatcher previewWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { atualizarPreview(); }
        };
        etValorExtrato.addTextChangedListener(previewWatcher);
        etValorPago.addTextChangedListener(previewWatcher);

        view.findViewById(R.id.btn_cancelar_pagar).setOnClickListener(v -> dismiss());
        view.findViewById(R.id.btn_confirmar_pagar).setOnClickListener(v -> confirmar());
    }

    private void atualizarPreview() {
        double valorExtrato = parseDouble(etValorExtrato);
        double valorPago = parseDouble(etValorPago);
        if (valorExtrato <= 0 || valorPago <= 0) {
            tvPreview.setVisibility(View.GONE);
            return;
        }
        PagamentoFaturaResult result = FinanceCalculator.calcularPagamentoFatura(
                BigDecimal.valueOf(valorExtrato),
                BigDecimal.valueOf(valorPago));
        String statusStr;
        switch (result.getStatusResultante()) {
            case PAGO: statusStr = getString(R.string.fatura_status_pago); break;
            case PAGO_PARCIAL: statusStr = getString(R.string.fatura_status_pago_parcial); break;
            default: statusStr = result.getStatusResultante().name();
        }
        String preview = getString(R.string.pagar_fatura_preview,
                valorPago, statusStr);
        if (result.getSaldoDevedor().compareTo(BigDecimal.ZERO) > 0) {
            preview += "\n" + getString(R.string.pagar_fatura_saldo_devedor, result.getSaldoDevedor().doubleValue());
        }
        tvPreview.setText(preview);
        tvPreview.setVisibility(View.VISIBLE);
    }

    private void confirmar() {
        double valorExtrato = parseDouble(etValorExtrato);
        double valorPago = parseDouble(etValorPago);
        if (valorExtrato <= 0) {
            etValorExtrato.setError(getString(R.string.erro_campo_obrigatorio));
            return;
        }
        if (valorPago <= 0) {
            etValorPago.setError(getString(R.string.erro_campo_obrigatorio));
            return;
        }
        if (onPagoListener != null) {
            onPagoListener.onPago(valorExtrato, valorPago);
        }
        dismiss();
    }

    private double parseDouble(TextInputEditText et) {
        try {
            String text = et.getText() != null ? et.getText().toString().trim().replace(",", ".") : "";
            return text.isEmpty() ? 0.0 : Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
