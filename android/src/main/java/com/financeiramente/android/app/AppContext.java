package com.financeiramente.android.app;

import android.content.Context;
import android.util.Log;

import com.financeiramente.android.db.AndroidDatabaseDriver;
import com.financeiramente.android.logging.AndroidAppLogger;
import com.financeiramente.core.bootstrap.CoreBootstrap;
import com.financeiramente.core.bootstrap.CoreServices;
import com.financeiramente.core.usecase.fatura.AtualizarStatusFaturasUseCase;
import com.financeiramente.core.usecase.saldo.CalcularSaldoDashboardUseCase;
import com.financeiramente.core.usecase.meta.CalcularTotalAportesMesUseCase;
import com.financeiramente.core.usecase.cartao.CriarCartaoCreditoUseCase;
import com.financeiramente.core.usecase.categoria.CriarCategoriaUseCase;
import com.financeiramente.core.usecase.meta.CalcularProjecaoMetaUseCase;
import com.financeiramente.core.usecase.meta.CriarMetaUseCase;
import com.financeiramente.core.usecase.meta.DeletarAporteMetaUseCase;
import com.financeiramente.core.usecase.meta.DeletarMetaUseCase;
import com.financeiramente.core.usecase.cartao.DesativarCartaoCreditoUseCase;
import com.financeiramente.core.usecase.cartao.EditarCartaoCreditoUseCase;
import com.financeiramente.core.usecase.meta.EditarMetaUseCase;
import com.financeiramente.core.usecase.fatura.PagarFaturaUseCase;
import com.financeiramente.core.usecase.meta.RegistrarAporteMetaUseCase;
import com.financeiramente.core.usecase.categoria.DeletarCategoriaUseCase;
import com.financeiramente.core.usecase.lancamento.DeletarLancamentoUseCase;
import com.financeiramente.core.usecase.lancamento.DeletarCompraCartaoUseCase;
import com.financeiramente.core.usecase.lancamento.CancelarRecorrenciaCartaoUseCase;
import com.financeiramente.core.usecase.categoria.EditarCategoriaUseCase;
import com.financeiramente.core.usecase.lancamento.EditarLancamentoUseCase;
import com.financeiramente.core.usecase.lancamento.GerarCobrancasRecorrentesCartaoUseCase;
import com.financeiramente.core.usecase.lancamento.ListarComprasCartaoUseCase;
import com.financeiramente.core.usecase.relatorio.GerarRelatorioUseCase;
import com.financeiramente.core.usecase.lancamento.ListarLancamentosUseCase;
import com.financeiramente.core.usecase.lancamento.RegistrarCompraCartaoUseCase;
import com.financeiramente.core.usecase.lancamento.RegistrarLancamentoCartaoUseCase;
import com.financeiramente.core.usecase.lancamento.RegistrarLancamentoUseCase;
import com.financeiramente.core.usecase.categoria.ReordenarCategoriasUseCase;
import com.financeiramente.core.usecase.fatura.ResolverFaturaParaLancamentoUseCase;

public class AppContext {

    private static final String TAG = "AppContext";
    private static final String DB_NAME = "financeiramente.db";

    private static AppContext instance;

    private final AndroidDatabaseDriver databaseDriver;
    private final AndroidAppLogger logger;

    private final CoreServices coreServices;

    private AppContext(Context context) {
        this.logger = new AndroidAppLogger(TAG);
        BootstrapResult bootstrap = inicializarCoreComRecuperacao(context);
        this.databaseDriver = bootstrap.databaseDriver;
        this.coreServices = bootstrap.coreServices;
    }

    private BootstrapResult inicializarCoreComRecuperacao(Context context) {
        try {
            AndroidDatabaseDriver driver = new AndroidDatabaseDriver(context);
            CoreServices services = CoreBootstrap.create(driver, logger);
            return new BootstrapResult(driver, services);
        } catch (Exception firstFailure) {
            // If Android restores an old DB backup, migration can fail at startup.
            Log.w(TAG, "Falha ao iniciar banco; tentando recriar base local", firstFailure);
            context.deleteDatabase(DB_NAME);
            try {
                AndroidDatabaseDriver driver = new AndroidDatabaseDriver(context);
                CoreServices services = CoreBootstrap.create(driver, logger);
                return new BootstrapResult(driver, services);
            } catch (Exception secondFailure) {
                throw new IllegalStateException("Nao foi possivel inicializar o banco de dados", secondFailure);
            }
        }
    }

    private static final class BootstrapResult {
        private final AndroidDatabaseDriver databaseDriver;
        private final CoreServices coreServices;

