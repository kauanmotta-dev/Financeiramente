package com.financeiramente.android.ui.recorrentes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.financeiramente.android.ui.common.CategoriaVisualFallback;
import com.financeiramente.android.viewmodel.RecorrentesViewModel;
import com.financeiramente.android.viewmodel.RecorrentesViewModelFactory;
import com.financeiramente.core.domain.entity.Categoria;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RecorrentesFragment extends Fragment {

    private RecorrentesViewModel viewModel;
    private RecorrenteAdapter adapter;
    private TextView tvVazio;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recorrentes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.btn_back_recorrentes).setOnClickListener(v ->
            Navigation.findNavController(view).navigateUp());

        AppContext ctx = AppContext.get(requireContext());
        RecorrentesViewModelFactory factory = new RecorrentesViewModelFactory(
                ctx.getCriarLancamentoRecorrenteUseCase(),
                ctx.getEditarLancamentoRecorrenteUseCase(),
                ctx.getDesativarLancamentoRecorrenteUseCase(),
                ctx.getLancamentoRecorrenteRepository());
        viewModel = new ViewModelProvider(this, factory).get(RecorrentesViewModel.class);

        tvVazio = view.findViewById(R.id.tv_vazio);

        adapter = new RecorrenteAdapter(
                recorrente -> {
                    Bundle args = new Bundle();
                    args.putString("recorrenteId", recorrente.getId());
                    Navigation.findNavController(view)
                            .navigate(R.id.action_recorrentesFragment_to_recorrenteFormFragment, args);
                },
                recorrente -> new AlertDialog.Builder(requireContext())
                        .setTitle(R.string.desativar_recorrente)
                        .setMessage(getString(R.string.confirmar_desativar_recorrente, recorrente.getDescricao()))
                        .setPositiveButton(android.R.string.ok, (d, w) ->
                                viewModel.desativar(recorrente.getId()))
                        .setNegativeButton(android.R.string.cancel, null)
                        .show()
        );

        RecyclerView rv = view.findViewById(R.id.rv_recorrentes);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        view.findViewById(R.id.btn_novo_recorrente).setOnClickListener(v ->
                Navigation.findNavController(view)
                        .navigate(R.id.action_recorrentesFragment_to_recorrenteFormFragment));

        viewModel.getRecorrentes().observe(getViewLifecycleOwner(), lista -> {
            adapter.setItems(lista);
            tvVazio.setVisibility(lista == null || lista.isEmpty() ? View.VISIBLE : View.GONE);
        });

        viewModel.getErro().observe(getViewLifecycleOwner(), erro -> {
            if (erro != null) Toast.makeText(requireContext(), erro, Toast.LENGTH_LONG).show();
        });

        carregarLabelsCategorias(ctx);
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.carregar();
    }

    private void carregarLabelsCategorias(AppContext ctx) {
        new Thread(() -> {
            try {
                List<Categoria> categorias = ctx.getCategoriaRepository().listarTodas();
                Map<String, Categoria> porId = new LinkedHashMap<>();
                for (Categoria categoria : categorias) {
                    porId.put(categoria.getId(), categoria);
                }

                Map<String, String> labels = new LinkedHashMap<>();
                for (Categoria categoria : categorias) {
                    String label = CategoriaVisualFallback.icone(categoria, null)
                            + " " + categoria.getNome();
                    if (categoria.getPaiId() != null) {
                        Categoria pai = porId.get(categoria.getPaiId());
                        if (pai != null) {
                            label = CategoriaVisualFallback.icone(pai, null)
                                    + " " + pai.getNome()
                                    + " > "
                                    + CategoriaVisualFallback.icone(categoria, null)
                                    + " " + categoria.getNome();
                        }
                    }
                    labels.put(categoria.getId(), label);
                }

                requireActivity().runOnUiThread(() -> adapter.setCategoryLabels(labels));
            } catch (Exception ignored) {
                // Mantém fallback sem interromper a tela.
            }
        }).start();
    }
}
