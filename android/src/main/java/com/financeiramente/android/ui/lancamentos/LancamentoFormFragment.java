package com.financeiramente.android.ui.lancamentos;

import android.app.DatePickerDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.financeiramente.android.R;
import com.financeiramente.android.app.AppContext;
import com.financeiramente.android.viewmodel.LancamentoFormViewModel;
import com.financeiramente.android.viewmodel.LancamentoFormViewModelFactory;
import com.financeiramente.core.domain.entity.Categoria;
import com.financeiramente.core.domain.entity.Tag;
import com.financeiramente.core.domain.vo.TipoLancamento;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class LancamentoFormFragment extends BottomSheetDialogFragment {

    private LancamentoFormViewModel viewModel;

    private MaterialButtonToggleGroup toggleTipo;
    private TextInputEditText etValor;
    private TextInputLayout tilValor;
    private TextInputEditText etDescricao;
    private TextInputEditText etData;
    private ChipGroup cgCategoria;
    private ChipGroup cgTags;
    private LinearLayout layoutDetalhesCompletos;

    private List<Categoria> listaCategorias = new ArrayList<>();
    private String categoriaIdSelecionada = null;

    private String editandoId = null;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_lancamento_form, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AppContext ctx = AppContext.get(requireContext());
        LancamentoFormViewModelFactory factory = new LancamentoFormViewModelFactory(
                ctx.getRegistrarLancamentoUseCase(),
                ctx.getEditarLancamentoUseCase(),
                ctx.getCategoriaRepository(),
                ctx.getTagRepository(),
                ctx.getLancamentoRepository());
        viewModel = new ViewModelProvider(this, factory).get(LancamentoFormViewModel.class);

        toggleTipo = view.findViewById(R.id.toggle_tipo);
        etValor = view.findViewById(R.id.et_valor);
        tilValor = view.findViewById(R.id.til_valor);
        etDescricao = view.findViewById(R.id.et_descricao);
        etData = view.findViewById(R.id.et_data);
        cgCategoria = view.findViewById(R.id.cg_categoria);
        cgTags = view.findViewById(R.id.cg_tags);
        layoutDetalhesCompletos = view.findViewById(R.id.layout_detalhes_completos);

        // Data padrão = hoje
        etData.setText(LocalDate.now().format(FORMATTER));
        etData.setOnClickListener(v -> abrirDatePicker());

        // Seleção inicial = DESPESA
        toggleTipo.check(R.id.btn_despesa);
        atualizarEstiloToggle(TipoLancamento.DESPESA);

        toggleTipo.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                TipoLancamento tipo = (checkedId == R.id.btn_receita)
                        ? TipoLancamento.RECEITA : TipoLancamento.DESPESA;
                atualizarEstiloToggle(tipo);
                categoriaIdSelecionada = null;
                viewModel.carregarCategoriasPorTipo(tipo);
            }
        });

        configurarBottomSheet();

        view.findViewById(R.id.btn_salvar).setOnClickListener(v -> salvar());

        // Verificar se é edição
        if (getArguments() != null) {
            editandoId = getArguments().getString("lancamentoId");
            if (editandoId != null) {
                // Modo edição: exibir título correto e expandir campos completos
                TextView tvTitulo = view.findViewById(R.id.tv_titulo_form);
                tvTitulo.setText(R.string.editar_lancamento);
                layoutDetalhesCompletos.setVisibility(View.VISIBLE);
                viewModel.carregarLancamento(editandoId);
            }
        }

        observarViewModel();
    }

    /** Configura o BottomSheetBehavior: peek = modo rápido, expanded = modo completo. */
    private void configurarBottomSheet() {
        if (!(getDialog() instanceof BottomSheetDialog)) return;
        BottomSheetDialog bsd = (BottomSheetDialog) getDialog();
        bsd.setOnShowListener(d -> {
            BottomSheetBehavior<FrameLayout> behavior = bsd.getBehavior();
            // ~380dp — cobre drag handle + título + toggle + valor + categoria chips + botão salvar
            int peekPx = (int) (getResources().getDisplayMetrics().density * 380);
            behavior.setPeekHeight(peekPx);
            behavior.setSkipCollapsed(false);
            behavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState) { }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                    // Mostra seção completa ao passar de metade do caminho entre collapsed e expanded
                    if (slideOffset > 0.4f) {
                        if (layoutDetalhesCompletos.getVisibility() != View.VISIBLE) {
                            layoutDetalhesCompletos.setVisibility(View.VISIBLE);
                        }
                    } else {
                        if (layoutDetalhesCompletos.getVisibility() == View.VISIBLE
                                && editandoId == null) {
                            layoutDetalhesCompletos.setVisibility(View.GONE);
                        }
                    }
                }
            });
        });
    }

    /**
     * Aplica cor de fundo e texto nos botões do toggle de acordo com o tipo selecionado.
     * Despesa → vermelho;  Receita → verde.
     */
    private void atualizarEstiloToggle(TipoLancamento tipo) {
        MaterialButton btnDespesa = requireView().findViewById(R.id.btn_despesa);
        MaterialButton btnReceita = requireView().findViewById(R.id.btn_receita);

        int corNeutra = getResources().getColor(R.color.cinza_secondary, requireContext().getTheme());

        if (tipo == TipoLancamento.DESPESA) {
            btnDespesa.setBackgroundTintList(ColorStateList.valueOf(
                    getResources().getColor(R.color.vermelho_error_container, requireContext().getTheme())));
            btnDespesa.setTextColor(
                    getResources().getColor(R.color.vermelho_error, requireContext().getTheme()));
            btnReceita.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
            btnReceita.setTextColor(corNeutra);
        } else {
            btnReceita.setBackgroundTintList(ColorStateList.valueOf(
                    getResources().getColor(R.color.verde_success_container, requireContext().getTheme())));
            btnReceita.setTextColor(
                    getResources().getColor(R.color.verde_success, requireContext().getTheme()));
            btnDespesa.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
            btnDespesa.setTextColor(corNeutra);
        }
    }

    private void observarViewModel() {
        viewModel.getCategorias().observe(getViewLifecycleOwner(), cats -> {
            listaCategorias = cats;
            popularChipsCategoria(cats);
        });

        viewModel.getTags().observe(getViewLifecycleOwner(), tags -> {
            cgTags.removeAllViews();
            for (Tag tag : tags) {
                Chip chip = new Chip(requireContext());
                chip.setText(tag.getNome());
                chip.setCheckable(true);
                chip.setTag(tag.getId());
                cgTags.addView(chip);
            }
        });

        viewModel.getLancamentoCarregado().observe(getViewLifecycleOwner(), lancamento -> {
            if (lancamento == null) return;
            etValor.setText(String.valueOf(lancamento.getValor()));
            etDescricao.setText(lancamento.getDescricao());
            etData.setText(lancamento.getData());

            TipoLancamento tipo = lancamento.getTipo();
            toggleTipo.check(tipo == TipoLancamento.RECEITA ? R.id.btn_receita : R.id.btn_despesa);
            atualizarEstiloToggle(tipo);

            categoriaIdSelecionada = lancamento.getCategoriaId();
            viewModel.carregarCategoriasPorTipo(tipo);
        });

        viewModel.getSucesso().observe(getViewLifecycleOwner(), ok -> {
            if (Boolean.TRUE.equals(ok)) {
                String lancId = viewModel.getSavedLancamentoId().getValue();
                android.os.Bundle result = new android.os.Bundle();
                result.putBoolean("saved", true);
                if (lancId != null) result.putString("lancamentoId", lancId);
                getParentFragmentManager().setFragmentResult("lancamento_salvo", result);
                dismiss();
            }
        });

        viewModel.getErro().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
        });
    }

    /** Popula o ChipGroup de categorias com chips coloridos (ícone + nome). */
    private void popularChipsCategoria(List<Categoria> cats) {
        cgCategoria.removeAllViews();
        for (Categoria cat : cats) {
            Chip chip = new Chip(requireContext());
            chip.setText(cat.getIcone() + "  " + cat.getNome());
            chip.setCheckable(true);
            chip.setTag(cat.getId());

            // Cor dinâmica da categoria: fundo claro (alpha 30) → fundo sólido quando checked
            try {
                int corSolida = Color.parseColor(cat.getCor());
                int corFundo = Color.argb(48, Color.red(corSolida), Color.green(corSolida), Color.blue(corSolida));
                chip.setChipBackgroundColor(new ColorStateList(
                        new int[][]{new int[]{android.R.attr.state_checked}, new int[]{}},
                        new int[]{corSolida, corFundo}
                ));
                chip.setTextColor(new ColorStateList(
                        new int[][]{new int[]{android.R.attr.state_checked}, new int[]{}},
                        new int[]{Color.WHITE, corSolida}
                ));
                chip.setChipStrokeColor(ColorStateList.valueOf(corSolida));
                chip.setChipStrokeWidth(getResources().getDimension(R.dimen.chip_stroke_width));
            } catch (IllegalArgumentException ignored) { }

            if (cat.getId().equals(categoriaIdSelecionada)) {
                chip.setChecked(true);
            }

            cgCategoria.addView(chip);
        }
    }

    private void salvar() {
        tilValor.setError(null);

        String valorStr = etValor.getText() != null ? etValor.getText().toString().trim() : "";
        boolean valido = true;

        if (valorStr.isEmpty()) {
            tilValor.setError(getString(R.string.erro_valor_obrigatorio));
            valido = false;
        }

        double valor = 0;
        if (valido) {
            try {
                valor = Double.parseDouble(valorStr);
                if (valor <= 0) {
                    tilValor.setError(getString(R.string.erro_valor_invalido));
                    valido = false;
                }
            } catch (NumberFormatException e) {
                tilValor.setError(getString(R.string.erro_valor_invalido));
                valido = false;
            }
        }
        if (!valido) return;

        TipoLancamento tipo = (toggleTipo.getCheckedButtonId() == R.id.btn_receita)
                ? TipoLancamento.RECEITA : TipoLancamento.DESPESA;

        // Categoria selecionada no ChipGroup
        int checkedChipId = cgCategoria.getCheckedChipId();
        if (checkedChipId != View.NO_ID) {
            Chip chip = cgCategoria.findViewById(checkedChipId);
            categoriaIdSelecionada = (String) chip.getTag();
        }
        if (categoriaIdSelecionada == null) {
            Toast.makeText(requireContext(), getString(R.string.erro_categoria_obrigatoria), Toast.LENGTH_SHORT).show();
            return;
        }

        // Descrição (opcional no modo rápido)
        String descricao = (etDescricao.getText() != null)
                ? etDescricao.getText().toString().trim() : "";

        // Data
        String data = (etData.getText() != null && !etData.getText().toString().isEmpty())
                ? etData.getText().toString().trim()
                : LocalDate.now().format(FORMATTER);

        // Tags selecionadas
        List<String> tagIds = new ArrayList<>();
        for (int i = 0; i < cgTags.getChildCount(); i++) {
            Chip chip = (Chip) cgTags.getChildAt(i);
            if (chip.isChecked()) tagIds.add((String) chip.getTag());
        }

        if (editandoId != null) {
            viewModel.editar(editandoId, valor, tipo, data, descricao, categoriaIdSelecionada, tagIds);
        } else {
            viewModel.salvar(valor, tipo, data, descricao, categoriaIdSelecionada, tagIds);
        }
    }

    private void abrirDatePicker() {
        Calendar cal = Calendar.getInstance();
        String dataAtual = etData.getText() != null ? etData.getText().toString() : "";
        if (!dataAtual.isEmpty()) {
            try {
                LocalDate ld = LocalDate.parse(dataAtual, FORMATTER);
                cal.set(ld.getYear(), ld.getMonthValue() - 1, ld.getDayOfMonth());
            } catch (Exception ignored) { }
        }
        new DatePickerDialog(requireContext(),
                (picker, year, month, day) ->
                        etData.setText(String.format("%04d-%02d-%02d", year, month + 1, day)),
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
                .show();
    }
}
