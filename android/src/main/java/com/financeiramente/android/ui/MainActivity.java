package com.financeiramente.android.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.financeiramente.android.R;
import com.financeiramente.android.app.AppContext;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.time.LocalDate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppContext.get(this); // initialize singleton and run migrations
        setContentView(R.layout.activity_main);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        NavigationUI.setupWithNavController(bottomNav, navController);

        gerarLancamentosRecorrentesDoMes();
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
