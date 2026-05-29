package com.financeiramente.core.bootstrap;

import com.financeiramente.core.dao.AporteMetaDao;
import com.financeiramente.core.dao.CartaoCreditoDao;
import com.financeiramente.core.dao.CategoriaDao;
import com.financeiramente.core.dao.CompraCartaoDao;
import com.financeiramente.core.dao.FaturaDao;
import com.financeiramente.core.dao.LancamentoDao;
import com.financeiramente.core.dao.MetaDao;
import com.financeiramente.core.dao.TagDao;
import com.financeiramente.core.db.AppLogger;
import com.financeiramente.core.db.DatabaseDriver;
import com.financeiramente.core.db.DatabaseMigrator;
import com.financeiramente.core.repository.AporteMetaRepository;
import com.financeiramente.core.repository.CartaoCreditoRepository;
import com.financeiramente.core.repository.CategoriaRepository;
import com.financeiramente.core.repository.CompraCartaoRepository;
import com.financeiramente.core.repository.FaturaRepository;
import com.financeiramente.core.repository.LancamentoRepository;
import com.financeiramente.core.repository.MetaRepository;
import com.financeiramente.core.repository.TagRepository;
import com.financeiramente.core.usecase.fatura.AlterarStatusFaturaUseCase;
import com.financeiramente.core.usecase.fatura.AnteciparLancamentosFaturaUseCase;
import com.financeiramente.core.usecase.fatura.AtualizarStatusFaturasUseCase;
import com.financeiramente.core.usecase.fatura.PagarFaturaUseCase;
import com.financeiramente.core.usecase.fatura.ResolverFaturaParaLancamentoUseCase;
import com.financeiramente.core.usecase.categoria.CriarCategoriaUseCase;
import com.financeiramente.core.usecase.categoria.DeletarCategoriaUseCase;
import com.financeiramente.core.usecase.categoria.EditarCategoriaUseCase;
import com.financeiramente.core.usecase.categoria.ReordenarCategoriasUseCase;
import com.financeiramente.core.usecase.cartao.CriarCartaoCreditoUseCase;
import com.financeiramente.core.usecase.cartao.DesativarCartaoCreditoUseCase;
import com.financeiramente.core.usecase.cartao.EditarCartaoCreditoUseCase;
import com.financeiramente.core.usecase.lancamento.DeletarLancamentoUseCase;
import com.financeiramente.core.usecase.lancamento.DeletarCompraCartaoUseCase;
import com.financeiramente.core.usecase.lancamento.EditarCompraCartaoUseCase;
import com.financeiramente.core.usecase.lancamento.EditarLancamentoUseCase;
import com.financeiramente.core.usecase.lancamento.GerarCobrancasRecorrentesCartaoUseCase;
import com.financeiramente.core.usecase.lancamento.ListarLancamentosUseCase;
import com.financeiramente.core.usecase.lancamento.ListarComprasCartaoUseCase;
import com.financeiramente.core.usecase.lancamento.CancelarRecorrenciaCartaoUseCase;
import com.financeiramente.core.usecase.lancamento.RegistrarCompraCartaoUseCase;
import com.financeiramente.core.usecase.lancamento.RegistrarLancamentoCartaoUseCase;
import com.financeiramente.core.usecase.lancamento.RegistrarLancamentoUseCase;
import com.financeiramente.core.usecase.meta.CalcularProjecaoMetaUseCase;
import com.financeiramente.core.usecase.meta.CalcularTotalAportesMesUseCase;
import com.financeiramente.core.usecase.meta.CriarMetaUseCase;
import com.financeiramente.core.usecase.meta.DeletarAporteMetaUseCase;
import com.financeiramente.core.usecase.meta.DeletarMetaUseCase;
import com.financeiramente.core.usecase.meta.EditarMetaUseCase;
import com.financeiramente.core.usecase.meta.RegistrarAporteMetaUseCase;
import com.financeiramente.core.usecase.relatorio.GerarRelatorioUseCase;
import com.financeiramente.core.usecase.saldo.CalcularSaldoDashboardUseCase;

public final class CoreBootstrap {

    private CoreBootstrap() {
    }

