package com.financeiramente.android.ui.lancamentos;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.financeiramente.android.R;
import com.financeiramente.android.app.AppContext;
import com.financeiramente.android.viewmodel.LancamentoFormViewModel;
import com.financeiramente.android.viewmodel.LancamentoFormViewModelFactory;
import com.financeiramente.core.domain.entity.Categoria;
import com.financeiramente.core.domain.entity.Tag;
import com.financeiramente.core.domain.vo.TipoLancamento;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class LancamentoFormFragment extends Fragment {

    private LancamentoFormViewModel viewModel;

    private RadioGroup rgTipo;
    private TextInputEditText etValor;
    private TextInputLayout tilValor;
    private TextInputEditText etDescricao;
    private TextInputLayout tilDescricao;
    private Spinner spCategoria;
    private TextInputEditText etData;
    private ChipGroup cgTags;

    private List<Categoria> listaCategorias = new ArrayList<>();
    private List<Tag> listaTags = new ArrayList<>();

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

        rgTipo = view.findViewById(R.id.rg_tipo);
        etValor = view.findViewById(R.id.et_valor);
        tilValor = view.findViewById(R.id.til_valor);
        etDescricao = view.findViewById(R.id.et_descricao);
        tilDescricao = view.findViewById(R.id.til_descricao);
        spCategoria = view.findViewById(R.id.sp_categoria);
        etData = view.findViewById(R.id.et_data);
        cgTags = view.findViewById(R.id.cg_tags);

        // Data padrão = hoje
        etData.setText(LocalDate.now().format(FORMATTER));
        etData.setOnClickListener(v -> abrirDatePicker());

        // Focar no campo valor imediatamente
        etValor.requestFocus();

        // Recarregar categorias quando o tipo de lançamento mudar
        rgTipo.setOnCheckedChangeListener((group, checkedId) -> {
            TipoLancamento tipoSelecionado = (checkedId == R.id.rb_receita)
                    ? TipoLancamento.RECEITA : TipoLancamento.DESPESA;
            viewModel.carregarCategoriasPorTipo(tipoSelecionado);
        });

        view.findViewById(R.id.btn_salvar).setOnClickListener(v -> salvar(view));

        // Verificar se é edição
        if (getArguments() != null) {
            editandoId = getArguments().getString("lancamentoId");
            if (editandoId != null) {
                viewModel.carregarLancamento(editandoId);
            }
        }

        observarViewModel(view);
    }

    private void observarViewModel(View view) {
        viewModel.getCategorias().observe(getViewLifecycleOwner(), cats -> {
            listaCategorias = cats;
            List<String> nomes = new ArrayList<>();
            for (Categoria c : cats) nomes.add(c.getNome());
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_item, nomes);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spCategoria.setAdapter(adapter);
        });

        viewModel.getTags().observe(getViewLifecycleOwner(), tags -> {
            listaTags = tags;
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
            if (lancamento.getTipo() == TipoLancamento.RECEITA) {
                view.findViewById(R.id.rb_receita).setSelected(true);
                rgTipo.check(R.id.rb_receita);
            } else {
                rgTipo.check(R.id.rb_despesa);
            }
            // Selecionar categoria
            for (int i = 0; i < listaCategorias.size(); i++) {
                if (listaCategorias.get(i).getId().equals(lancamento.getCategoriaId())) {
                    spCategoria.setSelection(i);
                    break;
                }
            }
        });

        viewModel.getSucesso().observe(getViewLifecycleOwner(), ok -> {
            if (Boolean.TRUE.equals(ok)) {
                Navigation.findNavController(requireView()).navigateUp();
            }
        });

        viewModel.getErro().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
        });
    }

    private void salvar(View view) {
        tilValor.setError(null);
        tilDescricao.setError(null);

        String valorStr = etValor.getText() != null ? etValor.getText().toString().trim() : "";
        String descricao = etDescricao.getText() != null ? etDescricao.getText().toString().trim() : "";
        String data = etData.getText() != null ? etData.getText().toString().trim() : "";

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
        if (descricao.isEmpty()) {
            tilDescricao.setError(getString(R.string.erro_descricao_obrigatoria));
            valido = false;
        }
        if (!valido) return;

        TipoLancamento tipo = (rgTipo.getCheckedRadioButtonId() == R.id.rb_receita)
                ? TipoLancamento.RECEITA : TipoLancamento.DESPESA;

        int catPos = spCategoria.getSelectedItemPosition();
        if (listaCategorias.isEmpty() || catPos < 0) {
            Toast.makeText(requireContext(), getString(R.string.erro_categoria_obrigatoria), Toast.LENGTH_SHORT).show();
            return;
        }
        String categoriaId = listaCategorias.get(catPos).getId();

        List<String> tagIds = new ArrayList<>();
        for (int i = 0; i < cgTags.getChildCount(); i++) {
            Chip chip = (Chip) cgTags.getChildAt(i);
            if (chip.isChecked()) tagIds.add((String) chip.getTag());
        }

        if (editandoId != null) {
            viewModel.editar(editandoId, valor, tipo, data, descricao, categoriaId, tagIds);
        } else {
            viewModel.salvar(valor, tipo, data, descricao, categoriaId, tagIds);
        }
    }

    private void abrirDatePicker() {
        Calendar cal = Calendar.getInstance();
        String dataAtual = etData.getText() != null ? etData.getText().toString() : "";
        if (!dataAtual.isEmpty()) {
            try {
                LocalDate ld = LocalDate.parse(dataAtual, FORMATTER);
                cal.set(ld.getYear(), ld.getMonthValue() - 1, ld.getDayOfMonth());
            } catch (Exception ignored) {}
        }
        DatePickerDialog dialog = new DatePickerDialog(requireContext(),
                (picker, year, month, day) -> {
                    String data = String.format("%04d-%02d-%02d", year, month + 1, day);
                    etData.setText(data);
                },
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }
}
