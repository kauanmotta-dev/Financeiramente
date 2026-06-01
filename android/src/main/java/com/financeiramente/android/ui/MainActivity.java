package com.financeiramente.android.ui;

import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.financeiramente.android.R;
import com.financeiramente.android.app.AppContext;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private NavController navController;
    private BottomNavigationView bottomNav;
    private FloatingActionButton fabNovoLancamento;
    private View navHostContainer;
    private boolean fabPermitidoNaTelaAtual = true;
    private float gestureStartX;
    private float gestureStartY;

    private static final float SWIPE_MIN_DISTANCE_DP = 80f;
    private static final float SWIPE_MAX_OFFPATH_DP = 96f;

    private static final int[] ORDEM_ABAS = {R.id.nav_metas, R.id.nav_dashboard, R.id.nav_gastos};
    private int indiceAbaAtual = 1; // começa no dashboard

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppContext appContext = AppContext.get(this); // initialize singleton and run migrations
        setContentView(R.layout.activity_main);

        new Thread(() -> {
            try {
                appContext.getGerarCobrancasRecorrentesCartaoUseCase().executar();
            } catch (Exception e) {
                Log.w(TAG, "Falha ao gerar cobrancas recorrentes de cartao no startup", e);
            }
        }).start();

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();
        navHostContainer = findViewById(R.id.nav_host_fragment);

        bottomNav = findViewById(R.id.bottom_nav);
        fabNovoLancamento = findViewById(R.id.fab_novo_lancamento);
        configurarBottomNavigation(bottomNav);
        configurarFabGlobal();
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int destinationId = destination.getId();
            fabPermitidoNaTelaAtual = destinationId != R.id.nav_configuracoes
                    && destinationId != R.id.nav_cartoes
                    && destinationId != R.id.nav_dashboard
                    && destinationId != R.id.nav_metas
                    && destinationId != R.id.metaDetalheFragment
                    && destinationId != R.id.categoriasFragment
                    && destinationId != R.id.tagsFragment
                    && destinationId != R.id.nav_relatorios
                    && destinationId != R.id.nav_gastos
                    && destinationId != R.id.lancamentosListFragment
                    && destinationId != R.id.lancamentoFormFragment
                    && destinationId != R.id.faturaListFragment
                    && destinationId != R.id.compraCartaoListFragment
                    && destinationId != R.id.faturaDetalheFragment;
            aplicarVisibilidadeFab(true);
        });
        configurarInsetsEComportamentoTeclado();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        tratarSwipeEntreAbas(ev);

        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View focused = getCurrentFocus();
            if (focused instanceof EditText) {
                Rect outRect = new Rect();
                focused.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                    focused.clearFocus();
                    InputMethodManager imm = ContextCompat.getSystemService(this, InputMethodManager.class);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(focused.getWindowToken(), 0);
                    }
                    setBottomNavVisible(true);
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private void tratarSwipeEntreAbas(MotionEvent ev) {
        if (ev == null || navController == null || bottomNav == null) {
            return;
        }

        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            gestureStartX = ev.getX();
            gestureStartY = ev.getY();
            return;
        }

        if (ev.getAction() != MotionEvent.ACTION_UP || !podeNavegarPorSwipe()) {
            return;
        }

        float dx = ev.getX() - gestureStartX;
        float dy = ev.getY() - gestureStartY;
        float minDistancePx = dpToPx(SWIPE_MIN_DISTANCE_DP);
        float maxOffpathPx = dpToPx(SWIPE_MAX_OFFPATH_DP);

        if (Math.abs(dx) < minDistancePx || Math.abs(dy) > maxOffpathPx) {
            return;
        }

        int[] ordemAbas = ORDEM_ABAS;
        int abaAtual = bottomNav.getSelectedItemId();
        int indiceAtual = -1;
        for (int i = 0; i < ordemAbas.length; i++) {
            if (ordemAbas[i] == abaAtual) {
                indiceAtual = i;
                break;
            }
        }

        if (indiceAtual < 0) {
            return;
        }

        int proximoIndice = dx < 0 ? indiceAtual + 1 : indiceAtual - 1;
        if (proximoIndice < 0 || proximoIndice >= ordemAbas.length) {
            return;
        }

        bottomNav.setSelectedItemId(ordemAbas[proximoIndice]);
    }

    private boolean podeNavegarPorSwipe() {
        NavDestination destinoAtual = navController.getCurrentDestination();
        if (destinoAtual == null) {
            return false;
        }

        int id = destinoAtual.getId();
        return id == R.id.nav_metas
                || id == R.id.nav_dashboard
                || id == R.id.nav_gastos;
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    private int getIndiceAba(int itemId) {
        for (int i = 0; i < ORDEM_ABAS.length; i++) {
            if (ORDEM_ABAS[i] == itemId) return i;
        }
        return -1;
    }

    private void configurarBottomNavigation(BottomNavigationView bottomNav) {
        bottomNav.setOnItemSelectedListener(item -> {
            int destinoRaiz = item.getItemId();
            NavDestination destinoAtual = navController.getCurrentDestination();
            Bundle argsAtuais = navController.getCurrentBackStackEntry() != null
                    ? navController.getCurrentBackStackEntry().getArguments() : null;
            int abaAtual = destinoAtual != null ? resolverAbaSelecionada(destinoAtual, argsAtuais) : 0;

            if (destinoAtual != null && (destinoAtual.getId() == destinoRaiz || abaAtual == destinoRaiz)) {
                return true;
            }

            int indiceDestino = getIndiceAba(destinoRaiz);
            int enterAnim = indiceDestino >= indiceAbaAtual
                    ? R.anim.slide_in_from_right : R.anim.slide_in_from_left;
            int exitAnim = indiceDestino >= indiceAbaAtual
                    ? R.anim.slide_out_to_left : R.anim.slide_out_to_right;
            if (indiceDestino >= 0) indiceAbaAtual = indiceDestino;

            NavOptions navOptions = new NavOptions.Builder()
                    .setLaunchSingleTop(true)
                    .setRestoreState(false)
                    .setPopUpTo(navController.getGraph().getId(), false, false)
                    .setEnterAnim(enterAnim)
                    .setExitAnim(exitAnim)
                    .build();

            navController.navigate(destinoRaiz, null, navOptions);
            return true;
        });

        bottomNav.setOnItemReselectedListener(item -> {
            // Mantém a aba atual sem reabrir telas filhas.
        });

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int selectedItemId = resolverAbaSelecionada(destination, arguments);
            if (selectedItemId == 0) {
                return;
            }

            int idx = getIndiceAba(selectedItemId);
            if (idx >= 0) {
                indiceAbaAtual = idx;
            }

            if (bottomNav.getSelectedItemId() != selectedItemId) {
                bottomNav.getMenu().findItem(selectedItemId).setChecked(true);
            }
        });
    }

    private void configurarFabGlobal() {
        if (fabNovoLancamento == null) {
            return;
        }
        fabNovoLancamento.setOnClickListener(v -> abrirFormularioNovoLancamento());
    }

    private void aplicarVisibilidadeFab(boolean baseVisivel) {
        if (fabNovoLancamento == null) {
            return;
        }
        int visibility = (baseVisivel && fabPermitidoNaTelaAtual) ? View.VISIBLE : View.GONE;
        if (fabNovoLancamento.getVisibility() != visibility) {
            fabNovoLancamento.setVisibility(visibility);
        }
    }

    private void configurarInsetsEComportamentoTeclado() {
        View root = findViewById(R.id.activity_main_root);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            if (navHostContainer != null) {
                navHostContainer.setPadding(
                        systemBars.left,
                        systemBars.top,
                        systemBars.right,
                        0);
            }

            if (bottomNav != null) {
                bottomNav.setPadding(
                        systemBars.left,
                        0,
                        systemBars.right,
                        systemBars.bottom);
            }

            if (fabNovoLancamento != null
                    && fabNovoLancamento.getLayoutParams()
                    instanceof androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) {
                androidx.constraintlayout.widget.ConstraintLayout.LayoutParams fabParams =
                        (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams)
                                fabNovoLancamento.getLayoutParams();
                int margemPadrao = getResources().getDimensionPixelSize(R.dimen.spacing_md);
                fabParams.bottomMargin = margemPadrao + systemBars.bottom;
                fabNovoLancamento.setLayoutParams(fabParams);
            }

            boolean tecladoAberto = insets.isVisible(WindowInsetsCompat.Type.ime());
            setBottomNavVisible(!tecladoAberto);
            return insets;
        });
        ViewCompat.requestApplyInsets(root);
    }

    private void setBottomNavVisible(boolean visible) {
        if (bottomNav == null && fabNovoLancamento == null) {
            return;
        }

        int visibility = visible ? View.VISIBLE : View.GONE;
        if (bottomNav != null && bottomNav.getVisibility() != visibility) {
            bottomNav.setVisibility(visibility);
        }
        if (fabNovoLancamento != null && fabNovoLancamento.getVisibility() != visibility) {
            aplicarVisibilidadeFab(visible);
        }
    }

    private void abrirFormularioNovoLancamento() {
        if (navController == null) {
            return;
        }

        NavDestination destinoAtual = navController.getCurrentDestination();
        if (destinoAtual != null && destinoAtual.getId() == R.id.lancamentoFormFragment) {
            return;
        }

        Bundle args = new Bundle();
        int abaAtual = bottomNav != null ? bottomNav.getSelectedItemId() : R.id.nav_dashboard;
        args.putInt("sourceTab", abaAtual == R.id.nav_gastos ? R.id.nav_gastos : R.id.nav_dashboard);
        navController.navigate(R.id.lancamentoFormFragment, args);
    }

    private int resolverAbaSelecionada(NavDestination destination, Bundle arguments) {
        int destinationId = destination.getId();
        if (destinationId == R.id.metaDetalheFragment) {
            return R.id.nav_metas;
        }

        if (destinationId == R.id.categoriasFragment
                || destinationId == R.id.categoriaFormFragment
                || destinationId == R.id.tagsFragment
                || destinationId == R.id.nav_cartoes
                || destinationId == R.id.cartaoFormFragment
                || destinationId == R.id.faturaListFragment
                || destinationId == R.id.compraCartaoListFragment
                || destinationId == R.id.faturaDetalheFragment) {
            return R.id.nav_dashboard;
        }

        if (destinationId == R.id.lancamentosListFragment
                || destinationId == R.id.lancamentoFormFragment) {
            int sourceTab = arguments != null ? arguments.getInt("sourceTab", 0) : 0;
            return sourceTab == R.id.nav_gastos ? R.id.nav_gastos : R.id.nav_dashboard;
        }

        if (destinationId == R.id.nav_relatorios) {
            return R.id.nav_gastos;
        }

        if (destinationId == R.id.nav_configuracoes) {
            return R.id.nav_dashboard;
        }

        if (destinationId == R.id.nav_dashboard
                || destinationId == R.id.nav_metas
                || destinationId == R.id.nav_gastos) {
            return destinationId;
        }

        return 0;
    }

}