    public static CoreServices create(DatabaseDriver databaseDriver) {
        return create(databaseDriver, new AppLogger() {});
    }

    public static CoreServices create(DatabaseDriver databaseDriver, AppLogger logger) {
        AppLogger safeLogger = logger != null ? logger : new AppLogger() {};

        new DatabaseMigrator(databaseDriver, safeLogger).migrate();

        CategoriaRepository categoriaRepository = new CategoriaDao(databaseDriver);
        LancamentoRepository lancamentoRepository = new LancamentoDao(databaseDriver);
        TagRepository tagRepository = new TagDao(databaseDriver);
        MetaRepository metaRepository = new MetaDao(databaseDriver);
        AporteMetaRepository aporteMetaRepository = new AporteMetaDao(databaseDriver);
        CartaoCreditoRepository cartaoRepository = new CartaoCreditoDao(databaseDriver);
        FaturaRepository faturaRepository = new FaturaDao(databaseDriver);
        CompraCartaoRepository compraCartaoRepository = new CompraCartaoDao(databaseDriver);

        CriarCategoriaUseCase criarCategoriaUseCase = new CriarCategoriaUseCase(categoriaRepository);
        EditarCategoriaUseCase editarCategoriaUseCase = new EditarCategoriaUseCase(categoriaRepository);
        DeletarCategoriaUseCase deletarCategoriaUseCase = new DeletarCategoriaUseCase(
                categoriaRepository,
                lancamentoRepository);
        ReordenarCategoriasUseCase reordenarCategoriasUseCase = new ReordenarCategoriasUseCase(categoriaRepository);
        RegistrarLancamentoUseCase registrarLancamentoUseCase = new RegistrarLancamentoUseCase(
                lancamentoRepository,
                tagRepository,
                categoriaRepository,
                faturaRepository);
        EditarLancamentoUseCase editarLancamentoUseCase = new EditarLancamentoUseCase(
                lancamentoRepository,
                tagRepository,
                categoriaRepository,
                faturaRepository);
        DeletarLancamentoUseCase deletarLancamentoUseCase = new DeletarLancamentoUseCase(lancamentoRepository);
        ListarLancamentosUseCase listarLancamentosUseCase = new ListarLancamentosUseCase(lancamentoRepository);
        CalcularTotalAportesMesUseCase calcularTotalAportesMesUseCase = new CalcularTotalAportesMesUseCase(
                metaRepository,
                aporteMetaRepository);
        CriarMetaUseCase criarMetaUseCase = new CriarMetaUseCase(metaRepository);
        EditarMetaUseCase editarMetaUseCase = new EditarMetaUseCase(metaRepository);
        DeletarMetaUseCase deletarMetaUseCase = new DeletarMetaUseCase(metaRepository);
        RegistrarAporteMetaUseCase registrarAporteMetaUseCase = new RegistrarAporteMetaUseCase(
                metaRepository,
                aporteMetaRepository);
        DeletarAporteMetaUseCase deletarAporteMetaUseCase = new DeletarAporteMetaUseCase(
                metaRepository,
                aporteMetaRepository);
        CalcularProjecaoMetaUseCase calcularProjecaoMetaUseCase = new CalcularProjecaoMetaUseCase(metaRepository);
        GerarRelatorioUseCase gerarRelatorioUseCase = new GerarRelatorioUseCase(
                lancamentoRepository,
                categoriaRepository,
                tagRepository);
        CriarCartaoCreditoUseCase criarCartaoCreditoUseCase = new CriarCartaoCreditoUseCase(cartaoRepository);
        EditarCartaoCreditoUseCase editarCartaoCreditoUseCase = new EditarCartaoCreditoUseCase(cartaoRepository);
        DesativarCartaoCreditoUseCase desativarCartaoCreditoUseCase = new DesativarCartaoCreditoUseCase(
                cartaoRepository,
                faturaRepository);
        ResolverFaturaParaLancamentoUseCase resolverFaturaParaLancamentoUseCase =
                new ResolverFaturaParaLancamentoUseCase(cartaoRepository, faturaRepository);
        RegistrarCompraCartaoUseCase registrarCompraCartaoUseCase = new RegistrarCompraCartaoUseCase(
                compraCartaoRepository,
                resolverFaturaParaLancamentoUseCase,
                registrarLancamentoUseCase,
                databaseDriver,
                safeLogger);
        EditarCompraCartaoUseCase editarCompraCartaoUseCase = new EditarCompraCartaoUseCase(compraCartaoRepository);
        DeletarCompraCartaoUseCase deletarCompraCartaoUseCase =
                new DeletarCompraCartaoUseCase(compraCartaoRepository, lancamentoRepository);
        GerarCobrancasRecorrentesCartaoUseCase gerarCobrancasRecorrentesCartaoUseCase =
                new GerarCobrancasRecorrentesCartaoUseCase(
                        compraCartaoRepository,
                        resolverFaturaParaLancamentoUseCase,
                        registrarLancamentoUseCase,
                        databaseDriver,
                        safeLogger);
        CancelarRecorrenciaCartaoUseCase cancelarRecorrenciaCartaoUseCase =
                new CancelarRecorrenciaCartaoUseCase(compraCartaoRepository);
        ListarComprasCartaoUseCase listarComprasCartaoUseCase =
                new ListarComprasCartaoUseCase(compraCartaoRepository, lancamentoRepository);
        RegistrarLancamentoCartaoUseCase registrarLancamentoCartaoUseCase = new RegistrarLancamentoCartaoUseCase(
                resolverFaturaParaLancamentoUseCase,
                registrarLancamentoUseCase,
                databaseDriver);
        AtualizarStatusFaturasUseCase atualizarStatusFaturasUseCase = new AtualizarStatusFaturasUseCase(faturaRepository);
        PagarFaturaUseCase pagarFaturaUseCase = new PagarFaturaUseCase(
                faturaRepository,
                lancamentoRepository,
                registrarLancamentoUseCase,
                resolverFaturaParaLancamentoUseCase,
                databaseDriver);
        AlterarStatusFaturaUseCase alterarStatusFaturaUseCase = new AlterarStatusFaturaUseCase(
                faturaRepository,
                lancamentoRepository,
                pagarFaturaUseCase,
                databaseDriver);
        AnteciparLancamentosFaturaUseCase anteciparLancamentosUseCase = new AnteciparLancamentosFaturaUseCase(
                faturaRepository,
                lancamentoRepository);
        CalcularSaldoDashboardUseCase calcularSaldoDashboardUseCase = new CalcularSaldoDashboardUseCase(
                lancamentoRepository,
                categoriaRepository,
                faturaRepository,
                aporteMetaRepository);

        return new CoreServices(
                categoriaRepository,
                lancamentoRepository,
                tagRepository,
                metaRepository,
                aporteMetaRepository,
                cartaoRepository,
                faturaRepository,
                compraCartaoRepository,
                criarCategoriaUseCase,
                editarCategoriaUseCase,
                deletarCategoriaUseCase,
                reordenarCategoriasUseCase,
                registrarLancamentoUseCase,
                editarLancamentoUseCase,
                deletarLancamentoUseCase,
                listarLancamentosUseCase,
                calcularSaldoDashboardUseCase,
                calcularTotalAportesMesUseCase,
                criarMetaUseCase,
                editarMetaUseCase,
                deletarMetaUseCase,
                registrarAporteMetaUseCase,
                deletarAporteMetaUseCase,
                calcularProjecaoMetaUseCase,
                gerarRelatorioUseCase,
                criarCartaoCreditoUseCase,
                editarCartaoCreditoUseCase,
                desativarCartaoCreditoUseCase,
                resolverFaturaParaLancamentoUseCase,
                registrarCompraCartaoUseCase,
                editarCompraCartaoUseCase,
                deletarCompraCartaoUseCase,
                gerarCobrancasRecorrentesCartaoUseCase,
                cancelarRecorrenciaCartaoUseCase,
                listarComprasCartaoUseCase,
                registrarLancamentoCartaoUseCase,
                atualizarStatusFaturasUseCase,
                pagarFaturaUseCase,
                alterarStatusFaturaUseCase,
                anteciparLancamentosUseCase);
    }
}
