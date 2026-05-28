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
        configurarInsetsEComportamentoTeclado();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
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

    private void configurarBottomNavigation(BottomNavigationView bottomNav) {
        bottomNav.setOnItemSelectedListener(item -> {
            int destinoRaiz = item.getItemId();
            NavDestination destinoAtual = navController.getCurrentDestination();

            if (destinoAtual != null && destinoAtual.getId() == destinoRaiz) {
                return true;
            }

            NavOptions navOptions = new NavOptions.Builder()
                    .setLaunchSingleTop(true)
                    .setRestoreState(false)
                    .setPopUpTo(navController.getGraph().getId(), false, false)
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
            fabNovoLancamento.setVisibility(visibility);
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
