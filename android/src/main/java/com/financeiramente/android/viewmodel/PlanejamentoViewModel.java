package com.financeiramente.android.viewmodel;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.financeiramente.core.domain.entity.Categoria;
import com.financeiramente.core.domain.entity.PlanejamentoCategoria;
import com.financeiramente.core.domain.entity.PlanejamentoMensal;
import com.financeiramente.core.repository.CategoriaRepository;
import com.financeiramente.core.repository.PlanejamentoRepository;
import com.financeiramente.core.usecase.ConfirmarPlanejamentoUseCase;
import com.financeiramente.core.usecase.CriarPlanejamentoMensalUseCase;
import com.financeiramente.core.usecase.DefinirComoPlanosPadraoUseCase;
import com.financeiramente.core.usecase.DefinirLimiteCategoriaUseCase;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlanejamentoViewModel extends ViewModel {

    private final CriarPlanejamentoMensalUseCase criarPlanejamento;
    private final ConfirmarPlanejamentoUseCase confirmarPlanejamento;
    private final DefinirLimiteCategoriaUseCase definirLimite;
    private final DefinirComoPlanosPadraoUseCase definirPadrao;
    private final PlanejamentoRepository planejamentoRepository;
    private final CategoriaRepository categoriaRepository;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final MutableLiveData<PlanejamentoMensal> planoAtual = new MutableLiveData<>();
    private final MutableLiveData<List<PlanejamentoCategoria>> itensCategoria = new MutableLiveData<>();
    private final MutableLiveData<List<Categoria>> todasCategorias = new MutableLiveData<>();
    private final MutableLiveData<String> erro = new MutableLiveData<>();
    private final MutableLiveData<Boolean> sucesso = new MutableLiveData<>();

    private int ano;
    private int mes;

    public PlanejamentoViewModel(CriarPlanejamentoMensalUseCase criarPlanejamento,
                                  ConfirmarPlanejamentoUseCase confirmarPlanejamento,
                                  DefinirLimiteCategoriaUseCase definirLimite,
                                  DefinirComoPlanosPadraoUseCase definirPadrao,
                                  PlanejamentoRepository planejamentoRepository,
                                  CategoriaRepository categoriaRepository) {
        this.criarPlanejamento = criarPlanejamento;
        this.confirmarPlanejamento = confirmarPlanejamento;
        this.definirLimite = definirLimite;
        this.definirPadrao = definirPadrao;
        this.planejamentoRepository = planejamentoRepository;
        this.categoriaRepository = categoriaRepository;

        LocalDate hoje = LocalDate.now();
        this.ano = hoje.getYear();
        this.mes = hoje.getMonthValue();
        carregarPlano();
    }

    /** Carrega ou cria o planejamento do mês atual. */
    public void carregarPlano() {
        executor.execute(() -> {
            try {
                PlanejamentoMensal plano = criarPlanejamento.executar(ano, mes);
                List<PlanejamentoCategoria> itens =
                        planejamentoRepository.listarItensPorPlano(plano.getId());
                List<Categoria> categorias = categoriaRepository.listarTodas();

                mainHandler.post(() -> {
                    planoAtual.setValue(plano);
                    itensCategoria.setValue(itens);
                    todasCategorias.setValue(categorias);
                });
            } catch (Exception e) {
                mainHandler.post(() -> erro.setValue(e.getMessage()));
            }
        });
    }

    public void setMes(int ano, int mes) {
        this.ano = ano;
        this.mes = mes;
        carregarPlano();
    }

    /** Atualiza receita esperada e reserva de imprevistos e persiste. */
    public void atualizarCabecalho(double receitaEsperada, double reservaImprevisto) {
        PlanejamentoMensal plano = planoAtual.getValue();
        if (plano == null) return;

        executor.execute(() -> {
            try {
                plano.setReceitaEsperada(receitaEsperada);
                plano.setReservaImprevisto(reservaImprevisto);
                planejamentoRepository.atualizar(plano);
                mainHandler.post(() -> planoAtual.setValue(plano));
            } catch (Exception e) {
                mainHandler.post(() -> erro.setValue(e.getMessage()));
            }
        });
    }

    /** Define ou atualiza o limite de uma categoria no plano atual. */
    public void definirLimiteCategoria(String categoriaId, double limite) {
        PlanejamentoMensal plano = planoAtual.getValue();
        if (plano == null) return;

        executor.execute(() -> {
            try {
                definirLimite.executar(plano, categoriaId, limite);
                List<PlanejamentoCategoria> itens =
                        planejamentoRepository.listarItensPorPlano(plano.getId());
                mainHandler.post(() -> itensCategoria.setValue(itens));
            } catch (Exception e) {
                mainHandler.post(() -> erro.setValue(e.getMessage()));
            }
        });
    }

    /** Confirma o plano atual. */
    public void confirmarPlano() {
        PlanejamentoMensal plano = planoAtual.getValue();
        if (plano == null) return;

        executor.execute(() -> {
            try {
                confirmarPlanejamento.executar(plano);
                mainHandler.post(() -> {
                    planoAtual.setValue(plano);
                    sucesso.setValue(true);
                });
            } catch (Exception e) {
                mainHandler.post(() -> erro.setValue(e.getMessage()));
            }
        });
    }

    /** Define o plano atual como plano padrão. */
    public void definirComoPadrao() {
        PlanejamentoMensal plano = planoAtual.getValue();
        if (plano == null) return;

        executor.execute(() -> {
            try {
                definirPadrao.executar(plano);
                mainHandler.post(() -> {
                    planoAtual.setValue(plano);
                    sucesso.setValue(true);
                });
            } catch (Exception e) {
                mainHandler.post(() -> erro.setValue(e.getMessage()));
            }
        });
    }

    public int getAno() { return ano; }
    public int getMes() { return mes; }

    public LiveData<PlanejamentoMensal> getPlanoAtual()                { return planoAtual; }
    public LiveData<List<PlanejamentoCategoria>> getItensCategoria()    { return itensCategoria; }
    public LiveData<List<Categoria>> getTodasCategorias()              { return todasCategorias; }
    public LiveData<String> getErro()                                  { return erro; }
    public LiveData<Boolean> getSucesso()                              { return sucesso; }

    @Override
    protected void onCleared() {
        executor.shutdown();
    }
}
