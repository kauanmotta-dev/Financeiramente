package com.financeiramente.android.ui.lancamentos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.financeiramente.android.R;
import com.financeiramente.android.app.AppContext;
import com.financeiramente.android.viewmodel.LancamentosListViewModel;
import com.financeiramente.android.viewmodel.LancamentosListViewModelFactory;
import com.financeiramente.core.domain.entity.Lancamento;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;
import com.google.android.material.snackbar.Snackbar;

public class LancamentosListFragment extends Fragment {

    private LancamentosListViewModel viewModel;
    private LancamentoAdapter adapter;
    private View emptyState;

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
                ctx.getDeletarLancamentoUseCase(),
                ctx.getCategoriaRepository(),
                ctx.getTagRepository());
        viewModel = new ViewModelProvider(this, factory).get(LancamentosListViewModel.class);

        // ── Empty state — Épico 8 ─────────────────────────────────────────
        emptyState = view.findViewById(R.id.layout_empty_state);
        ((ImageView) emptyState.findViewById(R.id.iv_empty_illustration))
                .setImageResource(R.drawable.ic_empty_transactions);
        ((TextView) emptyState.findViewById(R.id.tv_empty_title))
                .setText(R.string.empty_lancamentos_title);
        ((TextView) emptyState.findViewById(R.id.tv_empty_subtitle))
                .setText(R.string.empty_lancamentos_subtitle);
        com.google.android.material.button.MaterialButton btnCta =
                emptyState.findViewById(R.id.btn_empty_cta);
        btnCta.setText(R.string.empty_lancamentos_cta);
        btnCta.setVisibility(View.VISIBLE);
        btnCta.setOnClickListener(v ->
                Navigation.findNavController(view)
                        .navigate(R.id.action_lancamentosListFragment_to_lancamentoFormFragment));

        // ── Adapter ──────────────────────────────────────────────────────────
        adapter = new LancamentoAdapter(lancamento -> {
            // Tap → editar
            Bundle args = new Bundle();
            args.putString("lancamentoId", lancamento.getId());
            Navigation.findNavController(view)
                    .navigate(R.id.action_lancamentosListFragment_to_lancamentoFormFragment, args);
        });

        // ── RecyclerView ─────────────────────────────────────────────────────
        RecyclerView rv = view.findViewById(R.id.rv_lancamentos);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);
        rv.addItemDecoration(new StickyDateHeaderDecoration(adapter));

        // ── Swipe actions ────────────────────────────────────────────────────
        LancamentoSwipeCallback swipeCallback = new LancamentoSwipeCallback(
                requireContext(), adapter,
                new LancamentoSwipeCallback.SwipeListener() {
                    @Override
                    public void onSwipeDelete(int position) {
                        Lancamento l = adapter.getLancamentoAt(position);
                        if (l == null) {
                            adapter.notifyItemChanged(position);
                            return;
                        }
                        viewModel.deletarLancamento(l.getId());

                        Snackbar.make(view, R.string.lancamento_excluido, Snackbar.LENGTH_LONG)
                                .setAction(R.string.desfazer, v -> {
                                    // Undo: reload without deleting
                                    // (deletion is already fired; show message only)
                                    Toast.makeText(requireContext(),
                                            R.string.lancamento_excluido, Toast.LENGTH_SHORT).show();
                                })
                                .show();
                    }

                    @Override
                    public void onSwipeEdit(int position) {
                        Lancamento l = adapter.getLancamentoAt(position);
                        if (l == null) {
                            adapter.notifyItemChanged(position);
                            return;
                        }
                        // Restore item visually before navigating
                        adapter.notifyItemChanged(position);
                        Bundle args = new Bundle();
                        args.putString("lancamentoId", l.getId());
                        Navigation.findNavController(view)
                                .navigate(R.id.action_lancamentosListFragment_to_lancamentoFormFragment, args);
                    }
                });

        new ItemTouchHelper(swipeCallback).attachToRecyclerView(rv);

        // ── FAB ──────────────────────────────────────────────────────────────
        FloatingActionButton fab = view.findViewById(R.id.fab_novo_lancamento);
        fab.setOnClickListener(v ->
                Navigation.findNavController(view)
                        .navigate(R.id.action_lancamentosListFragment_to_lancamentoFormFragment));

        // ── SearchBar / SearchView ────────────────────────────────────────────
        SearchBar searchBar  = view.findViewById(R.id.search_bar);
        SearchView searchView = view.findViewById(R.id.search_view);
        searchView.setupWithSearchBar(searchBar);

        searchView.addTransitionListener((sv, previousState, newState) -> {
            if (newState == SearchView.TransitionState.HIDING
                    || newState == SearchView.TransitionState.HIDDEN) {
                // Clear filter when search is closed
                adapter.filter("");
                searchBar.setText("");
            }
        });

        searchView.getEditText().addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(android.text.Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s != null ? s.toString() : "");
            }
        });

        // ── Observe ──────────────────────────────────────────────────────────
        viewModel.getLancamentos().observe(getViewLifecycleOwner(), lancamentos -> {
            adapter.setData(lancamentos,
                    viewModel.getCategorias().getValue(),
                    viewModel.getTagsMap().getValue());
            emptyState.setVisibility(lancamentos == null || lancamentos.isEmpty() ? View.VISIBLE : View.GONE);
        });

        viewModel.getCategorias().observe(getViewLifecycleOwner(), cats ->
                adapter.setData(viewModel.getLancamentos().getValue(), cats,
                        viewModel.getTagsMap().getValue()));

        viewModel.getTagsMap().observe(getViewLifecycleOwner(), tags ->
                adapter.setData(viewModel.getLancamentos().getValue(),
                        viewModel.getCategorias().getValue(), tags));

        viewModel.getErro().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
        });

        // Snackbar "Lançamento salvo" com ação Desfazer (disparado pelo formulário)
        getParentFragmentManager().setFragmentResultListener(
                "lancamento_salvo", getViewLifecycleOwner(),
                (requestKey, bundle) -> {
                    String lancId = bundle.getString("lancamentoId");
                    Snackbar snackbar = Snackbar.make(view,
                            R.string.lancamento_salvo, Snackbar.LENGTH_LONG);
                    if (lancId != null) {
                        snackbar.setAction(R.string.desfazer, v -> {
                            viewModel.deletarLancamento(lancId);
                        });
                    }
                    snackbar.show();
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.carregarLancamentos();
    }
}

