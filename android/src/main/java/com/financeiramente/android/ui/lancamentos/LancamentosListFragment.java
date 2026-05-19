package com.financeiramente.android.ui.lancamentos;

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
import com.financeiramente.android.viewmodel.LancamentosListViewModel;
import com.financeiramente.android.viewmodel.LancamentosListViewModelFactory;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class LancamentosListFragment extends Fragment {

    private LancamentosListViewModel viewModel;
    private LancamentoAdapter adapter;
    private TextView tvVazio;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_lancamentos_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AppContext ctx = AppContext.get(requireContext());
        LancamentosListViewModelFactory factory = new LancamentosListViewModelFactory(
                ctx.getListarLancamentosUseCase(),
                ctx.getDeletarLancamentoUseCase());
        viewModel = new ViewModelProvider(this, factory).get(LancamentosListViewModel.class);

        tvVazio = view.findViewById(R.id.tv_vazio);

        adapter = new LancamentoAdapter(
                lancamento -> {
                    // Navegar para edição
                    Bundle args = new Bundle();
                    args.putString("lancamentoId", lancamento.getId());
                    Navigation.findNavController(view)
                            .navigate(R.id.action_lancamentosListFragment_to_lancamentoFormFragment, args);
                },
                lancamento -> {
                    // Confirmação de exclusão
                    new AlertDialog.Builder(requireContext())
                            .setTitle(R.string.excluir_lancamento)
                            .setMessage(getString(R.string.confirmar_exclusao_lancamento, lancamento.getDescricao()))
                            .setPositiveButton(android.R.string.ok, (d, w) ->
                                    viewModel.deletarLancamento(lancamento.getId()))
                            .setNegativeButton(android.R.string.cancel, null)
                            .show();
                });

        RecyclerView rv = view.findViewById(R.id.rv_lancamentos);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        FloatingActionButton fab = view.findViewById(R.id.fab_novo_lancamento);
        fab.setOnClickListener(v ->
                Navigation.findNavController(view)
                        .navigate(R.id.action_lancamentosListFragment_to_lancamentoFormFragment));

        viewModel.getLancamentos().observe(getViewLifecycleOwner(), lancamentos -> {
            adapter.setItems(lancamentos);
            tvVazio.setVisibility(lancamentos == null || lancamentos.isEmpty() ? View.VISIBLE : View.GONE);
        });

        viewModel.getErro().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.carregarLancamentos();
    }
}
