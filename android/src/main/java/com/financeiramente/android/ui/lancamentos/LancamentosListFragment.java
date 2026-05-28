package com.financeiramente.android.ui.lancamentos;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.financeiramente.core.domain.entity.CartaoCredito;
import com.financeiramente.core.domain.entity.Lancamento;
import com.financeiramente.core.domain.vo.TipoLancamento;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class LancamentosListFragment extends Fragment {

    private static final String ARG_SOURCE_TAB = "sourceTab";

    private LancamentosListViewModel viewModel;
    private LancamentoAdapter adapter;
    private RecyclerView recyclerLancamentos;
    private View emptyState;
    private ShimmerFrameLayout shimmerLancamentos;
    private TipoLancamento filtroTipoAtual = null;
    private int sourceTab = 0;
    private boolean carregamentoInicialFinalizado = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_lancamentos_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        sourceTab = args != null ? args.getInt(ARG_SOURCE_TAB, 0) : 0;

        MaterialToolbar toolbarLancamentos = view.findViewById(R.id.toolbar_lancamentos);
        toolbarLancamentos.setNavigationOnClickListener(v -> {
            if (!Navigation.findNavController(view).navigateUp()) {
                Navigation.findNavController(view).navigate(
                        sourceTab == R.id.nav_gastos ? R.id.nav_gastos : R.id.nav_dashboard);
            }
        });

        AppContext ctx = AppContext.get(requireContext());
        LancamentosListViewModelFactory factory = new LancamentosListViewModelFactory(
                ctx.getListarLancamentosUseCase(),
                ctx.getDeletarLancamentoUseCase(),
                ctx.getCoreServices().getCategoriaRepository(),
                ctx.getCoreServices().getTagRepository());
        viewModel = new ViewModelProvider(this, factory).get(LancamentosListViewModel.class);

        // ── Empty state — Épico 8 ─────────────────────────────────────────
        shimmerLancamentos = view.findViewById(R.id.shimmer_lancamentos);
        mostrarLoadingInicial(true);

        emptyState = view.findViewById(R.id.layout_empty_state);
        ((ImageView) emptyState.findViewById(R.id.iv_empty_illustration))
            .setVisibility(View.GONE);
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
                .navigate(R.id.action_lancamentosListFragment_to_lancamentoFormFragment,
                    criarArgsLancamento(null)));

        // ── Adapter ──────────────────────────────────────────────────────────
        adapter = new LancamentoAdapter(
            lancamento -> navegarParaEdicao(view, lancamento),
            lancamento -> {
                mostrarOpcoesLancamento(view, lancamento);
                return true;
            });

        // ── RecyclerView ─────────────────────────────────────────────────────
        recyclerLancamentos = view.findViewById(R.id.rv_lancamentos);
        recyclerLancamentos.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerLancamentos.setAdapter(adapter);
        recyclerLancamentos.addItemDecoration(new StickyDateHeaderDecoration(adapter));

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
                        confirmarExclusaoLancamento(view, l.getId(), l.getDescricao(), position);
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
                        navegarParaEdicao(view, l);
                    }
                });

        new ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerLancamentos);

        // ── SearchBar / SearchView ────────────────────────────────────────────
        SearchBar searchBar = view.findViewById(R.id.search_bar);
        SearchView searchView = view.findViewById(R.id.search_view);
        searchView.setupWithSearchBar(searchBar);
        searchView.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s != null ? s.toString() : "");
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        searchView.addTransitionListener((sv, previousState, newState) -> {
            if (newState == SearchView.TransitionState.HIDDEN
                    && (searchView.getEditText().getText() == null
                    || searchView.getEditText().getText().length() == 0)) {
                adapter.filter("");
            }
        });

        ChipGroup chipGroupTipo = view.findViewById(R.id.chip_group_lancamentos_tipo);
        chipGroupTipo.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chip_lancamentos_receitas) {
                filtroTipoAtual = TipoLancamento.RECEITA;
            } else if (id == R.id.chip_lancamentos_despesas) {
                filtroTipoAtual = TipoLancamento.DESPESA;
            } else {
                filtroTipoAtual = null;
            }
            renderLista();
        });

        // ── Observe ──────────────────────────────────────────────────────────
        viewModel.getLancamentos().observe(getViewLifecycleOwner(), lancamentos -> {
            renderLista();
            if (!carregamentoInicialFinalizado) {
                carregamentoInicialFinalizado = true;
                mostrarLoadingInicial(false);
            }
        });

        viewModel.getCategorias().observe(getViewLifecycleOwner(), cats ->
            renderLista());

        viewModel.getTagsMap().observe(getViewLifecycleOwner(), tags ->
            renderLista());

        viewModel.getErro().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
        });

        // Seção de cartões
        carregarCartoesAtivos(view);

        // Snackbar "Lançamento salvo" com ação Desfazer (disparado pelo formulário)
        getParentFragmentManager().setFragmentResultListener(
                "lancamento_salvo", getViewLifecycleOwner(),
                (requestKey, bundle) -> {
                    String lancId = bundle.getString("lancamentoId");
                    Snackbar snackbar = Snackbar.make(view,
                            R.string.lancamento_salvo, Snackbar.LENGTH_LONG);
                    if (lancId != null) {
                        snackbar.setAction(R.string.desfazer,
                                v -> confirmarExclusaoLancamento(view, lancId, null, null));
                    }
                    snackbar.show();
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!carregamentoInicialFinalizado) {
            mostrarLoadingInicial(true);
        }
        viewModel.carregarLancamentos();
    }

    private void mostrarLoadingInicial(boolean loading) {
        if (recyclerLancamentos != null) {
            recyclerLancamentos.setVisibility(loading ? View.INVISIBLE : View.VISIBLE);
        }
        if (loading && emptyState != null) {
            emptyState.setVisibility(View.GONE);
        }
        if (shimmerLancamentos != null) {
            shimmerLancamentos.setVisibility(loading ? View.VISIBLE : View.GONE);
            if (loading) {
                shimmerLancamentos.startShimmer();
            } else {
                shimmerLancamentos.stopShimmer();
            }
        }
    }

    private void carregarCartoesAtivos(View rootView) {
        AppContext ctx = AppContext.get(requireContext());
        new Thread(() -> {
            java.util.List<CartaoCredito> cartoes = ctx.getCoreServices().getCartaoRepository().listarAtivos();
            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                if (cartoes == null || cartoes.isEmpty()) return;

                View tvTitulo = rootView.findViewById(R.id.tv_cartoes_titulo);
                View hsv = rootView.findViewById(R.id.hsv_cartoes);
                ChipGroup cg = rootView.findViewById(R.id.cg_lancamentos_cartoes);

                tvTitulo.setVisibility(View.VISIBLE);
                hsv.setVisibility(View.VISIBLE);

                for (CartaoCredito cartao : cartoes) {
                    Chip chip = new Chip(requireContext());
                    String icone = cartao.getIcone() == null || cartao.getIcone().trim().isEmpty() ? "💳" : cartao.getIcone();
                    chip.setText(icone + " " + cartao.getNome());
                    chip.setCheckable(true);
                    chip.setOnCheckedChangeListener((cb, checked) -> {
                        if (checked) {
                            android.os.Bundle args = new android.os.Bundle();
                            args.putString("cartaoId", cartao.getId());
                            Navigation.findNavController(rootView)
                                    .navigate(R.id.action_lancamentosListFragment_to_faturaListFragment, args);
                            cb.setChecked(false);
                        }
                    });
                    cg.addView(chip);
                }
            });
        }).start();
    }

    private void renderLista() {
        List<Lancamento> base = viewModel.getLancamentos().getValue();
        List<Lancamento> filtrada = new ArrayList<>();
        if (base != null) {
            for (Lancamento lancamento : base) {
                // Ocultar lançamentos vinculados a fatura de cartão
                if (lancamento.getFaturaId() != null) continue;
                if (filtroTipoAtual == null || filtroTipoAtual == lancamento.getTipo()) {
                    filtrada.add(lancamento);
                }
            }
        }
        adapter.setData(filtrada,
                viewModel.getCategorias().getValue(),
                viewModel.getTagsMap().getValue());
        emptyState.setVisibility(filtrada.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void mostrarOpcoesLancamento(View rootView, Lancamento lancamento) {
        CharSequence[] opcoes = new CharSequence[] {
                getString(R.string.editar),
                getString(R.string.excluir)
        };
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(lancamento.getDescricao())
                .setItems(opcoes, (dialog, which) -> {
                    if (which == 0) {
                        navegarParaEdicao(rootView, lancamento);
                    } else if (which == 1) {
                        confirmarExclusaoLancamento(
                                rootView,
                                lancamento.getId(),
                                lancamento.getDescricao(),
                                null);
                    }
                })
                .show();
    }

    private void navegarParaEdicao(View rootView, Lancamento lancamento) {
        Bundle args = criarArgsLancamento(lancamento.getId());
        Navigation.findNavController(rootView)
                .navigate(R.id.action_lancamentosListFragment_to_lancamentoFormFragment, args);
    }

    private Bundle criarArgsLancamento(@Nullable String lancamentoId) {
        Bundle args = new Bundle();
        args.putInt(ARG_SOURCE_TAB, sourceTab);
        if (lancamentoId != null) {
            args.putString("lancamentoId", lancamentoId);
        }
        return args;
    }

    private void confirmarExclusaoLancamento(View rootView,
                                             String lancamentoId,
                                             @Nullable String descricao,
                                             @Nullable Integer swipePosition) {
        String mensagem = descricao != null && !descricao.trim().isEmpty()
                ? getString(R.string.confirmar_exclusao_lancamento, descricao)
                : getString(R.string.confirmar_exclusao_lancamento_generico);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.excluir_lancamento)
                .setMessage(mensagem)
                .setPositiveButton(R.string.excluir,
                        (dialog, which) -> excluirLancamento(rootView, lancamentoId))
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                    if (swipePosition != null) {
                        adapter.notifyItemChanged(swipePosition);
                    }
                })
                .setOnCancelListener(dialog -> {
                    if (swipePosition != null) {
                        adapter.notifyItemChanged(swipePosition);
                    }
                })
                .show();
    }

    private void excluirLancamento(View rootView, String lancamentoId) {
        viewModel.deletarLancamento(lancamentoId);
        Snackbar.make(rootView, R.string.lancamento_excluido, Snackbar.LENGTH_LONG)
                .setAction(R.string.desfazer, v -> {
                    Toast.makeText(requireContext(),
                            R.string.lancamento_excluido, Toast.LENGTH_SHORT).show();
                })
                .show();
    }
}

