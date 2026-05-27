package com.financeiramente.android.ui.lancamentos;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import android.view.MotionEvent;

import com.financeiramente.android.R;
import com.financeiramente.android.app.AppContext;
import com.financeiramente.android.ui.common.CategoriaVisualFallback;
import com.financeiramente.android.viewmodel.LancamentoFormViewModel;
import com.financeiramente.android.viewmodel.LancamentoFormViewModelFactory;
import com.financeiramente.core.domain.entity.CartaoCredito;
import com.financeiramente.core.domain.entity.Categoria;
import com.financeiramente.core.domain.entity.Tag;
import com.financeiramente.core.domain.vo.TipoCategoria;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LancamentoFormFragment extends BottomSheetDialogFragment {

    private LancamentoFormViewModel viewModel;

    private MaterialButtonToggleGroup toggleTipo;
    private TextInputEditText etValor;
    private TextInputLayout tilValor;
    private TextInputEditText etDescricao;
    private TextInputEditText etData;
    private MaterialButton btnToggleCategorias;
    private MaterialButton btnToggleTags;
    private MaterialButton btnToggleCartoes;
    private LinearLayout layoutSecaoCategorias;
    private LinearLayout layoutSecaoTags;
    private LinearLayout layoutSecaoCartoes;
    private LinearLayout layoutCategoriaLinhas;
    private ChipGroup cgTags;
    private ChipGroup cgCartoes;
    private LinearLayout layoutDetalhesCompletos;
    private MaterialButtonToggleGroup toggleParcelamento;
    private TextInputLayout tilParcelas;
    private TextInputEditText etParcelas;
    private TextInputLayout tilValorParcela;
    private TextInputEditText etValorParcela;
    private final List<ChipGroup> chipGroupsCategorias = new ArrayList<>();

    private List<Categoria> listaCategorias = new ArrayList<>();
    private String categoriaIdSelecionada = null;
    private boolean valorAutoPreenchido = false;
    private boolean categoriasExpandidas = false;
    private boolean tagsExpandidas = false;
    private boolean cartoesExpandidos = false;

    private String editandoId = null;
    private List<CartaoCredito> cartoesAtivos = new ArrayList<>();
    private String cartaoIdSelecionado = null;
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
                ctx.getCoreServices().getCategoriaRepository(),
                ctx.getCoreServices().getTagRepository(),
                ctx.getCoreServices().getLancamentoRepository());
        viewModel = new ViewModelProvider(this, factory).get(LancamentoFormViewModel.class);

        toggleTipo = view.findViewById(R.id.toggle_tipo);
        etValor = view.findViewById(R.id.et_valor);
        tilValor = view.findViewById(R.id.til_valor);
        etDescricao = view.findViewById(R.id.et_descricao);
        etData = view.findViewById(R.id.et_data);
        btnToggleCategorias = view.findViewById(R.id.btn_toggle_categorias);
        btnToggleTags = view.findViewById(R.id.btn_toggle_tags);
        btnToggleCartoes = view.findViewById(R.id.btn_toggle_cartoes);
        layoutSecaoCategorias = view.findViewById(R.id.layout_secao_categorias);
        layoutSecaoTags = view.findViewById(R.id.layout_secao_tags);
        layoutSecaoCartoes = view.findViewById(R.id.layout_secao_cartoes);
        layoutCategoriaLinhas = view.findViewById(R.id.layout_categoria_linhas);
        cgTags = view.findViewById(R.id.cg_tags);
        cgCartoes = view.findViewById(R.id.cg_cartoes);
        layoutDetalhesCompletos = view.findViewById(R.id.layout_detalhes_completos);
        toggleParcelamento = view.findViewById(R.id.toggle_parcelamento);
        tilParcelas = view.findViewById(R.id.til_parcelas);
        etParcelas = view.findViewById(R.id.et_parcelas);
        tilValorParcela = view.findViewById(R.id.til_valor_parcela);
        etValorParcela = view.findViewById(R.id.et_valor_parcela);
        view.findViewById(R.id.btn_fechar_form).setOnClickListener(v -> dismiss());

        // Data padrão = hoje
        etData.setText(LocalDate.now().format(FORMATTER));
        etData.setOnClickListener(v -> abrirDatePicker());

        // Limpar auto-preenchimento ao tocar no campo de valor
        etValor.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN && valorAutoPreenchido) {
                etValor.setText("");
                valorAutoPreenchido = false;
            }
            return false;
        });

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
                if (tipo != TipoLancamento.DESPESA) {
                    cartaoIdSelecionado = null;
                }
                atualizarEstadoSecoesRapidas();
            }
        });

        btnToggleCategorias.setOnClickListener(v -> {
            categoriasExpandidas = !categoriasExpandidas;
            atualizarEstadoSecoesRapidas();
        });
        btnToggleTags.setOnClickListener(v -> {
            tagsExpandidas = !tagsExpandidas;
            atualizarEstadoSecoesRapidas();
        });
        btnToggleCartoes.setOnClickListener(v -> {
            cartoesExpandidos = !cartoesExpandidos;
            atualizarEstadoSecoesRapidas();
        });
        atualizarEstadoSecoesRapidas();

        if (toggleParcelamento != null) {
            toggleParcelamento.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
                if (isChecked) {
                    boolean parcelado = checkedId == R.id.btn_parcelado;
                    tilParcelas.setVisibility(parcelado ? View.VISIBLE : View.GONE);
                    tilValorParcela.setVisibility(parcelado ? View.VISIBLE : View.GONE);
                    if (!parcelado) etValorParcela.setText("");
                    else atualizarValorParcela();
                }
            });
            // Check initial state
            toggleParcelamento.check(R.id.btn_a_vista);
        }

        // Atualizar valor por parcela quando número de parcelas ou valor total mudam
        android.text.TextWatcher parcelaWatcher = new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(android.text.Editable s) { atualizarValorParcela(); }
        };
        etParcelas.addTextChangedListener(parcelaWatcher);
        etValor.addTextChangedListener(parcelaWatcher);

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

        AppContext ctx2 = AppContext.get(requireContext());
        viewModel.setCartaoSupport(
            ctx2.getRegistrarCompraCartaoUseCase(),
                ctx2.getCoreServices().getCartaoRepository());
    }

    private void atualizarEstadoSecoesRapidas() {
        boolean tipoDespesa = toggleTipo.getCheckedButtonId() != R.id.btn_receita;
        layoutSecaoCategorias.setVisibility(categoriasExpandidas ? View.VISIBLE : View.GONE);
        layoutSecaoTags.setVisibility(tagsExpandidas ? View.VISIBLE : View.GONE);
        btnToggleCartoes.setVisibility(tipoDespesa ? View.VISIBLE : View.GONE);
        layoutSecaoCartoes.setVisibility(tipoDespesa && cartoesExpandidos ? View.VISIBLE : View.GONE);
        btnToggleCategorias.setText(getString(categoriasExpandidas
            ? R.string.lancamento_secao_categorias_aberta
            : R.string.lancamento_secao_categorias_fechada));
        btnToggleTags.setText(getString(tagsExpandidas
            ? R.string.lancamento_secao_tags_aberta
            : R.string.lancamento_secao_tags_fechada));
        btnToggleCartoes.setText(getString(cartoesExpandidos
                ? R.string.lancamento_secao_cartoes_aberta
                : R.string.lancamento_secao_cartoes_fechada));
    }

    /** Configura o BottomSheetBehavior: peek = modo rápido, expanded = modo completo. */
    private void configurarBottomSheet() {
        if (!(getDialog() instanceof BottomSheetDialog)) return;
        BottomSheetDialog bsd = (BottomSheetDialog) getDialog();
        bsd.setOnShowListener(d -> {
            BottomSheetBehavior<FrameLayout> behavior = bsd.getBehavior();
            // ~460dp — cobre também o bloco de tags no modo rápido.
            int peekPx = (int) (getResources().getDisplayMetrics().density * 460);
            behavior.setPeekHeight(peekPx);
            behavior.setDraggable(true);
            behavior.setHideable(true);
            behavior.setSkipCollapsed(false);
            behavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState) {
                    if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                        dismiss();
                    }
                }

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
        View root = getView();
        if (root == null) return;
        MaterialButton btnDespesa = root.findViewById(R.id.btn_despesa);
        MaterialButton btnReceita = root.findViewById(R.id.btn_receita);

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
            listaCategorias = cats != null ? cats : new ArrayList<>();
            popularChipsCategoria(listaCategorias);
        });

        viewModel.getTags().observe(getViewLifecycleOwner(), tags -> {
            cgTags.removeAllViews();
            if (tags == null) return;
            for (Tag tag : tags) {
                Chip chip = new Chip(requireContext());
                String emoji = tag.getEmoji() == null || tag.getEmoji().trim().isEmpty()
                        ? "🏷️"
                        : tag.getEmoji();
                chip.setText(emoji + " " + tag.getNome());
                chip.setCheckable(true);
                chip.setTag(tag.getId());
                aplicarEstiloTagChip(chip, tag.getCor());
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

        viewModel.getCartoesAtivos().observe(getViewLifecycleOwner(), cartoes -> {
            if (cgCartoes == null) return;
            cgCartoes.removeAllViews();
            if (cartoes == null) return;
            cartoesAtivos = cartoes;

            Chip chipSemCartao = new Chip(requireContext());
            chipSemCartao.setText(getString(R.string.lancamento_cartao_sem));
            chipSemCartao.setCheckable(true);
            chipSemCartao.setChecked(cartaoIdSelecionado == null);
            chipSemCartao.setOnClickListener(v -> {
                cartaoIdSelecionado = null;
                chipSemCartao.setChecked(true);
                toggleParcelamento.setVisibility(View.GONE);
                tilParcelas.setVisibility(View.GONE);
            });
            cgCartoes.addView(chipSemCartao);

            for (CartaoCredito cartao : cartoesAtivos) {
                Chip chip = new Chip(requireContext());
                String icone = cartao.getIcone() == null || cartao.getIcone().trim().isEmpty()
                        ? "💳"
                        : cartao.getIcone();
                chip.setText(icone + " " + cartao.getNome());
                chip.setCheckable(true);
                chip.setTag(cartao.getId());
                aplicarEstiloTagChip(chip, cartao.getCor());

                if (cartao.getId().equals(cartaoIdSelecionado)) {
                    chip.setChecked(true);
                }

                chip.setOnClickListener(v -> cartaoIdSelecionado = (String) chip.getTag());
                chip.setOnCheckedChangeListener((cb, checked) -> {
                    if (checked) {
                        cartaoIdSelecionado = (String) chip.getTag();
                        toggleParcelamento.setVisibility(View.VISIBLE);
                    } else {
                        if (cartaoIdSelecionado != null && cartaoIdSelecionado.equals(chip.getTag())) {
                            toggleParcelamento.setVisibility(View.GONE);
                            tilParcelas.setVisibility(View.GONE);
                        }
                    }
                });
                cgCartoes.addView(chip);
            }
        });
    }

    /** Popula o ChipGroup de categorias com chips coloridos (ícone + nome), separados por seção. */
    private void popularChipsCategoria(List<Categoria> cats) {
        layoutCategoriaLinhas.removeAllViews();
        chipGroupsCategorias.clear();
        if (cats == null || cats.isEmpty()) return;

        List<Categoria> essenciais = new ArrayList<>();
        List<Categoria> naoEssenciais = new ArrayList<>();
        List<Categoria> outras = new ArrayList<>();

        for (Categoria cat : cats) {
            if (cat.getTipo() == TipoCategoria.ESSENCIAL) {
                essenciais.add(cat);
            } else if (cat.getTipo() == TipoCategoria.NAO_ESSENCIAL) {
                naoEssenciais.add(cat);
            } else {
                outras.add(cat);
            }
        }

        boolean temSecoes = !essenciais.isEmpty() && !naoEssenciais.isEmpty();

        if (temSecoes) {
            adicionarLabelSecaoCategoria(getString(R.string.categorias_secao_essenciais));
            adicionarChipsCategoriasPorPai(essenciais);
            adicionarLabelSecaoCategoria(getString(R.string.categorias_secao_nao_essenciais));
            adicionarChipsCategoriasPorPai(naoEssenciais);
        } else {
            List<Categoria> todos = new ArrayList<>();
            todos.addAll(essenciais);
            todos.addAll(naoEssenciais);
            todos.addAll(outras);
            adicionarChipsCategoriasPorPai(todos);
        }
    }

    private void adicionarLabelSecaoCategoria(String titulo) {
        TextView tv = new TextView(requireContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = (int) (getResources().getDisplayMetrics().density * 8);
        params.bottomMargin = (int) (getResources().getDisplayMetrics().density * 2);
        params.setMarginStart((int) (getResources().getDisplayMetrics().density * 4));
        tv.setLayoutParams(params);
        tv.setText(titulo.toUpperCase());
        tv.setTextSize(10f);
        tv.setTextColor(getResources().getColor(R.color.cinza_secondary, requireContext().getTheme()));
        tv.setLetterSpacing(0.08f);
        layoutCategoriaLinhas.addView(tv);
    }

    private void adicionarChipsCategoriasPorPai(List<Categoria> cats) {
        Map<String, List<Categoria>> porPai = new LinkedHashMap<>();
        for (Categoria cat : cats) {
            String pId = cat.getPaiId() != null ? cat.getPaiId() : cat.getId();
            porPai.computeIfAbsent(pId, key -> new ArrayList<>()).add(cat);
        }

        for (Map.Entry<String, List<Categoria>> entry : porPai.entrySet()) {
            HorizontalScrollView scroller = new HorizontalScrollView(requireContext());
            LinearLayout.LayoutParams scrollerParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            scrollerParams.bottomMargin = (int) (getResources().getDisplayMetrics().density * 6);
            scroller.setLayoutParams(scrollerParams);
            scroller.setHorizontalScrollBarEnabled(false);
            scroller.setFillViewport(true);

            ChipGroup row = new ChipGroup(requireContext());
            row.setSingleLine(true);
            row.setSingleSelection(true);
            row.setChipSpacingHorizontal((int) (getResources().getDisplayMetrics().density * 8));
            chipGroupsCategorias.add(row);

            for (Categoria cat : entry.getValue()) {
                String icone = CategoriaVisualFallback.icone(cat, null);
                String corHex = CategoriaVisualFallback.cor(cat, null);
                Chip chip = new Chip(requireContext());
                chip.setText(icone + "  " + cat.getNome());
                chip.setCheckable(true);
                chip.setTag(cat.getId());

                try {
                    int corSolida = Color.parseColor(corHex);
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
                } catch (Exception ignored) { }

                if (cat.getId().equals(categoriaIdSelecionada)) {
                    chip.setChecked(true);
                }

                chip.setOnClickListener(v -> {
                    categoriaIdSelecionada = (String) chip.getTag();
                    for (ChipGroup group : chipGroupsCategorias) {
                        if (group != row) {
                            group.clearCheck();
                        }
                    }
                    chip.setChecked(true);
                    // Auto-preencher valor para categorias essenciais com limite definido
                    if (cat.getTipo() == TipoCategoria.ESSENCIAL && cat.getLimiteMensal() != null) {
                        etValor.setText(String.valueOf(cat.getLimiteMensal()));
                        valorAutoPreenchido = true;
                    } else {
                        valorAutoPreenchido = false;
                    }
                });

                row.addView(chip);
            }

            scroller.addView(row);
            layoutCategoriaLinhas.addView(scroller);
        }
    }

    private void aplicarEstiloTagChip(Chip chip, String corHex) {
        try {
            int corSolida = Color.parseColor(corHex);
            int corFundo = Color.argb(40, Color.red(corSolida), Color.green(corSolida), Color.blue(corSolida));
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
        } catch (Exception ignored) {
            // Keep default style for invalid colors.
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

        // Categoria — null auto-resolved to SemCategoria by use case
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

        // Parcelamento
        boolean parcelado = toggleParcelamento.getVisibility() == View.VISIBLE
                && toggleParcelamento.getCheckedButtonId() == R.id.btn_parcelado;
        int numeroParcelas = 1;
        if (parcelado) {
            String parcStr = etParcelas.getText() != null ? etParcelas.getText().toString().trim() : "";
            if (!parcStr.isEmpty()) {
                try { numeroParcelas = Integer.parseInt(parcStr); } catch (NumberFormatException ignored) { }
            }
            if (numeroParcelas < 1) numeroParcelas = 1;
        }

        mostrarDialogoConfirmacao(valor, tipo, data, descricao, tagIds, parcelado, numeroParcelas);
    }

    private void mostrarDialogoConfirmacao(
            double valor,
            TipoLancamento tipo,
            String data,
            String descricao,
            List<String> tagIds,
            boolean parcelado,
            int numeroParcelas) {

        View confView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_lancamento_confirmacao, null);

        android.widget.TextView tvValor = confView.findViewById(R.id.tv_conf_valor);
        android.widget.TextView tvTipo = confView.findViewById(R.id.tv_conf_tipo);
        android.widget.TextView tvData = confView.findViewById(R.id.tv_conf_data);
        android.widget.TextView tvCategoria = confView.findViewById(R.id.tv_conf_categoria);
        android.widget.TextView tvTags = confView.findViewById(R.id.tv_conf_tags);
        android.widget.TextView tvCartao = confView.findViewById(R.id.tv_conf_cartao);
        android.widget.TextView tvParcelas = confView.findViewById(R.id.tv_conf_parcelas);
        android.widget.TextView tvDescricao = confView.findViewById(R.id.tv_conf_descricao);

        // Valor
        tvValor.setText(String.format("R$ %.2f", valor));
        confView.findViewById(R.id.row_conf_tipo).setVisibility(View.VISIBLE);
        tvTipo.setText(tipo == TipoLancamento.RECEITA ? "Receita" : "Despesa");

        // Data
        confView.findViewById(R.id.row_conf_data).setVisibility(View.VISIBLE);
        tvData.setText(data);

        // Categoria
        String catNome = null;
        if (categoriaIdSelecionada != null) {
            for (Categoria cat : listaCategorias) {
                if (cat.getId().equals(categoriaIdSelecionada)) { catNome = cat.getNome(); break; }
            }
        }
        if (catNome != null) {
            confView.findViewById(R.id.row_conf_categoria).setVisibility(View.VISIBLE);
            tvCategoria.setText(catNome);
        }

        // Tags
        if (!tagIds.isEmpty() && viewModel.getTags().getValue() != null) {
            List<String> tagNomes = new ArrayList<>();
            for (Tag t : viewModel.getTags().getValue()) {
                if (tagIds.contains(t.getId())) tagNomes.add(t.getNome());
            }
            if (!tagNomes.isEmpty()) {
                confView.findViewById(R.id.row_conf_tags).setVisibility(View.VISIBLE);
                tvTags.setText(android.text.TextUtils.join(", ", tagNomes));
            }
        }

        // Cartão
        if (cartaoIdSelecionado != null) {
            for (CartaoCredito c : cartoesAtivos) {
                if (c.getId().equals(cartaoIdSelecionado)) {
                    confView.findViewById(R.id.row_conf_cartao).setVisibility(View.VISIBLE);
                    tvCartao.setText(c.getNome());
                    break;
                }
            }
        }

        // Parcelas
        if (parcelado && cartaoIdSelecionado != null && numeroParcelas > 1) {
            confView.findViewById(R.id.row_conf_parcelas).setVisibility(View.VISIBLE);
            tvParcelas.setText(numeroParcelas + "x de " + String.format("R$ %.2f", valor));
        }

        // Descrição
        if (descricao != null && !descricao.isEmpty()) {
            confView.findViewById(R.id.row_conf_descricao).setVisibility(View.VISIBLE);
            tvDescricao.setText(descricao);
        }

        final int finalNumeroParcelas = numeroParcelas;
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.lancamento_confirmar_titulo)
                .setView(confView)
                .setPositiveButton(R.string.confirmar, (dlg, w) ->
                        executarSalvamento(valor, tipo, data, descricao, tagIds, parcelado, finalNumeroParcelas))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void executarSalvamento(
            double valor,
            TipoLancamento tipo,
            String data,
            String descricao,
            List<String> tagIds,
            boolean parcelado,
            int numeroParcelas) {

        if (editandoId != null) {
            viewModel.editar(editandoId, valor, tipo, data, descricao, categoriaIdSelecionada, tagIds);
        } else if (tipo == TipoLancamento.DESPESA && cartaoIdSelecionado != null) {
            if (parcelado && numeroParcelas > 1) {
                viewModel.salvarParceladoComCartao(valor, tipo, data, descricao,
                        categoriaIdSelecionada, tagIds, cartaoIdSelecionado, numeroParcelas);
            } else {
                viewModel.salvarComCartao(valor, tipo, data, descricao,
                        categoriaIdSelecionada, tagIds, cartaoIdSelecionado);
            }
        } else {
            viewModel.salvar(valor, tipo, data, descricao, categoriaIdSelecionada, tagIds);
        }
    }

    private void atualizarValorParcela() {
        if (etValorParcela == null) return;
        try {
            String valorStr = etValor.getText() != null ? etValor.getText().toString().trim() : "";
            String parcStr = etParcelas.getText() != null ? etParcelas.getText().toString().trim() : "";
            if (!valorStr.isEmpty() && !parcStr.isEmpty()) {
                double valor = Double.parseDouble(valorStr);
                int n = Integer.parseInt(parcStr);
                if (n > 0 && valor > 0) {
                    etValorParcela.setText(String.format(java.util.Locale.getDefault(), "R$ %.2f", valor / n));
                    return;
                }
            }
        } catch (NumberFormatException ignored) { }
        etValorParcela.setText("");
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
