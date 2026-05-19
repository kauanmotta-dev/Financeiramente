package com.financeiramente.android.ui.provisoes;

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
import com.financeiramente.android.viewmodel.ProvisoesViewModel;
import com.financeiramente.android.viewmodel.ProvisoesViewModelFactory;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ProvisoesFragment extends Fragment {

    private ProvisoesViewModel viewModel;
    private ProvisaoAdapter adapter;
    private TextView tvVazio;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_provisoes, container, false);
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
        viewModel = new ViewModelProvider(this, factory).get(ProvisoesViewModel.class);

        tvVazio = view.findViewById(R.id.tv_vazio);

        adapter = new ProvisaoAdapter(
                provisao -> {
                    Bundle args = new Bundle();
                    args.putString("provisaoId", provisao.getId());
                    Navigation.findNavController(view)
                            .navigate(R.id.action_provicoesFragment_to_provisaoFormFragment, args);
                },
                provisao -> new AlertDialog.Builder(requireContext())
                        .setTitle(R.string.desativar_provisao)
                        .setMessage(getString(R.string.confirmar_desativar_provisao, provisao.getNome()))
                        .setPositiveButton(android.R.string.ok, (d, w) ->
                                viewModel.desativar(provisao.getId()))
                        .setNegativeButton(android.R.string.cancel, null)
                        .show()
        );

        RecyclerView rv = view.findViewById(R.id.rv_provisoes);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        FloatingActionButton fab = view.findViewById(R.id.fab_nova_provisao);
        fab.setOnClickListener(v ->
                Navigation.findNavController(view)
                        .navigate(R.id.action_provicoesFragment_to_provisaoFormFragment));

        viewModel.getProvisoes().observe(getViewLifecycleOwner(), lista -> {
            adapter.setItems(lista);
            tvVazio.setVisibility(lista == null || lista.isEmpty() ? View.VISIBLE : View.GONE);
        });

        viewModel.getErro().observe(getViewLifecycleOwner(), erro -> {
            if (erro != null) Toast.makeText(requireContext(), erro, Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.carregar();
    }
}
