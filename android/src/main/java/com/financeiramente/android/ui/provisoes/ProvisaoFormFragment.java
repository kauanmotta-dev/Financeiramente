package com.financeiramente.android.ui.provisoes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.financeiramente.android.R;
import com.financeiramente.android.app.AppContext;
import com.financeiramente.android.viewmodel.ProvisoesViewModel;
import com.financeiramente.android.viewmodel.ProvisoesViewModelFactory;
import com.financeiramente.core.domain.entity.Categoria;
import com.financeiramente.core.domain.entity.Provisao;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class ProvisaoFormFragment extends Fragment {

    private ProvisoesViewModel viewModel;

    private TextInputEditText etNome;
    private TextInputEditText etTotalAnual;
    private TextInputEditText etValorMensal;
    private Spinner spCategoria;

    private List<Categoria> categorias = new ArrayList<>();
    private String provisaoId; // null = novo

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_provisao_form, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AppContext ctx = AppContext.get(requireContext());
        ProvisoesViewModelFactory factory = new ProvisoesViewModelFactory(
                ctx.getCriarProvisaoUseCase(),
                ctx.getEditarProvisaoUseCase(),
                ctx.getDesativarProvisaoUseCase(),
                ctx.getProvisaoRepository());
        viewModel = new ViewModelProvider(requireParentFragment(), factory)
                .get(ProvisoesViewModel.class);

        etNome       = view.findViewById(R.id.et_nome);
        etTotalAnual = view.findViewById(R.id.et_total_anual);
        etValorMensal= view.findViewById(R.id.et_valor_mensal);
        spCategoria  = view.findViewById(R.id.sp_categoria);

        if (getArguments() != null) {
            provisaoId = getArguments().getString("provisaoId");
        }

        carregarCategorias(ctx);

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
                    List<String> nomes = new ArrayList<>();
                    nomes.add(getString(R.string.provisao_categoria_opcional));
                    for (Categoria c : categorias) nomes.add(c.getNome());
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                            android.R.layout.simple_spinner_item, nomes);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spCategoria.setAdapter(adapter);

                    if (provisaoId != null) preencherFormulario();
                });
            } catch (Exception e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void preencherFormulario() {
        viewModel.getProvisoes().getValue();
        new Thread(() -> {
            try {
                AppContext ctx = AppContext.get(requireContext());
                ctx.getProvisaoRepository().buscarPorId(provisaoId).ifPresent(p -> {
                    requireActivity().runOnUiThread(() -> {
                        etNome.setText(p.getNome());
                        etTotalAnual.setText(String.valueOf(p.getTotalAnual()));
                        etValorMensal.setText(String.valueOf(p.getValorMensal()));
                        // Selecionar categoria (índice +1 por causa do "Nenhuma")
                        if (p.getCategoriaId() != null) {
                            for (int i = 0; i < categorias.size(); i++) {
                                if (categorias.get(i).getId().equals(p.getCategoriaId())) {
                                    spCategoria.setSelection(i + 1);
                                    break;
                                }
                            }
                        }
                    });
                });
            } catch (Exception ignored) { }
        }).start();
    }

    private void salvar(View view) {
        String nome = etNome.getText() != null ? etNome.getText().toString().trim() : "";
        String totalAnualStr = etTotalAnual.getText() != null ? etTotalAnual.getText().toString().trim() : "";
        String valorMensalStr = etValorMensal.getText() != null ? etValorMensal.getText().toString().trim() : "";

        if (nome.isEmpty()) {
            Toast.makeText(requireContext(), R.string.erro_nome_obrigatorio, Toast.LENGTH_SHORT).show();
            return;
        }

        double totalAnual;
        double valorMensal;
        try {
            totalAnual = Double.parseDouble(totalAnualStr);
            valorMensal = Double.parseDouble(valorMensalStr);
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), R.string.erro_valor_invalido, Toast.LENGTH_SHORT).show();
            return;
        }

        // Categoria: posição 0 = "Nenhuma" (null)
        int pos = spCategoria.getSelectedItemPosition();
        String categoriaId = (pos > 0 && pos - 1 < categorias.size())
                ? categorias.get(pos - 1).getId()
                : null;

        if (provisaoId == null) {
            viewModel.criar(nome, totalAnual, valorMensal, categoriaId);
        } else {
            viewModel.editar(provisaoId, nome, totalAnual, valorMensal, categoriaId);
        }
    }
}
