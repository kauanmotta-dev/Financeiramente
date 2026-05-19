package com.financeiramente.android.viewmodel;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.financeiramente.core.domain.entity.AporteMeta;
import com.financeiramente.core.domain.entity.Meta;
import com.financeiramente.core.repository.AporteMetaRepository;
import com.financeiramente.core.repository.MetaRepository;
import com.financeiramente.core.usecase.CalcularProjecaoMetaUseCase;
import com.financeiramente.core.usecase.DeletarAporteMetaUseCase;
import com.financeiramente.core.usecase.RegistrarAporteMetaUseCase;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MetaDetalheViewModel extends ViewModel {

    private final MetaRepository metaRepository;
    private final AporteMetaRepository aporteRepository;
    private final RegistrarAporteMetaUseCase registrarAporte;
    private final DeletarAporteMetaUseCase deletarAporte;
    private final CalcularProjecaoMetaUseCase calcularProjecao;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final MutableLiveData<Meta> meta = new MutableLiveData<>();
    private final MutableLiveData<List<AporteMeta>> aportes = new MutableLiveData<>();
    private final MutableLiveData<LocalDate> projecao = new MutableLiveData<>();
    private final MutableLiveData<String> erro = new MutableLiveData<>();
    private final MutableLiveData<Boolean> sucesso = new MutableLiveData<>();

    public MetaDetalheViewModel(MetaRepository metaRepository,
                                 AporteMetaRepository aporteRepository,
                                 RegistrarAporteMetaUseCase registrarAporte,
                                 DeletarAporteMetaUseCase deletarAporte,
                                 CalcularProjecaoMetaUseCase calcularProjecao) {
        this.metaRepository = metaRepository;
        this.aporteRepository = aporteRepository;
        this.registrarAporte = registrarAporte;
        this.deletarAporte = deletarAporte;
        this.calcularProjecao = calcularProjecao;
    }

    public void carregar(String metaId) {
        executor.execute(() -> {
            try {
                Meta m = metaRepository.buscarPorId(metaId).orElse(null);
                List<AporteMeta> lista = aporteRepository.listarPorMeta(metaId);
                Optional<LocalDate> proj = m != null ? calcularProjecao.executar(metaId) : Optional.empty();
                mainHandler.post(() -> {
                    meta.setValue(m);
                    aportes.setValue(lista);
                    projecao.setValue(proj.orElse(null));
                });
            } catch (Exception e) {
                mainHandler.post(() -> erro.setValue(e.getMessage()));
            }
        });
    }

    public void registrarAporte(String metaId, double valor, String data, String descricao) {
        executor.execute(() -> {
            try {
                registrarAporte.executar(metaId, valor, data, descricao);
                Meta m = metaRepository.buscarPorId(metaId).orElse(null);
                List<AporteMeta> lista = aporteRepository.listarPorMeta(metaId);
                Optional<LocalDate> proj = calcularProjecao.executar(metaId);
                mainHandler.post(() -> {
                    meta.setValue(m);
                    aportes.setValue(lista);
                    projecao.setValue(proj.orElse(null));
                    sucesso.setValue(true);
                });
            } catch (Exception e) {
                mainHandler.post(() -> erro.setValue(e.getMessage()));
            }
        });
    }

    public void deletarAporte(String aporteId, String metaId) {
        executor.execute(() -> {
            try {
                deletarAporte.executar(aporteId, metaId);
                Meta m = metaRepository.buscarPorId(metaId).orElse(null);
                List<AporteMeta> lista = aporteRepository.listarPorMeta(metaId);
                Optional<LocalDate> proj = calcularProjecao.executar(metaId);
                mainHandler.post(() -> {
                    meta.setValue(m);
                    aportes.setValue(lista);
                    projecao.setValue(proj.orElse(null));
                });
            } catch (Exception e) {
                mainHandler.post(() -> erro.setValue(e.getMessage()));
            }
        });
    }

    public LiveData<Meta> getMeta() { return meta; }
    public LiveData<List<AporteMeta>> getAportes() { return aportes; }
    public LiveData<LocalDate> getProjecao() { return projecao; }
    public LiveData<String> getErro() { return erro; }
    public LiveData<Boolean> getSucesso() { return sucesso; }
}