        private BootstrapResult(AndroidDatabaseDriver databaseDriver, CoreServices coreServices) {
            this.databaseDriver = databaseDriver;
            this.coreServices = coreServices;
        }
    }

    public static synchronized AppContext get(Context context) {
        if (instance == null) {
            instance = new AppContext(context.getApplicationContext());
        }
        return instance;
    }

    public CoreServices getCoreServices() { return coreServices; }

    public CriarCategoriaUseCase getCriarCategoriaUseCase() { return coreServices.getCriarCategoriaUseCase(); }
    public EditarCategoriaUseCase getEditarCategoriaUseCase() { return coreServices.getEditarCategoriaUseCase(); }
    public DeletarCategoriaUseCase getDeletarCategoriaUseCase() { return coreServices.getDeletarCategoriaUseCase(); }
    public ReordenarCategoriasUseCase getReordenarCategoriasUseCase() { return coreServices.getReordenarCategoriasUseCase(); }
    public RegistrarLancamentoUseCase getRegistrarLancamentoUseCase() { return coreServices.getRegistrarLancamentoUseCase(); }
    public EditarLancamentoUseCase getEditarLancamentoUseCase() { return coreServices.getEditarLancamentoUseCase(); }
    public DeletarLancamentoUseCase getDeletarLancamentoUseCase() { return coreServices.getDeletarLancamentoUseCase(); }
    public ListarLancamentosUseCase getListarLancamentosUseCase() { return coreServices.getListarLancamentosUseCase(); }
    public CalcularSaldoDashboardUseCase getCalcularSaldoDashboardUseCase() {
        return coreServices.getCalcularSaldoDashboardUseCase();
    }
    public CalcularTotalAportesMesUseCase getCalcularTotalAportesMesUseCase() {
        return coreServices.getCalcularTotalAportesMesUseCase();
    }
    public CriarMetaUseCase getCriarMetaUseCase() { return coreServices.getCriarMetaUseCase(); }
    public EditarMetaUseCase getEditarMetaUseCase() { return coreServices.getEditarMetaUseCase(); }
    public DeletarMetaUseCase getDeletarMetaUseCase() { return coreServices.getDeletarMetaUseCase(); }
    public RegistrarAporteMetaUseCase getRegistrarAporteMetaUseCase() { return coreServices.getRegistrarAporteMetaUseCase(); }
    public DeletarAporteMetaUseCase getDeletarAporteMetaUseCase() { return coreServices.getDeletarAporteMetaUseCase(); }
    public CalcularProjecaoMetaUseCase getCalcularProjecaoMetaUseCase() {
        return coreServices.getCalcularProjecaoMetaUseCase();
    }
    public GerarRelatorioUseCase getGerarRelatorioUseCase() { return coreServices.getGerarRelatorioUseCase(); }
    public CriarCartaoCreditoUseCase getCriarCartaoCreditoUseCase() { return coreServices.getCriarCartaoCreditoUseCase(); }
    public EditarCartaoCreditoUseCase getEditarCartaoCreditoUseCase() { return coreServices.getEditarCartaoCreditoUseCase(); }
    public DesativarCartaoCreditoUseCase getDesativarCartaoCreditoUseCase() {
        return coreServices.getDesativarCartaoCreditoUseCase();
    }
    public RegistrarLancamentoCartaoUseCase getRegistrarLancamentoCartaoUseCase() {
        return coreServices.getRegistrarLancamentoCartaoUseCase();
    }
    public RegistrarCompraCartaoUseCase getRegistrarCompraCartaoUseCase() {
        return coreServices.getRegistrarCompraCartaoUseCase();
    }
    public DeletarCompraCartaoUseCase getDeletarCompraCartaoUseCase() {
        return coreServices.getDeletarCompraCartaoUseCase();
    }
    public GerarCobrancasRecorrentesCartaoUseCase getGerarCobrancasRecorrentesCartaoUseCase() {
        return coreServices.getGerarCobrancasRecorrentesCartaoUseCase();
    }
    public CancelarRecorrenciaCartaoUseCase getCancelarRecorrenciaCartaoUseCase() {
        return coreServices.getCancelarRecorrenciaCartaoUseCase();
    }
    public ListarComprasCartaoUseCase getListarComprasCartaoUseCase() {
        return coreServices.getListarComprasCartaoUseCase();
    }
    public AtualizarStatusFaturasUseCase getAtualizarStatusFaturasUseCase() {
        return coreServices.getAtualizarStatusFaturasUseCase();
    }
    public PagarFaturaUseCase getPagarFaturaUseCase() { return coreServices.getPagarFaturaUseCase(); }
}
