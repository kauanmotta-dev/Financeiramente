package com.financeiramente.android.ui.categorias;

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
import androidx.navigation.Navigation;

import com.financeiramente.android.R;
import com.financeiramente.android.app.AppContext;
import com.financeiramente.core.domain.entity.Categoria;
import com.financeiramente.core.domain.vo.TipoCategoria;
import com.financeiramente.core.usecase.CriarCategoriaUseCase;
import com.financeiramente.core.usecase.EditarCategoriaUseCase;
import com.financeiramente.core.util.DomainException;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Optional;

public class CategoriaFormFragment extends Fragment {

    private TextInputLayout tilNome;
    private TextInputEditText etNome;
    private Spinner spTipo;
    private TextInputEditText etLimite;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_categoria_form, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tilNome = view.findViewById(R.id.til_nome);
        etNome = view.findViewById(R.id.et_nome);
        spTipo = view.findViewById(R.id.sp_tipo);
        etLimite = view.findViewById(R.id.et_limite);
        MaterialButton btnSalvar = view.findViewById(R.id.btn_salvar);

        // Populate tipo spinner
        TipoCategoria[] tipos = TipoCategoria.values();
        ArrayAdapter<TipoCategoria> tipoAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, tipos);
        tipoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTipo.setAdapter(tipoAdapter);

        // Read navigation arguments
        String categoriaId = getArguments() != null ? getArguments().getString("categoriaId") : null;
        String paiId = getArguments() != null ? getArguments().getString("paiId") : null;

        // Se for subcategoria, o tipo é herdado do pai — oculta o spinner
        if (paiId != null) {
            spTipo.setVisibility(View.GONE);
        }

        // If editing, populate form with existing values
        AppContext ctx = AppContext.get(requireContext());
        if (categoriaId != null) {
            Optional<Categoria> categoriaOpt = ctx.getCategoriaRepository().buscarPorId(categoriaId);
            categoriaOpt.ifPresent(categoria -> {
                etNome.setText(categoria.getNome());
                for (int i = 0; i < tipos.length; i++) {
                    if (tipos[i] == categoria.getTipo()) {
                        spTipo.setSelection(i);
                        break;
                    }
                }
                if (categoria.getLimiteMensal() != null) {
                    etLimite.setText(String.valueOf(categoria.getLimiteMensal()));
                }
            });
        }

        final String finalCategoriaId = categoriaId;
        final String finalPaiId = paiId;
        btnSalvar.setOnClickListener(v -> salvar(finalCategoriaId, finalPaiId, tipos, ctx, view));
    }

    private void salvar(String categoriaId, String paiId, TipoCategoria[] tipos,
                        AppContext ctx, View view) {
        String nome = etNome.getText() != null ? etNome.getText().toString().trim() : "";
        if (nome.isEmpty()) {
            tilNome.setError(getString(R.string.erro_nome_obrigatorio));
            return;
        }
        tilNome.setError(null);

        TipoCategoria tipo = (TipoCategoria) spTipo.getSelectedItem();

        Double limiteMensal = null;
        String limiteStr = etLimite.getText() != null ? etLimite.getText().toString().trim() : "";
        if (!limiteStr.isEmpty()) {
            try {
                limiteMensal = Double.parseDouble(limiteStr);
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), getString(R.string.erro_limite_invalido),
                        Toast.LENGTH_SHORT).show();
                return;
            }
        }

        try {
            if (categoriaId != null) {
                EditarCategoriaUseCase editar = ctx.getEditarCategoriaUseCase();
                editar.executar(categoriaId, nome, tipo, limiteMensal);
            } else {
                CriarCategoriaUseCase criar = ctx.getCriarCategoriaUseCase();
                criar.executar(nome, tipo, paiId, limiteMensal);
            }
            Navigation.findNavController(view).popBackStack();
        } catch (DomainException e) {
            Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
