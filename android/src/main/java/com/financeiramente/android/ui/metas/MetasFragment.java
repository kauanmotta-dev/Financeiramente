package com.financeiramente.android.ui.metas;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.financeiramente.android.R;
import com.financeiramente.android.app.AppContext;
import com.financeiramente.android.viewmodel.MetasViewModel;
import com.financeiramente.android.viewmodel.MetasViewModelFactory;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.LocalDate;
import java.util.Locale;

public class MetasFragment extends Fragment {

    private MetasViewModel viewModel;
    private MetaAdapter adapter;
    private TextView tvVazio;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_metas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AppContext ctx = AppContext.get(requireContext());
        MetasViewModelFactory factory = new MetasViewModelFactory(
                ctx.getCriarMetaUseCase(),
                ctx.getEditarMetaUseCase(),
                ctx.getDesativarMetaUseCase(),
                ctx.getMetaRepository());
        viewModel = new ViewModelProvider(this, factory).get(MetasViewModel.class);

        tvVazio = view.findViewById(R.id.tv_vazio);
        RecyclerView rv = view.findViewById(R.id.rv_metas);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new MetaAdapter(
                meta -> {
                    Bundle args = new Bundle();
                    args.putString("metaId", meta.getId());
                    Navigation.findNavController(view)
                            .navigate(R.id.action_metasFragment_to_metaDetalheFragment, args);
                },
                meta -> {
                    new AlertDialog.Builder(requireContext())
                            .setTitle(R.string.desativar_meta)
                            .setMessage(getString(R.string.confirmar_desativar_meta, meta.getNome()))
                            .setPositiveButton(android.R.string.ok, (d, w) -> viewModel.desativar(meta.getId()))
                            .setNegativeButton(android.R.string.cancel, null)
                            .show();
                    return true;
                }
        );
        rv.setAdapter(adapter);

        viewModel.getMetas().observe(getViewLifecycleOwner(), lista -> {
            adapter.setItems(lista);
            tvVazio.setVisibility(lista == null || lista.isEmpty() ? View.VISIBLE : View.GONE);
        });
        viewModel.getErro().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
        });

        FloatingActionButton fab = view.findViewById(R.id.fab_nova_meta);
        fab.setOnClickListener(v -> mostrarDialogoNovaMeta(view));
    }

    private void mostrarDialogoNovaMeta(View navView) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.fragment_meta_form, null);
        EditText etNome = dialogView.findViewById(R.id.et_meta_nome);
        EditText etValorObjetivo = dialogView.findViewById(R.id.et_meta_valor_objetivo);
        EditText etDataAlvo = dialogView.findViewById(R.id.et_meta_data_alvo);
        EditText etDescricao = dialogView.findViewById(R.id.et_meta_descricao);

        // Date picker para dataAlvo
        etDataAlvo.setOnClickListener(v -> {
            LocalDate hoje = LocalDate.now();
            new DatePickerDialog(requireContext(),
                    (dp, y, m, d) -> etDataAlvo.setText(
                            String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d)),
                    hoje.getYear(), hoje.getMonthValue() - 1, hoje.getDayOfMonth())
                    .show();
        });

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.add_meta)
                .setView(dialogView)
                .setPositiveButton(R.string.salvar, (d, w) -> {
                    String nome = etNome.getText().toString().trim();
                    String valorStr = etValorObjetivo.getText().toString().trim();
                    if (nome.isEmpty() || valorStr.isEmpty()) {
                        Toast.makeText(requireContext(), R.string.erro_nome_obrigatorio, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        double valor = Double.parseDouble(valorStr.replace(',', '.'));
                        String dataAlvo = etDataAlvo.getText().toString().trim();
                        String descricao = etDescricao.getText().toString().trim();
                        viewModel.criar(nome, valor,
                                dataAlvo.isEmpty() ? null : dataAlvo,
                                descricao.isEmpty() ? null : descricao);
                    } catch (NumberFormatException e) {
                        Toast.makeText(requireContext(), R.string.erro_valor_invalido, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.carregar();
    }
}
