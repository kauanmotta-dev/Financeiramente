package com.financeiramente.android.ui.recorrentes;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.financeiramente.android.R;
import com.financeiramente.android.app.AppContext;
import com.financeiramente.android.ui.common.CategoriaVisualFallback;
import com.financeiramente.android.viewmodel.RecorrentesViewModel;
import com.financeiramente.android.viewmodel.RecorrentesViewModelFactory;
import com.financeiramente.core.domain.entity.Categoria;
import com.financeiramente.core.domain.entity.LancamentoRecorrente;
import com.financeiramente.core.domain.vo.TipoLancamento;
import com.financeiramente.core.domain.vo.TipoRecorrencia;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RecorrenteFormFragment extends Fragment {

    private RecorrentesViewModel viewModel;

    private TextInputEditText etDescricao;
    private TextInputEditText etValor;
    private TextInputEditText etDia;
    private RadioGroup rgTipo;
    private MaterialButton btnSelecionarSubcategoria;
    private TextView tvSubcategoriaSelecionada;
    private Spinner spRecorrencia;

    private List<Categoria> categorias = new ArrayList<>();
    private final Map<String, Categoria> categoriasPorId = new LinkedHashMap<>();
    private final List<Categoria> categoriasRaiz = new ArrayList<>();
    private final Map<String, List<Categoria>> subcategoriasPorRaiz = new LinkedHashMap<>();
    private String categoriaIdSelecionada;
    private String recorrenteId; // null = novo

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recorrente_form, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.btn_back_recorrente_form).setOnClickListener(v ->
            Navigation.findNavController(view).navigateUp());

        AppContext ctx = AppContext.get(requireContext());
        RecorrentesViewModelFactory factory = new RecorrentesViewModelFactory(
                ctx.getCriarLancamentoRecorrenteUseCase(),
                ctx.getEditarLancamentoRecorrenteUseCase(),
                ctx.getDesativarLancamentoRecorrenteUseCase(),
                ctx.getLancamentoRecorrenteRepository());
        viewModel = new ViewModelProvider(requireParentFragment(), factory)
                .get(RecorrentesViewModel.class);

        etDescricao   = view.findViewById(R.id.et_descricao);
        etValor       = view.findViewById(R.id.et_valor);
        etDia         = view.findViewById(R.id.et_dia);
        rgTipo        = view.findViewById(R.id.rg_tipo);
        btnSelecionarSubcategoria = view.findViewById(R.id.btn_selecionar_subcategoria);
        tvSubcategoriaSelecionada = view.findViewById(R.id.tv_subcategoria_selecionada);
        spRecorrencia = view.findViewById(R.id.sp_recorrencia);
        btnSelecionarSubcategoria.setOnClickListener(v -> abrirSeletorCategoriasRaiz());

        // Carregar argumentos (edição)
        if (getArguments() != null) {
            recorrenteId = getArguments().getString("recorrenteId");
        }

        // Popular spinner de recorrência
        ArrayAdapter<String> recAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"Mensal", "Semanal", "Diária", "Anual"});
        recAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRecorrencia.setAdapter(recAdapter);

        // Carregar categorias e popular spinner
        carregarCategorias(ctx);

        // Se edição, pré-preencher
        if (recorrenteId != null) {
            preencherFormulario();
        }

        view.findViewById(R.id.btn_salvar).setOnClickListener(v -> salvar(view));

        viewModel.getErro().observe(getViewLifecycleOwner(), erro -> {
            if (erro != null) Toast.makeText(requireContext(), erro, Toast.LENGTH_LONG).show();
        });

        viewModel.getSucesso().observe(getViewLifecycleOwner(), ok -> {
            if (Boolean.TRUE.equals(ok)) {
                Navigation.findNavController(view).popBackStack();
            }
        });
    }

    private void carregarCategorias(AppContext ctx) {
        new Thread(() -> {
            try {
                categorias = ctx.getCategoriaRepository().listarTodas();
                requireActivity().runOnUiThread(() -> {
                    categoriasPorId.clear();
                    categoriasRaiz.clear();
                    subcategoriasPorRaiz.clear();

                    for (Categoria categoria : categorias) {
                        categoriasPorId.put(categoria.getId(), categoria);
                        if (categoria.getPaiId() == null) {
                            categoriasRaiz.add(categoria);
                        } else {
                            subcategoriasPorRaiz
                                    .computeIfAbsent(categoria.getPaiId(), k -> new ArrayList<>())
                                    .add(categoria);
                        }
                    }

                    atualizarResumoSubcategoria();

                    if (recorrenteId != null) preencherFormulario();
                });
            } catch (Exception e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void preencherFormulario() {
        List<LancamentoRecorrente> lista = viewModel.getRecorrentes().getValue();
        if (lista == null) return;

        for (LancamentoRecorrente rec : lista) {
            if (rec.getId().equals(recorrenteId)) {
                etDescricao.setText(rec.getDescricao());
                etValor.setText(String.valueOf(rec.getValor()));
                if (rec.getDiaRecorrencia() != null) {
                    etDia.setText(String.valueOf(rec.getDiaRecorrencia()));
                }

                if (rec.getTipo() == TipoLancamento.RECEITA) {
                    rgTipo.check(R.id.rb_receita);
                } else {
                    rgTipo.check(R.id.rb_despesa);
                }

                // Selecionar recorrência
                TipoRecorrencia[] valores = TipoRecorrencia.values();
                for (int i = 0; i < valores.length; i++) {
                    if (valores[i] == rec.getRecorrencia()) {
                        spRecorrencia.setSelection(mapRecorrenciaIndex(rec.getRecorrencia()));
                        break;
                    }
                }

                // Selecionar categoria
                categoriaIdSelecionada = rec.getCategoriaId();
                atualizarResumoSubcategoria();
                break;
            }
        }
    }

    private int mapRecorrenciaIndex(TipoRecorrencia r) {
        switch (r) {
            case MENSAL: return 0;
            case SEMANAL: return 1;
            case DIARIA: return 2;
            case ANUAL: return 3;
            default: return 0;
        }
    }

    private TipoRecorrencia recorrenciaDaSelecao(int pos) {
        switch (pos) {
            case 0: return TipoRecorrencia.MENSAL;
            case 1: return TipoRecorrencia.SEMANAL;
            case 2: return TipoRecorrencia.DIARIA;
            case 3: return TipoRecorrencia.ANUAL;
            default: return TipoRecorrencia.MENSAL;
        }
    }

    private void salvar(View view) {
        String descricao = etDescricao.getText() != null ? etDescricao.getText().toString().trim() : "";
        String valorStr  = etValor.getText() != null ? etValor.getText().toString().replace(",", ".") : "";
        String diaStr    = etDia.getText() != null ? etDia.getText().toString().trim() : "";

        if (descricao.isEmpty()) {
            Toast.makeText(requireContext(), R.string.erro_descricao_obrigatoria, Toast.LENGTH_SHORT).show();
            return;
        }

        double valor;
        try {
            valor = Double.parseDouble(valorStr);
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), R.string.erro_valor_invalido, Toast.LENGTH_SHORT).show();
            return;
        }

        if (categoriaIdSelecionada == null || categoriaIdSelecionada.isEmpty()) {
            Toast.makeText(requireContext(), R.string.erro_categoria_obrigatoria, Toast.LENGTH_SHORT).show();
            return;
        }

        TipoLancamento tipo = rgTipo.getCheckedRadioButtonId() == R.id.rb_receita
                ? TipoLancamento.RECEITA : TipoLancamento.DESPESA;
        TipoRecorrencia recorrencia = recorrenciaDaSelecao(spRecorrencia.getSelectedItemPosition());
        Integer dia = diaStr.isEmpty() ? null : Integer.parseInt(diaStr);

        if (recorrenteId == null) {
            viewModel.criar(descricao, valor, tipo, categoriaIdSelecionada, recorrencia, dia);
        } else {
            viewModel.editar(recorrenteId, descricao, valor, tipo, categoriaIdSelecionada, recorrencia, dia);
        }
    }

    private void abrirSeletorCategoriasRaiz() {
        if (categoriasRaiz.isEmpty()) {
            Toast.makeText(requireContext(), R.string.recorrente_sem_categorias, Toast.LENGTH_SHORT).show();
            return;
        }

        CharSequence[] opcoes = new CharSequence[categoriasRaiz.size()];
        for (int i = 0; i < categoriasRaiz.size(); i++) {
            opcoes[i] = montarLabelCategoria(categoriasRaiz.get(i));
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.recorrente_selecione_categoria_mae)
                .setItems(opcoes, (dialog, which) -> {
                    Categoria categoriaRaiz = categoriasRaiz.get(which);
                    abrirSeletorSubcategorias(categoriaRaiz);
                })
                .show();
    }

    private void abrirSeletorSubcategorias(Categoria categoriaRaiz) {
        List<Categoria> subcategorias = subcategoriasPorRaiz.get(categoriaRaiz.getId());
        if (subcategorias == null || subcategorias.isEmpty()) {
            Toast.makeText(requireContext(), R.string.recorrente_categoria_sem_subcategorias, Toast.LENGTH_SHORT).show();
            return;
        }

        CharSequence[] opcoes = new CharSequence[subcategorias.size()];
        for (int i = 0; i < subcategorias.size(); i++) {
            opcoes[i] = montarLabelCategoria(subcategorias.get(i));
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.recorrente_selecione_subcategoria, categoriaRaiz.getNome()))
                .setItems(opcoes, (dialog, which) -> {
                    categoriaIdSelecionada = subcategorias.get(which).getId();
                    atualizarResumoSubcategoria();
                })
                .show();
    }

    private void atualizarResumoSubcategoria() {
        if (categoriaIdSelecionada == null) {
            tvSubcategoriaSelecionada.setText(R.string.recorrente_subcategoria_nao_selecionada);
            return;
        }

        Categoria subcategoria = categoriasPorId.get(categoriaIdSelecionada);
        if (subcategoria == null) {
            tvSubcategoriaSelecionada.setText(R.string.recorrente_subcategoria_nao_selecionada);
            return;
        }

        Categoria categoriaRaiz = subcategoria;
        if (subcategoria.getPaiId() != null) {
            Categoria possivelRaiz = categoriasPorId.get(subcategoria.getPaiId());
            if (possivelRaiz != null) {
                categoriaRaiz = possivelRaiz;
            }
        }

        String label = CategoriaVisualFallback.icone(categoriaRaiz, null)
                + " " + categoriaRaiz.getNome()
                + "  >  "
                + CategoriaVisualFallback.icone(subcategoria, null)
                + " " + subcategoria.getNome();
        tvSubcategoriaSelecionada.setText(label);
    }

    private CharSequence montarLabelCategoria(Categoria categoria) {
        String label = "● " + CategoriaVisualFallback.icone(categoria, null) + " " + categoria.getNome();
        SpannableString spannable = new SpannableString(label);
        try {
            int color = android.graphics.Color.parseColor(CategoriaVisualFallback.cor(categoria, null));
            spannable.setSpan(new ForegroundColorSpan(color), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } catch (IllegalArgumentException ignored) {
            // Keep default text color when category color is invalid.
        }
        return spannable;
    }
}
