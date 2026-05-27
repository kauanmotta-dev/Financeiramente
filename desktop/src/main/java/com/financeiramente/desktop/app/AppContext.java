package com.financeiramente.desktop.app;

import com.financeiramente.core.bootstrap.CoreBootstrap;
import com.financeiramente.core.bootstrap.CoreServices;
import com.financeiramente.core.usecase.saldo.CalcularSaldoDashboardUseCase;
import com.financeiramente.core.usecase.categoria.CriarCategoriaUseCase;
import com.financeiramente.core.usecase.meta.CalcularProjecaoMetaUseCase;
import com.financeiramente.core.usecase.meta.CriarMetaUseCase;
import com.financeiramente.core.usecase.meta.DeletarAporteMetaUseCase;
import com.financeiramente.core.usecase.meta.DeletarMetaUseCase;
import com.financeiramente.core.usecase.meta.EditarMetaUseCase;
import com.financeiramente.core.usecase.meta.RegistrarAporteMetaUseCase;
import com.financeiramente.core.usecase.categoria.DeletarCategoriaUseCase;
import com.financeiramente.core.usecase.lancamento.DeletarLancamentoUseCase;
import com.financeiramente.core.usecase.categoria.EditarCategoriaUseCase;
import com.financeiramente.core.usecase.lancamento.EditarLancamentoUseCase;
import com.financeiramente.core.usecase.relatorio.GerarRelatorioUseCase;
import com.financeiramente.core.usecase.lancamento.ListarLancamentosUseCase;
import com.financeiramente.core.usecase.lancamento.RegistrarLancamentoUseCase;
import com.financeiramente.core.usecase.categoria.ReordenarCategoriasUseCase;
import com.financeiramente.desktop.db.JdbcDatabaseDriver;

public class AppContext {

    private static AppContext instance;

    private final JdbcDatabaseDriver databaseDriver;

    private final CoreServices coreServices;

    private AppContext() {
        this.databaseDriver = new JdbcDatabaseDriver();
        this.coreServices = CoreBootstrap.create(databaseDriver);
    }

    public static synchronized AppContext get() {
        if (instance == null) {
            instance = new AppContext();
        }
        return instance;
    }

    public CoreServices getCoreServices() { return coreServices; }
    public JdbcDatabaseDriver getDatabaseDriver()                         { return databaseDriver; }

    public CriarCategoriaUseCase getCriarCategoriaUseCase() { return coreServices.getCriarCategoriaUseCase(); }
    public EditarCategoriaUseCase getEditarCategoriaUseCase() { return coreServices.getEditarCategoriaUseCase(); }
    public DeletarCategoriaUseCase getDeletarCategoriaUseCase() { return coreServices.getDeletarCategoriaUseCase(); }
    public ReordenarCategoriasUseCase getReordenarCategoriasUseCase() { return coreServices.getReordenarCategoriasUseCase(); }
    public RegistrarLancamentoUseCase getRegistrarLancamentoUseCase() { return coreServices.getRegistrarLancamentoUseCase(); }
    public EditarLancamentoUseCase getEditarLancamentoUseCase() { return coreServices.getEditarLancamentoUseCase(); }
    public DeletarLancamentoUseCase getDeletarLancamentoUseCase() { return coreServices.getDeletarLancamentoUseCase(); }
    public ListarLancamentosUseCase getListarLancamentosUseCase() { return coreServices.getListarLancamentosUseCase(); }
    public CalcularSaldoDashboardUseCase getCalcularSaldoDashboardUseCase() { return coreServices.getCalcularSaldoDashboardUseCase(); }
    public CriarMetaUseCase getCriarMetaUseCase() { return coreServices.getCriarMetaUseCase(); }
    public EditarMetaUseCase getEditarMetaUseCase() { return coreServices.getEditarMetaUseCase(); }
    public DeletarMetaUseCase getDeletarMetaUseCase() { return coreServices.getDeletarMetaUseCase(); }
    public RegistrarAporteMetaUseCase getRegistrarAporteMetaUseCase() { return coreServices.getRegistrarAporteMetaUseCase(); }
    public DeletarAporteMetaUseCase getDeletarAporteMetaUseCase() { return coreServices.getDeletarAporteMetaUseCase(); }
    public CalcularProjecaoMetaUseCase getCalcularProjecaoMetaUseCase() { return coreServices.getCalcularProjecaoMetaUseCase(); }
    public GerarRelatorioUseCase getGerarRelatorioUseCase() { return coreServices.getGerarRelatorioUseCase(); }
}
