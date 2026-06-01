package com.financeiramente.android.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.financeiramente.core.domain.entity.Categoria;
import com.financeiramente.core.domain.vo.TipoCategoria;
import com.financeiramente.core.repository.CategoriaRepository;
import com.financeiramente.core.usecase.categoria.CriarCategoriaUseCase;
import com.financeiramente.core.usecase.categoria.DeletarCategoriaUseCase;
import com.financeiramente.core.usecase.categoria.EditarCategoriaUseCase;
import com.financeiramente.core.usecase.categoria.ReordenarCategoriasUseCase;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CategoriasViewModel extends ViewModel {

    private final CategoriaRepository categoriaRepository;
    private final CriarCategoriaUseCase criarCategoriaUseCase;
    private final EditarCategoriaUseCase editarCategoriaUseCase;
    private final DeletarCategoriaUseCase deletarCategoriaUseCase;
    private final ReordenarCategoriasUseCase reordenarCategoriasUseCase;

    private final MutableLiveData<List<Categoria>> categoriasRaiz = new MutableLiveData<>();
    private final MutableLiveData<List<Categoria>> categoriasDespesasRaiz = new MutableLiveData<>();
    private final MutableLiveData<List<Categoria>> categoriasReceitasRaiz = new MutableLiveData<>();
    private final MutableLiveData<String> erro = new MutableLiveData<>();

    public CategoriasViewModel(CategoriaRepository categoriaRepository,
                               CriarCategoriaUseCase criarCategoriaUseCase,
                               EditarCategoriaUseCase editarCategoriaUseCase,
                               DeletarCategoriaUseCase deletarCategoriaUseCase,
                               ReordenarCategoriasUseCase reordenarCategoriasUseCase) {
        this.categoriaRepository = categoriaRepository;
        this.criarCategoriaUseCase = criarCategoriaUseCase;
        this.editarCategoriaUseCase = editarCategoriaUseCase;
        this.deletarCategoriaUseCase = deletarCategoriaUseCase;
        this.reordenarCategoriasUseCase = reordenarCategoriasUseCase;
        carregarCategorias();
    }

    public LiveData<List<Categoria>> getCategoriasRaiz() {
        return categoriasRaiz;
    }

    public LiveData<List<Categoria>> getCategoriasDespesasRaiz() {
        return categoriasDespesasRaiz;
    }

    public LiveData<List<Categoria>> getCategoriasReceitasRaiz() {
        return categoriasReceitasRaiz;
    }

    public LiveData<String> getErro() {
        return erro;
    }

    public void carregarCategorias() {
        List<Categoria> todasRaw = categoriaRepository.listarRaizes();
        // Excluir "Sem Categoria" da tela de configurações
        List<Categoria> todas = new ArrayList<>();
        for (Categoria c : todasRaw) {
            if (!"Sem Categoria".equals(c.getNome())) todas.add(c);
        }
        categoriasRaiz.setValue(todas);

        List<Categoria> despesas = new ArrayList<>();
        List<Categoria> receitas = new ArrayList<>();
        for (Categoria c : todas) {
            if (c.getTipo() == TipoCategoria.RECEITA) {
                receitas.add(c);
            } else {
                despesas.add(c);
            }
        }
        categoriasDespesasRaiz.setValue(despesas);
        categoriasReceitasRaiz.setValue(receitas);
    }

    public List<Categoria> listarFilhas(String paiId) {
        return categoriaRepository.listarFilhas(paiId);
    }

    public void criarCategoria(String nome, TipoCategoria tipo, String paiId, Double limiteMensal) {
        try {
            criarCategoriaUseCase.executar(
                    nome,
                    tipo,
                    paiId,
                    limiteMensal != null ? BigDecimal.valueOf(limiteMensal) : null,
                    null,
                    null);
            carregarCategorias();
        } catch (Exception e) {
            erro.setValue(e.getMessage());
        }
    }

    public void editarCategoria(String id, String nome, TipoCategoria tipo, Double limiteMensal) {
        try {
            editarCategoriaUseCase.executar(
                    id,
                    nome,
                    tipo,
                    limiteMensal != null ? BigDecimal.valueOf(limiteMensal) : null,
                    null,
                    null);
            carregarCategorias();
        } catch (Exception e) {
            erro.setValue(e.getMessage());
        }
    }

    public void deletarCategoria(String id) {
        try {
            deletarCategoriaUseCase.executar(id);
            carregarCategorias();
        } catch (Exception e) {
            erro.setValue(e.getMessage());
        }
    }

    public void reordenarCategorias(List<String> idsOrdenados) {
        try {
            reordenarCategoriasUseCase.executar(idsOrdenados);
            carregarCategorias();
        } catch (Exception e) {
            erro.setValue(e.getMessage());
        }
    }
}
