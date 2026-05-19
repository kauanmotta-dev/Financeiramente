package com.financeiramente.android.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.financeiramente.android.R;
import com.financeiramente.android.app.AppContext;
import com.financeiramente.core.domain.entity.PlanejamentoMensal;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "financeiramente_prefs";
    private static final String KEY_LAST_CHECKED_MES = "last_checked_ano_mes";
    private static final String KEY_LAST_CREDITADO_MES = "last_creditado_ano_mes";

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
        creditarProvisoesMensais();
        verificarPlanejamentoMesNovo();
    }

    /**
     * Credita o valor mensal de cada provisão ativa (EP-09).
     * Idempotente: executa apenas uma vez por mês usando SharedPreferences.
     */
    private void creditarProvisoesMensais() {
        LocalDate hoje = LocalDate.now();
        String chaveAtual = hoje.getYear() + "-" + hoje.getMonthValue();

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String ultimoCreditado = prefs.getString(KEY_LAST_CREDITADO_MES, "");

        if (chaveAtual.equals(ultimoCreditado)) {
            return; // Já creditou neste mês
        }

        prefs.edit().putString(KEY_LAST_CREDITADO_MES, chaveAtual).apply();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                AppContext.get(this)
                        .getCreditarProvisoesMensaisUseCase()
                        .executar();
            } catch (Exception ignored) {
                // Não bloqueia o usuário em caso de falha
            } finally {
                executor.shutdown();
            }
        });
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

    /**
     * Verifica se o mês mudou desde a última abertura do app.
     * Se sim e não há planejamento confirmado, exibe alerta sugerindo acessar o planejamento.
     */
    private void verificarPlanejamentoMesNovo() {
        LocalDate hoje = LocalDate.now();
        String chaveAtual = hoje.getYear() + "-" + hoje.getMonthValue();

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String ultimoMes = prefs.getString(KEY_LAST_CHECKED_MES, "");

        // Atualiza a chave do mês atual
        prefs.edit().putString(KEY_LAST_CHECKED_MES, chaveAtual).apply();

        if (chaveAtual.equals(ultimoMes)) {
            return; // Mesmo mês — não verifica novamente
        }

        // Mês novo ou primeira abertura: verifica se há plano confirmado
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                Optional<PlanejamentoMensal> planoOpt = AppContext.get(this)
                        .getPlanejamentoRepository()
                        .buscarPorMes(hoje.getYear(), hoje.getMonthValue());

                boolean semPlanoConfirmado = planoOpt.isEmpty() || !planoOpt.get().isConfirmado();

                if (semPlanoConfirmado) {
                    handler.post(this::mostrarAlertaMesNovo);
                }
            } catch (Exception ignored) {
                // Ignora erros silenciosamente — não bloqueia o usuário
            } finally {
                executor.shutdown();
            }
        });
    }

    private void mostrarAlertaMesNovo() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.alerta_planejamento_titulo)
                .setMessage(R.string.alerta_planejamento_mensagem)
                .setPositiveButton(R.string.alerta_planejamento_ir, (dialog, which) ->
                        navController.navigate(R.id.planejamentoFragment))
                .setNegativeButton(R.string.alerta_planejamento_depois, null)
                .show();
    }
}
