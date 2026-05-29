package com.financeiramente.android.viewmodel;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.financeiramente.core.domain.entity.Categoria;
import com.financeiramente.core.domain.entity.CompraCartao;
import com.financeiramente.core.domain.entity.Lancamento;
import com.financeiramente.core.domain.entity.Tag;
import com.financeiramente.core.domain.vo.TipoLancamento;
import com.financeiramente.core.repository.CategoriaRepository;
import com.financeiramente.core.repository.CompraCartaoRepository;
import com.financeiramente.core.repository.LancamentoRepository;
import com.financeiramente.core.repository.TagRepository;
import com.financeiramente.core.domain.entity.CartaoCredito;
import com.financeiramente.core.domain.vo.TipoCompraCartao;
import com.financeiramente.core.repository.CartaoCreditoRepository;
import com.financeiramente.core.usecase.lancamento.EditarCompraCartaoUseCase;
import com.financeiramente.core.usecase.lancamento.EditarLancamentoUseCase;
import com.financeiramente.core.usecase.lancamento.RegistrarCompraCartaoInput;
import com.financeiramente.core.usecase.lancamento.RegistrarCompraCartaoUseCase;
import com.financeiramente.core.usecase.lancamento.RegistrarLancamentoInput;
import com.financeiramente.core.usecase.lancamento.RegistrarLancamentoUseCase;

