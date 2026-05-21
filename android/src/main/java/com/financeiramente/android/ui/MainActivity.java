package com.financeiramente.android.ui;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.financeiramente.android.R;
import com.financeiramente.android.app.AppContext;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.time.LocalDate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppContext.get(this); // initialize singleton and run migrations
        setContentView(R.layout.activity_main);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        bottomNav = findViewById(R.id.bottom_nav);
        configurarBottomNavigation(bottomNav);
        configurarComportamentoTeclado();

        gerarLancamentosRecorrentesDoMes();
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
            int selectedItemId = resolverAbaSelecionada(destination);
            if (selectedItemId == 0) {
                return;
            }

            if (bottomNav.getSelectedItemId() != selectedItemId) {
                bottomNav.getMenu().findItem(selectedItemId).setChecked(true);
            }
        });
    }

    private void configurarComportamentoTeclado() {
        View root = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            boolean tecladoAberto = insets.isVisible(WindowInsetsCompat.Type.ime());
            setBottomNavVisible(!tecladoAberto);
            return insets;
        });
    }

    private void setBottomNavVisible(boolean visible) {
        if (bottomNav == null) {
            return;
        }
        int visibility = visible ? View.VISIBLE : View.GONE;
        if (bottomNav.getVisibility() != visibility) {
            bottomNav.setVisibility(visibility);
        }
    }

    private int resolverAbaSelecionada(NavDestination destination) {
        int destinationId = destination.getId();
        if (destinationId == R.id.metaDetalheFragment) {
            return R.id.nav_metas;
        }

        if (destinationId == R.id.categoriasFragment
                || destinationId == R.id.categoriaFormFragment
                || destinationId == R.id.recorrentesFragment
                || destinationId == R.id.recorrenteFormFragment
                || destinationId == R.id.tagsFragment) {
            return R.id.nav_dashboard;
        }

        if (destinationId == R.id.lancamentosListFragment
                || destinationId == R.id.lancamentoFormFragment) {
            return R.id.nav_dashboard;
        }

        if (destinationId == R.id.nav_relatorios
                || destinationId == R.id.nav_configuracoes) {
            return R.id.nav_gastos;
        }

        if (destinationId == R.id.nav_dashboard
                || destinationId == R.id.nav_metas
                || destinationId == R.id.nav_gastos) {
            return destinationId;
        }

        return 0;
    }

    /**
     * Gera automaticamente os lançamentos recorrentes do mês atual (EP-08).
     * A operação é idempotente — executar mais de uma vez no mesmo mês não duplica lançamentos.
     */
    private void gerarLancamentosRecorrentesDoMes() {
        LocalDate hoje = LocalDate.now();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                AppContext.get(this)
                        .getGerarLancamentosRecorrentesUseCase()
                        .executar(hoje.getYear(), hoje.getMonthValue());
            } catch (Exception ignored) {
                // Não bloqueia o usuário em caso de falha
            } finally {
                executor.shutdown();
            }
        });
    }
}