import java.util.Collections;
import java.util.List;
import java.math.BigDecimal;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class LancamentoFormViewModel extends ViewModel {

    private final RegistrarLancamentoUseCase registrar;
    private final EditarLancamentoUseCase editar;
    private final CategoriaRepository categoriaRepository;
    private final TagRepository tagRepository;
    private final LancamentoRepository lancamentoRepository;
    private RegistrarCompraCartaoUseCase registrarCompraCartaoUseCase;
    private EditarCompraCartaoUseCase editarCompraCartaoUseCase;
    private CompraCartaoRepository compraCartaoRepository;
    private CartaoCreditoRepository cartaoRepository;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final MutableLiveData<List<Categoria>> categorias = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<List<Tag>> tags = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Lancamento> lancamentoCarregado = new MutableLiveData<>();
    private final MutableLiveData<CompraCartao> compraCartaoCarregada = new MutableLiveData<>();
    private final MutableLiveData<Boolean> sucesso = new MutableLiveData<>();
    private final MutableLiveData<String> savedLancamentoId = new MutableLiveData<>();
    private final MutableLiveData<String> erro = new MutableLiveData<>();
    private final MutableLiveData<List<CartaoCredito>> cartoesAtivos = new MutableLiveData<>(Collections.emptyList());

    public LancamentoFormViewModel(RegistrarLancamentoUseCase registrar,
                                   EditarLancamentoUseCase editar,
                                   CategoriaRepository categoriaRepository,
                                   TagRepository tagRepository,
                                   LancamentoRepository lancamentoRepository) {
        this.registrar = registrar;
        this.editar = editar;
        this.categoriaRepository = categoriaRepository;
        this.tagRepository = tagRepository;
        this.lancamentoRepository = lancamentoRepository;
        carregarDadosAuxiliares();
    }

    private void carregarDadosAuxiliares() {
        executor.execute(() -> {
            // Carrega subcategorias compatíveis com DESPESA (padrão inicial do formulário)
            List<Categoria> cats = subcategoriasPorTipo(TipoLancamento.DESPESA);
            List<Tag> ts = tagRepository.listarTodas();
            mainHandler.post(() -> {
                categorias.setValue(cats);
                tags.setValue(ts);
            });
        });
    }

    /** Recarrega categorias quando o tipo de lançamento muda no formulário. */
    public void carregarCategoriasPorTipo(TipoLancamento tipo) {
        executor.execute(() -> {
            List<Categoria> cats = subcategoriasPorTipo(tipo);
            mainHandler.post(() -> categorias.setValue(cats));
        });
    }

    private List<Categoria> subcategoriasPorTipo(TipoLancamento tipo) {
        return categoriaRepository.listarTodas().stream()
                .filter(c -> c.getPaiId() != null)
                .filter(c -> c.getTipo() != null)
                .filter(c -> c.getTipo().isCompativelCom(tipo))
                .filter(c -> !"Sem Categoria".equals(c.getNome()))
                .collect(Collectors.toList());
    }

    private String resolverDescricaoPadrao(String descricao, String categoriaId) {
        if (descricao != null && !descricao.trim().isEmpty()) {
            return descricao.trim();
        }

        if (categoriaId != null) {
            return categoriaRepository.buscarPorId(categoriaId)
                    .map(Categoria::getNome)
                    .filter(nome -> nome != null && !nome.trim().isEmpty())
                    .orElse("Sem Categoria");
        }

        return "Sem Categoria";
    }

    public void carregarLancamento(String id) {
        executor.execute(() -> {
            lancamentoRepository.buscarPorId(id).ifPresent(l ->
                    mainHandler.post(() -> lancamentoCarregado.setValue(l)));
        });
    }

    public void carregarCompraCartao(String id, CompraCartaoRepository compraCartaoRepository) {
        executor.execute(() -> {
            compraCartaoRepository.buscarPorId(id).ifPresent(compra ->
                    mainHandler.post(() -> compraCartaoCarregada.setValue(compra)));
        });
    }

    public void carregarCompraCartao(String id) {
        if (compraCartaoRepository == null) {
            mainHandler.post(() -> erro.setValue("Repositório de compra não configurado"));
            return;
        }
        carregarCompraCartao(id, compraCartaoRepository);
    }

    public void setCartaoSupport(RegistrarCompraCartaoUseCase registrarCompraCartaoUseCase,
                                   CartaoCreditoRepository cartaoRepository) {
        this.registrarCompraCartaoUseCase = registrarCompraCartaoUseCase;
        this.cartaoRepository = cartaoRepository;
        executor.execute(() -> {
            List<CartaoCredito> ativos = cartaoRepository.listarAtivos();
            mainHandler.post(() -> cartoesAtivos.setValue(ativos));
        });
    }

    public void setCompraCartaoSupport(EditarCompraCartaoUseCase editarCompraCartaoUseCase,
                                       CompraCartaoRepository compraCartaoRepository) {
        this.editarCompraCartaoUseCase = editarCompraCartaoUseCase;
        this.compraCartaoRepository = compraCartaoRepository;
    }

    public void editarCompraCartao(String compraCartaoId, RegistrarCompraCartaoInput input) {
        if (editarCompraCartaoUseCase == null) {
            mainHandler.post(() -> erro.setValue("Use case de edição de compra não configurado"));
            return;
        }
        executor.execute(() -> {
            try {
                editarCompraCartaoUseCase.executar(compraCartaoId, input);
                mainHandler.post(() -> sucesso.setValue(true));
            } catch (Exception e) {
                mainHandler.post(() -> erro.setValue(e.getMessage()));
            }
        });
    }

    public void salvarComCartao(double valor, TipoLancamento tipo, String data, String descricao,
                                 String categoriaId, List<String> tagIds, String cartaoId) {
        if (registrarCompraCartaoUseCase == null) {
            mainHandler.post(() -> erro.setValue("Use case de cartão não configurado"));
            return;
        }
        executor.execute(() -> {
            try {
                RegistrarCompraCartaoInput input = new RegistrarCompraCartaoInput(
                        cartaoId,
                        TipoCompraCartao.CREDITO,
                        BigDecimal.valueOf(valor),
                        data,
                        resolverDescricaoPadrao(descricao, categoriaId),
                        categoriaId,
                        tagIds,
                        1,
                        null);
                registrarCompraCartaoUseCase.executar(input);
                mainHandler.post(() -> {
                    savedLancamentoId.setValue(null);
                    sucesso.setValue(true);
                });
            } catch (Exception e) {
                mainHandler.post(() -> erro.setValue(e.getMessage()));
            }
        });
    }

    public void salvarParceladoComCartao(double valor, TipoLancamento tipo, String data, String descricao,
                                          String categoriaId, List<String> tagIds, String cartaoId,
                                          int numeroParcelas) {
        if (registrarCompraCartaoUseCase == null) {
            mainHandler.post(() -> erro.setValue("Use case de parcelamento não configurado"));
            return;
        }
        executor.execute(() -> {
            try {
                RegistrarCompraCartaoInput input = new RegistrarCompraCartaoInput(
                        cartaoId,
                        TipoCompraCartao.PARCELADO,
                        BigDecimal.valueOf(valor),
                        data,
                        resolverDescricaoPadrao(descricao, categoriaId),
                        categoriaId,
                        tagIds,
                        numeroParcelas,
                        null);
                registrarCompraCartaoUseCase.executar(input);
                mainHandler.post(() -> sucesso.setValue(true));
            } catch (Exception e) {
                mainHandler.post(() -> erro.setValue(e.getMessage()));
            }
        });
    }

    public void salvarRecorrenteComCartao(double valor, TipoLancamento tipo, String data, String descricao,
                                          String categoriaId, List<String> tagIds, String cartaoId,
                                          int diaRecorrencia) {
        if (registrarCompraCartaoUseCase == null) {
            mainHandler.post(() -> erro.setValue("Use case de recorrência não configurado"));
            return;
        }
        executor.execute(() -> {
            try {
                RegistrarCompraCartaoInput input = new RegistrarCompraCartaoInput(
                        cartaoId,
                        TipoCompraCartao.RECORRENTE,
                        BigDecimal.valueOf(valor),
                        data,
                    resolverDescricaoPadrao(descricao, categoriaId),
                        categoriaId,
                        tagIds,
                        1,
                        diaRecorrencia);
                registrarCompraCartaoUseCase.executar(input);
                mainHandler.post(() -> sucesso.setValue(true));
            } catch (Exception e) {
                mainHandler.post(() -> erro.setValue(e.getMessage()));
            }
        });
    }

    public void salvar(double valor, TipoLancamento tipo, String data, String descricao,
                       String categoriaId, List<String> tagIds) {
        executor.execute(() -> {
            try {
                RegistrarLancamentoInput input = new RegistrarLancamentoInput(
                        BigDecimal.valueOf(valor), tipo, data,
                        resolverDescricaoPadrao(descricao, categoriaId),
                        categoriaId, null, tagIds);
                Lancamento salvo = registrar.executar(input);
                mainHandler.post(() -> {
                    savedLancamentoId.setValue(salvo.getId());
                    sucesso.setValue(true);
                });
            } catch (Exception e) {
                mainHandler.post(() -> erro.setValue(e.getMessage()));
            }
        });
    }

    public void editar(String id, double valor, TipoLancamento tipo, String data,
                       String descricao, String categoriaId, List<String> tagIds) {
        executor.execute(() -> {
            try {
                RegistrarLancamentoInput input = new RegistrarLancamentoInput(
                        BigDecimal.valueOf(valor), tipo, data,
                        resolverDescricaoPadrao(descricao, categoriaId),
                        categoriaId, null, tagIds);
                editar.executar(id, input);
                mainHandler.post(() -> sucesso.setValue(true));
            } catch (Exception e) {
                mainHandler.post(() -> erro.setValue(e.getMessage()));
            }
        });
    }

    public LiveData<List<Categoria>> getCategorias() { return categorias; }
    public LiveData<List<Tag>> getTags() { return tags; }
    public LiveData<Lancamento> getLancamentoCarregado() { return lancamentoCarregado; }
    public LiveData<CompraCartao> getCompraCartaoCarregada() { return compraCartaoCarregada; }
    public LiveData<Boolean> getSucesso() { return sucesso; }
    public LiveData<String> getSavedLancamentoId() { return savedLancamentoId; }
    public LiveData<String> getErro() { return erro; }
    public LiveData<List<CartaoCredito>> getCartoesAtivos() { return cartoesAtivos; }

    @Override
    protected void onCleared() {
        executor.shutdown();
    }
}
